# Versioning

Versions are managed automatically using [Semantic Release](https://semantic-release.gitbook.io/semantic-release/). You never set a version number manually — it is derived from your commit messages.

The `api` and `web` services are versioned independently.

## How a release happens

1. You open a PR with a title following the format below
2. The PR title check passes
3. You squash merge into `master`
4. CI reads the commit message, determines the version bump, and:
   - Updates the version in `build.gradle.kts` (api) or `package.json` (web)
   - Creates a git tag (`api-v1.2.0` or `web-v1.2.0`)
   - Creates a GitHub release with a changelog
   - Builds and pushes a Docker image tagged with the new version

## PR title format

```
<type>: <short description>
```

Examples:
```
feat: add event search by location
fix: prevent duplicate ticket reservations
chore: update spring boot to 4.1.2
```

## Types and their effect

| Type | Use for | Releases? | Version bump |
|---|---|---|---|
| `feat` | new feature | yes | minor `1.0.0 → 1.1.0` |
| `fix` | bug fix | yes | patch `1.0.0 → 1.0.1` |
| `feat!` | breaking change | yes | major `1.0.0 → 2.0.0` |
| `chore` | deps, config, tooling | no | — |
| `refactor` | code restructuring | no | — |
| `ci` | CI/CD changes | no | — |
| `docs` | documentation | no | — |
| `test` | adding/fixing tests | no | — |
| `style` | formatting | no | — |

## Breaking changes

Add `!` after the type to signal a breaking change:

```
feat!: remove v1 API endpoints
```

## Day-to-day workflow

```bash
# 1. Create a branch
git checkout -b feature/my-feature

# 2. Commit however you like during development
git commit -m "wip"
git commit -m "almost there"
git commit -m "done"

# 3. Open a PR — only the title matters
#    Title: "feat: add my feature"

# 4. Squash merge → CI handles the rest
```

## Docker image tags

Each release produces three Docker image tags:

```
ghcr.io/d-kens/entri-api:latest        # always points to the latest release
ghcr.io/d-kens/entri-api:api-v1.2.0   # specific version
ghcr.io/d-kens/entri-api:<git-sha>     # specific commit
```
