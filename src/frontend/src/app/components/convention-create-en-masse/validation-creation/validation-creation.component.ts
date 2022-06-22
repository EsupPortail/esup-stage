import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-validation-creation',
  templateUrl: './validation-creation.component.html',
  styleUrls: ['./validation-creation.component.scss']
})
export class ValidationCreationComponent implements OnInit {

  @Input() groupeEtudiant: any;
  @Output() validated = new EventEmitter<any>();

  constructor(public groupeEtudiantService: GroupeEtudiantService,
              private messageService: MessageService,
              ) { }

  ngOnInit(): void {
  }

  validate(): void {
    this.groupeEtudiantService.validate(this.groupeEtudiant.id).subscribe((response: any) => {
      this.messageService.setSuccess('Les conventions ont étés validées avec succès');
      this.groupeEtudiant = response;
      this.validated.emit(this.groupeEtudiant);
    });
  }

}
