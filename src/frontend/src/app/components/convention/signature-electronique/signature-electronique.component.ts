import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { ConventionService } from "../../../services/convention.service";
import { CentreGestionService } from "../../../services/centre-gestion.service";

@Component({
  selector: 'app-signature-electronique',
  templateUrl: './signature-electronique.component.html',
  styleUrls: ['./signature-electronique.component.scss']
})
export class SignatureElectroniqueComponent implements OnInit {

  @Input() convention!: any;
  @Output() conventionChanged = new EventEmitter<any>();

  profils: any[] = [];
  data: any[] = [];
  isGestionnaire = false;

  constructor(
    private authService: AuthService,
    private conventionService: ConventionService,
    private centreGestionService: CentreGestionService,
  ) { }

  ngOnInit(): void {
    this.isGestionnaire = this.authService.isAdmin() || this.authService.isGestionnaire();
    this.centreGestionService.getById(this.convention.centreGestion.id).subscribe((response: any) => {
      for (let p of JSON.parse(response.ordreSignature)) {
        const capitalize = p[0].toUpperCase() + p.slice(1)
        let label = '';
        switch (p) {
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
      this.conventionChanged.emit(this.convention);
    });
  }

}
