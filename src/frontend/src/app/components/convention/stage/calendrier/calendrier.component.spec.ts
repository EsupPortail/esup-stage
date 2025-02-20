import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
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

  it('should save periods with correct hours', () => {
    component.periodesForm.setValue({ dateDebut: new Date('2023-01-01'), dateFin: new Date('2023-01-07') });
    component.addPeriode();
    component.heuresJournalieresForm.get('heuresJournalieres0')!.setValue('7.5');
    component.save();
    expect(component.periodes[0].nbHeuresJournalieres).toBe(7.5);
  });
});
