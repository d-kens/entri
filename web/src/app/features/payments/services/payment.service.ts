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
import {AuthService} from '@core/services/auth-service';
import {EventSourcePolyfill} from 'event-source-polyfill';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private http: HttpClient = inject(HttpClient)
  private authService: AuthService = inject(AuthService)

  initiatePayment(payload: InitiatePaymentRequest): Observable<InitiatePaymentResponse> {
    return this.http.post<InitiatePaymentResponse>(
      `${environment.apiBaseUrl}/payments/initiate`,
      payload
    )
  }

  subscribeToPaymentEvents(paymentId: string): Observable<PaymentEvent> {
    return new Observable(observer => {
      const token = this.authService.getToken();
      const eventSource = new EventSourcePolyfill(
        `${environment.apiBaseUrl}/payments/${paymentId}/events`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      eventSource.addEventListener('payment-status', ((event: MessageEvent) => {
        const data: PaymentEvent = JSON.parse(event.data);
        observer.next(data);

        // Complete if payment is done (success or failure)
        if (data.status === PaymentStatus.SUCCESS || data.status === PaymentStatus.FAILED) {
          eventSource.close();
          observer.complete();
        }
      }) as any);

      eventSource.onerror = () => {
        eventSource.close();
        observer.error(new Error('SSE connection error'));
      };

      // Cleanup on unsubscribe
      return () => {
        eventSource.close();
      };
    });
  }
}
