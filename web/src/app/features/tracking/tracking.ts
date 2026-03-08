import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tracking',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './tracking.html',
  styleUrl: './tracking.css'
})
export class Tracking {

}

/*
* Implement the frontend page tracking delivery by tracking number. The user enters the tracking number, clicks Track, and we show the package's information and delivery details. For now, create the UI only. Mock backend functionality. This page should not be protected. Simple and minimal UI. No events for this
* */
