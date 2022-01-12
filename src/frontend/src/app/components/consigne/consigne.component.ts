import { Component, Input, OnInit, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import * as FileSaver from "file-saver";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ConsigneService } from "../../services/consigne.service";
import { MessageService } from "../../services/message.service";
import * as Editor from '../../../custom-ck5/ckeditor';

@Component({
  selector: 'app-consigne',
  templateUrl: './consigne.component.html',
  styleUrls: ['./consigne.component.scss']
})
export class ConsigneComponent implements OnInit, OnChanges {

  public Editor = Editor;
  public onReady(editor: any) {
    editor.ui.getEditableElement().parentElement.insertBefore(
      editor.ui.view.toolbar.element,
      editor.ui.getEditableElement()
    );
  }

  @Input() consigne: any;
  @Input() idCentreGestion: number;
  @Output() sumitted = new EventEmitter<any>();

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private consigneService: ConsigneService,
    private messageService: MessageService,
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      texte: [this.consigne ? this.consigne.texte : null, [Validators.required]],
      idCentreGestion: [this.idCentreGestion, [Validators.required]],
    });
  }

  ngOnChanges(changes: SimpleChanges) {
  }

  saveConsigne(): void {
    if (this.form.valid) {
      if (this.consigne) {
        this.consigneService.updateConsigne(this.consigne.id, this.form.value).subscribe((response: any) => {
          this.consigne = response;
          this.messageService.setSuccess('Consigne modifée');
          this.sumitted.emit(this.consigne);
        });
      } else {
        this.consigneService.createConsigne(this.form.value).subscribe((response: any) => {
          this.consigne = response;
          this.messageService.setSuccess('Consigne créé');
          this.sumitted.emit(this.consigne);
        });
      }
    }
  }

  downloadDoc(event: any, doc: any): void {
    event.preventDefault();
    event.stopPropagation();
    let mimetype = 'applicaton/pdf';
    if (doc.nomReel.endsWith('.doc')) mimetype = 'application/msword';
    if (doc.nomReel.endsWith('.docx')) mimetype = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    this.consigneService.getDocument(this.consigne.id, doc.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: mimetype});
      FileSaver.saveAs(blob, doc.nomReel);
    });
  }

  addDoc(event: any): void {
    if (this.consigne) {
      const doc = event.target.files.item(0);
      if (doc) {
        if (['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'].indexOf(doc?.type) === -1) {
          this.messageService.setError("Le fichier doit être au format pdf, doc ou docx");
          return;
        }
        const formData = new FormData();
        formData.append('doc', doc, doc.name);
        this.consigneService.addDoc(this.consigne.id, formData).subscribe((response: any) => {
          this.consigne = response;
          this.messageService.setSuccess('Document ajouté');
        });
      }
    }
  }

  deleteDoc(idDoc: number): void {
    this.consigneService.deleteDoc(this.consigne.id, idDoc).subscribe((response: any) => {
      this.consigne = response;
      this.messageService.setSuccess('Document supprimé');
    });
  }

}
