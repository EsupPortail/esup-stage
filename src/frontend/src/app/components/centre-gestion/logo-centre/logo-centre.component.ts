import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-logo-centre',
  templateUrl: './logo-centre.component.html',
  styleUrls: ['./logo-centre.component.scss']
})
export class LogoCentreComponent implements OnInit {

  @Input() centreGestion: any;

  @Output() refreshCentreGestion = new EventEmitter<any>();

  logoFile: File|undefined;

  constructor(private centreGestionService: CentreGestionService, private messageService: MessageService) { }

  ngOnInit(): void {
  }

  onLogoChange(event: any): void {
    this.logoFile = event.target.files.item(0);
    const formData = new FormData();
    if (this.logoFile?.type.indexOf('image/') === -1) {
      this.messageService.setError("Le fichier doit être au format image");
      this.logoFile = undefined;
      return;
    }

    if (this.logoFile !== undefined) {
      formData.append('logo', this.logoFile, this.logoFile.name);
      this.centreGestionService.insertLogoCentre(formData, this.centreGestion.id).subscribe((response: any) => {
        this.centreGestion = response;
        this.refreshCentreGestion.emit(this.centreGestion);
      });
    }
  }

}
