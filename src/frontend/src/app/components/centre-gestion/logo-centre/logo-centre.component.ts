import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";
import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { debounceTime } from 'rxjs/operators';
import Quill from 'quill'
import BlotFormatter from 'quill-blot-formatter';
Quill.register('modules/blotFormatter', BlotFormatter);

@Component({
  selector: 'app-logo-centre',
  templateUrl: './logo-centre.component.html',
  styleUrls: ['./logo-centre.component.scss']
})
export class LogoCentreComponent implements OnInit {

  @Input() centreGestion: any;
  @Output() refreshCentreGestion = new EventEmitter<any>();

  logoFile: File|undefined;
  url: any;
  currentFile: any;

  modules = {};
  form: UntypedFormGroup;

  dimensions: any[] = [];
  height: any;
  width: any;

  constructor(private centreGestionService: CentreGestionService, private messageService: MessageService, private fb: UntypedFormBuilder,) {
    this.modules = {
      blotFormatter: {
      },
      toolbar: false
    }

    this.form = this.fb.group({
      content: [null]
    });
  }

  ngOnInit(): void {
    this.getLogo();
    this.resizeLogoOnChange();
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
        this.getLogo();
      });
    }
  }

  getLogo() {
    this.centreGestionService.getLogoCentre(this.centreGestion.id).subscribe((response: any) => {
      this.currentFile = response;
      if (this.currentFile.size > 0) {
        const reader = new FileReader();
        reader.readAsDataURL(this.currentFile);
        reader.onload = (_event) => {
          this.url = reader.result;
          this.form.get('content')?.setValue('<p style="text-align: center"><img src="' + this.url + '" /><p>');
        }
      }
    });
  }

  setImageSize(event: any) {
    if (event.content.ops[0].attributes) {
      this.height = Math.round(event.content.ops[0].attributes.height);
      this.width = Math.round(event.content.ops[0].attributes.width);
    }
  }

  resizeLogoOnChange() {
    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(val => {
      if (this.height != null && this.width != null) {
        this.dimensions.push(this.width)
        this.dimensions.push(this.height)
        this.centreGestionService.resizeLogoCentre(this.centreGestion.id, this.dimensions).subscribe((response: any) => {
          this.dimensions = [];
          this.height = null;
          this.width = null;
          this.getLogo();
        });
      }
    });
  }

}
