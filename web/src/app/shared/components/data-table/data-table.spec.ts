import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataTable } from './data-table';

describe('DataTable', () => {
  let component: DataTable;
  let fixture: ComponentFixture<DataTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataTable],
    }).compileComponents();

    fixture = TestBed.createComponent(DataTable);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render skeleton rows while loading', () => {
    fixture.componentRef.setInput('loading', true);
    fixture.detectChanges();

    const shimmerRows = fixture.nativeElement.querySelectorAll('.shimmer-row');
    expect(shimmerRows.length).toBe(component.skeletonRows.length);
    expect(fixture.nativeElement.querySelector('.table-card')).toBeNull();
  });

  it('should render an error state and emit retry when the retry button is clicked', () => {
    fixture.componentRef.setInput('error', true);
    fixture.detectChanges();

    const retrySpy = vi.fn();
    component.retry.subscribe(retrySpy);

    const retryButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.empty-state button');
    retryButton.click();

    expect(retrySpy).toHaveBeenCalledOnce();
  });

  it('should render the empty state with the provided title and message', () => {
    fixture.componentRef.setInput('empty', true);
    fixture.componentRef.setInput('emptyTitle', 'No events yet');
    fixture.componentRef.setInput('emptyMessage', 'Create your first event to see it here.');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No events yet');
    expect(text).toContain('Create your first event to see it here.');
  });

  it('should render the table content when not loading, in error, or empty', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.table-card')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.empty-state')).toBeNull();
    expect(fixture.nativeElement.querySelector('.shimmer-row')).toBeNull();
  });
});
