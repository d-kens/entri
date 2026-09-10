import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { PageError } from './page-error';

describe('PageError', () => {
  let component: PageError;
  let fixture: ComponentFixture<PageError>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageError],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(PageError);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the default message and hide the back link by default', () => {
    expect(fixture.nativeElement.textContent).toContain('Something went wrong. Please try again.');
    expect(fixture.nativeElement.querySelector('a')).toBeNull();
  });

  it('should render a custom message', () => {
    fixture.componentRef.setInput('message', 'Event not found.');
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Event not found.');
  });

  it('should render the back link when backLink is provided', () => {
    fixture.componentRef.setInput('backLink', ['/dashboard', 'events']);
    fixture.detectChanges();

    const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a');
    expect(link).toBeTruthy();
    expect(link.textContent?.trim()).toBe('Go back');
  });

  it('should emit retry when the try again button is clicked', () => {
    const retrySpy = vi.fn();
    component.retry.subscribe(retrySpy);

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    button.click();

    expect(retrySpy).toHaveBeenCalledOnce();
  });
});
