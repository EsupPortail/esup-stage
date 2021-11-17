import { Component, EventEmitter, Input, Output, OnInit, ViewChild } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { LdapService } from "../../../services/ldap.service";
import { PageEvent } from "@angular/material/paginator";

@Component({
  selector: 'app-enseignant-referent',
  templateUrl: './enseignant-referent.component.html',
  styleUrls: ['./enseignant-referent.component.scss']
})
export class EnseignantReferentComponent implements OnInit {

  form: FormGroup;
  columns = ['nomprenom', 'mail', 'departement', 'action'];

  enseignants: any[] = [];
  @Input() enseignant: any;

  formAdresse: FormGroup;

  @ViewChild(MatExpansionPanel) searchEnseignantPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();

  constructor(private authService: AuthService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private ldapService: LdapService,
     ) {
    this.form = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
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

  choose(row: any): void {
      this.enseignant = row;
      if (this.searchEnseignantPanel) {
        this.searchEnseignantPanel.expanded = false;
      }
      this.updateEnseignant();
  }

  updateEnseignant(): void {
    const data = {
      "field":'idEnseignant',
      "value":this.enseignant.id,
    };
    //TODO update
  }
}
