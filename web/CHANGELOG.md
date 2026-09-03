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
