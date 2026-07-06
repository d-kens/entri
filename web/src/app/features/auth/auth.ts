import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-auth-page',
  imports: [RouterOutlet, RouterLink, MatIconModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css',
})
export class Auth {}
