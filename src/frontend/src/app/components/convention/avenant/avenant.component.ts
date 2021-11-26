import { Component, OnInit, Input } from '@angular/core';
import { AvenantService } from "../../../services/avenant.service";

@Component({
  selector: 'app-avenant',
  templateUrl: './avenant.component.html',
  styleUrls: ['./avenant.component.scss']
})
export class AvenantComponent implements OnInit {

  avenants: any[] = [];

  @Input() convention: any;

  constructor(private avenantService: AvenantService,
  ) {
  }

  ngOnInit(): void {
    this.updateAvenants();
  }

  updateAvenants(): void {
    this.avenantService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.avenants = response;
    });
  }

  isConventionValide(): boolean {
    return this.convention && this.convention.validationPedagogique && this.convention.validationConvention;
  }
}
