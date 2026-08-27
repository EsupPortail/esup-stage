import { Component } from '@angular/core';
import { ContactService } from '../../../services/contact.service';
import { MessageService } from '../../../services/message.service';

@Component({
    selector: 'app-droit-opposition',
    templateUrl: './droit-opposition.component.html',
    styleUrl: './droit-opposition.component.scss',
    standalone: false
})
export class DroitOppositionComponent {

  saisie = '';
  enCours = false;
  resultat: any = null;

  constructor(private contactService: ContactService, private messageService: MessageService) {}

  /**
   * Les adresses collées depuis la boîte générique peuvent être séparées par un retour à la ligne,
   * un point-virgule, une virgule ou une tabulation.
   */
  extraireAdresses(): string[] {
    return this.saisie
      .split(/[\n\r;,\t]+/)
      .map(mail => mail.trim())
      .filter(mail => mail.length > 0);
  }

  enregistrer(): void {
    const mails = this.extraireAdresses();
    if (mails.length === 0) {
      this.messageService.setError('Veuillez saisir au moins une adresse mail');
      return;
    }

    this.enCours = true;
    this.contactService.enregistrerRefusEtreContacte(mails).subscribe({
      next: (response: any) => {
        this.resultat = response;
        this.enCours = false;
        const nbContacts = (response.traitees ?? []).reduce((total: number, ligne: any) => total + ligne.nbContacts, 0);
        this.messageService.setSuccess(`${nbContacts} contact(s) mis à jour`);
      },
      error: () => {
        this.enCours = false;
      }
    });
  }

  reinitialiser(): void {
    this.saisie = '';
    this.resultat = null;
  }
}
