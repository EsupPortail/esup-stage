import { Component, EventEmitter, HostListener, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ContenuService } from '../../../../services/contenu.service';

type ConfirmPreset = 'info' | 'success' | 'warning' | 'danger';
type ColorKey = 'info' | 'success' | 'warning' | 'danger';

const PRESETS: Record<ConfirmPreset, { icon: string; color: ColorKey; title: string }> = {
  info:    { icon: 'info',          color: 'info',    title: 'Information' },
  success: { icon: 'check_circle',  color: 'success', title: 'Confirmation' },
  warning: { icon: 'warning',       color: 'warning', title: 'Attention' },
  danger:  { icon: 'error',         color: 'danger',  title: 'Confirmation' },
};

@Component({
    selector: '[confirm]',
    templateUrl: './confirm-eval-tuteur.component.html',
    styleUrls: ['./confirm-eval-tuteur.component.scss'],
    standalone: false
})
export class ConfirmEvalTuteurComponent implements OnInit {

  /** Message HTML ou texte */
  @Input() confirmMessage: string = '';

  /** Préréglage (définit icône + couleur + titre par défaut) */
  @Input() preset: ConfirmPreset = 'info';

  /** Icône Material à afficher (ex: "warning"). Prioritaire sur le preset si fourni */
  @Input() icon?: string;

  /** Couleur de texte parmi les clés prédéfinies. Prioritaire sur le preset si fournie */
  @Input() color?: ColorKey;

  /** Titre du dialogue (facultatif). Par défaut: titre du preset */
  @Input() title?: string;

  @Output() confirm = new EventEmitter<void>();

  textBoutonValider!: string;
  textBoutonAnnuler!: string;

  @ViewChild('confirmDialog') template!: TemplateRef<any>;
  dialogRef!: MatDialogRef<any>;

  constructor(
    public dialog: MatDialog,
    private contenuService: ContenuService,
  ) {}

  ngOnInit(): void {
    this.contenuService.get('BOUTON_VALIDER').subscribe((response: any) => {
      this.textBoutonValider = response.texte;
    });
    this.contenuService.get('BOUTON_ANNULER').subscribe((response: any) => {
      this.textBoutonAnnuler = response.texte;
    });
  }

  /** Icône résolue (input > preset) */
  get resolvedIcon(): string {
    return this.icon ?? PRESETS[this.preset].icon;
  }

  /** Classe couleur résolue (input > preset) */
  get resolvedColorClass(): string {
    const key = this.color ?? PRESETS[this.preset].color;
    return `text-${key}`;
  }

  /** Titre résolu (input > preset) */
  get resolvedTitle(): string {
    return this.title ?? PRESETS[this.preset].title;
  }

  @HostListener('click') onClick(): void {
    this.dialogRef = this.dialog.open(this.template);
  }

  confirmed(): void {
    this.confirm.emit();
    this.dialogRef.close();
  }
}
