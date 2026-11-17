import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';

import { TableComponent } from './table.component';
import { AuthService } from '../../services/auth.service';
import { TechnicalService } from '../../services/technical.service';
import { AccessibilityService } from '../../services/accessibility.service';
import { MatDialog } from '@angular/material/dialog';

describe('TableComponent', () => {
  let component: TableComponent;
  let fixture: ComponentFixture<TableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TableComponent],
      providers: [
        { provide: AuthService, useValue: { isEtudiant: () => false } },
        { provide: TechnicalService, useValue: { isMobile: of(false) } },
        { provide: AccessibilityService, useValue: { disableAutoSearch$: of(false) } },
        { provide: MatDialog, useValue: {} },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TableComponent);
    component = fixture.componentInstance;
    component.service = {
      getPaginated: () => of({ total: 0, data: [] }),
      exportData: () => of(new Blob()),
      getMobileTitle: () => '',
    } as any;
    component.filters = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('compareFilterOptionValue', () => {
    it('compare des scalaires par égalité stricte', () => {
      expect(component.compareFilterOptionValue(1, 1)).toBeTrue();
      expect(component.compareFilterOptionValue('FR', 'FR')).toBeTrue();
      expect(component.compareFilterOptionValue(1, 2)).toBeFalse();
    });

    it('compare les EtapeId / UfrId par code (+ univ / version)', () => {
      const a = { code: 'M1INFO', codeUniversite: 'UNIV', codeVersionEtape: '210' };
      const b = { code: 'M1INFO', codeUniversite: 'UNIV', codeVersionEtape: '210' };
      const c = { code: 'M1INFO', codeUniversite: 'UNIV', codeVersionEtape: '211' };
      expect(component.compareFilterOptionValue(a, b)).toBeTrue();
      expect(component.compareFilterOptionValue(a, c)).toBeFalse();
    });
  });

  describe('applyListFilter searchAlsoCode', () => {
    beforeEach(() => {
      component.filters = [{
        id: 'etape.id',
        type: 'list',
        keyLibelle: 'libelle',
        keyId: 'id',
        searchable: true,
        searchAlsoCode: true,
        requireSearch: true,
        options: [
          { id: { code: 'ABC1', codeUniversite: 'U', codeVersionEtape: '1' }, libelle: 'Master Informatique' },
          { id: { code: 'XYZ9', codeUniversite: 'U', codeVersionEtape: '2' }, libelle: 'Licence Droit' },
        ],
      }];
      (component as any).filteredListOptions = {};
    });

    it('filtre sur le libellé', () => {
      (component as any).applyListFilter('etape.id', 'droit');
      expect((component as any).filteredListOptions['etape.id'].length).toBe(1);
      expect((component as any).filteredListOptions['etape.id'][0].libelle).toBe('Licence Droit');
    });

    it('filtre aussi sur le code étape', () => {
      (component as any).applyListFilter('etape.id', 'abc1');
      expect((component as any).filteredListOptions['etape.id'].length).toBe(1);
      expect((component as any).filteredListOptions['etape.id'][0].id.code).toBe('ABC1');
    });

    it('n’affiche aucune option tant que requireSearch et requête vide', () => {
      (component as any).applyListFilter('etape.id', '');
      expect((component as any).filteredListOptions['etape.id'].length).toBe(0);
    });
  });
});
