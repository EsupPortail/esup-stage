import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder, FormControl } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CalendrierComponent } from './calendrier.component';

describe('CalendrierComponent', () => {
  let component: CalendrierComponent;
  let fixture: ComponentFixture<CalendrierComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CalendrierComponent ],
      imports: [ ReactiveFormsModule ],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: { close: () => {} } },
        { provide: MAT_DIALOG_DATA, useValue: { convention: {}, interruptionsStage: [] } }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CalendrierComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add a period', () => {
    component.periodesForm.setValue({ dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07') });
    component.addPeriode();
    expect(component.periodes.length).toBe(1);
    expect(component.heuresJournalieresForm.contains('heuresJournalieres0')).toBeTrue();
  });

  it('should remove a period', () => {
    component.periodesForm.setValue({ dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07') });
    component.addPeriode();
    component.removePeriode(component.periodes[0]);
    expect(component.periodes.length).toBe(0);
    expect(component.heuresJournalieresForm.contains('heuresJournalieres0')).toBeFalse();
  });

  it('retire une période déjà enregistrée sans appel réseau', () => {
    const periode = {
      id: 12,
      dateDebut: new Date(2023, 0, 2),
      dateFin: new Date(2023, 0, 6),
      formControlName: 'heuresJournalieres0'
    };
    component.periodes = [periode];
    component.heuresJournalieresForm.addControl('heuresJournalieres0', new FormControl(7));

    component.removePeriode(periode);

    expect(component.periodes.length).toBe(0);
    expect(component.heuresJournalieresForm.contains('heuresJournalieres0')).toBeFalse();
  });

  it('should save periods with correct hours', () => {
    component.periodesForm.setValue({ dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07') });
    component.addPeriode();
    component.heuresJournalieresForm.get('heuresJournalieres0')!.setValue('7.5');
    component.save();
    expect(component.periodes[0].nbHeuresJournalieres).toBe(7.5);
  });
});

describe('CalendrierComponent isolation des périodes source', () => {
  const sourcePeriodes: any[] = [
    { id: 1, dateDebut: new Date(2023, 0, 2), dateFin: new Date(2023, 0, 6), nbHeuresJournalieres: 7 },
    { id: 2, dateDebut: new Date(2023, 0, 9), dateFin: new Date(2023, 0, 13), nbHeuresJournalieres: 7 }
  ];

  let component: CalendrierComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CalendrierComponent ],
      imports: [ ReactiveFormsModule ],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: { close: () => {} } },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            convention: { dateDebutStage: '2023-01-01', dateFinStage: '2023-12-31' },
            interruptionsStage: [],
            periodes: sourcePeriodes
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CalendrierComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('ne mute pas la liste source quand on retire une période (régression #708 / review #744)', () => {
    expect(component.periodes.length).toBe(2);
    expect(component.periodes).not.toBe(sourcePeriodes);
    expect(component.periodes[0]).not.toBe(sourcePeriodes[0]);

    component.removePeriode(component.periodes[0]);

    expect(component.periodes.length).toBe(1);
    expect(component.periodes[0].id).toBe(2);
    // La liste Stage doit rester intacte pour que savePeriodesStage calcule les delete
    expect(sourcePeriodes.length).toBe(2);
    expect(sourcePeriodes.map((p) => p.id)).toEqual([1, 2]);
    expect(sourcePeriodes[0].formControlName).toBeUndefined();
  });
});
