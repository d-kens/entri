import { Component, input } from '@angular/core';

@Component({
  selector: 'app-shimmer',
  imports: [],
  template: `<div
    class="shimmer"
    [style.width]="width()"
    [style.height]="height()"
    [style.border-radius]="borderRadius()"
  ></div>`,
  styleUrl: './shimmer.css',
})
export class Shimmer {
  width = input<string>('100%');
  height = input<string>('16px');
  borderRadius = input<string>('6px');
}
