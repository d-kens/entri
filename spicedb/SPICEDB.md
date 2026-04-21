# SpiceDB Infrastructure

SpiceDB is the authorization engine for this project. It stores and evaluates permissions using a relationship-based access control (ReBAC) model.

## Services

### `spicedb-postgres`
A PostgreSQL 15 database that SpiceDB uses as its datastore. All relationship data and schema are persisted here.

### `spicedb-migrate`
A one-shot job that runs `spicedb datastore migrate head` to apply SpiceDB's internal database migrations. Runs before `spicedb` starts and exits once complete. Required on a fresh database.

### `spicedb`
The SpiceDB server. Exposes a gRPC API on port `50051`. Starts only after migrations have been applied.

- **Endpoint:** `localhost:50051`
- **Auth token:** `devkey`
- **TLS:** disabled (dev only)

### `spicedb-init`
A one-shot job that uploads `spicedb/schema.zed` to the running SpiceDB instance using the `zed` CLI. Runs after `spicedb` passes its health check.

## Startup Order

```
postgres (healthy) → spicedb-migrate (exits 0) → spicedb (healthy) → spicedb-init (exits 0)
```

Each step must complete before the next begins:

**1. `postgres` (healthy)**
The database must be fully initialized and accepting connections before anything else runs. Without this, both `spicedb-migrate` and `spicedb` would fail trying to connect to a database that isn't ready. The healthcheck (`pg_isready`) ensures postgres is not just started but actually able to accept queries.

**2. `spicedb-migrate` (exits 0)**
SpiceDB does not create its own tables automatically on first run. It expects the database to already have its internal schema. `spicedb-migrate` runs `spicedb datastore migrate head` to apply all of these migrations against the empty postgres database. It then exits. If `spicedb` were to start before this step, it would crash immediately because the tables it queries do not exist yet.

The migration creates the following tables:

| Table | Purpose |
|---|---|
| `alembic_version` | Tracks which migrations have been applied. SpiceDB checks this on startup to know if the database is up to date. |
| `relation_tuple` | The core table. Stores every relationship tuple written by your app — e.g. `group:chama-1#member@user:alice`. |
| `relation_tuple_transaction` | Tracks every write transaction with a timestamp and snapshot. Used to provide consistent reads and support the watch API. |
| `namespace_config` | Stores the serialized schema definitions (object types, relations, permissions) uploaded via `zed schema write`. |
| `caveat` | Stores caveat definitions — conditional expressions that can be attached to relationships for context-aware permissions (e.g. time-based access). |
| `relationships_counters` | Tracks counts of relationships per type. Used internally for metrics and query optimization. |
| `metadata` | Stores a unique ID for the datastore instance. Used for telemetry and to identify the datastore across restarts. |

**3. `spicedb` (healthy)**
The server starts and begins serving the gRPC API on port `50051`. The healthcheck (`grpc_health_probe`) polls the gRPC health endpoint until SpiceDB confirms it is ready to accept requests. `spicedb-init` must not run until this point — writing a schema to a server that is still starting up would fail.

**4. `spicedb-init` (exits 0)**
With SpiceDB running and healthy, `spicedb-init` uploads `schema.zed` via `zed schema write`. This loads your authorization model — the object types, relations, and permissions — into SpiceDB's database. Without this step, SpiceDB would be running but have no schema, and any permission check from your application would return `not found`.

## Common Commands
**Re-apply the schema without restarting the stack:**
```bash
zed schema write spicedb/schema.zed \
  --endpoint localhost:50051 \
  --insecure \
  --token devkey
```

**Read the current schema from SpiceDB:**
```bash
zed schema read \
  --endpoint localhost:50051 \
  --insecure \
  --token devkey
```

## Notes

- `devkey` is a placeholder token for local development. Replace with a secret environment variable in production.
- Schema writes are additive-safe. Removing relations or definitions that have existing data will be rejected by SpiceDB until the conflicting data is deleted first.
- The `docker-compose down -v` flag wipes the postgres volume — all relationship data and the schema are erased. `spicedb-init` reapplies the schema automatically on the next `docker-compose up`.
