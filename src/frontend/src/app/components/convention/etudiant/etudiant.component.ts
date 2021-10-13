import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import { EtudiantService } from "../../../services/etudiant.service";
import { MatExpansionPanel } from "@angular/material/expansion";

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

  @ViewChild(MatExpansionPanel) searchEtudiantPanel: MatExpansionPanel|undefined;

  constructor(private authService: AuthService, private etudiantService: EtudiantService, private fb: FormBuilder, private messageService: MessageService) {
    this.form = this.fb.group({
      id: [null, []],
      nom: [null, []],
      prenom: [null, []],
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
    this.etudiantService.searchEtudiants(this.form.value).subscribe((response: any) => {
      this.etudiants = response;
    });
  }

  choose(row: any): void {
    // TODO récupération des données apogée de l'étudiant
    if (this.searchEtudiantPanel) {
      this.searchEtudiantPanel.expanded = false;
    }
    this.etudiant = row;
    console.log(this.etudiant);
  }

  getApogeeData(): void {

  }

}
