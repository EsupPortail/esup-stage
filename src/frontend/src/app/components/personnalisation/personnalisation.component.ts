import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { Color } from "@angular-material-components/color-picker";
import { PersonnalisationService } from "../../services/personnalisation.service";

@Component({
  selector: 'app-personnalisation',
  templateUrl: './personnalisation.component.html',
  styleUrls: ['./personnalisation.component.scss']
})
export class PersonnalisationComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder, private personnalisationService: PersonnalisationService) {
    this.form = fb.group({
      primaryColor: [new Color(46, 69, 136), [Validators.required]],
    });
  }

  ngOnInit(): void {
  }

  submit(): void {
    const config = this.form.value;
    config.primaryColor = '#' + config.primaryColor.hex;
    this.personnalisationService.updateConfig(config);
  }

}
