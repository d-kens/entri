# Puuul

## SpiceDB (Authorization)

SpiceDB handles fine-grained authorization. The schema lives in [`spicedb/schema.zed`](./spicedb/schema.zed).

### Starting the stack

```bash
docker compose up -d
```

On first run, `spicedb-migrate` applies the datastore migrations and `spicedb-init` writes the schema automatically.

### Updating the schema

1. Edit `spicedb/schema.zed`
2. Apply the changes to the running SpiceDB instance:

```bash
docker compose run --rm spicedb-init
```

`schema write` is atomic and idempotent — SpiceDB validates the new schema and applies it in one shot.

> **Note:** SpiceDB rejects schema changes that would invalidate existing relationship data (e.g., removing a relation that still has tuples). Delete the affected relationships first, then re-run the command.
