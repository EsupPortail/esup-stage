import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {EvaluationService} from "../../../services/evaluation.service";
import * as FileSaver from "file-saver";

type TypeFiche = 0 | 1 | 2 | 3; // 0 = étudiant, 1 = enseignant référent, 2 = tuteur pro, 3 = tous

@Component({
  selector: 'app-export-evaluation',
  templateUrl: './export-evaluation.component.html',
  styleUrl: './export-evaluation.component.scss'
})
export class ExportEvaluationComponent implements OnInit{

  form!: FormGroup;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { rows: any[] },
    public dialogRef: MatDialogRef<ExportEvaluationComponent>,
    private fb: FormBuilder,
    private evaluationService : EvaluationService,
  ) {
    this.form = this.fb.group({
      typeFiche: [null as TypeFiche | null, Validators.required],
    });
  }

  ngOnInit() {
    console.log(this.data.rows);
  }

  confirm(){
    console.log(this.form.value.typeFiche)
    this.evaluationService.getExportExcel(this.data.rows.map(r=>r.id),this.form.value.typeFiche).subscribe(response=>{
      const type = 'application/vnd.ms-excel';
      let blob = new Blob([response as BlobPart], {type: type});
      let filename = 'export_' + (new Date()).getTime() + '.xls';
      FileSaver.saveAs(blob, filename);
    })
    this.dialogRef.close();
  }

  cancel(){
    this.dialogRef.close();
  }

  reset(currentSheet: any) {

  }
}
