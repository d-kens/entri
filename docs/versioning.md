# Versioning

Versions are managed automatically by [semantic-release](https://semantic-release.gitbook.io). You never set a version number manually — it is derived from your commit messages.

`api` and `web` are versioned **independently**. A change in one never bumps the version of the other.

---

## How a release works

1. You squash-merge a PR into `master`
2. CI analyses the commit message and determines the version bump
3. The version is updated in `build.gradle.kts` (api) or `package.json` (web)
4. A git tag is created — `api-v1.2.0` or `web-v1.2.0`
5. A GitHub release is created with a generated changelog
6. A Docker image is built and pushed to GHCR

Steps 3–6 only happen for the service whose files actually changed.

---

## Commit message format

```
<type>: <short description>
```

The **PR title** is what matters — individual commits during development can be anything. When you squash merge, the PR title becomes the commit message that CI reads.

---

## Types and version bumps

| Type | When to use | Triggers release | Bump |
|---|---|---|---|
| `feat` | New feature | Yes | Minor `1.1.0 → 1.2.0` |
| `fix` | Bug fix | Yes | Patch `1.2.0 → 1.2.1` |
| `feat!` | Breaking change | Yes | Major `1.0.0 → 2.0.0` |
| `chore` | Dependencies, config, tooling | No | — |
| `refactor` | Code restructuring, no behaviour change | No | — |
| `ci` | CI/CD pipeline changes | No | — |
| `docs` | Documentation only | No | — |
| `style` | Formatting, whitespace | No | — |

---

## Breaking changes

Append `!` to the type:

```
feat!: remove legacy auth endpoints
```

---

## Workflow

```bash
# 1. Branch off master
git checkout -b feat/my-feature

# 2. Commit freely during development
git commit -m "wip"

# 3. Open a PR — set the title correctly
#    e.g. "feat: add organiser dashboard stats endpoint"

# 4. Squash merge → CI handles versioning automatically
```

---

## Docker images

Each release produces three tags:

```
ghcr.io/d-kens/entri-api:latest          # always the latest release
ghcr.io/d-kens/entri-api:api-v1.2.0     # pinned to a specific version
ghcr.io/d-kens/entri-api:<git-sha>       # pinned to a specific commit
```

Same pattern for `entri-web` with `web-v*` tags.
