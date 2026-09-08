import { Routes } from '@angular/router';

export const EVENTS_ROUTES: Routes = [
  {
    path: '',
    title: 'Manage Events',
    loadComponent: () =>
      import('@features/events/events-list/events-list').then((m) => m.EventsList),
  },
  {
    path: 'create',
    title: 'Create Event',
    loadComponent: () =>
      import('@features/events/create-event/create-event').then((m) => m.CreateEvent),
  },
  {
    path: ':eventExternalId',
    title: 'Event Details',
    loadComponent: () =>
      import('@features/events/event-details/event-details').then((m) => m.EventDetails),
  },
  {
    path: ':eventExternalId/edit',
    title: 'Edit Event',
    loadComponent: () => import('@features/events/edit-event/edit-event').then((m) => m.EditEvent),
  },
  {
    path: ':eventExternalId/ticket-types',
    title: 'Create Ticket Type',
    loadComponent: () =>
      import('@features/events/create-ticket-type/create-ticket-type').then(
        (m) => m.CreateTicketType,
      ),
  },
  {
    path: ':eventExternalId/ticket-types/:ticketTypeId/edit',
    title: 'Edit Ticket Type',
    loadComponent: () =>
      import('@features/events/edit-ticket-type/edit-ticket-type').then((m) => m.EditTicketType),
  },
  {
    path: ':eventExternalId/tickets',
    title: 'Tickets',
    loadComponent: () => import('@features/tickets/tickets').then((m) => m.Tickets),
  },
];
