import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-signature-electronique',
  templateUrl: './signature-electronique.component.html',
  styleUrls: ['./signature-electronique.component.scss']
})
export class SignatureElectroniqueComponent implements OnInit {

  @Input() convention!: any;
  profils = ['etudiant', 'enseignant', 'tuteur', 'signataire', 'viseur'];
  data: any[] = [];

  constructor() { }

  ngOnInit(): void {
    for (let profil of this.profils) {
      this.data.push({
        profil: profil,
        dateSignature: this.convention['dateSignature' + profil[0].toUpperCase() + profil.slice(1)],
        statutSignature: this.convention['statutSignature' + profil[0].toUpperCase() + profil.slice(1)],
        url: null,
      });
    }
  }

}
