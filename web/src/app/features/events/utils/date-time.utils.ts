export function formatTime(date: Date): string {
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });
}

export function combineDateTime(date: Date | string, time: string): string {
  const [hours, minutes] = time.split(':').map(Number);

  const dt = new Date(date);
  dt.setHours(hours, minutes, 0, 0);

  return dt.toISOString();
}
