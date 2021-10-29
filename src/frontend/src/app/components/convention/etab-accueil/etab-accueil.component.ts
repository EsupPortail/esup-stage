import { Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { TypeStructureService } from "../../../services/type-structure.service";
import { NafN1Service } from "../../../services/naf-n1.service";
import { StructureService } from "../../../services/structure.service";
import { TableComponent } from "../../table/table.component";
import { StatutJuridiqueService } from "../../../services/statut-juridique.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { EffectifService } from "../../../services/effectif.service";
import { MessageService } from "../../../services/message.service";
import { NafN5Service } from "../../../services/naf-n5.service";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: 'app-etab-accueil',
  templateUrl: './etab-accueil.component.html',
  styleUrls: ['./etab-accueil.component.scss']
})
export class EtabAccueilComponent implements OnInit {

  columns = ['raisonSociale', 'numeroSiret', 'nafN5', 'pays', 'commune', 'typeStructure', 'statutJuridique', 'action'];
  sortColumn = 'raisonSociale';
  filters: any[] = [];

  countries: any[] = [];
  typeStructures: any[] = [];
  secteurs: any[] = [];
  statutJuridiques: any[] = [];
  effectifs: any[] = [];

  formTabIndex = 1;
  data: any;

  createButton = {
    libelle: 'Créer un établissement d\'accueil',
    action: () => this.initCreate(),
  }

  etab: any;
  modif: boolean = false;
  form: FormGroup;
  selectedNafN5: any;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();
  @Output() etabSelected = new EventEmitter<any>();

  constructor(public structureService: StructureService,
              private paysService: PaysService,
              private typeStructureService: TypeStructureService,
              private nafN1Service: NafN1Service,
              private nafN5Service: NafN5Service,
              private statutJuridiqueService: StatutJuridiqueService,
              private effectifService: EffectifService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
  ) {
    this.form = this.fb.group({
      raisonSociale: [null, [Validators.required, Validators.maxLength(150)]],
      numeroSiret: [null, [Validators.maxLength(14), Validators.pattern('[0-9]{14}')]],
      idEffectif: [null, [Validators.required]],
      idTypeStructure: [null, [Validators.required]],
      idStatutJuridique: [null, [Validators.required]],
      codeNafN5: [null, []],
      activitePrincipale: [null, []],
      voie: [null, [Validators.required, Validators.maxLength(200)]],
      codePostal: [null, [Validators.required, Validators.maxLength(10)]],
      batimentResidence: [null, [Validators.maxLength(200)]],
      commune: [null, [Validators.required, Validators.maxLength(200)]],
      libCedex: [null, [Validators.maxLength(20)]],
      idPays: [null, [Validators.required]],
      mail: [null, [Validators.email, Validators.maxLength(50)]],
      telephone: [null, [Validators.required, Validators.maxLength(20)]],
      siteWeb: [null, [Validators.maxLength(200), Validators.pattern('^https?://(\\w([\\w\\-]{0,61}\\w)?\\.)+[a-zA-Z]{2,6}([/]{1}.*)?$')]],
      fax: [null, [Validators.maxLength(20)]],
    });
  }

  ngOnInit(): void {
    this.filters = [
      { id: 'raisonSociale', libelle: 'Raison sociale' },
      { id: 'numeroSiret', libelle: 'Numéro SIRET' },
      { id: 'nafN1.code', libelle: 'Activité', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'code', specific: true },
      { id: 'pays.id', libelle: 'Pays', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'commune', libelle: 'Commune' },
      { id: 'typeStructure.id', libelle: 'Type d\'organisme', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'statutJuridique.id', libelle: 'Forme juridique', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
    ];
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
      const filter = this.filters.find((f: any) => f.id === 'pays.id');
      if (filter) {
        filter.options = this.countries;
      }
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.typeStructures = response.data;
      const filter = this.filters.find((f: any) => f.id === 'typeStructure.id');
      if (filter) {
        filter.options = this.typeStructures;
      }
    });
    this.nafN1Service.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.secteurs = response.data;
      const filter = this.filters.find((f: any) => f.id === 'nafN1.code');
      if (filter) {
        filter.options = this.secteurs;
      }
    });
    this.statutJuridiqueService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.statutJuridiques = response.data;
      const filter = this.filters.find((f: any) => f.id === 'statutJuridique.id');
      if (filter) {
        filter.options = this.statutJuridiques;
      }
    });
    this.effectifService.getAll().subscribe((response: any) => {
      this.effectifs =  response;
    });
  }

  canCreate(): boolean {
    return this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.MODIFICATION]});
  }

  choose(row: any): void {
    this.modif = false;
    this.structureService.getById(row.id).subscribe((response: any) => {
      this.etab = response;
      if (this.etab.nafN5) {
        this.selectedNafN5 = this.etab.nafN5;
      }
      if (this.firstPanel) {
        this.firstPanel.expanded = false;
      }

      this.validated.emit(2);
      this.etabSelected.emit(this.etab);
    });
  }

  initCreate(): void {
    this.etab = {};
    this.form.reset();
    this.selectedNafN5 = undefined;
    this.modif = true;
  }

  edit(): void {
    this.form.setValue({
      raisonSociale: this.etab.raisonSociale,
      numeroSiret: this.etab.numeroSiret,
      idEffectif: this.etab.effectif ? this.etab.effectif.id : null,
      idTypeStructure: this.etab.typeStructure ? this.etab.typeStructure.id : null,
      idStatutJuridique: this.etab.statutJuridique ? this.etab.statutJuridique.id : null,
      codeNafN5: this.etab.nafN5 ? this.etab.nafN5.code : null,
      activitePrincipale: this.etab.activitePrincipale,
      voie: this.etab.voie,
      codePostal: this.etab.codePostal,
      batimentResidence: this.etab.batimentResidence,
      commune: this.etab.commune,
      libCedex: this.etab.libCedex,
      idPays: this.etab.pays ? this.etab.pays.id : null,
      mail: this.etab.mail,
      telephone: this.etab.telephone,
      siteWeb: this.etab.siteWeb,
      fax: this.etab.fax,
    });
    this.modif = true;
  }

  cancelEdit(): void {
    this.modif = false;
  }

  getNafN5(): void {
    const nafN5Code = this.form.get('nafN5')?.value;
    if (nafN5Code) {
      this.nafN5Service.getByCode(nafN5Code).subscribe((response: any) => {
        this.selectedNafN5 = response;
      });
    }
  }

  save(): void {
    if (this.form.valid) {
      // Contrôle code APE ou activité principale renseignée
      if (!this.form.get('nafN5')?.value && !this.form.get('activitePrincipale')?.value) {
        this.messageService.setError('Une de ces deux informations doivent être renseignée : Code APE, Activité principale');
        return;
      }
      // TODO contrôle de saisie
      const data = {...this.form.value};
      data.nafN5 = this.selectedNafN5;
      if (this.etab.id) {
        this.structureService.update(this.etab.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Établissement d\'accueil modifié');
          this.etab = response;
          this.modif = false;
        });
      } else {
        this.structureService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Établissement d\'accueil créé');
          this.etab = response;
          this.choose(this.etab);
        });
      }
    }
  }

}
