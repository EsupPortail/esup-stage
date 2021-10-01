import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MenuService } from "../../services/menu.service";
import { AuthService } from "../../services/auth.service";
import { ConfigService } from "../../services/config.service";
import { environment } from "../../../environments/environment";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @ViewChild('logo') logo: ElementRef;

  constructor(private menuService: MenuService, private authService: AuthService, private configService: ConfigService) { }

  ngOnInit(): void {
    this.configService.themeModified.subscribe((config: any) => {
      if (this.logo !== null && config.logo) {
        this.logo.nativeElement.src = `data:${config.logo.contentType};base64,${config.logo.base64}`;
      }
    })
  }

  slideNavbar(): void {
    let opened = this.menuService.navbarOpened;
    this.menuService.navbarOpened = !opened;
  }

  logout(): void {
    this.authService.logout();
  }
}
