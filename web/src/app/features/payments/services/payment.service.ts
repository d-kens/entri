import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {environment} from '../../../../environments/environment';
import {InitiatePaymentRequest} from '@features/payments/models/payment.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private http: HttpClient = inject(HttpClient)

  initiatePayment(payload: InitiatePaymentRequest): Observable<any> {
    return this.http.post(
      `${environment.apiBaseUrl}/payments/initiate`,
      payload
    )
  }
}
