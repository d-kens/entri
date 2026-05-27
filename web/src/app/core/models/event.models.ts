export type CreateTicketTypeRequest = {
  name: string;
  description?: string | null;
  price: number;
  currency: string;
  quantity: number;
  maxPerOrder?: number | null;
  saleStartDate?: string | null;
  saleEndDate?: string | null;
  isHidden: boolean;
}

export type CreateEventRequest = {
  title: string;
  description: string;
  categoryId: number;
  isPublic: boolean;
  venueName: string;
  venueCity: string;
  venueCountry: string;
  startTime: string;
  endTime: string;
  bannerUrl: string;
  ticketTypes: CreateTicketTypeRequest[];
}

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
}

export type EventDetailResponse = {
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
