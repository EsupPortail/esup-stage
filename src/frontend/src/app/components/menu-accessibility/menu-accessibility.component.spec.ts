import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuAccessibilityComponent } from './menu-accessibility.component';

describe('MenuAccessibilityComponent', () => {
  let component: MenuAccessibilityComponent;
  let fixture: ComponentFixture<MenuAccessibilityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuAccessibilityComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuAccessibilityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
