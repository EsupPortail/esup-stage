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

  // bouton trigger (pour rendre le focus à la fermeture)
  @ViewChild(MatMenuTrigger) accessibilityTrigger!: MatMenuTrigger;

  // wrapper du menu (focusable)
  @ViewChild('accessibilityMenuRoot') accessibilityMenuRoot!: ElementRef<HTMLElement>;

  fontSize: number = 100;
  highContrast: boolean = false;
  reducedMotion: boolean = false;

  // NOUVEAU
  disableAutoSearch: boolean = false;

  // NOUVEAU : pour informer le reste de l'app
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
  }

  isConnected() {
    return this.authService.userConnected;
  }

  logout(): void {
    this.authService.logout();
  }

  onAccessibilityMenuOpened(): void {
    // Si jamais le trap ne capture pas immédiatement (selon versions),
    // on force le focus sur le wrapper.
    queueMicrotask(() => this.accessibilityMenuRoot?.nativeElement?.focus());
  }

  onAccessibilityMenuClosed(): void {
    // rend le focus au bouton déclencheur (super important clavier)
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
    document.body.classList.toggle('reduced-motion', this.reducedMotion);
    this.saveAccessibilityPreferences();
  }

  // NOUVEAU
  toggleDisableAutoSearch(): void {
    this.disableAutoSearch = !this.disableAutoSearch;
    this.saveAccessibilityPreferences();
    this.disableAutoSearchChange.emit(this.disableAutoSearch);
  }

  resetAccessibilitySettings(): void {
    this.fontSize = 100;
    this.highContrast = false;
    this.reducedMotion = false;
    this.disableAutoSearch = false;

    this.applyAccessibilitySettings();
    this.saveAccessibilityPreferences();
    this.disableAutoSearchChange.emit(this.disableAutoSearch);
  }

  private applyAccessibilitySettings(): void {
    this.applyFontSize();
    document.body.classList.toggle('high-contrast', this.highContrast);
    document.body.classList.toggle('reduced-motion', this.reducedMotion);
  }

  private saveAccessibilityPreferences(): void {
    const preferences = {
      fontSize: this.fontSize,
      highContrast: this.highContrast,
      reducedMotion: this.reducedMotion,
      disableAutoSearch: this.disableAutoSearch
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
    }
  }
}
