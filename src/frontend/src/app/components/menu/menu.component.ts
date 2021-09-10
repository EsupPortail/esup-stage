import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth.service";
import { Role } from "../../constants/role";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit {

  routes: any;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.routes = [
      { libelle: 'Utilisateurs', path: '/utilisateurs', icon: 'fa-users', canView: () => { return this.authService.checkRights([Role.ADM.code, Role.ADM_TECH.code])} }
    ];
  }

  getAppVersion(): string {
    return this.authService.appVersion;
  }
}
