export type OrganizerSummaryMetrics = {
  totalRevenue: number;
  walletBalance: number | null;
  walletCurrency: string | null;
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
