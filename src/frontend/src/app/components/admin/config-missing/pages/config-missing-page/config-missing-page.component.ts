import { Component, OnInit } from '@angular/core';
import { ConfigMissingService } from "../../../../../services/config-missing.service";

@Component({
  selector: 'app-config-missing-page',
  templateUrl: './config-missing-page.component.html',
  styleUrl: './config-missing-page.component.scss',
  standalone: false
})
export class ConfigMissingPageComponent implements OnInit {

  missingKeys: string[] = [];

  constructor(private configMissingService: ConfigMissingService) {}

  ngOnInit(): void {
    this.configMissingService.getMissing().subscribe({
      next: (resp: { missing: string[] }) => {
        this.missingKeys = resp?.missing || [];
      },
      error: () => {
        this.missingKeys = [];
      }
    });
  }
}
