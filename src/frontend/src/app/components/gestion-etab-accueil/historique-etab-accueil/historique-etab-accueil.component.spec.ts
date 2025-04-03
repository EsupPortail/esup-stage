import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoriqueEtabAccueilComponent } from './historique-etab-accueil.component';

describe('HistoriqueEtabAccueilComponent', () => {
  let component: HistoriqueEtabAccueilComponent;
  let fixture: ComponentFixture<HistoriqueEtabAccueilComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoriqueEtabAccueilComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoriqueEtabAccueilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
