import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { AvenantService } from "../../../services/avenant.service";
import { MatExpansionPanel } from "@angular/material/expansion";

@Component({
  selector: 'app-avenant',
  templateUrl: './avenant.component.html',
  styleUrls: ['./avenant.component.scss']
})
export class AvenantComponent implements OnInit {

  avenants: any[] = [];

  @Input() convention: any;
  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  constructor(private avenantService: AvenantService,
  ) {
  }

  ngOnInit(): void {
    this.updateAvenants();
  }

  updateAvenants(): void {
    this.avenantService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.avenants = response;
      if (this.firstPanel && this.avenants.length > 0) {
        this.firstPanel.expanded = false;
      }
    });
  }

  isConventionValide(): boolean {
    return this.convention && this.convention.validationPedagogique && this.convention.validationConvention;
  }
}
