import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';


export type MenuItem = {
  icon: string,
  label: string,
  route?: string
}

@Component({
  selector: 'app-sidenav',
  imports: [
    CommonModule,
    MatListModule,
    MatIconModule
  ],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
})
export class Sidenav {
  menuItem = signal<MenuItem[]>([
    {
      icon: 'dashboard',
      label: 'Dashboard',
      route: 'dashboard'
    },
    {
      icon: 'dashboard',
      label: 'Dashboard',
      route: 'dashboard'
    },
    {
      icon: 'dashboard',
      label: 'Dashboard',
      route: 'dashboard'
    },
    {
      icon: 'dashboard',
      label: 'Dashboard',
      route: 'dashboard'
    },
    {
      icon: 'dashboard',
      label: 'Dashboard',
      route: 'dashboard'
    }
  ])

}
