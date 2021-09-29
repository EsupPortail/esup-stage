import { Component, OnInit } from '@angular/core';
import { ConfigService } from "../../../services/config.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-config-generale',
  templateUrl: './config-generale.component.html',
  styleUrls: ['./config-generale.component.scss']
})
export class ConfigGeneraleComponent implements OnInit {

  configGenerale: any;
  configAlerte: any;
  configTheme: any;

  formGenerale: FormGroup;

  alertes = [
    {code: 'creationConventionEtudiant', libelle: 'Création d\'une convention par l\'étudiant'},
    {code: 'modificationConventionEtudiant', libelle: 'Modification d\'une convention par l\'étudiant'},
    {code: 'creationConventionGestionnaire', libelle: 'Création d\'une convention par le gestionnaire'},
    {code: 'modificationConventionGestionnaire', libelle: 'Modification d\'une convention par le gestionnaire'},
    {code: 'creationAvenantEtudiant', libelle: 'Création d\'un avenant par l\'étudiant'},
    {code: 'modificationAvenantEtudiant', libelle: 'Modification d\'un avenant par l\'étudiant'},
    {code: 'creationAvenantGestionnaire', libelle: 'Création d\'un avenant par le gestionnaire'},
    {code: 'modificationAvenantGestionnaire', libelle: 'Modification d\'un avenant par le gestionnaire'},
    {code: 'validationPedagogiqueConvention', libelle: 'Validation pédagogique d\'une convention'},
    {code: 'validationAdministrativeConvention', libelle: 'Validation administrative d\'une convention'},
    {code: 'validationAvenant', libelle: 'Validation d\'un avenant'},
  ];
  alerteColumns = ['alertes', 'alerteEtudiant', 'alerteGestionnaire', 'alerteRespGestionnaire', 'alerteEnseignant'];

  formTheme: FormGroup;

  typeCentres = ['VIDE', 'COMPOSANTE', 'ETAPE', 'MIXTE'];

  constructor(private configService: ConfigService, private fb: FormBuilder, private authService: AuthService, private messageService: MessageService) {
    this.formGenerale = this.fb.group({
      anneeBasculeJour: [null, [Validators.required, Validators.min(1), Validators.max(31)]],
      anneeBasculeMois: [null, [Validators.required, Validators.min(1), Validators.max(12)]],
      autoriserConventionsOrphelines: [null, [Validators.required]],
      typeCentre: [null, [Validators.required]],
      autoriserCentresBloquerImpressionConvention: [null, [Validators.required]],
      autoriserEtudiantAModifierEntreprise: [null, [Validators.required]],
      autoriserValidationAutoOrgaAccCreaEtu: [null, [Validators.required]],
      ldapFiltreEnseignant: [null, [Validators.required]],
    })
  }

  ngOnInit(): void {
    if (!this.canEdit()) {
      this.formGenerale.disable();
    }
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.configGenerale = response;
      if (this.configGenerale.typeCentre === null) this.configGenerale.typeCentre = 'VIDE';
      this.formGenerale.setValue(this.configGenerale);
    });
    this.configService.getConfigAlerteMail().subscribe((response: any) => {
      this.configAlerte = response;
    });
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION]});
  }

  saveGenerale(): void {
    if (this.formGenerale.valid) {
      this.configService.updateGenerale(this.formGenerale.value).subscribe((response: any) => {
        this.configGenerale = response;
        this.messageService.setSuccess('Paramètre d\'élements généraux modifés');
      });
    }
  }

  saveAlerteMail(): void {
    this.configService.updateAlerteMail(this.configAlerte).subscribe((response: any) => {
      this.configAlerte = response;
      this.messageService.setSuccess('Paramètre d\'alertes mail modifés');
    });
  }

}
