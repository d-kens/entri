import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './forbidden.html',
})
export class Forbidden {

  goBack() {
    history.back();
  }
}
