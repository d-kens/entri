import { LoginPo } from './login.po';

/**
 * E2E tests for the Login page.
 *
 * These run in a real browser against the running Angular app (localhost:4200).
 * They test what a real user would see and do — not internal component state.
 *
 * The LoginPo hides all cy.get() calls so this file only describes behaviour.
 */
describe('Login page', () => {
  const po = new LoginPo();

  beforeEach(() => {
    po.visit();
  });

  it('displays the login form', () => {
    po.hasText('Welcome Back');
    po.hasText('Sign In');
  });

  it('shows required-field errors when submitted empty', () => {
    po.submit();

    po.hasText('Email is required');
    po.hasText('Password is required');
  });

  it('shows an error for an invalid email format', () => {
    po.typeEmail('notanemail').submit();

    po.hasText('Enter a valid email');
  });

  it('navigates to the register page via the "Create one" link', () => {
    cy.contains('Create one').click();

    po.urlIncludes('/auth/register');
  });

  it('navigates to the dashboard after a successful login', () => {
    cy.env(['TEST_USER_EMAIL', 'TEST_USER_PASSWORD']).then(({ TEST_USER_EMAIL: email, TEST_USER_PASSWORD: password }) => {
      po.typeEmail(email).typePassword(password).submit();
    });

    po.urlIncludes('/');
    cy.url().should('not.include', '/auth');
  });
});
