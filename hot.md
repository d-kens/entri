# hot.md

## Work Log

- 2026-11-02 — User wants to turn Entri into a real business. Audited the money path (checkout, payment webhook, wallet, payouts, cancel, check-in) for production gaps. Top blockers: webhook trusts unauthenticated payloads (challenge optional), late payments after reservation expiry are silently dropped, no refunds on cancel, organizers can withdraw before event ends, no organizer KYC/terms.
- 2026-11-02 — Fixed #1 (forged webhooks). Challenge now mandatory + constant-time for checkout & payout webhooks; COMPLETE checkouts are verified via IntaSend `POST /api/v1/payment/status/` (secret key) and amount/currency come from that response; reservation only confirmed if paid >= total and currency matches. Webhook errors now propagate (502) so IntaSend retries instead of the payment being dropped. PaymentResult/PaymentResultEvent gained amount+currency. Status-endpoint shape not yet confirmed against IntaSend sandbox.
- 2026-11-03 — User asked if Entri can become a business. Verdict: yes on tech; the risks are distribution, how funds are held (in-app wallet may count as e-money under CBK rules, so prefer IntaSend-held funds or split payouts), and ODPC data-protection registration. Suggested signing 3–5 organizers before building more features.
- 2026-11-03 — Fixed payment-after-expiry. A PAID webhook for an EXPIRED reservation now confirms it if stock is still available and the event is PUBLISHED (sold += qty; reserved was already released). Otherwise the reservation becomes the new status REFUND_REQUIRED, logged at ERROR with 'REFUND REQUIRED'. No migration needed (status is VARCHAR). Checkout page shows a 'payment received too late, you will be refunded' banner. Refunds themselves are still manual.
- 2026-11-03 — Checkout now refuses a PENDING reservation whose expiresAt has passed (409 'has expired'), closing the up-to-2-minute gap before the expiry job runs.

## Next

- Test #1 end-to-end in IntaSend sandbox (status endpoint response shape, `value` vs `net_amount` with CUSTOMER-PAYS).
- Process REFUND_REQUIRED reservations (manual for now; automate together with the refund-on-cancel flow).
- Payout hold until event ends + refund flow on cancel.
- Organizer verification, terms/privacy pages, VPS deployment.
