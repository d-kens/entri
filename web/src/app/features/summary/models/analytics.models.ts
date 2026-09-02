export type OrganizerSummaryMetrics = {
  totalRevenue: number;
  totalTicketsSold: number;
  upcomingEventsCount: number;
  liveEventsCount: number;
};

export type PlatformSummaryMetrics = {
  totalOrganizers: number;
  totalEvents: number;
  publishedEvents: number;
  liveEventsCount: number;
  totalTicketsSold: number;
  totalGmv: number;
  platformRevenue: number;
};

export type SalesTrendDataPoint = {
  date: string;
  ticketsSold: number;
  revenue: number;
};

export type SalesTrendResponse = {
  period: string;
  data: SalesTrendDataPoint[];
};
