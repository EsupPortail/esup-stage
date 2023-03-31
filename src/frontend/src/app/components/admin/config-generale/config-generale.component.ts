import { Component, OnInit } from '@angular/core';
import { ConfigService } from "../../../services/config.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { MessageService } from "../../../services/message.service";
import { Color } from "@angular-material-components/color-picker";
import { CentreGestionService } from "../../../services/centre-gestion.service";

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
    {code: 'verificationAdministrativeConvention', libelle: 'Vérification administrative d\'une convention'},
    {code: 'validationAvenant', libelle: 'Validation d\'un avenant'},
    {code: 'codeCesure', libelle: 'Codes césure'},
  ];
  alerteColumns = ['alertes', 'alerteEtudiant', 'alerteGestionnaire', 'alerteRespGestionnaire', 'alerteEnseignant'];

  formTheme: FormGroup;

  typeCentres = ['VIDE', 'COMPOSANTE', 'ETAPE', 'MIXTE'];

  logoFile: File|undefined;
  faviconFile: File|undefined;

  centreGestion: any;

  constructor(
    private configService: ConfigService,
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private centreGestionService: CentreGestionService,
  ) {
    this.formGenerale = this.fb.group({
      codeUniversite: [null, [Validators.required]],
      anneeBasculeJour: [null, [Validators.required, Validators.min(1), Validators.max(31)]],
      anneeBasculeMois: [null, [Validators.required, Validators.min(1), Validators.max(12)]],
      autoriserConventionsOrphelines: [null, [Validators.required]],
      typeCentre: [null, [Validators.required]],
      autoriserEtudiantAModifierEntreprise: [null, [Validators.required]],
      autoriserValidationAutoOrgaAccCreaEtu: [null, [Validators.required]],
      utiliserMailPersoEtudiant: [null, [Validators.required]],
      autoriserElementPedagogiqueFacultatif: [null, [Validators.required]],
      validationPedagogiqueLibelle: [null, [Validators.required]],
      validationAdministrativeLibelle: [null, [Validators.required]],
      codeCesure: [null, ],
    });
    this.formTheme = this.fb.group({
      logo: [null, [Validators.required]],
      favicon: [null, [Validators.required]],
      fontFamily: [null, [Validators.required]],
      fontSize: [null, [Validators.required]],
      primaryColor: [null, [Validators.required]],
      secondaryColor: [null, [Validators.required]],
      dangerColor: [null, [Validators.required]],
      warningColor: [null, [Validators.required]],
      successColor: [null, [Validators.required]],
    });
  }

  ngOnInit(): void {
    if (!this.canEdit()) {
      this.formGenerale.disable();
      this.formTheme.disable();
    }
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.configGenerale = response;
      if (this.configGenerale.typeCentre === null) this.configGenerale.typeCentre = 'VIDE';
      this.formGenerale.patchValue(this.configGenerale);
    });
    this.configService.getConfigAlerteMail().subscribe((response: any) => {
      this.configAlerte = response;
    });
    this.configService.getConfigTheme().then((response: any) => {
      this.configTheme = response;
      this.setFormThemeValue();
    });
    this.centreGestionService.getCentreEtablissement().subscribe((response: any) => {
      this.centreGestion = response;
    });
  }

  setFormThemeValue(): void {
    this.formTheme.get('fontFamily')?.setValue(this.configTheme.fontFamily);
    this.formTheme.get('fontSize')?.setValue(this.configTheme.fontSize);
    this.setColor('primaryColor');
    this.setColor('secondaryColor');
    this.setColor('dangerColor');
    this.setColor('warningColor');
    this.setColor('successColor');
  }

  setColor(key: string): void {
    const color = this.hexToRgb(this.configTheme[key]);
    this.formTheme.get(key)?.setValue(new Color(color.r, color.g, color.b));
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION]});
  }

  canDelete(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.SUPPRESSION]});
  }

  saveGenerale(): void {
    if (this.formGenerale.valid) {
      this.configService.updateGenerale(this.formGenerale.value).subscribe((response: any) => {    
        this.configGenerale = response;
        this.messageService.setSuccess('Paramètre d\'éléments généraux modifiés');
      });
    }
  }

  saveAlerteMail(): void {
    this.configService.updateAlerteMail(this.configAlerte).subscribe((response: any) => {
      this.configAlerte = response;
      this.messageService.setSuccess('Paramètre d\'alertes mail modifiés');
    });
  }

  saveTheme(): void {
    const config = {...this.formTheme.value};
    config.primaryColor = '#' + config.primaryColor.hex;
    config.secondaryColor = '#' + config.secondaryColor.hex;
    config.dangerColor = '#' + config.dangerColor.hex;
    config.warningColor = '#' + config.warningColor.hex;
    config.successColor = '#' + config.successColor.hex;
    const formData = new FormData();
    if (this.logoFile !== undefined) {
      formData.append('logo', this.logoFile, this.logoFile.name);
    }
    if (this.faviconFile !== undefined) {
      formData.append('favicon', this.faviconFile, this.faviconFile.name);
    }
    formData.append('data', JSON.stringify(config));
    this.configService.updateTheme(formData).then((response: any) => {
      this.configTheme = response;
      this.messageService.setSuccess('Thème modifié');
      this.setFormThemeValue();
    });
  }

  rollbackTheme(): void {
    this.configService.rollbackTheme().then((response: any) => {
      this.configTheme = response;
      this.messageService.setSuccess('Thème réinitialisé');
      this.setFormThemeValue();
    });
  }

  hexToRgb(hex: string): any {
    hex = hex.replace('#', '');
    const hexR = hex.substring(0, 2);
    const hexG = hex.substring(2, 4);
    const hexB = hex.substring(4);
    return {
      r: parseInt(hexR, 16),
      g: parseInt(hexG, 16),
      b: parseInt(hexB, 16),
    }
  }

  onLogoChange(event: any): void {
    this.logoFile = event.target.files.item(0);
    if (this.logoFile?.type.indexOf('image/') === -1) {
      this.messageService.setError("Le fichier doit être au format image");
      this.logoFile = undefined;
      return;
    }
  }

  onFaviconChange(event: any): void {
    this.faviconFile = event.target.files.item(0);
    if (this.faviconFile?.type.indexOf('image/') === -1) {
      this.messageService.setError("Le fichier doit être au format image");
      this.faviconFile = undefined;
      return;
    }
  }

}
