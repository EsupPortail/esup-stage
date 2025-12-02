import { Component } from '@angular/core';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  standalone: false
})
export class FooterComponent {

  constructor(public authService: AuthService) {
  }

  getAppVersion(): string {
    return this.authService.appVersion;
  }

}
