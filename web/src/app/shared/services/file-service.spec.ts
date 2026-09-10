import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from 'environments/environment';
import { FileService } from './file-service';

describe('FileService', () => {
  let service: FileService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FileService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should upload the file as multipart form data and return the url', () => {
    const file = new File(['content'], 'photo.png', { type: 'image/png' });
    let result: string | undefined;

    service.upload(file).subscribe((url) => (result = url));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/media/upload`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    expect((req.request.body as FormData).get('file')).toBe(file);
    req.flush({ url: 'https://cdn.example.com/photo.png' });

    expect(result).toBe('https://cdn.example.com/photo.png');
  });
});
