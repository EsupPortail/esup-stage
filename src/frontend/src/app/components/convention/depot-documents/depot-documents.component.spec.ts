import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepotDocumentsComponent } from './depot-documents.component';

describe('DepotDocumentsComponent', () => {
  let component: DepotDocumentsComponent;
  let fixture: ComponentFixture<DepotDocumentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepotDocumentsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DepotDocumentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
