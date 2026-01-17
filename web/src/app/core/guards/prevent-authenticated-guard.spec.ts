import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { preventAuthenticatedGuard } from './prevent-authenticated-guard';

describe('preventAuthenticatedGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => preventAuthenticatedGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
