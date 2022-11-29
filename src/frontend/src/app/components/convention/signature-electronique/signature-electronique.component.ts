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

  profils: string[] = [];
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
      this.profils = JSON.parse(response.ordreSignature);
      for (let profil of this.profils) {
        this.data.push({
          profil: profil,
          statutSignature: this.convention['statutSignature' + profil[0].toUpperCase() + profil.slice(1)],
          dateSignature: this.convention['dateSignature' + profil[0].toUpperCase() + profil.slice(1)],
        });
      }
    });
  }

  updateSignatureInfos(): void {
    this.conventionService.updateSignatureInfo(this.convention.id).subscribe((response: any) => {
      this.convention = response;
      this.conventionChanged.emit(this.convention);
    });
  }

}
