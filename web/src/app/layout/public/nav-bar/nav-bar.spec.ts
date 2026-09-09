import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { NavBar } from './nav-bar';

describe('NavBar', () => {
  let component: NavBar;
  let fixture: ComponentFixture<NavBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavBar],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(NavBar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the brand, primary nav link, and auth links', () => {
    const links: HTMLAnchorElement[] = Array.from(fixture.nativeElement.querySelectorAll('a'));
    const texts = links.map((link) => link.textContent?.trim());

    expect(texts).toContain('Events');
    expect(texts).toContain('Sign In');
    expect(texts).toContain('Sign Up');
  });

  it('should point the sign in and sign up links at the auth routes', () => {
    const links: HTMLAnchorElement[] = Array.from(fixture.nativeElement.querySelectorAll('a'));
    const signIn = links.find((link) => link.textContent?.trim() === 'Sign In');
    const signUp = links.find((link) => link.textContent?.trim() === 'Sign Up');

    expect(signIn?.getAttribute('href')).toBe('/auth/login');
    expect(signUp?.getAttribute('href')).toBe('/auth/register');
  });
});
