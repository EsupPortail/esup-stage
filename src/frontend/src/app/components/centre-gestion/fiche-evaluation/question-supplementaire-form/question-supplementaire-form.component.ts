import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, Validators } from "@angular/forms";

@Component({
  selector: 'app-question-supplementaire-form',
  templateUrl: './question-supplementaire-form.component.html',
  styleUrls: ['./question-supplementaire-form.component.scss']
})
export class QuestionSupplementaireFormComponent implements OnInit {

  question: any;
  isModif: any;

  typeQuestions: any = [
    {code: "txt", libelle: "Champ de texte libre"},
    {code: "not", libelle: "Notation"},
    {code: "yn", libelle: "Oui/Non"},
  ]

  form: any;

  constructor(private dialogRef: MatDialogRef<QuestionSupplementaireFormComponent>,
              private fb: FormBuilder,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.question = data.question
    this.isModif = data.isModif

    this.form = this.fb.group({
      question: [null, [Validators.required, Validators.maxLength(200)]],
      typeQuestion: [null, [Validators.required, Validators.maxLength(5)]],
    });

    if (this.isModif) {
      this.form.setValue({
        question: this.question.question,
        typeQuestion: this.question.typeQuestion,
      });
    }
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(null);
  }

  save(): void {
    if (this.form.valid) {
      const data = {...this.form.value};
      this.dialogRef.close(data);
    }
  }

}
