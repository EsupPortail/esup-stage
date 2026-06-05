import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { MessageService } from "../../../services/message.service";
import { Router } from "@angular/router";

@Component({
    selector: 'app-validation-creation',
    templateUrl: './validation-creation.component.html',
    styleUrls: ['./validation-creation.component.scss'],
    standalone: false
})
export class ValidationCreationComponent implements OnInit {

  @Input() groupeEtudiant: any;
  @Input() allValid: boolean;

  @Output() validated = new EventEmitter<any>();

  constructor(public groupeEtudiantService: GroupeEtudiantService,
              private messageService: MessageService,
              private router: Router
              ) { }

  ngOnInit(): void {
  }

  validate(): void {
    this.groupeEtudiantService.mergeAndValidateConventions(this.groupeEtudiant.id).subscribe((response: any) => {
      if(this.isModif())
        this.messageService.setSuccess('Les conventions du groupe ont étés mergées et validées avec succès');
      else
        this.messageService.setSuccess('Les conventions du groupe ont étés modifiées avec succès');
      this.router.navigate([`/convention-create-en-masse/groupes`])
    });
  }

  createGroup(): void {
    this.groupeEtudiantService.validateBrouillon(this.groupeEtudiant.id).subscribe((response: any) => {
      this.messageService.setSuccess('Le groupe a été créé avec succès');
      this.router.navigate([`/convention-create-en-masse/groupes`])
    });
  }

  isModif(): boolean {
    return this.groupeEtudiant && this.groupeEtudiant.validationCreation
  }
}
