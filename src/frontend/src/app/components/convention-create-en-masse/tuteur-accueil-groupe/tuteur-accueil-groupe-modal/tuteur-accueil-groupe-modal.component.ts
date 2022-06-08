import { Component, EventEmitter, Inject, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { PaysService } from "../../../../services/pays.service";
import { ContactService } from "../../../../services/contact.service";
import { MessageService } from "../../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../../constants/app-fonction";
import { Droit } from "../../../../constants/droit";
import { AuthService } from "../../../../services/auth.service";
import { ConfigService } from "../../../../services/config.service";
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-tuteur-accueil-groupe-modal',
  templateUrl: './tuteur-accueil-groupe-modal.component.html',
  styleUrls: ['./tuteur-accueil-groupe-modal.component.scss']
})
export class TuteurAccueilGroupeModalComponent implements OnInit {

  contact: any;
  serviceId: any;
  centreGestion: any;

  contacts:any[] = [];


  constructor(public contactService: ContactService,
              private messageService: MessageService,
              private authService: AuthService,
              private paysService: PaysService,
              private configService: ConfigService,
              private dialogRef: MatDialogRef<TuteurAccueilGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.serviceId = data.serviceId;
  }

  ngOnInit(): void {
    this.contactService.getByService(this.serviceId, -1).subscribe((response: any) => {
      this.contacts = response;
    });
  }

  close(): void {
    this.dialogRef.close(null);
  }

  choose(row: any): void {
    this.dialogRef.close(row.id);
  }

}

