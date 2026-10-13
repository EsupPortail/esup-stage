import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { StageComponent } from './stage.component';
import { of } from 'rxjs';
import { TempsTravailService } from "../../../services/temps-travail.service";
import { ConventionService } from "../../../services/convention.service";
import { AuthService } from "../../../services/auth.service";
import { ContenuService } from "../../../services/contenu.service";
import { PaysService } from "../../../services/pays.service";
import { ThemeService } from "../../../services/theme.service";
import { LangueConventionService } from "../../../services/langue-convention.service";
import { ModeVersGratificationService } from "../../../services/mode-vers-gratification.service";
import { UniteDureeService } from "../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../services/unite-gratification.service";
import { DeviseService } from "../../../services/devise.service";
import { OrigineStageService } from "../../../services/origine-stage.service";
import { NatureTravailService } from "../../../services/nature-travail.service";
import { ModeValidationStageService } from "../../../services/mode-validation-stage.service";
import { PeriodeInterruptionStageService } from "../../../services/periode-interruption-stage.service";
import { PeriodeStageService } from "../../../services/periode-stage.service";
import { TypeConventionService } from "../../../services/type-convention.service";
import { MatDialog } from '@angular/material/dialog';

describe('StageComponent', () => {
  let component: StageComponent;
  let fixture: ComponentFixture<StageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StageComponent ],
      imports: [ ReactiveFormsModule ],
      providers: [
        FormBuilder,
        { provide: TempsTravailService, useValue: { getPaginated: () => of([]) } },
        { provide: ConventionService, useValue: { controleChevauchement: () => of(false) } },
        { provide: AuthService, useValue: { isEtudiant: () => false, isGestionnaire: () => true, isAdmin: () => false } },
        { provide: ContenuService, useValue: { get: () => of({ texte: '' }) } },
        { provide: PaysService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: ThemeService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: LangueConventionService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: ModeVersGratificationService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: UniteDureeService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: UniteGratificationService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: DeviseService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: OrigineStageService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: NatureTravailService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: ModeValidationStageService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: PeriodeInterruptionStageService, useValue: { getByConvention: () => of([]), create: () => of({}), update: () => of({}), delete: () => of({}), deleteByConvention: () => of({}) } },
        { provide: PeriodeStageService, useValue: { getByConvention: () => of([]), create: () => of({}), update: () => of({}), delete: () => of({}), deleteByConvention: () => of({}) } },
        { provide: TypeConventionService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of([]) }) } }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StageComponent);
    component = fixture.componentInstance;
    component.convention = {};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate working hours correctly', () => {
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 8 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(40);
  });

  it('should handle empty periods', () => {
    const periodes: any[] = [];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(0);
  });

  it('should handle single day period', () => {
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-01'), nbHeuresJournalieres: 8 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(8);
  });

  it('should skip weekends in the period', () => {
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 8 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(40); // Only 5 weekdays
  });

  it('should skip holidays in the period', () => {
    component.joursFeries = [new Date('2023-01-02')];
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 8 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(32); // 4 weekdays + 1 holiday
  });

  it('should skip interruptions in the period', () => {
    component.interruptionsStage = [
      { dateDebutInterruption: '2023-01-03', dateFinInterruption: '2023-01-04' }
    ];
    component.form.get('interruptionStage')!.setValue(true);
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 8 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(24); // 5 weekdays - 2 interruption days
  });

  it('should handle decimal hours', () => {
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 7.5 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(37.5); // 5 weekdays * 7.5 hours
  });
  it('should calculate working hours correctly for irregular hours', () => {
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 3.5 },
      { dateDebut: new Date('2023-01-08'), dateFin: new Date('2023-01-14'), nbHeuresJournalieres: 7 },
      { dateDebut: new Date('2023-01-15'), dateFin: new Date('2023-01-21'), nbHeuresJournalieres: 3.5 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(70); // 17.5 + 35 + 17.5
  });

  it('should handle additional week with irregular hours', () => {
    const periodes = [
      { dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 3.5 },
      { dateDebut: new Date('2023-01-08'), dateFin: new Date('2023-01-14'), nbHeuresJournalieres: 7 },
      { dateDebut: new Date('2023-01-15'), dateFin: new Date('2023-01-21'), nbHeuresJournalieres: 3.5 },
      { dateDebut: new Date('2023-01-22'), dateFin: new Date('2023-01-28'), nbHeuresJournalieres: 7 }
    ];
    const result = component.calculHeuresTravails(periodes);
    expect(result).toBe(105); // 17.5 + 35 + 17.5 + 35
  });

  describe('validateForm - periodes de travail', () => {
    let fb: FormBuilder;
    let lastStatus: number | undefined;

    beforeEach(() => {
      fb = TestBed.inject(FormBuilder);
      lastStatus = undefined;
      component.validated.subscribe((status: number) => lastStatus = status);
      component.form = fb.group({
        horairesReguliers: [false],
        interruptionStage: [false],
      });
      component.periodesInterruptionsValid = true;
    });

    it('should emit status 1 when irregular hours without periods', () => {
      component.periodesCalculHeuresStage = [];
      component.checkPeriodesTravailValid();
      expect(lastStatus).toBe(1);
    });

    it('should emit status 2 when irregular hours with at least one period and valid form', () => {
      component.form.disable();
      component.periodesCalculHeuresStage = [
        { id: 1, dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 7 }
      ];
      component.checkPeriodesTravailValid();
      expect(lastStatus).toBe(2);
    });

    it('should emit status 2 when regular hours without periods', () => {
      component.form = fb.group({ horairesReguliers: [true], interruptionStage: [false] });
      component.form.disable();
      component.periodesCalculHeuresStage = [];
      component.checkPeriodesTravailValid();
      expect(lastStatus).toBe(2);
    });

    it('should invalidate tab after deleting last period', () => {
      component.form.disable();
      component.periodesCalculHeuresStage = [
        { id: 42, dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07'), nbHeuresJournalieres: 7 }
      ];
      component.checkPeriodesTravailValid();
      expect(lastStatus).toBe(2);

      component.deletePeriodeStage(0);
      expect(lastStatus).toBe(1);
    });
  });

  describe('calcul période irrégulière', () => {
    beforeEach(() => {
      component.form = TestBed.inject(FormBuilder).group({
        horairesReguliers: [false],
        interruptionStage: [false],
        nbHeuresHebdo: [null],
        dureeExceptionnelle: [null],
        periodeStageMois: [null, [Validators.min(0), Validators.max(30)]],
        periodeStageJours: [null, [Validators.min(0), Validators.max(31)]],
        periodeStageHeures: [null, [Validators.min(0), Validators.max(23)]],
      });
      component.joursFeries = [];
      component.interruptionsStage = [];
      component.dureeStageManuelle = false;
    });

    it('conserve un reste déjà inférieur ou égal à 23 heures', () => {
      component.periodesCalculHeuresStage = [
        { dateDebut: new Date(2023, 0, 2), dateFin: new Date(2023, 0, 13), nbHeuresJournalieres: 7.5 }
      ];
      (component as any).calculPeriodeIrreguliere(75);

      expect(component.dureeStage).toEqual({ dureeMois: 0, dureeJours: 10, dureeHeures: 5 });
    });

    it('laisse inchangé un reste de 23 heures exactement', () => {
      component.periodesCalculHeuresStage = [
        { dateDebut: new Date(2023, 0, 2), dateFin: new Date(2023, 2, 6), nbHeuresJournalieres: 7.5 }
      ];
      (component as any).calculPeriodeIrreguliere(345);

      expect(component.dureeStage.dureeHeures).toBe(23);
      expect(component.dureeStage.dureeMois).toBe(2);
      expect(component.dureeStage.dureeJours).toBe(2);
    });

    it('convertit en jours un reste supérieur à 23 heures', () => {
      component.periodesCalculHeuresStage = [
        { dateDebut: new Date(2023, 0, 2), dateFin: new Date(2023, 1, 10), nbHeuresJournalieres: 7.9 }
      ];
      (component as any).calculPeriodeIrreguliere(237);

      expect(component.dureeStage).toEqual({ dureeMois: 1, dureeJours: 9, dureeHeures: 3 });
    });

    it('ne réécrit pas une période valide lors d\'un recalcul en arrière-plan', () => {
      component.dureeStage = { dureeMois: 1, dureeJours: 2, dureeHeures: 3 };
      component.form.patchValue({ periodeStageMois: 1, periodeStageJours: 2, periodeStageHeures: 3 });

      component.refreshPeriodeStageFields(true);

      expect(component.form.get('periodeStageHeures')!.value).toBe(3);
    });

    it('recalcule un reste enregistré supérieur à 23 heures', () => {
      component.dureeStage = { dureeMois: 1, dureeJours: 8, dureeHeures: 42 };
      component.form.patchValue({
        dureeExceptionnelle: 237,
        periodeStageMois: 1,
        periodeStageJours: 8,
        periodeStageHeures: 42
      });
      component.periodesCalculHeuresStage = [
        { dateDebut: new Date(2023, 0, 2), dateFin: new Date(2023, 1, 10), nbHeuresJournalieres: 7.9 }
      ];

      component.refreshPeriodeStageFields(true);

      expect(component.dureeStage).toEqual({ dureeMois: 1, dureeJours: 9, dureeHeures: 3 });
      expect(component.form.get('periodeStageHeures')!.valid).toBeTrue();
    });
  });

  it('supprime en base les périodes retirées du calendrier seulement à l\'enregistrement', () => {
    const service = TestBed.inject(PeriodeStageService) as any;
    spyOn(service, 'delete').and.returnValue(of({}));
    spyOn(service, 'update').and.returnValue(of({}));
    spyOn(service, 'getByConvention').and.returnValue(of([]));
    component.convention = { id: 9 };
    component.periodesCalculHeuresStage = [
      { id: 1, dateDebut: new Date(2023, 0, 2), dateFin: new Date(2023, 0, 6), nbHeuresJournalieres: 7 },
      { id: 2, dateDebut: new Date(2023, 0, 9), dateFin: new Date(2023, 0, 13), nbHeuresJournalieres: 7 }
    ];

    component.savePeriodesStage([
      { id: 1, idConvention: 9, dateDebut: new Date(2023, 0, 2), dateFin: new Date(2023, 0, 6), nbHeuresJournalieres: 7 }
    ]);

    expect(service.delete).toHaveBeenCalledOnceWith(2);
    expect(service.update).toHaveBeenCalled();
  });
});
