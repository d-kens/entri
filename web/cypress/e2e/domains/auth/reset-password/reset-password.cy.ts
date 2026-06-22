import { ResetPasswordPo } from './reset-password.po';

describe('Reset Password Page', () => {
  const resetPasswordPo = new ResetPasswordPo();

  describe('validation', () => {
    it('shows a required-field error when submitted empty', () => {
      resetPasswordPo.visit().submit();
      resetPasswordPo.hasText('Password is required');
    });

    it('shows an error when passwords do not match', () => {
      resetPasswordPo
        .visit()
        .typePassword('password123')
        .typeConfirmPassword('differentPassword123')
        .submit();
      resetPasswordPo.hasText('Passwords do not match');
    });

    it('show a minLength error when password is fewer than 8 characters', () => {
      resetPasswordPo.visit().typePassword('short').submit();
      resetPasswordPo.hasText('Must be at least 8 characters');
    });

    it('show a required error when confirm password is empty', () => {
      resetPasswordPo.visit().typePassword('password123').submit();
      resetPasswordPo.hasText('Please confirm your password');
    });
  });

  describe('submission', () => {
    it('sends the token from the URL in the request body', () => {
      cy.intercept('POST', '**/auth/reset-password', (req) => {
        expect(req.body.token).to.equal('test-token');
        req.reply({ statusCode: 200 });
      }).as('resetPassword');

      resetPasswordPo
        .visit()
        .typePassword('password123')
        .typeConfirmPassword('password123')
        .submit();

      cy.wait('@resetPassword');
    });

    it('disables the submit button and shows loading text while request is in-flight', () => {
      cy.intercept('POST', '**/auth/reset-password', (req) => {
        req.reply({ delay: 500, statusCode: 200 });
      }).as('resetPassword');

      resetPasswordPo
        .visit()
        .typePassword('password123')
        .typeConfirmPassword('password123')
        .submit();

      cy.get('[data-cy="reset-password-submit"]').should('be.disabled');
      resetPasswordPo.hasText('Resetting Password...');
      cy.wait('@resetPassword');
    });

    it('redirects to forgot-password page when token is expired or invalid', () => {
      cy.intercept('POST', '**/auth/reset-password', { statusCode: 401 }).as('resetPassword');

      resetPasswordPo
        .visit()
        .typePassword('password123')
        .typeConfirmPassword('password123')
        .submit();

      cy.wait('@resetPassword');
      cy.url().should('include', '/auth/forgot-password');
    });

    it('redirects to login and shows success message on valid submission', () => {
      cy.intercept('POST', '**/auth/reset-password', { statusCode: 200 }).as('resetPassword');

      resetPasswordPo
        .visit()
        .typePassword('strongPassword3456')
        .typeConfirmPassword('strongPassword3456')
        .submit();

      cy.wait('@resetPassword');
      cy.url().should('include', '/auth/login');
      resetPasswordPo.hasText('Password reset successfully!');
    });
  });
});
