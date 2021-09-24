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
  formAlerte: FormGroup;
  formTheme: FormGroup;

  typeCentres = ['VIDE', 'COMPOSANTE', 'ETAPE', 'MIXTE'];

  constructor(private configService: ConfigService, private fb: FormBuilder, private authService: AuthService, private messageService: MessageService) {
    this.formGenerale = this.fb.group({
      anneeBasculeJour: [null, [Validators.required, Validators.min(1), Validators.max(31)]],
      anneeBasculeMois: [null, [Validators.required, Validators.min(1), Validators.max(12)]],
      autoriserConventionsOrphelines: [null, []],
      typeCentre: [null, [Validators.required]],
      autoriserCentresBloquerImpressionConvention: [null, []],
      autoriserEtudiantAModifierEntreprise: [null, []],
      autoriserValidationAutoOrgaAccCreaEtu: [null, []],
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

}
