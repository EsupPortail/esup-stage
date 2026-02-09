import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {FooterService} from "../../services/footer.service";

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
    this.footerService.getLinks().subscribe(links => {
      this.docUrl = links.wiki;
      this.supportUrl = links.support;
      this.githubUrl = links.github;
      this.webSiteUrl = links.site;
    });
  }

  getAppVersion(): string {
    return this.authService.appVersion;
  }

  hasAnyHelpLink(): boolean {
    return !!(this.docUrl || this.supportUrl);
  }

}
