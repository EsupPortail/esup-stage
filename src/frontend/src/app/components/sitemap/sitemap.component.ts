import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SitemapService } from '../../services/sitemap.service';
import { SitemapGroup } from '../../models/sitemap.model';

@Component({
  selector: 'app-sitemap',
  imports: [CommonModule, RouterLink],
  templateUrl: './sitemap.component.html',
  styleUrl: './sitemap.component.scss'
})
export class SitemapComponent {
  private sitemapService = inject(SitemapService);
  groups: SitemapGroup[] = this.sitemapService.getGroups();
}
