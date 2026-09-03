import { Routes } from '@angular/router';

export const EVENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('@features/events/events-list/events-list').then((m) => m.EventsList),
  },
  {
    path: 'create',
    loadComponent: () =>
      import('@features/events/create-event/create-event').then((m) => m.CreateEvent),
  },
  {
    path: ':eventExternalId',
    loadComponent: () =>
      import('@features/events/event-details/event-details').then((m) => m.EventDetails),
  },
  {
    path: ':eventExternalId/edit',
    loadComponent: () => import('@features/events/edit-event/edit-event').then((m) => m.EditEvent),
  },
  {
    path: ':eventExternalId/ticket-types',
    loadComponent: () =>
      import('@features/events/create-ticket-type/create-ticket-type').then(
        (m) => m.CreateTicketType,
      ),
  },
  {
    path: ':eventExternalId/ticket-types/:ticketTypeId/edit',
    loadComponent: () =>
      import('@features/events/edit-ticket-type/edit-ticket-type').then((m) => m.EditTicketType),
  },
  {
    path: ':eventExternalId/tickets',
    loadComponent: () => import('@features/tickets/tickets').then((m) => m.Tickets),
  },
];
