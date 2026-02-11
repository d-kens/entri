import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class DeliveryService {
  private http: HttpClient = inject(HttpClient);
}
