import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminNomenclaturesEditionComponent } from './admin-nomenclatures-edition.component';

describe('AdminNomenclaturesEditionComponent', () => {
  let component: AdminNomenclaturesEditionComponent;
  let fixture: ComponentFixture<AdminNomenclaturesEditionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminNomenclaturesEditionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminNomenclaturesEditionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
