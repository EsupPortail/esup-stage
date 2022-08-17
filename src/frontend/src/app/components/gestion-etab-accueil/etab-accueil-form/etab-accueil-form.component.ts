import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, OnDestroy } from '@angular/core';
import { FormBuilder, FormControl, Validators } from "@angular/forms";
import { StructureService } from "../../../services/structure.service";
import { CommuneService } from "../../../services/commune.service";
import { CodePostalService } from "../../../services/codePostal.service";
import { PaysService } from "../../../services/pays.service";
import { TypeStructureService } from "../../../services/type-structure.service";
import { NafN1Service } from "../../../services/naf-n1.service";
import { NafN5Service } from "../../../services/naf-n5.service";
import { StatutJuridiqueService } from "../../../services/statut-juridique.service";
import { EffectifService } from "../../../services/effectif.service";
import { MessageService } from "../../../services/message.service";
import { ReplaySubject, Subject, Observable } from 'rxjs';
import { take, takeUntil, map, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-etab-accueil-form',
  templateUrl: './etab-accueil-form.component.html',
  styleUrls: ['./etab-accueil-form.component.scss']
})
export class EtabAccueilFormComponent implements OnInit, OnChanges {

  @Input() etab: any;
  @Output() submitted = new EventEmitter<any>();
  @Output() canceled = new EventEmitter<boolean>();

  countries: any[] = [];
  typeStructures: any[] = [];
  secteurs: any[] = [];
  statutJuridiques: any[] = [];
  effectifs: any[] = [];
  selectedNafN5: any;
  nafN5List: any[] = [];

  nafN5FilterCtrl: FormControl = new FormControl();
  filteredNafN5List: ReplaySubject<any> = new ReplaySubject<any>(1);
  _onDestroy = new Subject<void>();

  form: any;

  constructor(
    public structureService: StructureService,
    private paysService: PaysService,
    public codePostalService: CodePostalService,
    public communeService: CommuneService,
    private typeStructureService: TypeStructureService,
    private nafN1Service: NafN1Service,
    private nafN5Service: NafN5Service,
    private statutJuridiqueService: StatutJuridiqueService,
    private effectifService: EffectifService,
    private fb: FormBuilder,
    private messageService: MessageService,
  ) { }

  ngOnInit(): void {
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.typeStructures = response.data;
    });
    this.nafN1Service.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.secteurs = response.data;
    });
    this.statutJuridiqueService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.statutJuridiques = response.data;
    });
    this.effectifService.getAll().subscribe((response: any) => {
      this.effectifs =  response;
    });
    this.getNafN5List();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.fb.group({
      raisonSociale: [this.etab.raisonSociale, [Validators.required, Validators.maxLength(150)]],
      numeroSiret: [this.etab.numeroSiret, [Validators.maxLength(14), Validators.pattern('[0-9]{14}')]],
      idEffectif: [this.etab.effectif ? this.etab.effectif.id : null, [Validators.required]],
      idTypeStructure: [this.etab.typeStructure ? this.etab.typeStructure.id : null, [Validators.required]],
      idStatutJuridique: [this.etab.statutJuridique ? this.etab.statutJuridique.id : null, [Validators.required]],
      codeNafN5: [this.etab.nafN5 ? this.etab.nafN5.code : null, []],
      activitePrincipale: [this.etab.activitePrincipale, []],
      voie: [this.etab.voie, [Validators.required, Validators.maxLength(200)]],
      codePostal: [this.etab.codePostal, [Validators.required, Validators.maxLength(10)]],
      batimentResidence: [this.etab.batimentResidence, [Validators.maxLength(200)]],
      commune: [this.etab.commune, [Validators.required, Validators.maxLength(200)]],
      libCedex: [this.etab.libCedex, [Validators.maxLength(20)]],
      idPays: [this.etab.pays ? this.etab.pays.id : null, [Validators.required]],
      mail: [this.etab.mail, [Validators.pattern('[^@ ]+@[^@. ]+\\.[^@ ]+'), Validators.maxLength(50)]],
      telephone: [this.etab.telephone, [Validators.required, Validators.maxLength(20)]],
      siteWeb: [this.etab.siteWeb, [Validators.maxLength(200), Validators.pattern('^https?://(\\w([\\w\\-]{0,61}\\w)?\\.)+[a-zA-Z]{2,6}([/]{1}.*)?$')]],
      fax: [this.etab.fax, [Validators.maxLength(20)]],
    });

    this.form.get('idTypeStructure')?.disable();

    if (this.etab.nafN5) {
      this.selectedNafN5 = this.etab.nafN5;
    }
  }

  getNafN5List(): void {
    this.nafN5Service.findAll().subscribe((response: any) => {
      this.nafN5List = response;
      this.filteredNafN5List.next(this.nafN5List.slice());
      this.nafN5FilterCtrl.valueChanges
        .pipe(takeUntil(this._onDestroy))
        .subscribe(() => {
          this.filterNafN5List();
        });
    });
  }

  filterNafN5List() {
    if (!this.nafN5List) {
      return;
    }

    let search = this.nafN5FilterCtrl.value;
    if (!search) {
      this.filteredNafN5List.next(this.nafN5List.slice());
      return;
    } else {
      search = search.toLowerCase();
    }

    this.filteredNafN5List.next(
      this.nafN5List.filter(nafN5 => nafN5.code.toLowerCase().indexOf(search) > -1 || nafN5.libelle.toLowerCase().indexOf(search) > -1)
    );
  }

  setSelectedNafN5(nafN5: any): void {
    this.selectedNafN5 = nafN5;
  }

  save(): void {
    if (this.form.valid) {
      // Contrôle code APE ou activité principale renseignée
      if (!this.form.get('codeNafN5')?.value && !this.form.get('activitePrincipale')?.value) {
        this.messageService.setError('Une de ces deux informations doivent être renseignée : Code APE, Activité principale');
        return;
      }

      if (this.form.get('numeroSiret')?.value === "") {
        this.form.get('numeroSiret')?.setValue(null);
      }
      const data = {...this.form.getRawValue()};
      data.nafN5 = this.selectedNafN5;
      if (this.etab.id) {
        this.structureService.update(this.etab.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Établissement d\'accueil modifié');
          this.etab = response;
          this.submitted.emit(this.etab);
        });
      } else {
        this.structureService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Établissement d\'accueil créé');
          this.etab = response;
          this.submitted.emit(this.etab);
        });
      }
    }
  }

  cancelEdit(): void {
    this.canceled.emit(true);
  }

  setTypeStructure(statutJuridique: any) {
    this.form.get('idTypeStructure')?.setValue(statutJuridique.typeStructure.id);
  }

  numeroSiretRequired() {
    let idTypeStructure = this.form.get('idTypeStructure')?.value;
    if (idTypeStructure) {
      let typeStructure = this.typeStructures.find(type => type.id === idTypeStructure);
      if (typeStructure)
        return typeStructure.siretObligatoire;
    }

    return false;
  }

  isFr() {
    let idPays = this.form.get('idPays')?.value;
    if (idPays) {
      let pays = this.countries.find(c => c.id === idPays);
      if (pays)
        return pays.libelle === 'FRANCE';
    }

    return true;
  }
}
