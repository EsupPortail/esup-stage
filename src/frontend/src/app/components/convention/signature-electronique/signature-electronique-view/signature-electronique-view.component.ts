import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AvenantService } from 'src/app/services/avenant.service';

@Component({
  selector: 'app-signature-electronique-view',
  templateUrl: './signature-electronique-view.component.html',
  styleUrls: ['./signature-electronique-view.component.scss']
})
export class SignatureElectroniqueViewComponent {

  @Input() avenant!: any;
  @Input() isGestionnaire: any;
  @Input() isMobile: any;
  @Input() profils: any[] = [];

  @Output() avenantChanged = new EventEmitter<any>();

  data: any[] = [];

  constructor(
    private avenantService: AvenantService,
  ) { }

  updateData(): void {
    this.data = [];
    for (let profil of this.profils) {
      this.data.push({
        profil: profil.label,
        dateDepot: this.avenant['dateDepot' + profil.capitalize],
        dateSignature: this.avenant['dateSignature' + profil.capitalize],
      });
    }
  }

  updateSignatureInfos(): void {
    this.avenantService.updateSignatureInfo(this.avenant.id).subscribe((avenant: any) => {
      this.avenant = avenant;
      this.updateData();
      this.avenantChanged.emit(this.avenant);
    });
  }
}
