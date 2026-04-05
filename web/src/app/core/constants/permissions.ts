export const Permissions = {
  DELIVERY: {
    READ:   'delivery:read',
    CREATE: 'delivery:create',
    UPDATE: 'delivery:update',
  },
  USER: {
    READ:   'user:read',
    CREATE: 'user:create',
    UPDATE: 'user:update',
    DELETE: 'user:delete',
  },
} as const;
