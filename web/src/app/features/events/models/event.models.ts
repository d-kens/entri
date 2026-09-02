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
  description: string;
};

export type CategoryRequest = {
  name: string;
  description: string;
};

export type TicketTypeRequest = {
  name: string;
  description?: string | null;
  price: number;
  quantity: number;
  maxTicketsPerOrder: number;
  saleStartDate?: string | null;
  saleEndDate?: string | null;
};

export type EventRequest = {
  title: string;
  description: string;
  categoryId: number;
  currency: string;
  venueName: string;
  venueCity: string;
  venueCountry: string;
  startTime: string;
  endTime: string;
  bannerUrl: string;
};

export type TicketTypeSaleStatus = 'UPCOMING' | 'ON_SALE' | 'ENDED';
export type TicketTypeAvailabilityStatus = 'AVAILABLE' | 'SOLD_OUT';

export type TicketTypeResponse = {
  id: number;
  name: string;
  description?: string;
  price: number;
  quantity: number;
  availableQuantity: number;
  maxTicketsPerOrder?: number;
  saleStartDate?: string;
  saleEndDate?: string;
  saleStatus: TicketTypeSaleStatus;
  availabilityStatus: TicketTypeAvailabilityStatus;
};

export type EventResponse = {
  externalId: string;
  title: string;
  description: string;
  categoryName: string;
  categoryId: number;
  currency: string;
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

export interface EventTicketReservationItemRequest {
  ticketTypeId: number;
  quantity: number;
}

export interface EventTicketReservationRequest {
  itemRequests: EventTicketReservationItemRequest[];
}

export interface EventTicketReservationResponse {
  expiresAt: string;
  reservationId: string;
  totalAmount: number;
  externalEventId: string;
}

export interface EventTicketReservationItemResponse {
  quantity: number;
  ticketType: string;
  unitPrice: number;
  totalAmount: number;
}

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'EXPIRED';

export interface EventTicketReservationDetailResponse {
  expiresAt: string;
  reservationId: string;
  totalAmount: number;
  externalEventId: string;
  status: ReservationStatus;
  reservationItems: EventTicketReservationItemResponse[];
}

export interface CheckoutRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}

export interface CheckoutResponse {
  checkoutUrl: string;
}

export type TicketStatus = 'VALID' | 'USED';

export interface TicketResponse {
  ticketCode: string;
  eventExternalId: string;
  eventTitle: string;
  ticketTypeName: string;
  status: TicketStatus;
  holderFirstName: string;
  holderLastName: string;
  checkedInAt?: string;
}
