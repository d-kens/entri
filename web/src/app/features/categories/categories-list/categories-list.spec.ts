import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { environment } from 'environments/environment';

import { CategoriesList } from './categories-list';
import { SnackbarService } from '@shared/services/snackbar-service';
import { CategoryResponse } from '@features/events/models/event.models';

describe('CategoriesList', () => {
  let component: CategoriesList;
  let fixture: ComponentFixture<CategoriesList>;
  let httpMock: HttpTestingController;
  let snackbar: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let dialog: { open: ReturnType<typeof vi.fn> };

  const category: CategoryResponse = { id: 1, name: 'Music', description: 'Music events' };

  beforeEach(async () => {
    snackbar = { showSuccess: vi.fn(), showError: vi.fn() };
    dialog = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [CategoriesList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: SnackbarService, useValue: snackbar },
        { provide: MatDialog, useValue: dialog },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoriesList);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushInitialLoad() {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush([category]);
  }

  it('should create', () => {
    flushInitialLoad();
    expect(component).toBeTruthy();
  });

  it('should load categories on init', () => {
    flushInitialLoad();
    expect(component.categories()).toEqual([category]);
    expect(component.loading()).toBe(false);
  });

  it('should set error state when loading fails', () => {
    fixture.detectChanges();
    httpMock
      .expectOne(`${environment.apiBaseUrl}/categories`)
      .flush('boom', { status: 500, statusText: 'Server Error' });

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should create a category on dialog confirm and show a success message', () => {
    flushInitialLoad();
    const newCategory = { name: 'Sports', description: 'Sports events' };
    dialog.open.mockReturnValue({ afterClosed: () => of(newCategory) });

    component.openCreateDialog();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newCategory);
    req.flush({ id: 2, ...newCategory });

    httpMock
      .expectOne(`${environment.apiBaseUrl}/categories`)
      .flush([category, { id: 2, ...newCategory }]);

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Category created');
    expect(component.loading()).toBe(false);
    expect(component.categories()).toEqual([category, { id: 2, ...newCategory }]);
  });

  it('should not create a category when the create dialog is cancelled', () => {
    flushInitialLoad();
    dialog.open.mockReturnValue({ afterClosed: () => of(undefined) });

    component.openCreateDialog();

    httpMock.expectNone((r) => r.method === 'POST');
  });

  it('should update a category on dialog confirm and show a success message', () => {
    flushInitialLoad();
    const updated = { name: 'Music Events', description: 'Updated' };
    dialog.open.mockReturnValue({ afterClosed: () => of(updated) });

    component.openEditDialog(category);

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updated);
    req.flush({ ...category, ...updated });

    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush([{ ...category, ...updated }]);

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Category updated');
    expect(component.loading()).toBe(false);
    expect(component.categories()).toEqual([{ ...category, ...updated }]);
  });

  it('should delete a category on dialog confirm and show a success message', () => {
    flushInitialLoad();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });

    component.confirmDelete(category);

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush([]);

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Category deleted');
    expect(component.loading()).toBe(false);
    expect(component.categories()).toEqual([]);
  });

  it('should not delete a category when the delete dialog is cancelled', () => {
    flushInitialLoad();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });

    component.confirmDelete(category);

    httpMock.expectNone((r) => r.method === 'DELETE');
  });

  it('should show an error message when deleting a category fails', () => {
    flushInitialLoad();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });

    component.confirmDelete(category);

    httpMock
      .expectOne(`${environment.apiBaseUrl}/categories/1`)
      .flush({ detail: 'Category is in use' }, { status: 409, statusText: 'Conflict' });

    expect(snackbar.showError).toHaveBeenCalledWith('Category is in use');
  });

  it('should re-fetch categories on retry', () => {
    flushInitialLoad();
    component.categories.set([]);

    component.retry();
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush([category]);

    expect(component.categories()).toEqual([category]);
    expect(component.loading()).toBe(false);
  });
});
