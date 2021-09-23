import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRoleComponent } from './admin-role.component';

describe('AdminRoleComponent', () => {
  let component: AdminRoleComponent;
  let fixture: ComponentFixture<AdminRoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminRoleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminRoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
