import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ConventionService } from "../../../../services/convention.service";
import { MessageService } from "../../../../services/message.service";
import { AuthService } from "../../../../services/auth.service";

@Component({
  selector: 'app-validation-card',
  templateUrl: './validation-card.component.html',
  styleUrls: ['./validation-card.component.scss']
})
export class ValidationCardComponent implements OnInit {

  @Input() convention: any;
  @Input() validation: string;
  @Input() validationLibelles: any;
  @Input() validationsActives: string[];
  @Output() conventionChanged = new EventEmitter<any>();

  constructor(
    private conventionService: ConventionService,
    private messageService: MessageService,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
  }

  canRevertValidation(): boolean {
    // On empêche la dévalidation si la convention a déjà été déposée pour signature électronique
    if (this.convention.documentId) {
      return false;
    }
    // Un enseignant n'a les droits que sur la validation pédagogique
    if (this.authService.isEnseignant() && this.validation === 'validationConvention') {
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
    // Un enseignant n'a les droits que sur la validation pédagogique
    if (this.authService.isEnseignant() && this.validation === 'validationConvention') {
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
