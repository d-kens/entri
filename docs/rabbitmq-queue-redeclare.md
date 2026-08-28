# RabbitMQ — Redeclaring Queues with New Arguments

## Problem

RabbitMQ does not allow modifying properties on an already-existing queue. If a queue was created without a dead-letter exchange (DLX) and the code is updated to declare it with one, the app will crash on startup with:

```
PRECONDITION_FAILED - inequivalent arg 'x-dead-letter-exchange' for queue 'notification.send'
in vhost '/': received the value 'notification.dlx' of type 'longstr' but current is none
```

Spring surfaces this as:

```
FatalListenerStartupException: Mismatched queues
```

## Fix

Delete the affected queues via the RabbitMQ management API and restart the app. Spring will recreate them with the correct arguments.

```bash
curl -u <user>:<pass> -X DELETE http://localhost:15672/api/queues/%2F/<queue-name>
```

### Example

```bash
curl -u guest:guest -X DELETE http://localhost:15672/api/queues/%2F/notification.send
curl -u guest:guest -X DELETE http://localhost:15672/api/queues/%2F/user.created.subscriber
curl -u guest:guest -X DELETE http://localhost:15672/api/queues/%2F/user.updated.subscriber
```

Then restart the application.

## Notes

- `%2F` is the URL-encoded `/` for the default vhost.
- Default credentials are `guest:guest`. Check `spring.rabbitmq.username/password` in `application.yaml` if different.
- Queues can also be deleted manually via the management UI at `http://localhost:15672` under the **Queues** tab.
- Only do this when the queue has no unprocessed messages you need to keep, or you are OK losing them.
