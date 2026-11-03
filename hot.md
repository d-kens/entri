# hot.md

## Work Log

- 2026-11-02 — User wants to turn Entri into a real business. Audited the money path (checkout, payment webhook, wallet, payouts, cancel, check-in) for production gaps. Top blockers: webhook trusts unauthenticated payloads (challenge optional), late payments after reservation expiry are silently dropped, no refunds on cancel, organizers can withdraw before event ends, no organizer KYC/terms.
- 2026-11-02 — Fixed #1 (forged webhooks). Challenge now mandatory + constant-time for checkout & payout webhooks; COMPLETE checkouts are verified via IntaSend `POST /api/v1/payment/status/` (secret key) and amount/currency come from that response; reservation only confirmed if paid >= total and currency matches. Webhook errors now propagate (502) so IntaSend retries instead of the payment being dropped. PaymentResult/PaymentResultEvent gained amount+currency. Status-endpoint shape not yet confirmed against IntaSend sandbox.
- 2026-11-03 — User asked if Entri can become a business. Verdict: yes on tech; the risks are distribution, how funds are held (in-app wallet may count as e-money under CBK rules, so prefer IntaSend-held funds or split payouts), and ODPC data-protection registration. Suggested signing 3–5 organizers before building more features.
- 2026-11-03 — Fixed payment-after-expiry. A PAID webhook for an EXPIRED reservation now confirms it if stock is still available and the event is PUBLISHED (sold += qty; reserved was already released). Otherwise the reservation becomes the new status REFUND_REQUIRED, logged at ERROR with 'REFUND REQUIRED'. No migration needed (status is VARCHAR). Checkout page shows a 'payment received too late, you will be refunded' banner. Refunds themselves are still manual.
- 2026-11-03 — Checkout now refuses a PENDING reservation whose expiresAt has passed (409 'has expired'), closing the up-to-2-minute gap before the expiry job runs.
- 2026-11-03 — Code audit (FE+BE). Security: refresh endpoint accepts any JWT (no token-type claim) so a stolen access token gives an endless session; disabled users keep access (filter/refresh don't check enabled); generateCheckInCode and media delete lack ownership checks; no rate limiting on login/forgot-password/check-in. Correctness: event update can change currency/dates after sales; cancelled events can be republished. Drop: web Name service (name.ts), empty Payments page + sidenav link, unused deps (chart.js, jwt-decode, event-source-polyfill, @angular/ssr, platform-server, express + @types, @angular-devkit/build-angular, semantic-release*). Backend has no dead classes.
- 2026-11-03 — Audit #1 done: refresh token is now an opaque 32-byte random string (OpaqueTokens), stored as SHA-256 in new refresh_tokens table (changeset 202611030001). NOT rotated on use (user's decision: frontend has no single-flight refresh, rotation would race). Revoked on logout, password change/reset, disable, delete; refresh also rejects disabled/deleted users. Existing sessions must log in again after deploy. Not done: expired/revoked rows are never purged; JWT filter still doesn't check `enabled` (#2).
- 2026-11-03 — User changed mind: refresh tokens now ROTATE. Each refresh issues a new token with the original expiry (absolute session lifetime); old token gets rotated_at (changeset 202611030002). Reuse of a rotated token within 10s grace is accepted (parallel tabs); after that, all the user's sessions are revoked. Frontend AuthService.refreshToken is single-flight (shareReplay). Logout always clears the cookie.
- 2026-11-03 — On branch fix/security-audit (uncommitted): audit #2 JWT filter ignores deleted/disabled users; #3 check-in code needs event ownership; #4 uploads are named '<ownerKey>_<uuid>_<name>' and organizers can only delete their own (old files: admin only); #6 currency locked once tickets sold/reserved; #7 cancelled events can't be republished (UI button removed); Swagger off unless SWAGGER_ENABLED=true. FE: removed empty Payments page, name.ts, 13 unused npm packages. API 314 + web 399 tests pass.

## Next

- Commit fix/security-audit (refresh token rotation + audit fixes), run app to verify migrations 202611030001/2 on MySQL.
- Rate limiting on login/forgot-password/check-in at nginx when deploying.

- Test #1 end-to-end in IntaSend sandbox (status endpoint response shape, `value` vs `net_amount` with CUSTOMER-PAYS).
- Process REFUND_REQUIRED reservations (manual for now; automate together with the refund-on-cancel flow).
- Payout hold until event ends + refund flow on cancel.
- Organizer verification, terms/privacy pages, VPS deployment.
