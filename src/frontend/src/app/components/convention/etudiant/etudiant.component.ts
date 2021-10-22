import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import { EtudiantService } from "../../../services/etudiant.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { LdapService } from "../../../services/ldap.service";

@Component({
  selector: 'app-convention-etudiant',
  templateUrl: './etudiant.component.html',
  styleUrls: ['./etudiant.component.scss']
})
export class EtudiantComponent implements OnInit {

  isEtudiant: boolean = true;

  form: FormGroup;
  columns = ['numetudiant', 'nomprenom', 'action'];
  etudiants: any[] = [];
  etudiant: any;
  selectedNumEtudiant: string|null = null;

  formAdresse: FormGroup;

  @ViewChild(MatExpansionPanel) searchEtudiantPanel: MatExpansionPanel|undefined;

  constructor(private authService: AuthService, private etudiantService: EtudiantService, private fb: FormBuilder, private messageService: MessageService, private ldapService: LdapService) {
    this.form = this.fb.group({
      id: [null, []],
      nom: [null, []],
      prenom: [null, []],
    });
    this.formAdresse = this.fb.group({
      mainAddress: [null, [Validators.required]],
      postalCode: [null, [Validators.required]],
      town: [null, [Validators.required]],
      country: [null, [Validators.required]],
      phone: [null, []],
      portablePhone: [null, []],
      mailPerso: [null, [Validators.required, Validators.email]],
    });
  }

  ngOnInit(): void {
    this.isEtudiant = this.authService.isEtudiant();
    if (this.isEtudiant) {
      // TODO récupération des données apogée et ldap de l'étudiant connecté
    }
  }

  search(): void {
    if (!this.form.get('id')?.value && !this.form.get('nom')?.value && !this.form.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.etudiant = undefined;
    this.selectedNumEtudiant = null;
    this.ldapService.searchEtudiants(this.form.value).subscribe((response: any) => {
      this.etudiants = response;
      if (this.etudiants.length === 1) {
        this.choose(this.etudiants[0]);
      }
    });
  }

  choose(row: any): void {
    this.etudiantService.getApogeeData(row.codEtu).subscribe((response: any) => {
      this.selectedNumEtudiant = row.codEtu;
      if (this.searchEtudiantPanel) {
        this.searchEtudiantPanel.expanded = false;
      }
      this.etudiant = response;
      this.formAdresse.setValue({
        mainAddress: this.etudiant.mainAddress,
        postalCode: this.etudiant.postalCode,
        town: this.etudiant.town,
        country: this.etudiant.country,
        phone: this.etudiant.phone,
        portablePhone: this.etudiant.portablePhone,
        mailPerso: this.etudiant.mailPerso,
      });
    });
  }

  validate(): void {

  }

}
