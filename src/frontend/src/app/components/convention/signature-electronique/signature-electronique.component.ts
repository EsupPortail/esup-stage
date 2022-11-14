import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { ConventionService } from "../../../services/convention.service";

@Component({
  selector: 'app-signature-electronique',
  templateUrl: './signature-electronique.component.html',
  styleUrls: ['./signature-electronique.component.scss']
})
export class SignatureElectroniqueComponent implements OnInit {

  @Input() convention!: any;
  @Output() conventionChanged = new EventEmitter<any>();

  profils = ['etudiant', 'enseignant', 'tuteur', 'signataire', 'viseur'];
  data: any[] = [];
  isGestionnaire = false;

  constructor(
    private authService: AuthService,
    private conventionService: ConventionService,
  ) { }

  ngOnInit(): void {
    this.isGestionnaire = this.authService.isAdmin() || this.authService.isGestionnaire();
    for (let profil of this.profils) {
      this.data.push({
        profil: profil,
        statutSignature: this.convention['statutSignature' + profil[0].toUpperCase() + profil.slice(1)],
        dateSignature: this.convention['dateSignature' + profil[0].toUpperCase() + profil.slice(1)],
      });
    }
  }

  updateSignatureInfos(): void {
    this.conventionService.updateSignatureInfo(this.convention.id).subscribe((response: any) => {
      this.convention = response;
      this.conventionChanged.emit(this.convention);
    });
  }

}
