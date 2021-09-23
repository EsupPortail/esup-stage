import { Component, OnInit, ViewChild } from '@angular/core';
import { RoleService } from "../../../services/role.service";
import { TableComponent } from "../../table/table.component";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AppFonction } from "../../../constants/app-fonction";
import { AuthService } from "../../../services/auth.service";
import { Droit } from "../../../constants/droit";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-admin-role',
  templateUrl: './admin-role.component.html',
  styleUrls: ['./admin-role.component.scss']
})
export class AdminRoleComponent implements OnInit {

  columns = ['code', 'libelle', 'action'];
  sortColumn = 'libelle';
  filters = [
    { id: 'code', libelle: 'Code' },
    { id: 'libelle', libelle: 'Libellé' },
  ];

  formTabIndex = 1;
  data: any;
  form: FormGroup;

  appFonctions: any[] = [];
  appFonctionColumns = ['fonctionnalite', 'droits'];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(public roleService: RoleService, private fb: FormBuilder, private authService: AuthService, private messageService: MessageService) {
    this.form = this.fb.group({
      code: [null, [Validators.required, Validators.maxLength(50)]],
      libelle: [null, [Validators.maxLength(255)]],
    });
    this.emptyData();
  }

  ngOnInit(): void {
    this.roleService.findAllAppFonction().subscribe((response: any) => {
      this.appFonctions = response;
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.formTabIndex) {
      this.emptyData();
    }
  }

  edit(data: any, copy: boolean): void {
    this.roleService.findById(data.id).subscribe((response: any) => {
      this.data = response;
      if (this.tabs) {
        this.tabs.selectedIndex = this.formTabIndex;
      }
      if (copy) {
        delete this.data.id;
        this.data.roleAppFonctions.forEach((ra: any) => {
          delete ra.id;
        });
      }
      this.setFormData();
    })
  }

  emptyData(): void {
    this.data = {
      code: null,
      libelle: null,
      roleAppFonctions: [],
    };
    this.appFonctions.forEach((af: any) => {
      this.data.roleAppFonctions.push({
        appFonction: af,
        lecture: false,
        creation: false,
        modification: false,
        suppression: false,
        validation: false,
      });
    });
    this.setFormData();
  }

  setFormData(): void {
    const data = {...this.data};
    delete data.id;
    delete data.roleAppFonctions;
    this.form.setValue(data);
  }

  get AppFonction() {
    return AppFonction;
  }

  get Droit() {
    return Droit;
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION]});
  }

  hasRight(fonction: AppFonction, droit: Droit): boolean {
    const roleAppFonction = this.data.roleAppFonctions.find((ra: any) => ra.appFonction.code === fonction);
    if (roleAppFonction) {
      if (droit === Droit.ECRITURE) {
        // Le droit d'écriture comprend l'ensemble des droits CREATION, MODIFICATION, SUPPRESSION et VALIDATION
        return roleAppFonction.creation;
      }
      return roleAppFonction[droit.toLowerCase()];
    }
    return false;
  }

  toogleRight(fonction: AppFonction, droit: Droit): void {
    const roleAppFonction = this.data.roleAppFonctions.find((ra: any) => ra.appFonction.code === fonction);
    if (roleAppFonction) {
      if (droit === Droit.ECRITURE) {
        roleAppFonction.creation = !roleAppFonction.creation;
        roleAppFonction.modification = !roleAppFonction.modification;
        roleAppFonction.suppression = !roleAppFonction.suppression;
        roleAppFonction.validation = !roleAppFonction.validation;
      } else {
        roleAppFonction[droit.toLowerCase()] = ! roleAppFonction[droit.toLowerCase()]
      }
    }
  }

  save(): void {
    const data = {...this.data, ...this.form.value};
    if (this.form.valid) {
      if (data.id) {
        this.roleService.update(data.id, data).subscribe((response: any) => {
          this.data = response;
          this.messageService.setSuccess('Rôle modifié');
          this.appTable?.update();
        });
      } else {
        this.roleService.create(data).subscribe((response: any) => {
          this.data = response;
          this.messageService.setSuccess('Rôle créé');
          this.appTable?.update();
        });
      }
    }
  }

}
