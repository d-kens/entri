# Versioning

Each service is versioned independently — a change in one never bumps the version of the others. The current version always lives in a file in the repo; CI tags it and bumps it automatically on every merge to master. You never set a version number manually unless you want to force a specific one.

---

## Approaches

Two different release strategies are used across the three services.

---

### Gradle Release Plugin — `api`

The API uses the [Gradle Release Plugin](https://github.com/researchgate/gradle-release). The version is stored in `api/gradle.properties` and follows this flow on every merge to master:

1. CI builds and tests the API
2. The release job runs `./gradlew release`
3. The plugin strips `-SNAPSHOT`, commits a "pre-tag" commit, and creates the git tag (e.g. `api-v1.3.1`)
4. The plugin immediately bumps to the next `-SNAPSHOT` and commits again (e.g. `1.3.2-SNAPSHOT`)
5. The tag push triggers a separate CI run that builds and pushes the Docker image to GHCR

**Re-trigger guard:** The plugin's own commits contain `[Gradle Release Plugin]` in the message — the workflow skips the release job for those commits.

**Skip a release:** Put `[skip release]` in the PR title.

**Force a specific version:** Dispatch manually from Actions → Web → Run workflow and pass `release_version` and/or `new_version`.

| Input | Effect |
|---|---|
| both blank | auto-increments patch |
| `release_version = 1.4.0` | releases exactly `1.4.0` |
| `new_version = 2.0.0-SNAPSHOT` | sets the next snapshot version after tagging |

---

### Semantic Release — `web` and `mobile`

Web and mobile use [semantic-release](https://semantic-release.gitbook.io). The version is stored in `package.json` and is determined automatically from **commit messages** — no manual version bumping, no version input needed.

#### How the version is determined

semantic-release reads all commits since the last tag and applies these rules:

| Commit prefix | Version bump |
|---|---|
| `feat:` | **minor** (e.g. `1.5.0 → 1.6.0`) |
| `fix:`, `perf:`, `refactor:`, `chore:`, `ci:`, `build:`, `docs:`, `test:` | **patch** (e.g. `1.5.0 → 1.5.1`) |
| `BREAKING CHANGE` in commit footer | **major** (e.g. `1.5.0 → 2.0.0`) |

#### What happens on every merge to master

1. CI builds and tests the service
2. The release job runs `npx semantic-release`
3. semantic-release analyzes commits since the last tag and calculates the next version
4. It updates `package.json` and generates/updates `CHANGELOG.md`
5. It commits those changes with `chore(release): <version> [skip ci]` and pushes to master
6. It creates and pushes the git tag (e.g. `web-v1.5.1`)
7. The tag push triggers a separate CI run that builds and pushes the Docker image to GHCR (web only)

**Re-trigger guard:** The release commit contains `[skip ci]` — GitHub natively skips all workflow runs for commits with this token, so no loop occurs.

**Skip a release:** Put `[skip ci]` in the PR title. GitHub will skip the entire workflow, including tests. To skip only the release but still run tests, there is currently no dedicated mechanism — semantic-release itself decides based on commit content (if no releasable commits exist, it does nothing).

**Force a specific version:** Not directly supported. semantic-release is purely commit-driven. Trigger the workflow manually from Actions if needed — semantic-release will re-evaluate commits and release if there are unreleased changes.

---

## Version files

| Service | File | Current format |
|---|---|---|
| `api` | `api/gradle.properties` | `version=1.3.2-SNAPSHOT` |
| `web` | `web/package.json` | `"version": "1.5.0"` |
| `mobile` | `mobile/package.json` | `"version": "1.1.0"` |

---

## Git tags

| Service | Tag format | Example |
|---|---|---|
| `api` | `api-v<version>` | `api-v1.3.1` |
| `web` | `web-v<version>` | `web-v1.5.1` |
| `mobile` | `mobile-v<version>` | `mobile-v1.1.1` |

---

## Docker images

Each release produces three image tags in GHCR:

```
ghcr.io/d-kens/entri-web:latest          # always points to the latest release
ghcr.io/d-kens/entri-web:web-v1.5.1     # pinned to this exact version
ghcr.io/d-kens/entri-web:<git-sha>       # pinned to this exact commit
```

Same pattern for `entri-api` with `api-v*` tags. Mobile does not produce a Docker image.
