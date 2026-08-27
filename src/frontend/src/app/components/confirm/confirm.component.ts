import { Component, EventEmitter, HostListener, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: '[confirm]',
    templateUrl: './confirm.component.html',
    styleUrls: ['./confirm.component.scss'],
    standalone: false
})
export class ConfirmComponent implements OnInit {

  @Input() confirmMessage: string = '';
  @Output() confirm = new EventEmitter();

  @ViewChild('confirmDialog') template!: TemplateRef<any>;
  dialogRef!: MatDialogRef<any>;

  constructor(public dialog: MatDialog) { }

  ngOnInit(): void {
  }

  @HostListener('click') onClick(): void {
    this.dialogRef = this.dialog.open(this.template);
  }

  confirmed(): void {
    this.confirm.emit();
    this.dialogRef.close();
  }

}
