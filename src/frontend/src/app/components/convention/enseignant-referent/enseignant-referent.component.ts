import { Component, EventEmitter, Input, Output, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { EnseignantService } from "../../../services/enseignant.service";
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { LdapService } from "../../../services/ldap.service";
import { PageEvent } from "@angular/material/paginator";
import { debounceTime, takeUntil } from "rxjs/operators";
import { Subject } from "rxjs";
import { AccessibilityService } from "../../../services/accessibility.service";

@Component({
    selector: 'app-enseignant-referent',
    templateUrl: './enseignant-referent.component.html',
    styleUrls: ['./enseignant-referent.component.scss'],
    standalone: false
})
export class EnseignantReferentComponent implements OnInit, OnDestroy {

  form: FormGroup;
  columns = ['nomprenom', 'mail', 'departement', 'action'];

  enseignants: any[] = [];
  @Input() enseignant: any;

  formAdresse!: FormGroup;

  @ViewChild(MatExpansionPanel) searchEnseignantPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();

  @Input() modifiable!: boolean;

  disableAutoSearch: boolean = false;
  hasPendingSearchEnseignant: boolean = false;

  private _destroy$ = new Subject<void>();

  constructor(private authService: AuthService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private ldapService: LdapService,
              private enseignantService: EnseignantService,
              private accessibilityService: AccessibilityService,
     ) {
    this.form = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
    this.loadDisableAutoSearchPref();

    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      if (this.disableAutoSearch) {
        this.hasPendingSearchEnseignant = this.hasEnseignantCriteria();
        if (!this.hasPendingSearchEnseignant) {
          this.enseignants = [];
        }
        return;
      }
      this.search();
    });

    this.accessibilityService.disableAutoSearch$
      .pipe(takeUntil(this._destroy$))
      .subscribe((value) => {
        const previous = this.disableAutoSearch;
        this.disableAutoSearch = value;

        if (this.disableAutoSearch && !previous) {
          this.hasPendingSearchEnseignant = this.hasEnseignantCriteria();
        } else if (!this.disableAutoSearch && previous) {
          if (this.hasPendingSearchEnseignant) {
            this.runSearchEnseignant();
          }
        }
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
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
    this.hasPendingSearchEnseignant = false;
  }

  choose(row: any): void {
      if (this.searchEnseignantPanel) {
        this.searchEnseignantPanel.expanded = false;
      }
      this.enseignantService.getByUid(row.supannAliasLogin).subscribe((response: any) => {
        this.enseignant = response;
        if (this.enseignant == null){
          this.createEnseignant(row);
        }else{
          // Mise à jour des données de l'enseignant
          this.updateEnseignant(this.enseignant.id, row);
        }
      });
  }

  createEnseignant(row: any): void {
    const data = {
      "nom": row.sn.join(' '),
      "prenom": row.givenName.join(' '),
      "mail": row.mail,
      "typePersonne": row.eduPersonPrimaryAffiliation,
      "uidEnseignant": row.uid,
      "tel": row.telephoneNumber,
    };

    this.enseignantService.create(data).subscribe((response: any) => {
      this.enseignant = response;
      this.validated.emit(this.enseignant);
    });
  }

  updateEnseignant(id: number, row: any): void {
    const data = {
      nom: row.sn.join(' '),
      prenom: row.givenName.join(' '),
      mail: row.mail,
      typePersonne: row.eduPersonPrimaryAffiliation,
      uidEnseignant: row.uid,
      tel: row.telephoneNumber,
    };

    this.enseignantService.update(id, data).subscribe((response: any) => {
      this.enseignant = response;
      this.validated.emit(this.enseignant);
    });
  }

  runSearchEnseignant(): void {
    if (!this.hasEnseignantCriteria()) {
      this.hasPendingSearchEnseignant = false;
      return;
    }
    this.search();
  }

  private hasEnseignantCriteria(): boolean {
    const nom = (this.form.get('nom')?.value || '').toString().trim();
    const prenom = (this.form.get('prenom')?.value || '').toString().trim();
    return nom.length > 0 || prenom.length > 0;
  }

  private loadDisableAutoSearchPref(): void {
    const saved = localStorage.getItem('accessibilityPreferences');
    if (!saved) return;
    try {
      const prefs = JSON.parse(saved);
      this.disableAutoSearch = !!prefs?.disableAutoSearch;
    } catch {}
  }
}
