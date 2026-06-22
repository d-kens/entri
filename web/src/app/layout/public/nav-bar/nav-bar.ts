import { Component, HostListener, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

const MENU_CLOSE_ANIMATION_MS = 300;
const MD_BREAKPOINT = 768;

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatButtonModule, MatIconModule],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBar {
  menuOpen = signal(false);
  menuClosing = signal(false);

  toggleMenu() {
    this.menuOpen() ? this.closeMenu() : this.menuOpen.set(true);
  }

  closeMenu() {
    if (!this.menuOpen()) return;
    this.menuClosing.set(true);
    setTimeout(() => {
      this.menuOpen.set(false);
      this.menuClosing.set(false);
    }, MENU_CLOSE_ANIMATION_MS);
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: UIEvent) {
    if ((event.target as Window).innerWidth >= MD_BREAKPOINT) {
      this.menuOpen.set(false);
      this.menuClosing.set(false);
    }
  }
}
