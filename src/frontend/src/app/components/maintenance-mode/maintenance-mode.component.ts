import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from "@angular/router";
import { Subscription } from "rxjs";
import { AuthService } from "../../services/auth.service";
import { MaintenanceStateService } from "../../services/maintenance-state.service";

@Component({
  selector: 'app-maintenance-mode',
  templateUrl: './maintenance-mode.component.html',
  styleUrls: ['./maintenance-mode.component.scss'],
  standalone: false
})
export class MaintenanceModeComponent implements OnInit, OnDestroy {
  private stateSubscription?: Subscription;

  constructor(
    private maintenanceStateService: MaintenanceStateService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.stateSubscription = this.maintenanceStateService.state$.subscribe(state => {
      if (!state.active) {
        void this.router.navigateByUrl('/');
        return;
      }

      if (this.authService.userConnected && this.authService.isAdmin()) {
        void this.router.navigateByUrl('/');
        return;
      }
    });
  }

  ngOnDestroy(): void {
    this.stateSubscription?.unsubscribe();
  }
}
