import { Directive, HostListener } from '@angular/core';
import { MatLegacyTabGroup as MatTabGroup } from "@angular/material/legacy-tabs";
import { TechnicalService } from "../services/technical.service";

@Directive({
  selector: 'mat-tab-group'
})
export class MatTabNavChangeDirective {

  constructor(
    private matTabs: MatTabGroup,
  ) {
  }

  @HostListener('click', ['$event.target']) onClick(element: HTMLElement): void {
    if (window.innerWidth < TechnicalService.MAX_WIDTH) {
      const tabSize = this.matTabs._tabs.length;
      if (element.className.indexOf('mat-tab-header-pagination-chevron') > -1) {
        element = element.parentElement!;
      }
      if (element.className.indexOf('mat-tab-header-pagination') > -1) {

        const increment = element.className.indexOf('mat-tab-header-pagination-after') > -1 ? 1 : -1;
        let newTabIndex = this.matTabs.selectedIndex! + increment;
        if (newTabIndex > tabSize - 1) newTabIndex = tabSize - 1;
        if (newTabIndex < 0) newTabIndex = 0

        let tab = this.matTabs._tabs.get(newTabIndex);
        let foundEnabled = tab && !tab!.disabled;
        while (newTabIndex < tabSize - 1 && newTabIndex > 0 && !foundEnabled) {
          newTabIndex = newTabIndex + increment;
          tab = this.matTabs._tabs.get(newTabIndex);
          foundEnabled = tab && !tab!.disabled
        }

        if (foundEnabled) this.matTabs.selectedIndex = newTabIndex;
      }
    }
  }
}
