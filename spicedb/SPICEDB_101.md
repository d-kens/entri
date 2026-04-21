# SpiceDB 101

SpiceDB is an open-source permissions database inspired by Google Zanzibar — the system that powers authorization across Google's products (Drive, YouTube, Gmail, etc.).

Instead of scattering permission checks across your application code, SpiceDB centralizes them in one place. Your app asks SpiceDB "can this user do this thing?" and SpiceDB answers yes or no.

---

## Core Concepts

### Object
Anything in your system that can have permissions — a user, a group, a document, a contribution. Every object has a **type** and an **ID**.

```
user:alice
group:chama-1
contribution:txn-42
```

### Relation
A named link between two objects. It describes a fact about their relationship.

```
group:chama-1#member@user:alice
          │       │        │
          │       │        └── the subject (who)
          │       └─────────── the relation (what role)
          └─────────────────── the resource (on what)
```

This reads: *"alice is a member of chama-1"*

### Permission
A derived yes/no answer computed from relations. Permissions are defined in the schema and evaluated at query time — they are never stored.

```
does user:alice have permission view on contribution:txn-42?
→ SpiceDB evaluates the schema rules and answers: true / false
```

### Schema
The schema defines your object types, what relations they can have, and how permissions are computed from those relations. It is the authorization model for your entire app.

---

## How It Works

### 1. Define the schema
Write your authorization model in `schema.zed`. This is done once (and updated as your model evolves).

```zed
definition user {}

definition group {
  relation member: user
  permission view = member
}
```

### 2. Write relationships
When something happens in your app (user joins a group, creates a resource), write a relationship tuple to SpiceDB.

```
group:chama-1#member@user:alice    ← alice joined chama-1
contribution:txn-42#owner@user:alice    ← alice created txn-42
contribution:txn-42#group@group:chama-1 ← txn-42 belongs to chama-1
```

### 3. Check permissions
Before performing an action, ask SpiceDB if the user is allowed.

```
can user:alice view contribution:txn-42?  → true
can user:bob   view contribution:txn-42?  → false (unless bob is an admin of chama-1)
```

---

## Relationship Tuple Format

Every relationship is stored as a tuple:

```
resource_type:resource_id#relation@subject_type:subject_id
```

| Part              | Example           | Meaning                    |
|-------------------|-------------------|----------------------------|
| `resource_type`   | `group`           | Type of the resource       |
| `resource_id`     | `chama-1`         | ID of the resource         |
| `relation`        | `member`          | The relation name          |
| `subject_type`    | `user`            | Type of the subject        |
| `subject_id`      | `alice`           | ID of the subject          |

---

## Schema Language (Zed)

### `definition`
Declares an object type.
```zed
definition user {}
definition group {}
```

### `relation`
Declares a named link from one object type to another.
```zed
definition group {
  relation admin: user
  relation member: user
}
```

### `permission`
Declares a computed yes/no rule using set operations on relations.
```zed
definition group {
  relation admin: user
  relation member: user

  permission view = member + admin   // union: member OR admin
  permission manage = admin          // only admin
}
```

### Set Operators

| Operator | Symbol | Meaning                              |
|----------|--------|--------------------------------------|
| Union    | `+`    | Subject has any of these relations   |
| Intersect| `&`    | Subject must have all of these       |
| Exclude  | `-`    | Subject has first but not second     |

### Arrow (`->`)
Traverses a relation to check a permission on a related object.

```zed
definition contribution {
  relation owner: user
  relation group: group

  permission view = owner + group->admin
  //                        │
  //                        └── follow 'group' relation, then check 'admin' on that group
}
```

---

## SpiceDB vs Traditional RBAC

| | Traditional RBAC | SpiceDB (ReBAC) |
|---|---|---|
| Model | Roles assigned to users | Relationships between objects |
| Rules | Static role → permission tables | Dynamic graph traversal |
| Example | "alice has role editor" | "alice is a member of chama-1 which owns txn-42" |
| Scales to | Simple flat hierarchies | Nested hierarchies, sharing, delegation |

---

## Key Terms Cheatsheet

| Term | Definition |
|---|---|
| **Object** | A typed entity (`user:alice`, `group:chama-1`) |
| **Relation** | A named link between objects (`member`, `admin`) |
| **Permission** | A computed rule evaluated at check time (`view`, `manage`) |
| **Tuple** | A stored relationship fact (`group:chama-1#member@user:alice`) |
| **Schema** | The file defining all types, relations, and permissions |
| **Check** | A yes/no permission query against SpiceDB |
| **Caveat** | An optional condition on a relation (e.g. time-based access) |
| **ZedToken** | A consistency token returned after a write, used to avoid stale reads |

---

## Further Reading

- [SpiceDB Docs](https://authzed.com/docs)
- [Zanzibar Paper](https://research.google/pubs/pub48190/) — the Google paper SpiceDB is based on
- [Playground](https://play.authzed.com) — write and test schemas in the browser
