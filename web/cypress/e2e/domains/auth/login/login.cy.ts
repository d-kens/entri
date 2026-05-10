import { LoginPo } from './login.po';

describe('Login page', () => {
  const po = new LoginPo();

  describe('validation', () => {
    it('shows required-field errors when submitted empty', () => {
      po.visit().submit();
      po.hasText('Email is required');
      po.hasText('Password is required');
    });

    it('shows an error for an invalid email format', () => {
      po.visit().typeEmail('notanemail').submit();
      po.hasText('Enter a valid email');
    });
  });

  describe('navigation', () => {
    it('navigates to the register page via the "Create account" link', () => {
      po.visit();
      cy.contains('Create account').click();
      po.urlIncludes('/auth/register');
    });

    it('navigates to the forgot password page via the "Forgot password?" link', () => {
      po.visit();
      cy.contains('Forgot password?').click();
      po.urlIncludes('/auth/forgot-password');
    });
  });

  describe('success', () => {
    it('navigates to the dashboard after a successful login', () => {
      cy.env(['TEST_USER_EMAIL', 'TEST_USER_PASSWORD']).then(({ TEST_USER_EMAIL: email, TEST_USER_PASSWORD: password }) => {
        po.visit().typeEmail(email).typePassword(password).submit();
        cy.url().should('not.include', '/auth');
      });
    });
  });
});
