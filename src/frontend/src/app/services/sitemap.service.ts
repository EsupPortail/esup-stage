import { Injectable } from '@angular/core';
import { Router, Route } from '@angular/router';
import { AuthService } from './auth.service';
import {
  SitemapGroup,
  SitemapItem,
  SitemapMeta,
  SITEMAP_GROUP_ORDER
} from '../models/sitemap.model';

@Injectable({ providedIn: 'root' })
export class SitemapService {
  constructor(private router: Router, private authService: AuthService) {}

  getGroups(): SitemapGroup[] {
    const items: SitemapItem[] = [];

    const walk = (routes: Route[], parentPath: string = '') => {
      for (const route of routes) {
        const fullPath = this.joinPaths(parentPath, route.path ?? '');

        const meta = route.data?.['sitemap'] as SitemapMeta | undefined;

        if (meta && !meta.exclude && route.path !== undefined) {
          const roleData = route.data?.['role'];

          // 🔐 filtre sur les droits : même logique que tes menus / AuthGuard
          // -> nécessite que AuthService.canAccess(...) existe et renvoie un boolean
          const authorized = this.authService.canAccess(roleData);

          if (authorized) {
            items.push({
              url: '/' + fullPath,
              label: meta.label,
              group: meta.group,
              order: meta.order
            });
          }
        }

        if (route.children?.length) {
          walk(route.children, fullPath);
        }
      }
    };

    walk(this.router.config);

    const groupsMap = new Map<string, SitemapItem[]>();

    for (const item of items) {
      const groupName = item.group ?? 'Autres';
      if (!groupsMap.has(groupName)) {
        groupsMap.set(groupName, []);
      }
      groupsMap.get(groupName)!.push(item);
    }

    const groups: SitemapGroup[] = [];
    for (const [name, groupItems] of groupsMap.entries()) {
      groups.push({
        name,
        items: groupItems.sort((a, b) => (a.order ?? 999) - (b.order ?? 999))
      });
    }

    // 🔢 Tri des groupes selon SITEMAP_GROUP_ORDER
    groups.sort((a, b) => {
      const orderA = SITEMAP_GROUP_ORDER[a.name] ?? 9999;
      const orderB = SITEMAP_GROUP_ORDER[b.name] ?? 9999;

      if (orderA !== orderB) {
        return orderA - orderB;
      }

      // En cas d’égalité ou groupe non listé, tri alpha
      return a.name.localeCompare(b.name);
    });

    return groups;
  }

  private joinPaths(parent: string, child: string): string {
    if (!parent) { return child; }
    if (!child) { return parent; }
    return `${parent.replace(/\/$/, '')}/${child.replace(/^\//, '')}`;
  }
}
