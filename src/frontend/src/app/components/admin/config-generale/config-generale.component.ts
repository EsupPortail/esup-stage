import {Component, OnInit, ViewContainerRef, WritableSignal} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ConfigService } from "../../../services/config.service";
import { AuthService } from "../../../services/auth.service";
import { MessageService } from "../../../services/message.service";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import {RoleService} from "../../../services/role.service";

@Component({
  selector: 'app-config-generale',
  templateUrl: './config-generale.component.html',
  styleUrls: ['./config-generale.component.scss']
})
export class ConfigGeneraleComponent implements OnInit {

  configGenerale: any;
  configAlerte: any;
  configTheme: any;
  configSignature: any;

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
    {code: 'conventionSignee', libelle: 'Convention signée par toutes les parties'},
    {code: 'changementEnseignant', libelle:' Changement d\'enseignant référent'},
  ];
  alerteColumns = ['alertes', 'alerteEtudiant', 'alerteGestionnaire', 'alerteRespGestionnaire', 'alerteEnseignant'];

  formTheme: FormGroup;

  typeCentres = ['VIDE', 'COMPOSANTE', 'ETAPE', 'MIXTE'];

  logoFile: File|undefined;
  faviconFile: File|undefined;

  centreGestion: any;
  primaryColor: string | WritableSignal<string> | undefined;
  secondaryColor: string | WritableSignal<string> | undefined;
  dangerColor: string | WritableSignal<string> | undefined;
  warningColor: string | WritableSignal<string> | undefined;
  successColor: string | WritableSignal<string> | undefined;

  formSignature: FormGroup;

  isEtuAutoriseToCreate!: boolean;

  constructor(
    private configService: ConfigService,
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private roleService: RoleService,
  ) {
    this.formGenerale = this.fb.group({
      codeUniversite: [null, [Validators.required]],
      anneeBasculeJour: [null, [Validators.required, Validators.min(1), Validators.max(31)]],
      anneeBasculeMois: [null, [Validators.required, Validators.min(1), Validators.max(12)]],
      autoriserConventionsOrphelines: [null, [Validators.required]],
      typeCentre: [null, [Validators.required]],
      autoriserValidationAutoOrgaAccCreaEtu: [null, [Validators.required]],
      utiliserMailPersoEtudiant: [null, [Validators.required]],
      autoriserElementPedagogiqueFacultatif: [null, [Validators.required]],
      validationPedagogiqueLibelle: [null, [Validators.required]],
      validationAdministrativeLibelle: [null, [Validators.required]],
      codeCesure: [null],
      autoriserEtudiantACreerEntrepriseFrance: [null],
      autoriserEtudiantACreerEntrepriseHorsFrance: [null],
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

    this.formSignature = this.fb.group({
      supprimerConventionUneFoisSigneEsupSignature: [null, [Validators.required]],
      verrouillerOrdreSignataireCG:[null,[Validators.required]]
    });
  }

  ngOnInit(): void {
    this.roleService.getDroitsRole("ETU","ORGA_ACC").subscribe((res: any)=>{
      this.isEtuAutoriseToCreate = res.creation;
    })

    if (!this.canEdit()) {
      this.formGenerale.disable();
      this.formTheme.disable();
    }

    // Récupération de la configuration générale
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.configGenerale = response;
      if (this.configGenerale.typeCentre === null) this.configGenerale.typeCentre = 'VIDE';
      this.formGenerale.patchValue(this.configGenerale);
    });

    // Récupération de la configuration des alertes
    this.configService.getConfigAlerteMail().subscribe((response: any) => {
      this.configAlerte = response;
    });

    // Récupération de la configuration du thème
    this.configService.getConfigTheme().then((response: any) => {
      this.configTheme = response;
      this.setFormThemeValue();
    });

    // Récupération de la configuration de la signature
    this.configService.getConfigSignature().subscribe((response: any) => {
      this.configSignature = response;
      this.formSignature.patchValue(this.configSignature);
    });
  }

  setFormThemeValue(): void {
    this.formTheme.patchValue({
      fontFamily: this.configTheme.fontFamily,
      fontSize: this.configTheme.fontSize,
      primaryColor: this.configTheme.primaryColor,
      secondaryColor: this.configTheme.secondaryColor,
      dangerColor: this.configTheme.dangerColor,
      warningColor: this.configTheme.warningColor,
      successColor: this.configTheme.successColor
    });
  }

  canEdit(): boolean {
    return this.authService.checkRights({ fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION] });
  }

  canDelete(): boolean {
    return this.authService.checkRights({ fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.SUPPRESSION] });
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
    const config = { ...this.formTheme.value };


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

  saveSignature(): void {
    this.configSignature = this.formSignature.value;
    this.configService.updateConfigSignature(this.configSignature).subscribe((response: any) => {
      this.configSignature = response;
      this.messageService.setSuccess('Configuration de la signature modifiée');
      this.formSignature.patchValue(this.configSignature);
    });
  }

}
