import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {environment} from '../../../../environments/environment';
import {
  InitiatePaymentRequest,
  InitiatePaymentResponse,
  PaymentEvent,
  PaymentStatus
} from '@features/payments/models/payment.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private http: HttpClient = inject(HttpClient)

  initiatePayment(payload: InitiatePaymentRequest): Observable<InitiatePaymentResponse> {
    return this.http.post<InitiatePaymentResponse>(
      `${environment.apiBaseUrl}/payments/initiate`,
      payload
    )
  }

  subscribeToPaymentEvents(paymentId: string): Observable<PaymentEvent> {
    return new Observable(observer => {
      const eventSource = new EventSource(
        `${environment.apiBaseUrl}/payments/${paymentId}/events`
      );

      eventSource.addEventListener('payment-status', (event: MessageEvent) => {
        const data: PaymentEvent = JSON.parse(event.data);
        observer.next(data);

        // Complete if payment is done (success or failure)
        if (data.status === PaymentStatus.PAID || data.status === PaymentStatus.FAILED) {
          eventSource.close();
          observer.complete();
        }
      });

      eventSource.onerror = (error) => {
        console.error('SSE Error:', error);
        eventSource.close();
        observer.error(error);
      };

      // Cleanup on unsubscribe
      return () => {
        eventSource.close();
      };
    });
  }
}
