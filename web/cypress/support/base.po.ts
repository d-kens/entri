/**
 * Base Page Object — every PO extends this.
 * Provides shared, reusable assertions so individual POs stay focused
 * on their own page's interactions.
 *
 * Usage:
 *   class LoginPo extends BasePo { ... }
 *   new LoginPo().visit().hasText('Welcome Back').urlIncludes('/auth/login');
 */
export abstract class BasePo {
  /** Assert the page body is visible (basic liveness check). */
  isVisible() {
    cy.get('body').should('be.visible');
    return this;
  }

  /** Assert that some text appears somewhere on the page. */
  hasText(text: string) {
    cy.contains(text).should('be.visible');
    return this;
  }

  /** Assert the current URL contains the given path segment. */
  urlIncludes(path: string) {
    cy.url().should('include', path);
    return this;
  }
}
