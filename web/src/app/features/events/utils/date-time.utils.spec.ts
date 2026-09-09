import { combineDateTime, formatTime } from './date-time.utils';

describe('formatTime', () => {
  it('formats a morning time with zero-padded hours and minutes', () => {
    const date = new Date(2024, 0, 1, 9, 5);

    expect(formatTime(date)).toBe('09:05');
  });

  it('formats an afternoon/evening time using 24-hour notation', () => {
    const date = new Date(2024, 0, 1, 23, 45);

    expect(formatTime(date)).toBe('23:45');
  });

  it('formats midnight as 00:00 rather than 24:00', () => {
    const date = new Date(2024, 0, 1, 0, 0);

    expect(formatTime(date)).toBe('00:00');
  });
});

describe('combineDateTime', () => {
  it('applies the given time to a Date object and returns an ISO string', () => {
    const date = new Date(2024, 5, 15);

    const result = combineDateTime(date, '14:30');

    const expected = new Date(2024, 5, 15);
    expected.setHours(14, 30, 0, 0);
    expect(result).toBe(expected.toISOString());
  });

  it('applies the given time to a date string and returns an ISO string', () => {
    const dateStr = '2024-06-15';

    const result = combineDateTime(dateStr, '09:15');

    const expected = new Date(dateStr);
    expected.setHours(9, 15, 0, 0);
    expect(result).toBe(expected.toISOString());
  });

  it('zeroes out seconds and milliseconds regardless of the input date', () => {
    const date = new Date(2024, 5, 15, 10, 10, 59, 999);

    const result = combineDateTime(date, '08:00');

    const expected = new Date(2024, 5, 15);
    expected.setHours(8, 0, 0, 0);
    expect(result).toBe(expected.toISOString());
  });
});
