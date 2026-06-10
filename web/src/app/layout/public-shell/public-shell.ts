import { Component, signal } from '@angular/core';

const MENU_CLOSE_ANIMATION_MS = 250;
import { RouterLink, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './public-shell.html',
  styleUrl: './public-shell.css',
})
export class PublicLayout {
  menuOpen    = signal(false);
  menuClosing = signal(false);

  toggleMenu() {
    this.menuOpen() ? this.closeMenu() : this.menuOpen.set(true);
  }

  closeMenu() {
    this.menuClosing.set(true);
    setTimeout(() => {
      this.menuOpen.set(false);
      this.menuClosing.set(false);
    }, MENU_CLOSE_ANIMATION_MS);
  }
}
