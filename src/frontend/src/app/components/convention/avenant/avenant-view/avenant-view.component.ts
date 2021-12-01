import { Component, Output, EventEmitter, OnInit, Input } from '@angular/core';
import { AuthService } from "../../../../services/auth.service";
import { MessageService } from "../../../../services/message.service";
import { AvenantService } from "../../../../services/avenant.service";

@Component({
  selector: 'app-avenant-view',
  templateUrl: './avenant-view.component.html',
  styleUrls: ['./avenant-view.component.scss']
})
export class AvenantViewComponent implements OnInit {

  @Input() avenant: any;
  @Input() convention: any;
  @Output() updated = new EventEmitter<any>();

  constructor(private authService: AuthService,
              private messageService: MessageService,
              private avenantService: AvenantService,
  ) { }

  ngOnInit(): void {
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  isGestionnaire(): boolean {
    return this.authService.isGestionnaire();
  }

  cancelValidation(): void {
    this.avenantService.cancelValidation(this.avenant.id).subscribe((response: any) => {
      this.avenant = response;
      this.messageService.setSuccess('Validation de l\'avenant annulée avec succès');
      this.updated.emit();
    });
  }
}
