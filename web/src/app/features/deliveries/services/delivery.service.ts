import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {
  CreateDeliveryDto,
  DeliveryFilters,
  DeliveryResponse,
  PageResponse, TrackDeliveryResponse
} from '@features/deliveries/models/delivery.model';

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

  getDeliveries(filters: DeliveryFilters = {}): Observable<PageResponse<DeliveryResponse>> {
    let params = new HttpParams();

    if (filters.page !== undefined) {
      params = params.set('page', filters.page.toString());
    }
    if (filters.size !== undefined) {
      params = params.set('size', filters.size.toString());
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.search) {
      params = params.set('search', filters.search);
    }

    return this.http.get<PageResponse<DeliveryResponse>>(
      `${environment.apiBaseUrl}/deliveries`,
      { params }
    );
  }

  trackDelivery(trackingNumber: string): Observable<TrackDeliveryResponse> {
    return this.http.get<TrackDeliveryResponse>(
      `${environment.apiBaseUrl}/deliveries/track/${trackingNumber}`
    );
  }
}
