import { Component, OnInit, ViewChild } from '@angular/core';
import { UserService } from "../../../services/user.service";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { RoleService } from "../../../services/role.service";
import { MessageService } from "../../../services/message.service";
import { TableComponent } from "../../table/table.component";
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: 'app-admin-user',
  templateUrl: './admin-user.component.html',
  styleUrls: ['./admin-user.component.scss']
})
export class AdminUserComponent implements OnInit {

  columns = [
    { id: 'login', libelle: 'Login', sortable: true },
    { id: 'nom', libelle: 'Nom', sortable: true },
    { id: 'prenom', libelle: 'Prénom', sortable: true },
    { id: 'actif', libelle: 'Actif', type: 'boolean' },
    { id: 'action', libelle: 'Actions' },
  ]
  sortColumn = 'login';
  filters = [
    { id: 'login', libelle: 'Login' },
    { id: 'nom', libelle: 'Nom' },
    { id: 'prenom', libelle: 'Prénom' },
    { id: 'actif', libelle: 'Actif', type: 'boolean' },
  ];

  formTabIndex = 1;
  data: any;
  form: FormGroup;
  roles: any;
  roleMultipleSettings = {
    idField: 'code',
    textField: 'code',
  }

  @ViewChild('table') table: TableComponent;
  @ViewChild('tabs') tabs: MatTabGroup;

  constructor(
    public userService: UserService,
    private fb: FormBuilder,
    private roleService: RoleService,
    private messageService: MessageService,
    private authService: AuthService,
  ) {
    this.form = this.fb.group({
      login: [null, [Validators.required, Validators.maxLength(255)]],
      nom: [null, [Validators.maxLength(255)]],
      prenom: [null, [Validators.maxLength(255)]],
      roles: [null, [Validators.required]],
      actif: [null],
    });
    this.emptyData();
  }

  ngOnInit(): void {
    this.roleService.findAll().subscribe((response: any) => {
      this.roles = response;
    });
  }

  emptyData(): void {
    this.data = {
      login: null,
      nom: null,
      prenom: null,
      roles: null,
      actif: null,
    };
    this.setFormData();
  }

  setFormData(): void {
    const data = {...this.data};
    delete data.id;
    delete data.password;
    delete data.dateCreation;
    this.form.setValue(data);
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.formTabIndex) {
      this.emptyData();
      this.form.get('login')?.enable();
      this.form.get('actif')?.enable();
    }
  }

  edit(data: any): void {
    this.data = data;
    this.tabs.selectedIndex = this.formTabIndex;
    this.setFormData();
    this.form.get('login')?.disable();
    // Interdiction de se désactiver soi-même
    if (this.data.id === this.authService.userConnected.id) {
      this.form.get('actif')?.disable();
    }
  }

  compareRole(option: any, value: any): boolean {
    if (option && value) {
      return option.code === value.code;
    }
    return false;
  }

  save(): void {
    if (this.form.valid) {
      if (this.data.id) {
        this.userService.update(this.data.id, this.form.value).subscribe((response: any) => {
          this.data = response;
          this.setFormData();
          this.table.update();
          this.messageService.setSuccess('Utilisateur modifé');
        })
      } else {
        // TODO create
      }
    }
  }

}
