import { Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-etab-accueil',
  templateUrl: './etab-accueil.component.html',
  styleUrls: ['./etab-accueil.component.scss']
})
export class EtabAccueilComponent implements OnInit {

  @Output() validated = new EventEmitter<number>();

  constructor() { }

  ngOnInit(): void {
  }

  validate(): void {
    this.validated.emit(1); // TODO get status from form completion
  }

}
