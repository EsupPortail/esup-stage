import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
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
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of([]) }) } }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StageComponent);
    component = fixture.componentInstance;
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
});
