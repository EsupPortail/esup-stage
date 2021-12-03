import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateConventionComponent } from './template-convention.component';

describe('TemplateConventionComponent', () => {
  let component: TemplateConventionComponent;
  let fixture: ComponentFixture<TemplateConventionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateConventionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateConventionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
