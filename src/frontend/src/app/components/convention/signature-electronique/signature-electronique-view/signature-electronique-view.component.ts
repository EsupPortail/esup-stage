import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AvenantService } from 'src/app/services/avenant.service';
import * as FileSaver from 'file-saver';
import {UserService} from "../../../../services/user.service";

@Component({
  selector: 'app-signature-electronique-view',
  templateUrl: './signature-electronique-view.component.html',
  styleUrls: ['./signature-electronique-view.component.scss']
})
export class SignatureElectroniqueViewComponent implements OnInit {

  @Input() avenant!: any;
  @Input() convention!: any;
  @Input() isMobile: any;
  @Input() profils: any[] = [];

  @Output() avenantChanged = new EventEmitter<any>();

  data: any[] = [];
  NomPrenomEnvoiSignature: string | undefined;


  constructor(
    private avenantService: AvenantService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.updateData();
    this.getNomPrenomEnvoiSignature();
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
      this.getNomPrenomEnvoiSignature();
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

  isActualisationActif(): boolean {
    const date = new Date();
    date.setMinutes(date.getMinutes() - 30);
    return new Date(this.avenant.dateActualisationSignature) >= date;
  }

  getNomPrenomEnvoiSignature(): void {
    if (this.avenant.loginEnvoiSignature) {
      this.userService.findOneByLogin(this.avenant.loginEnvoiSignature).subscribe((response: any) => {
        if (response) {
          this.NomPrenomEnvoiSignature = response.nom + ' ' + response.prenom;
        }
      });
    }
  }

}
