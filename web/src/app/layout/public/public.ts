import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavBar } from './nav-bar/nav-bar';

@Component({
  selector: 'app-public',
  standalone: true,
  imports: [RouterOutlet, NavBar],
  templateUrl: './public.html',
  styleUrl: './public.css',
})
export class Public {}
