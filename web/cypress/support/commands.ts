/// <reference types="cypress" />

import type { MswHandler } from './msw/handler';

/**
 * cy.startMsw(handlers)
 *
 * Registers mock API responses for the current test using cy.intercept().
 * Call this in beforeEach() before cy.visit() so intercepts are in place
 * before the page makes any requests.
 *
 * Always pair with cy.resetMsw() in afterEach() to keep tests isolated.
 */
Cypress.Commands.add('startMsw', (handlers: MswHandler[]) => {
  for (const handler of handlers) {
    const intercept = cy.intercept(handler.method, handler.url, {
      statusCode: handler.statusCode,
      body: handler.body,
    });
    if (handler.alias) {
      intercept.as(handler.alias);
    }
  }
});

/**
 * cy.resetMsw()
 *
 * Semantic cleanup pair to cy.startMsw(). Call this in afterEach() to
 * signal that mock overrides for this test are done.
 *
 * Cypress clears intercepts automatically between tests, so this is
 * primarily a readability and intent marker — it makes it explicit that
 * a test is cleaning up after itself.
 */
Cypress.Commands.add('resetMsw', () => {
  // Intercepts are cleared automatically between Cypress tests.
  // This command exists as an explicit, readable cleanup signal.
});

declare global {
  namespace Cypress {
    interface Chainable {
      startMsw(handlers: MswHandler[]): void;
      resetMsw(): void;
    }
  }
}
