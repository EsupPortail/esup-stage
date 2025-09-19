import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { AuthService } from "../../services/auth.service";
import { ConfigService } from "../../services/config.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @ViewChild('logo') logo !: ElementRef;

  constructor(private authService: AuthService, private configService: ConfigService) { }

  ngOnInit(): void {
    this.configService.themeModified.subscribe((config: any) => {
      if (this.logo !== null && config.logo) {
        this.logo.nativeElement.src = `data:${config.logo.contentType};base64,${config.logo.base64}`;
      }
    })
  }

  isConnected() {
    return this.authService.userConnected;
  }

  logout(): void {
    this.authService.logout();
  }
}
