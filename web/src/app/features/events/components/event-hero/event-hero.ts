import { Component, input } from '@angular/core';
import { EventDetailResponse } from '@features/auth/models/event.models';

@Component({
  selector: 'app-event-hero',
  standalone: true,
  imports: [],
  templateUrl: './event.hero.html',
  styleUrl: './event-hero.css',
})
export class EventHero {
  event = input.required<EventDetailResponse>();
  backLink = input<string[]>();
}
