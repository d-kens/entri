import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';

import { Sidenav } from './sidenav';
import { UsersService } from '@features/users/users-service';
import { UserResponse } from '@features/users/models/user.models';

describe('Sidenav', () => {
  let component: Sidenav;
  let fixture: ComponentFixture<Sidenav>;
  let currentUser: ReturnType<typeof signal<UserResponse | null>>;

  beforeEach(async () => {
    currentUser = signal<UserResponse | null>(null);

    await TestBed.configureTestingModule({
      imports: [Sidenav],
      providers: [provideRouter([]), { provide: UsersService, useValue: { currentUser } }],
    }).compileComponents();

    fixture = TestBed.createComponent(Sidenav);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show no menu items when there is no authenticated user', () => {
    expect(component.menuItem()).toEqual([]);
  });

  it('should show the admin menu for an ADMIN user', () => {
    currentUser.set({ role: 'ADMIN' } as UserResponse);
    fixture.detectChanges();

    const labels = component.menuItem().map((item) => item.label);
    expect(labels).toEqual(['Users', 'Events', 'Categories', 'Payments', 'Profile']);
  });

  it('should show the organizer menu for an ORGANIZER user', () => {
    currentUser.set({ role: 'ORGANIZER' } as UserResponse);
    fixture.detectChanges();

    const labels = component.menuItem().map((item) => item.label);
    expect(labels).toEqual(['Dashboard', 'Events', 'Wallet', 'Profile']);
  });

  it('should show no menu items for an unrecognized role', () => {
    currentUser.set({ role: 'AGENT' } as unknown as UserResponse);
    fixture.detectChanges();

    expect(component.menuItem()).toEqual([]);
  });

  it('should emit closeSidenav when onCloseSidenav is called', () => {
    const emitSpy = vi.fn();
    component.closeSidenav.subscribe(emitSpy);

    component.onCloseSidenav();

    expect(emitSpy).toHaveBeenCalledOnce();
  });
});
