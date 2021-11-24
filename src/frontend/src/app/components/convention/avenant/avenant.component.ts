import { Component, OnInit } from '@angular/core';
import { AvenantService } from "../../../services/avenant.service";

@Component({
  selector: 'app-avenant',
  templateUrl: './avenant.component.html',
  styleUrls: ['./avenant.component.scss']
})
export class AvenantComponent implements OnInit {

  constructor(private avenantService: AvenantService,
  ) { }


  ngOnInit(): void {
  }

}
