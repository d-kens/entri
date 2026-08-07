import { Component, ElementRef, inject, input } from '@angular/core';

@Component({
  selector: 'button[appButton], a[appButton]',
  standalone: true,
  templateUrl: './entri-button.component.html',
  styleUrl: './entri-button.component.css',
  host: {
    '[class.btn-primary]': `variant() === 'primary'`,
    '[class.btn-secondary]': `variant() === 'secondary'`,
    '[class.btn-outline]': `variant() === 'outline'`,
    '[class.btn-ghost]': `variant() === 'ghost'`,
    '[class.btn-danger]': `variant() === 'danger'`,
    '[class.btn-sm]': `size() === 'sm'`,
    '[class.btn-md]': `size() === 'md'`,
    '[class.btn-lg]': `size() === 'lg'`,
    '[class.btn-loading]': 'loading()',
    '[attr.disabled]': 'isButton && (loading() || null)',
    '[attr.aria-busy]': 'loading() ? "true" : "false"',
    '(click)': 'onHostClick($event)',
  },
})
export class EntriButton {
  variant = input<'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'>('primary');
  size = input<'sm' | 'md' | 'lg'>('md');
  loading = input(false);

  protected readonly isButton = inject(ElementRef).nativeElement.tagName.toLowerCase() === 'button';

  onHostClick(event: Event) {
    if (this.loading() && !this.isButton) {
      event.preventDefault();
    }
  }
}
