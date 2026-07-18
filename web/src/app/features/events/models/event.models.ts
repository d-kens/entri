export type EventFilter = {
  page?: number;
  size?: number;
  sortDirection?: 'ASC' | 'DESC';
  categoryId?: number;
  searchTerm?: string;
  organizerExternalId?: string;
  startFrom?: string;
  startTo?: string;
};

export type CategoryResponse = {
  id: number;
  name: string;
};

export type TicketTypeRequest = {
  name: string;
  description?: string | null;
  price: number;
  currency: string;
  quantity: number;
  maxTicketsPerOrder: number;
  saleStartDate?: string | null;
  saleEndDate?: string | null;
};

export type EventRequest = {
  title: string;
  description: string;
  categoryId: number;
  venueName: string;
  venueCity: string;
  venueCountry: string;
  startTime: string;
  endTime: string;
  bannerUrl: string;
};

export type TicketTypeResponse = {
  id: number;
  name: string;
  description?: string;
  price: number;
  currency: string;
  quantity: number;
  maxTicketsPerOrder: number;
  saleStartDate?: string;
  saleEndDate?: string;
  status: string;
};

export type EventResponse = {
  externalId: string;
  title: string;
  description: string;
  categoryName: string;
  categoryId: number;
  venueName: string;
  venueCity: string;
  venueCountry: string;
  startTime: string;
  endTime: string;
  bannerUrl: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED';
  publishedAt?: string;
  dateCreated: string;
};

export type EventDetailResponse = {
  externalId: string;
  title: string;
  description: string;
  categoryName: string;
  categoryId: number;
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
};
