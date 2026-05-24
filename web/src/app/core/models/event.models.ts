export type TicketTypeResponse = {
  externalId: string;
  name: string;
  description?: string;
  price: number;
  currency: string;
  quantity: number;
  maxPerOrder?: number;
  saleStartDate?: string;
  saleEndDate?: string;
  status: string;
  isHidden: boolean;
}

export type EventResponse = {
  externalId: string;
  title: string;
  description: string;
  categoryName: string;
  venueName: string;
  venueCity: string;
  venueCountry: string;
  startTime: string;
  endTime: string;
  bannerUrl: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED';
  isPublic: boolean;
  publishedAt?: string;
  dateCreated: string;
  ticketTypes: TicketTypeResponse[];
}
