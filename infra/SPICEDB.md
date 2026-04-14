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
