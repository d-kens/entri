import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BreakpointObserver } from '@angular/cdk/layout';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Subject } from 'rxjs';

import { Layout } from './dashboard';

describe('Layout', () => {
  let component: Layout;
  let fixture: ComponentFixture<Layout>;
  let breakpointResult$: Subject<{ matches: boolean }>;

  beforeEach(async () => {
    breakpointResult$ = new Subject();

    await TestBed.configureTestingModule({
      imports: [Layout],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: BreakpointObserver,
          useValue: { observe: () => breakpointResult$.asObservable() },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Layout);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default to desktop layout before any breakpoint match', () => {
    expect(component.isMobile()).toBe(false);
    expect(component.sidenavMode).toBe('side');
    expect(component.isOpen).toBe(true);
  });

  it('should switch to mobile layout and close the sidenav when a handset/tablet breakpoint matches', () => {
    const closeSpy = vi.spyOn(component.sidenav, 'close');

    breakpointResult$.next({ matches: true });

    expect(component.isMobile()).toBe(true);
    expect(component.sidenavMode).toBe('over');
    expect(component.isOpen).toBe(false);
    expect(closeSpy).toHaveBeenCalled();
  });

  it('should reopen the sidenav when returning to desktop width', () => {
    breakpointResult$.next({ matches: true });
    const openSpy = vi.spyOn(component.sidenav, 'open');

    breakpointResult$.next({ matches: false });

    expect(component.isMobile()).toBe(false);
    expect(openSpy).toHaveBeenCalled();
  });

  it('should toggle the sidenav only while in mobile mode', () => {
    const toggleSpy = vi.spyOn(component.sidenav, 'toggle');

    component.toggleSidenav();
    expect(toggleSpy).not.toHaveBeenCalled();

    breakpointResult$.next({ matches: true });
    component.toggleSidenav();
    expect(toggleSpy).toHaveBeenCalled();
  });

  it('should stop reacting to breakpoint changes after ngOnDestroy', () => {
    component.ngOnDestroy();
    breakpointResult$.next({ matches: true });

    expect(component.isMobile()).toBe(false);
  });
});
