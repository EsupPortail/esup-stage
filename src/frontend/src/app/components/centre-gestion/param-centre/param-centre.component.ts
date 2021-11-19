import { Component, OnInit, Input, Output, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";
import { LdapService } from "../../../services/ldap.service";
import { EnseignantService } from "../../../services/enseignant.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { debounceTime } from "rxjs/operators";

@Component({
  selector: 'app-param-centre',
  templateUrl: './param-centre.component.html',
  styleUrls: ['./param-centre.component.scss']
})
export class ParamCentreComponent implements OnInit {

  @Input() centreGestion: any;
  @Input() form: FormGroup;

  dureeRecupList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]

  viseurForm: FormGroup;
  columns = ['nomprenom', 'mail', 'departement', 'action'];
  enseignants: any[] = [];
  enseignant: any;
  @ViewChildren(MatExpansionPanel) pannels: QueryList<MatExpansionPanel>;

  @Output() update = new EventEmitter<any>();

  constructor(
    private centreGestionService: CentreGestionService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private ldapService: LdapService,
    private enseignantService: EnseignantService,) {
      this.viseurForm = this.fb.group({
        nom: [null, []],
        prenom: [null, []],
      });
    }

  ngOnInit(): void {
    if (this.centreGestion.id) {
      this.setFormData();
    }
    this.viseurForm.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      this.search();
    });
  }

  setFormData(): void {
    this.form.setValue({
      saisieTuteurProParEtudiant: this.centreGestion.saisieTuteurProParEtudiant,
      autorisationEtudiantCreationConvention: this.centreGestion.autorisationEtudiantCreationConvention,
      validationPedagogique: this.centreGestion.validationPedagogique,
      recupInscriptionAnterieure: this.centreGestion.recupInscriptionAnterieure,
      dureeRecupInscriptionAnterieure: this.centreGestion.dureeRecupInscriptionAnterieure,
      urlPageInstruction: this.centreGestion.urlPageInstruction,
      nomViseur: this.centreGestion.nomViseur,
      prenomViseur: this.centreGestion.prenomViseur,
      qualiteViseur: this.centreGestion.qualiteViseur,
    });
  }

  toggleRecupInscription(): void {
    if (!this.form.get('recupInscriptionAnterieure')?.value) {
      this.form.get('dureeRecupInscriptionAnterieure')?.reset();
    }
  }

  search(): void {
    if (!this.viseurForm.get('nom')?.value && !this.viseurForm.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.enseignant = undefined;
    this.ldapService.searchEnseignants(this.viseurForm.value).subscribe((response: any) => {
      this.enseignants = response;
      if (this.enseignants.length === 1) {
        this.choose(this.enseignants[0]);
      }
    });
  }

  choose(row: any): void {
      if (this.pannels) {
        this.pannels.first.open();
      }
      this.enseignantService.getByUid(row.supannAliasLogin).subscribe((response: any) => {
        this.enseignant = response;
        if (this.enseignant == null){
          this.createEnseignant(row);
        }else{
          this.setViseur(this.enseignant);
        }
      });
  }

  createEnseignant(row: any): void {
    const displayName = row.displayName.split(/(\s+)/);
    const data = {
      "nom": displayName[2],
      "prenom": displayName[0],
      "mail": row.mail,
      "typePersonne": row.eduPersonPrimaryAffiliation,
      "uidEnseignant": row.supannAliasLogin,
    };

    this.enseignantService.create(data).subscribe((response: any) => {
      this.enseignant = response;
      this.setViseur(this.enseignant);
    });
  }

  setViseur(enseignant: any) {
    this.form.get('nomViseur')?.setValue(enseignant.nom);
    this.form.get('prenomViseur')?.setValue(enseignant.prenom);
    this.form.get('qualiteViseur')?.reset();
  }

  resetViseur() {
    this.form.get('nomViseur')?.reset();
    this.form.get('prenomViseur')?.reset();
    this.form.get('qualiteViseur')?.reset();
  }

}
