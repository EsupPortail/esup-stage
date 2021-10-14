import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminNomenclaturesCreationComponent } from './admin-nomenclatures-creation.component';

describe('AdminNomenclaturesCreationComponent', () => {
  let component: AdminNomenclaturesCreationComponent;
  let fixture: ComponentFixture<AdminNomenclaturesCreationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminNomenclaturesCreationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminNomenclaturesCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
