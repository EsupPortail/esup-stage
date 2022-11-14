import { Component, Input, OnInit } from '@angular/core';
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: 'app-signature-electronique',
  templateUrl: './signature-electronique.component.html',
  styleUrls: ['./signature-electronique.component.scss']
})
export class SignatureElectroniqueComponent implements OnInit {

  @Input() convention!: any;
  profils = ['etudiant', 'enseignant', 'tuteur', 'signataire', 'viseur'];
  data: any[] = [];
  isEtuOrEns = true;

  constructor(
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.isEtuOrEns = this.authService.isEtudiant() || this.authService.isEnseignant();
    for (let profil of this.profils) {
      this.data.push({
        profil: profil,
        statutSignature: this.convention['statutSignature' + profil[0].toUpperCase() + profil.slice(1)],
        dateSignature: this.convention['dateSignature' + profil[0].toUpperCase() + profil.slice(1)],
      });
    }
  }

}
