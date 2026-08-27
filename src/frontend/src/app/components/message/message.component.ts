import { Component, OnInit } from '@angular/core';
import { MessageService } from "../../services/message.service";

@Component({
    selector: 'app-message',
    templateUrl: './message.component.html',
    styleUrls: ['./message.component.scss'],
    standalone: false
})
export class MessageComponent implements OnInit {

  constructor(private messageService: MessageService) { }

  ngOnInit(): void {
  }

  getType(): string {
    return this.messageService.getType();
  }

  getTitle(): string {
    return this.messageService.getTitle();
  }

  getMessage(): string {
    return this.messageService.getMessage();
  }

}
