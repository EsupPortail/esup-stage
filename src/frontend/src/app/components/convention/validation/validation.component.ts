import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-validation',
  templateUrl: './validation.component.html',
  styleUrls: ['./validation.component.scss']
})
export class ValidationComponent implements OnInit {

  @Input() convention: any;

  constructor() { }

  ngOnInit(): void {
  }

}
