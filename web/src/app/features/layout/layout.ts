import { Component, OnInit, OnDestroy, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule, MatSidenav } from '@angular/material/sidenav';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subject, takeUntil } from 'rxjs';
import { Sidenav } from '@features/layout/components/sidenav/sidenav';
import {Toolbar} from '@features/layout/components/toolbar/toolbar';

type UserRole = 'ADMIN' | 'MERCHANT' | 'AGENT';

@Component({
  selector: 'app-dashboard-layout',
  imports: [
    CommonModule,
    RouterOutlet,
    MatSidenavModule,
    Sidenav,
    Toolbar
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout implements OnInit, OnDestroy {
  @ViewChild('sidenav') sidenav!: MatSidenav;

  isMobile = signal(false);

  private destroy$ = new Subject<void>();

  constructor(private breakpointObserver: BreakpointObserver) {}

  ngOnInit() {
    this.breakpointObserver
      .observe([Breakpoints.Handset, Breakpoints.Tablet])
      .pipe(takeUntil(this.destroy$))
      .subscribe(result => {
        this.isMobile.set(result.matches);

        if (this.sidenav) {
          if (this.isMobile()) {
            this.sidenav.close();
          } else {
            this.sidenav.open();
          }
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleSidenav() {
    if (this.isMobile()) {
      this.sidenav.toggle();
    }
  }

  get sidenavMode() {
    return this.isMobile() ? 'over' : 'side';
  }

  get isOpen() {
    return !this.isMobile();
  }
}
