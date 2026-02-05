import { Component, EventEmitter, Input, Output, OnInit, ViewChild } from '@angular/core';
import { EnseignantService } from "../../../services/enseignant.service";
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { LdapService } from "../../../services/ldap.service";
import { PageEvent } from "@angular/material/paginator";
import { debounceTime } from "rxjs/operators";

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

  formAdresse!: FormGroup;

  @ViewChild(MatExpansionPanel) searchEnseignantPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();

  @Input() modifiable!: boolean;

  constructor(private authService: AuthService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private ldapService: LdapService,
              private enseignantService: EnseignantService,
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
      this.enseignants = this.sortByRelevance(response);
      if (this.enseignants.length === 1) {
        this.choose(this.enseignants[0]);
      }
    });
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

  /**
   * Normalise une chaîne pour la comparaison (minuscules, sans accents)
   */
  private normalizeString(str: string): string {
    if (!str) return '';
    return str
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  /**
   * Calcule un score de pertinence entre une recherche et une valeur
   */
  private calculateMatchScore(searchTerm: string, value: string | string[]): number {
    if (!searchTerm || !value) return 0;

    const normalizedSearch = this.normalizeString(searchTerm);
    const normalizedValue = Array.isArray(value)
      ? this.normalizeString(value.join(' '))
      : this.normalizeString(value);

    if (!normalizedSearch || !normalizedValue) return 0;

    let score = 0;

    // Correspondance exacte complète (score maximum)
    if (normalizedValue === normalizedSearch) {
      score += 1000;
    }
    // Correspondance exacte au début
    else if (normalizedValue.startsWith(normalizedSearch)) {
      score += 500;
    }
    // Correspondance exacte quelque part
    else if (normalizedValue.includes(normalizedSearch)) {
      score += 250;
    }

    // Bonus pour la longueur de correspondance
    const matchRatio = normalizedSearch.length / normalizedValue.length;
    score += matchRatio * 100;

    // Bonus pour correspondance des premiers caractères
    let consecutiveMatches = 0;
    for (let i = 0; i < Math.min(normalizedSearch.length, normalizedValue.length); i++) {
      if (normalizedSearch[i] === normalizedValue[i]) {
        consecutiveMatches++;
      } else {
        break;
      }
    }
    score += consecutiveMatches * 10;

    // Bonus si tous les caractères de recherche sont présents dans l'ordre
    let searchIndex = 0;
    for (let i = 0; i < normalizedValue.length && searchIndex < normalizedSearch.length; i++) {
      if (normalizedValue[i] === normalizedSearch[searchIndex]) {
        searchIndex++;
      }
    }
    if (searchIndex === normalizedSearch.length) {
      score += 50;
    }

    return score;
  }

  /**
   * Trie les enseignants par pertinence en fonction des critères de recherche
   */
  private sortByRelevance(enseignants: any[]): any[] {
    const nomRecherche = this.form.get('nom')?.value?.trim() || '';
    const prenomRecherche = this.form.get('prenom')?.value?.trim() || '';

    return enseignants.sort((a, b) => {
      let scoreA = 0;
      let scoreB = 0;

      // Calcul du score pour le nom
      if (nomRecherche) {
        const scoreNomA = this.calculateMatchScore(nomRecherche, a.sn);
        const scoreNomB = this.calculateMatchScore(nomRecherche, b.sn);
        scoreA += scoreNomA;
        scoreB += scoreNomB;
      }

      // Calcul du score pour le prénom
      if (prenomRecherche) {
        const scorePrenomA = this.calculateMatchScore(prenomRecherche, a.givenName);
        const scorePrenomB = this.calculateMatchScore(prenomRecherche, b.givenName);
        scoreA += scorePrenomA;
        scoreB += scorePrenomB;
      }

      // Si les scores sont différents, tri par score décroissant
      if (scoreB !== scoreA) {
        return scoreB - scoreA;
      }

      // En cas d'égalité de score, tri alphabétique par nom puis prénom
      const nomA = Array.isArray(a.sn) ? a.sn.join(' ') : a.sn || '';
      const nomB = Array.isArray(b.sn) ? b.sn.join(' ') : b.sn || '';
      const compareNom = nomA.localeCompare(nomB);

      if (compareNom !== 0) {
        return compareNom;
      }

      // Si même nom, tri par prénom
      const prenomA = Array.isArray(a.givenName) ? a.givenName.join(' ') : a.givenName || '';
      const prenomB = Array.isArray(b.givenName) ? b.givenName.join(' ') : b.givenName || '';
      return prenomA.localeCompare(prenomB);
    });
  }

}
