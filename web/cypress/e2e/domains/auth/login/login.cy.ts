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
    const email = `testlogin+${Date.now()}@example.com`;
    const password = 'Password123!';

    before(() => {
      cy.env(['API_BASE_URL']).then(({ API_BASE_URL: apiBaseUrl }) => {
        cy.request('POST', `${apiBaseUrl}/auth/register`, {
          firstName: 'Test',
          lastName: 'Login',
          email,
          phoneNumber: '0712345678',
          password,
          role: 'PLATFORM_USER',
        });
      });
    });

    it('navigates to the dashboard after a successful login', () => {
      po.visit().typeEmail(email).typePassword(password).submit();
      cy.url().should('not.include', '/auth');
    });
  });
});
