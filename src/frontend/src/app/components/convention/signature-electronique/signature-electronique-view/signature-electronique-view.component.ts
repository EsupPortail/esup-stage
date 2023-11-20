import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AvenantService } from 'src/app/services/avenant.service';
import * as FileSaver from 'file-saver';

@Component({
  selector: 'app-signature-electronique-view',
  templateUrl: './signature-electronique-view.component.html',
  styleUrls: ['./signature-electronique-view.component.scss']
})
export class SignatureElectroniqueViewComponent implements OnInit {

  @Input() avenant!: any;
  @Input() convention!: any;
  @Input() isGestionnaire: any;
  @Input() isMobile: any;
  @Input() profils: any[] = [];

  @Output() avenantChanged = new EventEmitter<any>();

  data: any[] = [];

  constructor(
    private avenantService: AvenantService,
  ) { }

  ngOnInit(): void {
    this.updateData();
  }

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

  downloadSignedDoc($event: Event, avenant: any): void {
    $event.preventDefault();
    $event.stopPropagation();
    this.avenantService.downloadSignedDoc(avenant.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: 'application/pdf'});
      FileSaver.saveAs(blob, `Avenant_${avenant.id}_${this.convention.etudiant.nom}_${this.convention.etudiant.prenom}_signe.pdf`);
    });
  }
}
