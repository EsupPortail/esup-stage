import { Component, ElementRef, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { AuthService } from "../../services/auth.service";
import { ConfigService } from "../../services/config.service";
import { MatMenuTrigger } from '@angular/material/menu';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: false
})
export class HeaderComponent implements OnInit {

  @ViewChild('logo') logo!: ElementRef;
  @ViewChild(MatMenuTrigger) accessibilityTrigger!: MatMenuTrigger;
  @ViewChild('accessibilityMenuRoot') accessibilityMenuRoot!: ElementRef<HTMLElement>;

  fontSize: number = 100;
  highContrast: boolean = false;
  reducedMotion: boolean = false;
  disableAutoSearch: boolean = false;

  // Nouvelle propriété pour l'espacement du texte
  textSpacing: 'normal' | 'comfortable' | 'spacious' = 'normal';

  // Nouvelle propriété pour la police
  selectedFont: string = 'default';

  // Liste des polices disponibles
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

  @Output() disableAutoSearchChange = new EventEmitter<boolean>();

  constructor(private authService: AuthService, private configService: ConfigService) { }

  ngOnInit(): void {
    this.configService.themeModified.subscribe((config: any) => {
      if (this.logo !== null && config.logo) {
        this.logo.nativeElement.src = `data:${config.logo.contentType};base64,${config.logo.base64}`;
      }
    });

    this.loadAccessibilityPreferences();
    this.applyAccessibilitySettings();
    this.disableAutoSearchChange.emit(this.disableAutoSearch);
  }

  isConnected() {
    return this.authService.userConnected;
  }

  logout(): void {
    this.authService.logout();
  }

  onAccessibilityMenuOpened(): void {
    queueMicrotask(() => this.accessibilityMenuRoot?.nativeElement?.focus());
  }

  onAccessibilityMenuClosed(): void {
    queueMicrotask(() => this.accessibilityTrigger?.focus());
  }

  closeAccessibilityMenu(): void {
    this.accessibilityTrigger?.closeMenu();
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
      // Désactive complètement les animations
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
    this.disableAutoSearchChange.emit(this.disableAutoSearch);
  }

  // Nouvelle méthode pour changer l'espacement du texte
  setTextSpacing(spacing: 'normal' | 'comfortable' | 'spacious'): void {
    this.textSpacing = spacing;
    this.applyTextSpacing();
    this.saveAccessibilityPreferences();
  }

  private applyTextSpacing(): void {
    // Retire toutes les classes d'espacement
    document.body.classList.remove('text-spacing-normal', 'text-spacing-comfortable', 'text-spacing-spacious');

    // Ajoute la classe correspondante
    document.body.classList.add(`text-spacing-${this.textSpacing}`);
  }

  // Nouvelle méthode pour changer la police
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
    this.disableAutoSearchChange.emit(this.disableAutoSearch);
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
    const preferences = {
      fontSize: this.fontSize,
      highContrast: this.highContrast,
      reducedMotion: this.reducedMotion,
      disableAutoSearch: this.disableAutoSearch,
      textSpacing: this.textSpacing,
      selectedFont: this.selectedFont
    };
    localStorage.setItem('accessibilityPreferences', JSON.stringify(preferences));
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
    }
  }
}
