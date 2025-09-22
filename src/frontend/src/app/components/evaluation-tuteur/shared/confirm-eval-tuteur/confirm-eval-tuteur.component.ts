import { Component, EventEmitter, HostListener, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ContenuService} from "../../../../services/contenu.service";

@Component({
  selector: '[confirm]',
  templateUrl: './confirm-eval-tuteur.component.html',
  styleUrls: ['./confirm-eval-tuteur.component.scss']
})
export class ConfirmEvalTuteurComponent implements OnInit {

  @Input() confirmMessage: string = '';
  @Output() confirm = new EventEmitter();

  textBoutonValider!: string;
  textBoutonAnnuler!: string;

  @ViewChild('confirmDialog') template!: TemplateRef<any>;
  dialogRef!: MatDialogRef<any>;

  constructor(
    public dialog: MatDialog,
    private contenuService: ContenuService,
  ) { }

  ngOnInit(): void {
    this.contenuService.get('BOUTON_VALIDER').subscribe((response: any) => {
      this.textBoutonValider = response.texte;
    })
    this.contenuService.get('BOUTON_ANNULER').subscribe((response: any) => {
      this.textBoutonAnnuler = response.texte;
    })
  }

  @HostListener('click') onClick(): void {
    this.dialogRef = this.dialog.open(this.template);
  }

  confirmed(): void {
    this.confirm.emit();
    this.dialogRef.close();
  }

}
