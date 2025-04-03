import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { ConventionService } from "../../../services/convention.service";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { TechnicalService } from "../../../services/technical.service";
import * as FileSaver from 'file-saver';
import { ConfigService } from '../../../services/config.service';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-signature-electronique',
  templateUrl: './signature-electronique.component.html',
  styleUrls: ['./signature-electronique.component.scss']
})
export class SignatureElectroniqueComponent implements OnInit {

  @Input() convention!: any;
  @Output() conventionChanged = new EventEmitter<any>();

  isMobile = false;
  profils: any[] = [];
  data: any[] = [];
  isGestionnaire = false;
  signatureType: string | undefined;
  NomPrenomEnvoiSignature: string | undefined;

  constructor(
    private technicalService: TechnicalService,
    private authService: AuthService,
    private conventionService: ConventionService,
    private centreGestionService: CentreGestionService,
    private configService: ConfigService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.technicalService.isMobile.subscribe((value: boolean) => {
      this.isMobile = value;
    });
    this.configService.getConfigGenerale().subscribe((response) => {
      this.signatureType = response.signatureType;
    });
    this.isGestionnaire = this.authService.isAdmin() || this.authService.isGestionnaire();
    this.getNomPrenomEnvoieSignature()
    this.centreGestionService.getById(this.convention.centreGestion.id).subscribe((response: any) => {
      for (let p of response.signataires) {
        const profil = p.id.signataire;
        const capitalize = profil[0].toUpperCase() + profil.slice(1)
        let label = '';
        switch (profil) {
          case 'etudiant':
            label = 'Étudiant';
            break;
          case 'tuteur':
            label = 'Tuteur professionnel';
            break;
          case 'viseur':
            label = 'Viseur établissement';
            break;
          case 'signataire':
            label = 'Signataire organisme d\'accueil';
            break;
          default:
            label = capitalize;
            break;
        }
        this.profils.push({
          key: p,
          capitalize: capitalize,
          label: label,
        });
      }
      this.updateData();
    });
  }

  updateData(): void {
    this.data = [];
    for (let profil of this.profils) {
      this.data.push({
        profil: profil.label,
        dateDepot: this.convention['dateDepot' + profil.capitalize],
        dateSignature: this.convention['dateSignature' + profil.capitalize],
      });
    }
  }

  updateSignatureInfos(): void {
    this.conventionService.updateSignatureInfo(this.convention.id).subscribe((response: any) => {
      this.convention = response;
      this.updateData();
      this.getNomPrenomEnvoieSignature()
      this.conventionChanged.emit(this.convention);
    });
  }

  downloadSignedDoc($event: Event, convention: any): void {
    $event.preventDefault();
    $event.stopPropagation();
    this.conventionService.downloadSignedDoc(convention.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: 'application/pdf'});
      FileSaver.saveAs(blob, `Convention_${convention.id}_${convention.etudiant.nom}_${convention.etudiant.prenom}_signe.pdf`);
    });
  }

  isActualisationActif(): boolean {
    const date = new Date();
    date.setMinutes(date.getMinutes() - 30);
    return new Date(this.convention.dateActualisationSignature) >= date;
  }

  getNomPrenomEnvoieSignature(): void {
    if (this.convention.loginEnvoiSignature) {
      this.userService.findOneByLogin(this.convention.loginEnvoiSignature).subscribe((response: any) => {
        if (response) {
          this.NomPrenomEnvoiSignature = response.nom + ' ' + response.prenom;
        }
      });
    }
  }

}
