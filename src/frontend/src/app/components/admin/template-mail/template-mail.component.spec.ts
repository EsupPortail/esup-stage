import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateMailComponent } from './template-mail.component';

describe('TemplateMailComponent', () => {
  let component: TemplateMailComponent;
  let fixture: ComponentFixture<TemplateMailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateMailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateMailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
