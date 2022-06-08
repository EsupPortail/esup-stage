import { Component, EventEmitter, Inject, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { PaysService } from "../../../../services/pays.service";
import { ServiceService } from "../../../../services/service.service";
import { MessageService } from "../../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../../constants/app-fonction";
import { Droit } from "../../../../constants/droit";
import { AuthService } from "../../../../services/auth.service";
import { ConfigService } from "../../../../services/config.service";
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-signataire-groupe-modal',
  templateUrl: './signataire-groupe-modal.component.html',
  styleUrls: ['./signataire-groupe-modal.component.scss']
})
export class SignataireGroupeModalComponent implements OnInit {

  service: any;
  etabId: any;
  centreGestion: any;

  services:any[] = [];


  constructor(public serviceService: ServiceService,
              private messageService: MessageService,
              private authService: AuthService,
              private paysService: PaysService,
              private configService: ConfigService,
              private dialogRef: MatDialogRef<SignataireGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.etabId = data.etabId;
  }

  ngOnInit(): void {
    this.serviceService.getByStructure(this.etabId, -1).subscribe((response: any) => {
      this.services = response;
    });
  }

  close(): void {
    this.dialogRef.close(null);
  }

  choose(row: any): void {
    this.dialogRef.close(row.id);
  }

}


