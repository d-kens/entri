import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './welcome.html',
  styleUrl: './welcome.css',
})
export class Welcome implements OnInit, OnDestroy {
  currentDate = signal('');
  currentTime = signal('');
  greeting = signal('');

  private timeInterval?: number;

  ngOnInit() {
    this.updateDateTime();
    this.updateGreeting();

    // Update time every minute
    this.timeInterval = window.setInterval(() => {
      this.updateDateTime();
      this.updateGreeting();
    }, 60000);
  }

  ngOnDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval);
    }
  }

  private updateDateTime() {
    const now = new Date();

    // Format date: "Sat, 7 Feb"
    const weekday = now.toLocaleDateString('en-US', { weekday: 'short' });
    const day = now.getDate();
    const month = now.toLocaleDateString('en-US', { month: 'short' });
    this.currentDate.set(`${weekday}, ${day} ${month}`);

    // Format time: "14:30" (24-hour format, no AM/PM)
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    this.currentTime.set(`${hours}:${minutes}`);
  }

  private updateGreeting() {
    const hour = new Date().getHours();

    if (hour >= 5 && hour < 12) {
      this.greeting.set('Good Morning');
    } else if (hour >= 12 && hour < 17) {
      this.greeting.set('Good Afternoon');
    } else {
      this.greeting.set('Good Evening');
    }
  }
}
