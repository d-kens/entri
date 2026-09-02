# Versioning

Each service is versioned independently — a change in one never bumps the version of the others.

Versions are explicit: the current version lives in a file (`gradle.properties` for api, `package.json` for web and mobile). CI tags it and bumps it automatically on every merge to master. You never need to set a version number manually unless you want to force a specific one.

---

## How a release works

1. Squash-merge a PR into `master`
2. CI builds and tests the changed service
3. The release job runs and creates a git tag from the current version in the version file
4. The version file is bumped to the next patch and committed (e.g. `1.2.0 → 1.2.1`)
5. The tag push triggers a second CI run which builds and pushes the Docker image to GHCR

Steps 2–5 only happen for the service whose files actually changed (path filters on the workflow).

---

## Version files

| Service | File | Example |
|---|---|---|
| `api` | `api/gradle.properties` | `version=1.2.0` |
| `web` | `web/package.json` | `"version": "1.5.0"` |
| `mobile` | `mobile/package.json` | `"version": "1.1.0"` |

---

## Re-trigger prevention

The version bump commit that CI pushes back to master would ordinarily re-trigger the release — to prevent a loop, both are guarded:

- **api**: commit message contains `[Gradle Release Plugin]` — the `release` job skips it
- **web / mobile**: commit message contains `[skip release]` — the `release` job skips it

---

## Forcing a specific version

Dispatch the workflow manually from **Actions → pick the workflow → Run workflow**.

| Service | Input | Effect |
|---|---|---|
| api | `release_version` blank | auto-increments patch |
| api | `release_version = 1.3.0` | releases exactly `1.3.0` |
| api | `new_version = 2.0.0` | sets next version to `2.0.0` after tagging |
| web / mobile | `release_version` blank | auto-increments patch |
| web / mobile | `release_version = 1.6.0` | releases exactly `1.6.0` |
| web / mobile | `release_version = minor` | bumps minor instead of patch |
| web / mobile | `release_version = major` | bumps major |

---

## Skipping a release

Put `[skip release]` in the PR title so it lands in the squash commit message — CI will build and test but not cut a new version.

For api you can also use `[skip ci]` to skip everything.

---

## Docker images

Each release produces three tags:

```
ghcr.io/d-kens/entri-api:latest        # always the latest release
ghcr.io/d-kens/entri-api:api-v1.2.0   # pinned to this version
ghcr.io/d-kens/entri-api:<git-sha>     # pinned to this commit
```

Same pattern for `entri-web` with `web-v*` tags.
