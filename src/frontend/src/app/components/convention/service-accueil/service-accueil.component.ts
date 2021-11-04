import { Component, EventEmitter, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { ServiceService } from "../../../services/service.service";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: 'app-service-accueil',
  templateUrl: './service-accueil.component.html',
  styleUrls: ['./service-accueil.component.scss']
})
export class ServiceAccueilComponent implements OnInit, OnChanges {

  countries: any[] = [];

  data: any;

  @Input() etab = {id:null!};
  services:any[] = [];

  service: any;
  modif: boolean = false;
  form: FormGroup;

  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();
  @Output() serviceSelected = new EventEmitter<any>();

  constructor(public serviceService: ServiceService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
              private paysService: PaysService,
  ) {
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(70)]],
      voie: [null, [Validators.required, Validators.maxLength(200)]],
      codePostal: [null, [Validators.required, Validators.maxLength(10), Validators.pattern('[0-9]+')]],
      batimentResidence: [null, [Validators.maxLength(200)]],
      commune: [null, [Validators.required, Validators.maxLength(200)]],
      idPays: [null, [Validators.required]],
      telephone: [null, [Validators.maxLength(20)]],
    });
  }

  ngOnInit(): void {
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
  }

  ngOnChanges(): void{
    this.refreshServices();
    this.resetState();
  }

  refreshServices(): void{
    if (this.etab){
      this.serviceService.getByStructure(this.etab.id).subscribe((response: any) => {
        this.services = response;
      });
    }
  }

  canCreate(): boolean {
    return this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.MODIFICATION]});
  }

  choose(row: any): void {
    this.modif = false;
    if (this.firstPanel) {
      this.firstPanel.expanded = false;
    }
    this.validated.emit(2);
    this.serviceSelected.emit(this.service);
  }

  resetState(): void {
    this.service = null;
    this.modif = false;
    if (this.firstPanel) {
      this.firstPanel.expanded = true;
    }
    this.validated.emit(0);
    this.serviceSelected.emit(this.service);
  }

  initCreate(): void {
    this.service = {};
    this.form.reset();
    this.modif = true;
  }

  edit(): void {
    this.form.setValue({
      nom: this.service.nom,
      voie: this.service.voie,
      codePostal: this.service.codePostal,
      batimentResidence: this.service.batimentResidence,
      commune: this.service.commune,
      idPays: this.service.pays ? this.service.pays.id : null,
      telephone: this.service.telephone,
    });
    this.modif = true;
  }

  cancelEdit(): void {
    this.modif = false;
  }

  save(): void {
    if (this.form.valid) {

      const data = {...this.form.value};

      if (this.service.id) {
        this.serviceService.update(this.service.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Service modifié');
          this.service = response;
          this.refreshServices();
          this.modif = false;
        });
      } else {
        //ajoute idStructure à l'objet service
        data.idStructure = this.etab.id;
        this.serviceService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Service créé');
          this.service = response;
          this.refreshServices();
          this.choose(this.service);
        });
      }
    }
  }

}
