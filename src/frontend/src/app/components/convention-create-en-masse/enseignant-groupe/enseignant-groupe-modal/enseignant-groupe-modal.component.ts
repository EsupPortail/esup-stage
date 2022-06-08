import { Component, EventEmitter, Inject, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { EnseignantService } from "../../../../services/enseignant.service";
import { AuthService } from "../../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { LdapService } from "../../../../services/ldap.service";
import { PageEvent } from "@angular/material/paginator";
import { debounceTime } from "rxjs/operators";
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-enseignant-groupe-modal',
  templateUrl: './enseignant-groupe-modal.component.html',
  styleUrls: ['./enseignant-groupe-modal.component.scss']
})
export class EnseignantGroupeModalComponent implements OnInit {

  form: FormGroup;
  columns = ['nomprenom', 'mail', 'departement', 'action'];

  enseignants: any[] = [];
  enseignant: any;

  constructor(private authService: AuthService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private ldapService: LdapService,
              private enseignantService: EnseignantService,
              private dialogRef: MatDialogRef<EnseignantGroupeModalComponent>,
  ) {
    this.form = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      this.search();
    });
  }

  search(): void {
    if (!this.form.get('nom')?.value && !this.form.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.enseignant = undefined;
    this.ldapService.searchEnseignants(this.form.value).subscribe((response: any) => {
      this.enseignants = response;
      if (this.enseignants.length === 1) {
        this.choose(this.enseignants[0]);
      }
    });
  }

  close(): void {
    this.dialogRef.close(null);
  }

  choose(row: any): void {
    this.enseignantService.getByUid(row.supannAliasLogin).subscribe((response: any) => {
      this.enseignant = response;
      if (this.enseignant == null){
        this.createEnseignant(row);
      }else{
        // Mise des données de l'enseignant
        this.updateEnseignant(this.enseignant.id, row);
      }
    });
  }

  createEnseignant(row: any): void {
    const displayName = row.displayName.split(/(\s+)/);
    const data = {
      "nom": displayName[2],
      "prenom": displayName[0],
      "mail": row.mail,
      "typePersonne": row.eduPersonPrimaryAffiliation,
      "uidEnseignant": row.supannAliasLogin,
      "tel": row.telephoneNumber,
    };

    this.enseignantService.create(data).subscribe((response: any) => {
      this.enseignant = response;
      this.dialogRef.close(this.enseignant.id);
    });
  }

  updateEnseignant(id: number, row: any): void {
    const displayName = row.displayName.split(/(\s+)/);
    const data = {
      nom: displayName[2],
      prenom: displayName[0],
      mail: row.mail,
      typePersonne: row.eduPersonPrimaryAffiliation,
      uidEnseignant: row.supannAliasLogin,
      tel: row.telephoneNumber,
    };

    this.enseignantService.update(id, data).subscribe((response: any) => {
      this.enseignant = response;
      this.dialogRef.close(this.enseignant.id);
    });
  }
}
