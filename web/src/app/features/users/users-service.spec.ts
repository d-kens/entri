import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { UsersService } from './users-service';
import { UserResponse } from './models/user.models';

describe('UsersService', () => {
  let service: UsersService;
  let httpMock: HttpTestingController;

  const user: UserResponse = {
    role: 'ORGANIZER',
    email: 'john@example.com',
    externalKey: 'ext-1',
    firstName: 'John',
    lastName: 'Doe',
    phoneNumber: '0712345678',
    enabled: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UsersService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsersService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should have no current user initially', () => {
    expect(service.currentUser()).toBeNull();
  });

  it('should GET a user by external key and update currentUser', () => {
    let response: UserResponse | undefined;
    service.getUserByExternalKey('ext-1').subscribe((res) => (response = res));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/users/ext-1`);
    expect(req.request.method).toBe('GET');
    req.flush(user);

    expect(response).toEqual(user);
    expect(service.currentUser()).toEqual(user);
  });

  it('should PUT the update request and update currentUser', () => {
    const request = {
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      phoneNumber: '0712345678',
    };
    const updated = { ...user, firstName: 'Jane', email: 'jane@example.com' };

    service.updateUser('ext-1', request).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/users/ext-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(updated);

    expect(service.currentUser()).toEqual(updated);
  });

  it('should POST the change-password request', () => {
    const request = { currentPassword: 'old', newPassword: 'new' };
    service.changePassword('ext-1', request).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/users/ext-1/change-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(null);
  });

  it('should GET a paginated list of users with default paging', () => {
    service.getUsers().subscribe();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/users` &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '20',
    );
    expect(req.request.method).toBe('GET');
    req.flush({
      content: [],
      number: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
      last: true,
      first: true,
    });
  });

  it('should GET a paginated list of users with the given paging', () => {
    service.getUsers(2, 5).subscribe();
    const req = httpMock.expectOne(
      (r) => r.params.get('page') === '2' && r.params.get('size') === '5',
    );
    req.flush({});
  });

  it('should DELETE a user', () => {
    service.deleteUser('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/users/ext-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should POST to enable a user', () => {
    service.enableUser('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/users/ext-1/enable`);
    expect(req.request.method).toBe('POST');
    req.flush(user);
  });

  it('should POST to disable a user', () => {
    service.disableUser('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/users/ext-1/disable`);
    expect(req.request.method).toBe('POST');
    req.flush(user);
  });
});
