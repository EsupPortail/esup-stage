import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateMailGroupeComponent } from './template-mail-groupe.component';

describe('TemplateMailGroupeComponent', () => {
  let component: TemplateMailGroupeComponent;
  let fixture: ComponentFixture<TemplateMailGroupeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateMailGroupeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateMailGroupeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
