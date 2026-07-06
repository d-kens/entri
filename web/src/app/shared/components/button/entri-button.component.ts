import { Component, input } from '@angular/core';

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
    '[class.btn-sm]': `size() === 'sm'`,
    '[class.btn-md]': `size() === 'md'`,
    '[class.btn-lg]': `size() === 'lg'`,
    '[class.btn-loading]': 'loading()',
    '[attr.disabled]': 'loading() || null',
    '[attr.aria-busy]': 'loading()',
  },
})
export class EntriButton {
  readonly variant = input<'primary' | 'secondary' | 'outline' | 'ghost'>('primary');
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly loading = input(false);
}
