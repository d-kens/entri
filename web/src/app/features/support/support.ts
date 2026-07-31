import { Component, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-support',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './support.html',
  styleUrl: './support.css',
})
export class Support {
  activeIndex = signal<number | null>(null);

  toggle(index: number): void {
    this.activeIndex.set(this.activeIndex() === index ? null : index);
  }
}
