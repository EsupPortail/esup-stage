import { Component, ElementRef, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { AuthService } from "../../services/auth.service";
import { ConfigService } from "../../services/config.service";
import { MatDialog } from '@angular/material/dialog';
import { MenuAccessibilityComponent } from '../menu-accessibility/menu-accessibility.component';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: false
})
export class HeaderComponent implements OnInit {

  @ViewChild('logo') logo!: ElementRef;

  @Output() disableAutoSearchChange = new EventEmitter<boolean>();

  constructor(
    private authService: AuthService,
    private configService: ConfigService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.configService.themeModified.subscribe((config: any) => {
      if (this.logo !== null && config.logo) {
        this.logo.nativeElement.src = `data:${config.logo.contentType};base64,${config.logo.base64}`;
      }
    });

    this.loadInitialAccessibilitySettings();
  }

  isConnected() {
    return this.authService.userConnected;
  }

  logout(): void {
    this.authService.logout();
  }

  openAccessibilityDialog(): void {
    const saved = localStorage.getItem('accessibilityPreferences');
    let currentSettings = {
      fontSize: 100,
      highContrast: false,
      reducedMotion: false,
      disableAutoSearch: false,
      textSpacing: 'normal' as 'normal' | 'comfortable' | 'spacious',
      selectedFont: 'default'
    };

    if (saved) {
      const preferences = JSON.parse(saved);
      currentSettings = {
        fontSize: preferences.fontSize ?? 100,
        highContrast: preferences.highContrast ?? false,
        reducedMotion: preferences.reducedMotion ?? false,
        disableAutoSearch: preferences.disableAutoSearch ?? false,
        textSpacing: preferences.textSpacing ?? 'normal',
        selectedFont: preferences.selectedFont ?? 'default'
      };
    }

    const dialogRef = this.dialog.open(MenuAccessibilityComponent, {
      width: '500px',
      maxWidth: '90vw',
      panelClass: 'accessibility-dialog-container',
      data: currentSettings,
      autoFocus: true,
      restoreFocus: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.disableAutoSearchChange.emit(result.disableAutoSearch);
      }
    });
  }

  private loadInitialAccessibilitySettings(): void {
    const saved = localStorage.getItem('accessibilityPreferences');
    if (saved) {
      const preferences = JSON.parse(saved);

      if (preferences.fontSize) {
        document.documentElement.style.fontSize = `${preferences.fontSize}%`;
      }

      if (preferences.highContrast) {
        document.body.classList.add('high-contrast');
      }

      if (preferences.reducedMotion) {
        document.body.classList.add('reduced-motion');
        document.body.classList.remove('animations-enabled');
      } else {
        document.body.classList.add('animations-enabled');
      }

      if (preferences.textSpacing) {
        document.body.classList.add(`text-spacing-${preferences.textSpacing}`);
      }

      if (preferences.selectedFont) {
        const fonts = [
          { value: 'default', family: '' },
          { value: 'arial', family: 'Arial, sans-serif' },
          { value: 'verdana', family: 'Verdana, sans-serif' },
          { value: 'tahoma', family: 'Tahoma, sans-serif' },
          { value: 'trebuchet', family: '"Trebuchet MS", sans-serif' },
          { value: 'georgia', family: 'Georgia, serif' },
          { value: 'times', family: '"Times New Roman", serif' },
          { value: 'courier', family: '"Courier New", monospace' },
          { value: 'comic', family: '"Comic Sans MS", cursive' },
          { value: 'opendyslexic', family: 'OpenDyslexic, sans-serif' }
        ];

        const selectedFontConfig = fonts.find(f => f.value === preferences.selectedFont);
        if (selectedFontConfig && selectedFontConfig.family) {
          document.documentElement.style.setProperty('--custom-font-family', selectedFontConfig.family);
          document.body.classList.add('custom-font-active');
        }
      }

      if (preferences.disableAutoSearch !== undefined) {
        this.disableAutoSearchChange.emit(preferences.disableAutoSearch);
      }
    }
  }
}
