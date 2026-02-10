import { inject, Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import {HttpClient} from '@angular/common/http';
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

@Injectable({
  providedIn: 'root',
})
export class ZonesService {
  private http: HttpClient = inject(HttpClient);

  getAllZones(): Observable<Zone[]> {
    return this.http.get<Zone[]>(
      `${environment.apiBaseUrl}/zones`
    );
  }

  getParcelPointsByZone(
    zoneId: number
  ): Observable<ParcelPoint[]> {
    return this.http.get<ParcelPoint[]>(
      `${environment.apiBaseUrl}/zones/${zoneId}/parcel-points`
    );
  }

  getAllParcelPoints(): Observable<ParcelPoint[]> {
    return this.http.get<ParcelPoint[]>(
      `${environment.apiBaseUrl}/parcel-points`
    );
  }
}
