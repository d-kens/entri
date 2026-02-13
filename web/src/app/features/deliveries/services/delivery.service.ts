import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {CreateDeliveryDto, DeliveryResponse} from '@features/deliveries/models/delivery.model';


export interface PaymentEvent {
  deliveryId: string;
  status: 'PENDING' | 'PROCESSING' | 'PAID' | 'FAILED';
  message: string;
  paymentReference?: string;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class DeliveryService {
  private http: HttpClient = inject(HttpClient);

  createDelivery(payload: CreateDeliveryDto): Observable<DeliveryResponse> {
    return this.http.post<DeliveryResponse>(
      `${environment.apiBaseUrl}/deliveries`,
      payload
    )
  }

  getDeliveryById(externalId: string): Observable<DeliveryResponse> {
    return this.http.get<DeliveryResponse>(
      `${environment.apiBaseUrl}/deliveries/${externalId}`
    );
  }

  initiatePayment(payload: {
    deliveryId: string;
    paymentMethod: string;
    phoneNumber?: string;
    amount: number;
  }): Observable<any> {
    return this.http.post(
      `${environment.apiBaseUrl}/deliveries/${payload.deliveryId}/payment`,
      payload
    );
  }


  // TODO: Get back to this
  subscribeToPaymentEvents(deliveryId: string): Observable<PaymentEvent> {
    return new Observable(observer => {
      const eventSource = new EventSource(
        `${environment.apiBaseUrl}/payments/${deliveryId}/events`
      );

      eventSource.addEventListener('payment-status', (event: MessageEvent) => {
        const data: PaymentEvent = JSON.parse(event.data);
        observer.next(data);

        // Complete if payment is done (PAID or FAILED)
        if (data.status === 'PAID' || data.status === 'FAILED') {
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
