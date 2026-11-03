# hot.md

## Work Log

- 2026-11-02 — User wants to turn Entri into a real business. Audited the money path (checkout, payment webhook, wallet, payouts, cancel, check-in) for production gaps. Top blockers: webhook trusts unauthenticated payloads (challenge optional), late payments after reservation expiry are silently dropped, no refunds on cancel, organizers can withdraw before event ends, no organizer KYC/terms.
- 2026-11-02 — Fixed #1 (forged webhooks). Challenge now mandatory + constant-time for checkout & payout webhooks; COMPLETE checkouts are verified via IntaSend `POST /api/v1/payment/status/` (secret key) and amount/currency come from that response; reservation only confirmed if paid >= total and currency matches. Webhook errors now propagate (502) so IntaSend retries instead of the payment being dropped. PaymentResult/PaymentResultEvent gained amount+currency. Status-endpoint shape not yet confirmed against IntaSend sandbox.

## Next

- Test #1 end-to-end in IntaSend sandbox (status endpoint response shape, `value` vs `net_amount` with CUSTOMER-PAYS).
- Handle payment-after-expiry (confirm if stock allows, else flag for refund).
- Payout hold until event ends + refund flow on cancel.
- Organizer verification, terms/privacy pages, VPS deployment.
