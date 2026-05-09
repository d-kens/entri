/**
 * Describes one mocked API route.
 *
 * cy.startMsw() takes an array of these and turns each into a cy.intercept() call
 * so tests can control exactly what the server returns without hitting a real backend.
 */
export type MswHandler = {
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  url: string;
  statusCode: number;
  body: unknown;
  /** Optional alias — lets tests use cy.wait('@alias') to assert the request was made. */
  alias?: string;
};
