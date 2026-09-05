# [1.11.0](https://github.com/d-kens/entri/compare/web-v1.10.1...web-v1.11.0) (2026-09-05)

### Bug Fixes

- **ci:** grant contents:read to the changes job in pr-checks ([73a1311](https://github.com/d-kens/entri/commit/73a1311cb9a41dfc72e51016065884ccd1af08d8))
- **ci:** merge release and publish into one job ([e659237](https://github.com/d-kens/entri/commit/e6592370c9d38d40baae45c1c072441902e0a8c3))
- **ci:** merge release/publish jobs and fix WIF access token ([92c89e8](https://github.com/d-kens/entri/commit/92c89e85b2b29839d4b538d7ac9ae5741c47940b))
- **deploy:** make Firebase secret file readable by the app container ([fdf00bb](https://github.com/d-kens/entri/commit/fdf00bb6cbc141bb705aadf2c28ce991caacc8c7))
- resize VM to e2-standard-2, fix web healthcheck, auto-sync deploy files ([3d89094](https://github.com/d-kens/entri/commit/3d890947436dbd0e4ae76ca0dc0b31fa5c9d93cf))

### Features

- migrate check-in/scanner from mobile to web, retire mobile app ([4e873d8](https://github.com/d-kens/entri/commit/4e873d862ae0b4a82bfed2cf71102aeb2976462f))
- **web:** add a 404 not-found page and catch-all route ([ee7ba62](https://github.com/d-kens/entri/commit/ee7ba622cf826b66aaa0686068091632a4d59e97))

## [1.10.1](https://github.com/d-kens/entri/compare/web-v1.10.0...web-v1.10.1) (2026-09-05)

# [1.10.0](https://github.com/d-kens/entri/compare/web-v1.9.0...web-v1.10.0) (2026-09-04)

### Bug Fixes

- **checkout:** fix form field error overlap with floating labels ([6b506d8](https://github.com/d-kens/entri/commit/6b506d832ff00e084d1279b54665e7be0811983f))

### Features

- **tickets:** redesign ticket cards with event banner and QR enlarge flow ([761758e](https://github.com/d-kens/entri/commit/761758e760219c60794c8a5962b08e7935d48b7e))
- **tickets:** show event date, venue and price on ticket cards ([e665789](https://github.com/d-kens/entri/commit/e665789afcfd4d4ee52e6ddd955048c77e3e552b))

# [1.9.0](https://github.com/d-kens/entri/compare/web-v1.8.0...web-v1.9.0) (2026-09-03)

### Bug Fixes

- **tickets:** make tickets table full width ([e3df61c](https://github.com/d-kens/entri/commit/e3df61c70d63583e5498f9e2091cdafd857f2fe2))
- **wallet:** fix withdrawal auth, min amount, and payout webhook parsing ([b2eaad1](https://github.com/d-kens/entri/commit/b2eaad1d4ec3104a142da33164320003cbcc13d2))

### Features

- **notifications:** include event name in ticket confirmation payload ([584cd7d](https://github.com/d-kens/entri/commit/584cd7d564261a34c9d5f73f7468b4fba82fdcd5))
- **tickets:** add paginated GET /events/{eventExternalId}/tickets endpoint ([f26e942](https://github.com/d-kens/entri/commit/f26e942fbe6db92cfe59c3d1fa4002f7ff9d05bc))
- **tickets:** add TicketFilter DTO for paginated ticket listing ([78a17c2](https://github.com/d-kens/entri/commit/78a17c2516281cdbd82c30363c663b2022dff91a))
- **tickets:** scope tickets to event, remove from sidenav, add view tickets buttons ([5f52784](https://github.com/d-kens/entri/commit/5f5278426b695ce91bb8789bf596fac35ca6749b))
- **wallet:** rewrite withdraw form with signal forms for reactive field visibility ([09dda6e](https://github.com/d-kens/entri/commit/09dda6ef2787c54f13e34acc0d803731a6cc8452))

# [1.8.0](https://github.com/d-kens/entri/compare/web-v1.7.1...web-v1.8.0) (2026-09-03)

### Bug Fixes

- **checkout:** rename response field url to checkoutUrl to match frontend model ([0a07c0a](https://github.com/d-kens/entri/commit/0a07c0a60070d8616e66104e3a103212962a9f93))
- **payment:** use app.api-url as payout callback URL ([b4132d6](https://github.com/d-kens/entri/commit/b4132d681e23ccb752dbd6ba1716d1684f51d84d))

### Features

- **checkout:** show failed payment state on checkout page ([8880711](https://github.com/d-kens/entri/commit/88807119c35bf03be29f02c5e5933589d62f6b36))
- **checkout:** support amount override for dev sandbox testing ([fbdf51a](https://github.com/d-kens/entri/commit/fbdf51aa25bba015707b982d1ebc83dc6b564738))
- **security:** permit unauthenticated access to checkout endpoint ([dd967de](https://github.com/d-kens/entri/commit/dd967deb9c08916adb5d3d21d520234a52502df2))
- **wallet:** accept walletId on withdraw endpoint to support platform wallet ([1bb6877](https://github.com/d-kens/entri/commit/1bb68772e0c9d63cfaa1925f7fd84e31539bc927))
- **wallet:** lazy-create organizer wallet on fetch, add platform wallet endpoint ([1803e8b](https://github.com/d-kens/entri/commit/1803e8b70fb57048615319dd9f37df47b14c0822))
- **wallet:** update withdraw call to use walletId instead of organizerExternalKey ([32562ad](https://github.com/d-kens/entri/commit/32562addea26f48424351e808efc9c3720e43aef))

## [1.7.1](https://github.com/d-kens/entri/compare/web-v1.7.0...web-v1.7.1) (2026-09-03)

### Bug Fixes

- **test:** remove stale platformProperties injection from reservation service test ([3218fac](https://github.com/d-kens/entri/commit/3218fac86944d9ad917aa5e1e68da439ad0619c2))
- **ui:** standardise sidenav page spacing and fix button icon alignment ([bfc374a](https://github.com/d-kens/entri/commit/bfc374a3104a96976e2d7498eb5af13d951887e2))

# [1.7.0](https://github.com/d-kens/entri/compare/web-v1.6.0...web-v1.7.0) (2026-09-03)

### Bug Fixes

- separate 401 and 403 error responses correctly ([a26011d](https://github.com/d-kens/entri/commit/a26011d4deccf04059ead2485ec67a550ff4c243))

### Features

- add GET /wallet/{organizerExternalKey} endpoint ([4f50887](https://github.com/d-kens/entri/commit/4f50887d2272ee24748cc93a2abf8e34b9356ea8))
- add GET /wallet/{organizerExternalKey}/transactions endpoint ([4634664](https://github.com/d-kens/entri/commit/4634664ccce97a89b851a732404164638f09b93a))
- add platform wallet, simplify analytics, and harden payment webhook handling ([2263422](https://github.com/d-kens/entri/commit/2263422dc7a0a2aa9cbf8b9013c28f50aa04fd68))
- add RBAC to wallet endpoints ([c60860a](https://github.com/d-kens/entri/commit/c60860a1b268ca4a6d6e2034c1072c54acf51be7))
- add wallet balance card to organizer dashboard ([6974979](https://github.com/d-kens/entri/commit/697497983b41d129982a5cd2c33888eb5545b1b6))
- add wallet feature to frontend ([a911a4e](https://github.com/d-kens/entri/commit/a911a4efe90ccc417bae72a89a898173108f05d3))
- add withdrawal API with atomic balance management and payout webhook handling ([535112a](https://github.com/d-kens/entri/commit/535112a42d3c728b4f9e8e45d1b88bf9d608fb8f))
- load bank codes from IntaSend when adding bank payout account ([54adb2e](https://github.com/d-kens/entri/commit/54adb2ea2c90fbb45b7414462aae5ad10177b09f))
- remove payout and payout account features from FE and BE ([00f5de9](https://github.com/d-kens/entri/commit/00f5de995232b2eac6f5d8a10a824e47d899d395))
- scaffold wallet module with organizer top-up on reservation confirmed ([d034a2b](https://github.com/d-kens/entri/commit/d034a2b83d7b2cfa433260f622113d994ef2a5fd))

# [1.6.0](https://github.com/d-kens/entri/compare/web-v1.5.0...web-v1.6.0) (2026-09-02)

### Bug Fixes

- bump api version to 1.3.0 to recover from incomplete release ([039402e](https://github.com/d-kens/entri/commit/039402efe01787aaa97d78c9d6d4fe1491ea6058))
- **ci:** install semantic-release plugins locally in api workflow ([ecc642e](https://github.com/d-kens/entri/commit/ecc642e6e5a2f5cc096434843d426b7a7824fe5d))
- configure release plugin to allow master branch ([369bd33](https://github.com/d-kens/entri/commit/369bd33e05042f0e6df0d6c3df2bcaf2931d3fa4))
- resolve code review blocking issues ([06ef7a8](https://github.com/d-kens/entri/commit/06ef7a8392bf33dd226035b2f405b670da8679e8))

### Features

- add organizer and platform analytics dashboard API endpoints ([2a03727](https://github.com/d-kens/entri/commit/2a03727dba78d1ecad4650ef65c942b535f7e8ff))
- integrate organizer and platform analytics dashboards ([8f1a082](https://github.com/d-kens/entri/commit/8f1a082c87852f17382de7d7cab5283655f44462))
- **mobile:** wire check-in service to real API endpoint ([36b9b13](https://github.com/d-kens/entri/commit/36b9b13ee39961be3b58926dedb522c53c1951b9))
- replace wallet model with direct organizer payout accounts ([6301c55](https://github.com/d-kens/entri/commit/6301c55e874d62f164aefc5f5c19c316baadf869))
- **wallet:** add withdrawal flow and transaction history to wallet page ([1d287e0](https://github.com/d-kens/entri/commit/1d287e0665be01201acc3b3a44cd83756eaa769c))
