import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { Title, Meta } from '@angular/platform-browser';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing implements OnInit {
  private title = inject(Title);
  private meta = inject(Meta);

  ngOnInit() {
    this.title.setTitle('Entri - Sell tickets for any event');

    this.meta.updateTag({ name: 'description', content: 'Create an event, publish your ticket types, and start selling in minutes. No approval process. Built for event organisers.' });

    this.meta.updateTag({ property: 'og:type',        content: 'website' });
    this.meta.updateTag({ property: 'og:title',       content: 'Entri - Sell tickets for any event' });
    this.meta.updateTag({ property: 'og:description', content: 'Create an event, publish your ticket types, and start selling in minutes. No approval process. Built for event organisers.' });
    this.meta.updateTag({ property: 'og:url',         content: 'https://entri.co' });

    this.meta.updateTag({ name: 'twitter:card',        content: 'summary_large_image' });
    this.meta.updateTag({ name: 'twitter:title',       content: 'Entri - Sell tickets for any event' });
    this.meta.updateTag({ name: 'twitter:description', content: 'Create an event, publish your ticket types, and start selling in minutes. No approval process. Built for event organisers.' });
  }
  readonly steps = [
    { icon: 'edit_note',          title: 'Create your event',    desc: 'Add your event details, upload a cover image, and set your ticket types in minutes.' },
    { icon: 'confirmation_number', title: 'Set ticket types',     desc: 'Create multiple ticket tiers - VIP, Early Bird, General - each with their own price and quantity.' },
    { icon: 'payments',           title: 'Get paid instantly',   desc: 'Payments go straight to you. Track sales and attendees from your dashboard in real time.' },
  ];

  readonly features = [
    { icon: 'bolt',              title: 'Live in minutes',        desc: 'Create an event and start selling tickets in under 5 minutes. No approval needed.' },
    { icon: 'qr_code_scanner',   title: 'QR check-in',           desc: 'Scan tickets at the door with any phone. No extra hardware required.' },
    { icon: 'insights',          title: 'Real-time analytics',   desc: 'Watch ticket sales come in live. See revenue, attendance, and trends as they happen.' },
    { icon: 'mail',              title: 'Automatic tickets',      desc: 'Attendees receive a branded ticket with QR code the moment they pay. Zero manual work.' },
    { icon: 'tune',              title: 'Multiple ticket types',  desc: 'Early bird, VIP, group discounts - full control over your pricing structure.' },
  ];

  readonly stats = [
    { value: '500+',  label: 'Events hosted' },
    { value: '50k+',  label: 'Tickets sold' },
    { value: '200+',  label: 'Organisers' },
    { value: '4.9★',  label: 'Avg. rating' },
  ];

  readonly testimonials = [
    {
      quote: 'We sold out 350 tickets in under 48 hours. I spent more time picking the cover photo than setting up the page.',
      name: 'Amara N.',
      role: 'Jazz Night Nairobi',
    },
    {
      quote: 'Finally an events platform that just works. No back-and-forth with support, no waiting for approval. I published and went live.',
      name: 'David K.',
      role: 'Nairobi Tech Meetup',
    },
  ];

  readonly plans = [
    {
      name: 'Basic',
      price: 'KES 999',
      period: '/ event',
      tag: 'Up to 200 tickets',
      desc: 'For small workshops, meetups, and intimate events.',
      features: [
        'Flat fee - no cut from your revenue',
        'Live in minutes, no approval needed',
        'Payments go directly to you',
      ],
      cta: 'Create an event',
      highlight: false,
    },
    {
      name: 'Standard',
      price: 'KES 2,499',
      period: '/ event',
      tag: 'Up to 1,000 tickets',
      desc: 'For mid-size events, conferences, and shows.',
      features: [
        'Flat fee - no cut from your revenue',
        'Live in minutes, no approval needed',
        'Payments go directly to you',
      ],
      cta: 'Create an event',
      highlight: true,
    },
    {
      name: 'Large',
      price: 'KES 4,999',
      period: '/ event',
      tag: 'Up to 5,000 tickets',
      desc: 'For concerts, festivals, and large-scale events.',
      features: [
        'Flat fee - no cut from your revenue',
        'Live in minutes, no approval needed',
        'Payments go directly to you',
      ],
      cta: 'Create an event',
      highlight: false,
    },
    {
      name: 'Unlimited',
      price: 'KES 9,999',
      period: '/ event',
      tag: 'Unlimited tickets',
      desc: 'For large-scale concerts, festivals, and venues.',
      features: [
        'Flat fee - no cut from your revenue',
        'Live in minutes, no approval needed',
        'Payments go directly to you',
      ],
      cta: 'Create an event',
      highlight: false,
    },
  ];

  readonly faqs = [
    {
      q: 'How does the pricing work?',
      a: 'Flat fee per event - KES 999 up to 200 tickets, KES 2,499 up to 1,000, KES 4,999 up to 5,000, KES 9,999 unlimited. Free events are always free.',
    },
    {
      q: 'Where does ticket money go?',
      a: 'Straight to your M-Pesa or bank account. You provide your details when creating the event. Entri does not hold your money.',
    },
    {
      q: 'What if I need to cancel my event?',
      a: 'Cancel anytime from your dashboard. You handle refunds to attendees - Entri gives you tools to do it in bulk or one by one.',
    },
    {
      q: 'How do I know who is coming?',
      a: 'Your dashboard shows a full attendee list with names and contacts. Export it anytime.',
    },
    {
      q: 'What if the internet goes down on event day?',
      a: 'The check-in app works offline. It syncs the ticket list beforehand so scanning continues without a connection.',
    },
    {
      q: 'Do attendees need an account to buy tickets?',
      a: 'No. Name, email, pay - done. Ticket arrives instantly. No app, no account.',
    },
  ];

  openFaq = signal<number | null>(null);

  toggleFaq(index: number): void {
    this.openFaq.set(this.openFaq() === index ? null : index);
  }

  readonly eventTypes = [
    { emoji: '🎵', label: 'Music & Concerts' },
    { emoji: '💻', label: 'Tech & Business' },
    { emoji: '🏃', label: 'Sports & Fitness' },
    { emoji: '🎨', label: 'Arts & Culture' },
    { emoji: '🍔', label: 'Food & Drink' },
    { emoji: '🎓', label: 'Education' },
    { emoji: '🕺', label: 'Nightlife' },
    { emoji: '🌿', label: 'Wellness' },
  ];
}
