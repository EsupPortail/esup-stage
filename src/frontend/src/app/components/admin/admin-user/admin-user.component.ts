import { Component, OnInit } from '@angular/core';
import { UserService } from "../../../services/user.service";

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

  constructor(public userService: UserService) { }

  ngOnInit(): void {
  }

  edit(data: any): void {
    console.log(data);
  }

}
