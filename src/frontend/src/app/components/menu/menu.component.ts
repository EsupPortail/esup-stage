import { Component, HostBinding, Input, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { animate, state, style, transition, trigger } from "@angular/animations";
import { MenuService } from "../../services/menu.service";
import { ConventionService } from "../../services/convention.service";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
  animations: [
    trigger('indicatorRotate', [
      state('collapsed', style({transform: 'rotate(90deg)'})),
      state('expanded', style({transform: 'rotate(180deg)'})),
      transition('expanded <=> collapsed',
        animate('225ms cubic-bezier(0.4,0.0,0.2,1)')
      ),
    ])
  ]
})
export class MenuComponent implements OnInit {

  expanded: boolean = false;
  @HostBinding('attr.aria-expanded') ariaExpanded = this.expanded;
  @Input() item: any;
  @Input() depth: number = 0;

  constructor(
    private menuService: MenuService,
    public router: Router,
    private authService: AuthService,
    private conventionService: ConventionService,
  ) { }

  ngOnInit() {
    this.setExpanded(this.router.url);
    this.menuService.currentUrl.subscribe((url: string) => {
      this.setExpanded(url);
    });
    if (this.authService.isEtudiant() && this.item.path === 'conventions/create') {
      this.conventionService.getBrouillon().subscribe((response: any) => {
        if (response.id) {
          this.item.alerte = true;
        }
      });
    }
  }

  setExpanded(url: string): void {
    if (this.item.path && url) {
      this.expanded = url.indexOf(`/${this.item.path}`) === 0;
      this.ariaExpanded = this.expanded;
    }
  }

  onItemSelected(item: any) {
    if (!item.children || !item.children.length) {
      // Suppression des filtres sur le clique du menu
      if (item.filterKey) {
        sessionStorage.removeItem(item.filterKey + '-filters');
        sessionStorage.removeItem(item.filterKey + '-paging');
      }
      this.router.navigate([item.path]);
    }
    if (item.children && item.children.length) {
      this.expanded = !this.expanded;
    }
  }
}
