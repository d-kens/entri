import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'app-page-error',
  imports: [RouterLink, MatIcon, MatButton],
  templateUrl: './page-error.html',
  styleUrl: './page-error.css',
})
export class PageError {
  message = input('Something went wrong. Please try again.');
  backLink = input<string[]>([]);
  retry = output<void>();
}
