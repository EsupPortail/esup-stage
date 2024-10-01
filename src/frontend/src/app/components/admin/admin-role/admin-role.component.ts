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
  exportColumns = {
    code: { title: 'Code' },
    libelle: { title: 'Libelle' },
  };

  formTabIndex = 1;
  data: any = {};
  form: FormGroup;

  appFonctions: any[] = [];
  appFonctionColumns = ['fonctionnalite', 'droits'];

  roleOrigine :  any  = undefined;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(public roleService: RoleService, private fb: FormBuilder, private authService: AuthService, private messageService: MessageService) {
    this.form = this.fb.group({
      code: [null, [Validators.required, Validators.maxLength(50)]],
      libelle: [null, [Validators.maxLength(255)]],
      origine: [null, [Validators.maxLength(255)]],
    });
  }

  ngOnInit(): void {
    this.roleService.findAllAppFonction().subscribe((response: any) => {
      this.appFonctions = response;
      this.emptyData();
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
        data.origine != null ? this.roleOrigine = data.origine : this.roleOrigine = data.code;
      }
      this.appFonctions.forEach((af: any) => {
        const ra = this.data.roleAppFonctions.find((r: any) => r.appFonction.code === af.code);
        if (ra === undefined) {
          this.addEmptyAppFonction(af);
        }
      });
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
      this.addEmptyAppFonction(af);
    });
    this.setFormData();
    this.roleOrigine = undefined;
  }

  addEmptyAppFonction(af: any): void {
    this.data.roleAppFonctions.push({
      appFonction: af,
      lecture: false,
      creation: false,
      modification: false,
      suppression: false,
      validation: false,
    });
  }

  setFormData(): void {
    const data = {...this.data};
    delete data.id;
    delete data.roleAppFonctions;
    if(this.roleOrigine != undefined){
      data.origine = this.roleOrigine;
    }
    this.form.patchValue(data);
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

  canDelete(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.SUPPRESSION]});
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

  toggleRight(fonction: AppFonction, droit: Droit): void {
    const roleAppFonction = this.data.roleAppFonctions.find((ra: any) => ra.appFonction.code === fonction);
    if (roleAppFonction) {
      if (droit === Droit.ECRITURE) {
        roleAppFonction.creation = !roleAppFonction.creation;
        roleAppFonction.modification = !roleAppFonction.modification;
        roleAppFonction.suppression = !roleAppFonction.suppression;
        roleAppFonction.validation = !roleAppFonction.validation;
        roleAppFonction.lecture = true;
      } else {
        roleAppFonction[droit.toLowerCase()] = ! roleAppFonction[droit.toLowerCase()]
        // On a désactivé le droit de lecture : il faut désactiver les droits d'écriture
        if (droit === Droit.LECTURE) {
          if (!roleAppFonction[droit.toLowerCase()]) {
            roleAppFonction.creation = false;
            roleAppFonction.modification = false;
            roleAppFonction.suppression = false;
            roleAppFonction.validation = false;
          }
        } else { // On a activé le droit d'écriture : il faut activer le droit de lecture
          if (roleAppFonction[droit.toLowerCase()]) {
            roleAppFonction.lecture = true;
          }
        }
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

  delete(data: any): void {
    this.roleService.delete(data.id).subscribe((response: any) => {
      this.messageService.setSuccess('Rôle supprimé');
      this.appTable?.update();
    });
  }

}
