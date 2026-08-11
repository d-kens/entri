import { Component, input } from '@angular/core';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { EventResponse } from '@features/events/models/event.models';

@Component({
  selector: 'app-event-hero',
  standalone: true,
  imports: [DatePipe, TitleCasePipe, RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './event.hero.html',
  styleUrl: './event-hero.css',
})
export class EventHero {
  event = input.required<EventResponse>();
  backLink = input<string[]>(['/dashboard/events']);
}
