import { Component } from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-evaluation-tuteur',
  templateUrl: './evaluation-tuteur.component.html',
  styleUrl: './evaluation-tuteur.component.scss'
})
export class EvaluationTuteurComponent {

  constructor(private route: ActivatedRoute) {}
  ngOnInit() {
    const token = this.route.parent?.snapshot.paramMap.get('token');
    if(token == "1"){
      console.log('OK');
    }else{
      console.log('KO');
    }
  }

}
