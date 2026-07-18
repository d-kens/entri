import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from 'environments/environment';

@Injectable({ providedIn: 'root' })
export class FileService {
  private http = inject(HttpClient);

  upload(file: File): Observable<string> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http
      .post<{ url: string }>(`${environment.apiBaseUrl}/media/upload`, fd)
      .pipe(map(({ url }) => url));
  }
}
