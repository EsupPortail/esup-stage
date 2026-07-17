import { Component, EventEmitter, Input, Output} from '@angular/core';
import {ConventionService} from "../../../../services/convention.service";
import {MessageService} from "../../../../services/message.service";
import {AuthService} from "../../../../services/auth.service";

@Component({
  selector: 'app-validation-card',
  templateUrl: './validation-card.component.html',
  styleUrls: ['./validation-card.component.scss'],
  standalone: false
})
export class ValidationCardComponent {

  @Input() convention: any;
  @Input() validation: string;
  @Input() validationLibelles: any;
  @Input() validationsActives: string[];
  @Output() conventionChanged = new EventEmitter<any>();

  constructor(
    private readonly conventionService: ConventionService,
    private readonly messageService: MessageService,
    private readonly authService: AuthService,
  ) {
  }

  canRevertValidation(): boolean {
    if (!this.canActOnValidation()) {
      return false;
    }
    const validationOrdre = this.convention.centreGestion[this.validation + 'Ordre'];
    // On peut toujours dévalider la dernière validation
    if (validationOrdre === this.validationsActives.length) {
      return true;
    }
    // On ne peut pas dévalider si la validation suivante est passée
    return !this.convention[this.validationsActives[validationOrdre]];
  }

  canValidate(): boolean {
    if (!this.canActOnValidation()) {
      return false;
    }
    const validationOrdre = this.convention.centreGestion[this.validation + 'Ordre'];
    // On peut toujours valider la 1er validation
    if (validationOrdre === 1) {
      return true;
    }
    // On ne peut pas valider si la validation précédente n'est pas passée
    return this.convention[this.validationsActives[validationOrdre - 2]];
  }

  /**
   * Droits de l'utilisateur sur ce type de validation, pour le centre de gestion de la convention.
   * Reflète les contrôles du backend (ConventionService.checkValidationType) : un enseignant seul
   * n'a que la validation pédagogique, et la vérification administrative suppose d'être gestionnaire.
   */
  private canActOnValidation(): boolean {
    const idCentreGestion = this.convention.centreGestion.id;
    if (this.validation === 'verificationAdministrative') {
      return this.authService.isGestionnaireForCentre(idCentreGestion);
    }
    if (this.validation === 'validationConvention') {
      return !this.authService.isEnseignantOnlyForCentre(idCentreGestion);
    }
    return true;
  }

  validate(): void {
    this.conventionService.validate(this.convention.id, this.validation).subscribe((response: any) => {
      this.messageService.setSuccess('La convention a été validée.');
      this.convention = response;
      this.conventionChanged.emit(this.convention);
    });
  }

  cancelValidation(): void {
    this.conventionService.unvalidate(this.convention.id, this.validation).subscribe((response: any) => {
      this.messageService.setSuccess('La convention a été dévalidée.');
      this.convention = response;
      this.conventionChanged.emit(this.convention);
    });
  }

}
