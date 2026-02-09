import { inject, Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Zone {
  id: number;
  zoneName: string;
  city: string;
  isActive: boolean;
  isCbd: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ParcelPoint {
  id: number;
  name: string;
  zoneId: number;
  zoneName: string;
  agentId: number;
  agentName: string;
  latitude: number;
  longitude: number;
  addressDescription: string;
  isActive: boolean;
  phone: string;
  openingTime: string;
  closingTime: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaginationResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ZonesService {
  private http: HttpClient = inject(HttpClient);

  getAllZones(page: number = 0, size: number = 100): Observable<PaginationResponse<Zone>> {
    return this.http.get<PaginationResponse<Zone>>(
      `${environment.apiBaseUrl}/zones?page=${page}&size=${size}`
    );
  }

  getParcelPointsByZone(
    zoneId: number,
    page: number = 0,
    size: number = 100
  ): Observable<PaginationResponse<ParcelPoint>> {
    return this.http.get<PaginationResponse<ParcelPoint>>(
      `${environment.apiBaseUrl}/zones/${zoneId}/parcel-points?page=${page}&size=${size}`
    );
  }

  getAllParcelPoints(page: number = 0, size: number = 100): Observable<PaginationResponse<ParcelPoint>> {
    return this.http.get<PaginationResponse<ParcelPoint>>(
      `${environment.apiBaseUrl}/parcel-points?page=${page}&size=${size}`
    );
  }
}
