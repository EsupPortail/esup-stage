import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { AvenantService } from "../../../services/avenant.service";
import { MatExpansionPanel } from '@angular/material/expansion';

@Component({
  selector: 'app-avenant',
  templateUrl: './avenant.component.html',
  styleUrls: ['./avenant.component.scss']
})
export class AvenantComponent implements OnInit {

  avenants: any[] = [];

  @Input() convention: any;
  @ViewChild('createAvenant') createAvenantPanel: MatExpansionPanel|undefined;
  @ViewChild('listAvenants') listAvenantsPanel: MatExpansionPanel|undefined;

  @Output() avenantChanged = new EventEmitter<any>();

  constructor(private avenantService: AvenantService,
  ) {
  }

  ngOnInit(): void {
    this.updateAvenants();
  }

  updateAvenants(): void {
    this.avenantService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.avenants = response;
      if (this.avenants.length > 0) {
        if (this.listAvenantsPanel) {
          this.listAvenantsPanel.expanded = true;
        }
      } else {
        if (this.createAvenantPanel) {
          this.createAvenantPanel.expanded = true;
        }
      }
      this.avenantChanged.emit(this.avenants);
    });
  }
  
  isConventionValide(): boolean {
    let conventionValide = null;
    if (this.convention.centreGestion != null) {
      if (this.convention.centreGestion.validationPedagogique) {
        conventionValide = this.convention.validationPedagogique;
      }
      if (this.convention.centreGestion.validationConvention) {
        if (conventionValide != null) {
          conventionValide = conventionValide && this.convention.validationConvention
        } else {
          conventionValide = this.convention.validationConvention
        }
      }
      return conventionValide;
    }
    return false;
  }
}
