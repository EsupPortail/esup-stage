import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { AccessibilityService } from '../../services/accessibility.service';

export interface AccessibilitySettings {
  fontSize: number;
  highContrast: boolean;
  reducedMotion: boolean;
  disableAutoSearch: boolean;
  textSpacing: 'normal' | 'comfortable' | 'spacious';
  selectedFont: string;
}

@Component({
  selector: 'app-menu-accessibility',
  templateUrl: './menu-accessibility.component.html',
  styleUrls: ['./menu-accessibility.component.scss'],
  standalone: false
})
export class MenuAccessibilityComponent implements OnInit {

  fontSize: number = 100;
  highContrast: boolean = false;
  reducedMotion: boolean = false;
  disableAutoSearch: boolean = false;
  textSpacing: 'normal' | 'comfortable' | 'spacious' = 'normal';
  selectedFont: string = 'default';

  availableFonts = [
    { value: 'default', label: 'Police par défaut', family: '' },
    { value: 'arial', label: 'Arial', family: 'Arial, sans-serif' },
    { value: 'verdana', label: 'Verdana', family: 'Verdana, sans-serif' },
    { value: 'tahoma', label: 'Tahoma', family: 'Tahoma, sans-serif' },
    { value: 'trebuchet', label: 'Trebuchet MS', family: '"Trebuchet MS", sans-serif' },
    { value: 'georgia', label: 'Georgia', family: 'Georgia, serif' },
    { value: 'times', label: 'Times New Roman', family: '"Times New Roman", serif' },
    { value: 'courier', label: 'Courier New', family: '"Courier New", monospace' },
    { value: 'comic', label: 'Comic Sans MS', family: '"Comic Sans MS", cursive' },
    { value: 'opendyslexic', label: 'OpenDyslexic', family: 'OpenDyslexic, sans-serif' }
  ];

  constructor(
    public dialogRef: MatDialogRef<MenuAccessibilityComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AccessibilitySettings,
    private accessibilityService: AccessibilityService
  ) {
    if (data) {
      this.fontSize = data.fontSize;
      this.highContrast = data.highContrast;
      this.reducedMotion = data.reducedMotion;
      this.disableAutoSearch = data.disableAutoSearch;
      this.textSpacing = data.textSpacing;
      this.selectedFont = data.selectedFont;
    }
  }

  ngOnInit(): void {
    this.loadAccessibilityPreferences();
  }

  increaseFontSize(): void {
    if (this.fontSize < 150) {
      this.fontSize += 10;
      this.applyFontSize();
      this.saveAccessibilityPreferences();
    }
  }

  decreaseFontSize(): void {
    if (this.fontSize > 80) {
      this.fontSize -= 10;
      this.applyFontSize();
      this.saveAccessibilityPreferences();
    }
  }

  private applyFontSize(): void {
    document.documentElement.style.fontSize = `${this.fontSize}%`;
  }

  toggleHighContrast(): void {
    this.highContrast = !this.highContrast;
    document.body.classList.toggle('high-contrast', this.highContrast);
    this.saveAccessibilityPreferences();
  }

  toggleReducedMotion(): void {
    this.reducedMotion = !this.reducedMotion;

    if (this.reducedMotion) {
      document.body.classList.add('reduced-motion');
      document.body.classList.remove('animations-enabled');
    } else {
      document.body.classList.remove('reduced-motion');
      document.body.classList.add('animations-enabled');
    }

    this.saveAccessibilityPreferences();
  }

  toggleDisableAutoSearch(): void {
    this.disableAutoSearch = !this.disableAutoSearch;
    this.saveAccessibilityPreferences();
  }

  setTextSpacing(spacing: 'normal' | 'comfortable' | 'spacious'): void {
    this.textSpacing = spacing;
    this.applyTextSpacing();
    this.saveAccessibilityPreferences();
  }

  private applyTextSpacing(): void {
    document.body.classList.remove('text-spacing-normal', 'text-spacing-comfortable', 'text-spacing-spacious');
    document.body.classList.add(`text-spacing-${this.textSpacing}`);
  }

  onFontChange(fontValue: string): void {
    this.selectedFont = fontValue;
    this.applyFont();
    this.saveAccessibilityPreferences();
  }

  private applyFont(): void {
    const selectedFontConfig = this.availableFonts.find(f => f.value === this.selectedFont);

    if (selectedFontConfig && selectedFontConfig.family) {
      document.documentElement.style.setProperty('--custom-font-family', selectedFontConfig.family);
      document.body.classList.add('custom-font-active');
    } else {
      document.documentElement.style.removeProperty('--custom-font-family');
      document.body.classList.remove('custom-font-active');
    }
  }

  resetAccessibilitySettings(): void {
    this.fontSize = 100;
    this.highContrast = false;
    this.reducedMotion = false;
    this.disableAutoSearch = false;
    this.textSpacing = 'normal';
    this.selectedFont = 'default';

    this.applyAccessibilitySettings();
    this.saveAccessibilityPreferences();
  }

  private applyAccessibilitySettings(): void {
    this.applyFontSize();
    this.applyTextSpacing();
    this.applyFont();
    document.body.classList.toggle('high-contrast', this.highContrast);

    if (this.reducedMotion) {
      document.body.classList.add('reduced-motion');
      document.body.classList.remove('animations-enabled');
    } else {
      document.body.classList.remove('reduced-motion');
      document.body.classList.add('animations-enabled');
    }
  }

  private saveAccessibilityPreferences(): void {
    const preferences: AccessibilitySettings = {
      fontSize: this.fontSize,
      highContrast: this.highContrast,
      reducedMotion: this.reducedMotion,
      disableAutoSearch: this.disableAutoSearch,
      textSpacing: this.textSpacing,
      selectedFont: this.selectedFont
    };
    localStorage.setItem('accessibilityPreferences', JSON.stringify(preferences));
    this.accessibilityService.setDisableAutoSearch(this.disableAutoSearch);
  }

  private loadAccessibilityPreferences(): void {
    const saved = localStorage.getItem('accessibilityPreferences');
    if (saved) {
      const preferences = JSON.parse(saved);
      this.fontSize = preferences.fontSize ?? 100;
      this.highContrast = preferences.highContrast ?? false;
      this.reducedMotion = preferences.reducedMotion ?? false;
      this.disableAutoSearch = preferences.disableAutoSearch ?? false;
      this.textSpacing = preferences.textSpacing ?? 'normal';
      this.selectedFont = preferences.selectedFont ?? 'default';
      this.applyAccessibilitySettings();
      this.accessibilityService.setDisableAutoSearch(this.disableAutoSearch);
    }
  }

  close(): void {
    this.dialogRef.close({
      fontSize: this.fontSize,
      highContrast: this.highContrast,
      reducedMotion: this.reducedMotion,
      disableAutoSearch: this.disableAutoSearch,
      textSpacing: this.textSpacing,
      selectedFont: this.selectedFont
    });
  }
}
