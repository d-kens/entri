import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { WalletService } from './wallet-service';

describe('WalletService', () => {
  let service: WalletService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WalletService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(WalletService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET the wallet for an organizer', () => {
    const wallet = { externalId: 'wallet-1', balance: 100 };
    let response: unknown;
    service.getWallet('org-1').subscribe((res) => (response = res));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/wallet/org-1`);
    expect(req.request.method).toBe('GET');
    req.flush(wallet);

    expect(response).toEqual(wallet);
  });

  it('should GET paginated transactions sorted by dateCreated desc', () => {
    service.getTransactions('org-1', 1, 10).subscribe();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/wallet/org-1/transactions` &&
        r.params.get('page') === '1' &&
        r.params.get('size') === '10' &&
        r.params.get('sort') === 'dateCreated,desc',
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should POST a withdrawal request', () => {
    const request = {
      amount: 500,
      accountType: 'PAYBILL' as const,
      account: '123456',
      name: 'John Doe',
      narrative: 'Payout',
    };
    const response = { externalId: 'w-1', status: 'PENDING' as const };

    let result: unknown;
    service.withdraw('wallet-1', request).subscribe((res) => (result = res));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/wallet/wallet-1/withdraw`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(response);

    expect(result).toEqual(response);
  });
});
