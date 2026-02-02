# Parrcel – Affordable, Realible and Same Day Delivery for Social Commerce Merchants

**Parrcel** logistics platform designed to make last-mile delivery cheap, reliable, and scalable for social commerce merchants in Kenya, Nairobi (Instagram, TikTok, Facebook WhatsApp sellers).

It uses a **consolidation model** with neighborhood agents (local dukas/shops) serving as both **drop-off points** for merchants and **pickup points** for customers. Parcels are batched at a central hub to minimize costs.

## Features
- **Merchant-friendly delivery order placement**
- **Agent network management** – onboarding, location mapping, commission tracking
- **Smart consolidation & batching** at hub – auto-group parcels by destination/neighborhood/time
- **Optimized routing** suggestions for riders (Google Maps API compatible)
- **Real-time tracking** with status updates (Dropped → At Hub → Out for Delivery → Delivered)
- **Multi-user roles**:
  - Admin (owner/manager dashboard)
  - Agent (mobile-friendly parcel handling)
  - Merchant (seller dashboard)
  - Customer (buyer tracking portal)
- **M-Pesa integration** (delivery fees, COD, payouts) – placeholders ready
- **Notifications** via SMS/WhatsApp (Twilio/AfricasTalking compatible)
- **Reporting** – volumes, costs per parcel, agent performance, revenue

## Why This Project?

E-commerce in Kenya is booming, but high delivery fees and unreliable last-mile service hurt small sellers. Parrcel solves this by dropping costs to KSh 100–150 per parcel via batching and leveraging local agents (parrcel points) for convenience and trust.

## Tech Stack (MVP)

- **Backend**: SpringBoot
- **Frontend**: Angular
- **Database**: MySQL
- **Authentication**: JWT + Role-based access
- **Maps/Routing**: Google Maps API
- **Payments**: M-Pesa Daraja API integration
- **Notifications**: AfricasTalking / Twilio SMS/WhatsApp, Novu
- **Deployment**: GCP

## System Roles & Key Functionalities

### Admin
- Full dashboard: orders, agents, riders, analytics, batching approval
- Onboard agents/merchants, set pricing/commissions
- Financial reports & KRA-ready exports

### Agent
- Scan/log incoming (merchant drop-off) & outgoing (customer pickup) parcels
- View commissions & request payouts
- Simple mobile interface

### Merchant
- Create delivery orders
- Find nearest drop-off agent
- Track parcels & view history

### Customer
- Track via ID or link
- Locate nearest pickup agent
- COD confirmation
