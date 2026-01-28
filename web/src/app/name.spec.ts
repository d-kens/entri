import { TestBed } from '@angular/core/testing';

import { Name } from './name';

describe('Name', () => {
  let service: Name;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Name);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
