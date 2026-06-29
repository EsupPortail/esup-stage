import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {FooterService} from "../../services/footer.service";
import { catchError, of } from "rxjs";

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  standalone: false
})
export class FooterComponent implements OnInit {

  docUrl: string | null = null;
  supportUrl: string | null = null;
  githubUrl: string | null = null;
  webSiteUrl: string | null = null;

  constructor(private authService: AuthService,private footerService: FooterService) {
  }

  ngOnInit(): void {
    this.footerService.getLinks().pipe(
      catchError(() => of(null))
    ).subscribe((links: any) => {
      const safeLinks = links || {};
      this.docUrl = safeLinks.wiki ?? null;
      this.supportUrl = safeLinks.support ?? null;
      this.githubUrl = safeLinks.github ?? null;
      this.webSiteUrl = safeLinks.site ?? null;
    });
  }

  getAppVersion(): string {
    return this.authService.appVersion;
  }

  hasAnyHelpLink(): boolean {
    return !!(this.docUrl || this.supportUrl);
  }

}
