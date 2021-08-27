import { Component, OnInit } from '@angular/core';
import { MenuService } from "../../services/menu.service";
import { AuthService } from "../../services/auth.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  constructor(private menuService: MenuService, private authService: AuthService) { }

  ngOnInit(): void {
  }

  slideNavbar(): void {
    let opened = this.menuService.navbarOpened;
    this.menuService.navbarOpened = !opened;
  }

  logout(): void {
    this.authService.logout();
  }
}
