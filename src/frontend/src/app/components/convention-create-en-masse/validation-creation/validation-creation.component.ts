import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';

@Component({
  selector: 'app-validation-creation',
  templateUrl: './validation-creation.component.html',
  styleUrls: ['./validation-creation.component.scss']
})
export class ValidationCreationComponent implements OnInit {


  @Input() groupeEtudiant: any;
  @Output() validated = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {
  }

}
