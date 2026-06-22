import { LoginPo } from '../login/login.po';
import { RegisterPo } from './register.po';

describe('Register page', () => {
  const po = new RegisterPo();
  const loginPo = new LoginPo();

  describe('validation', () => {
    it('shows required-field errors when submitted empty', () => {
      po.visit().submit();
      po.hasText('Required');
      po.hasText('Email is required');
      po.hasText('Phone number is required');
      po.hasText('Password is required');
    });

    it('shows an error for an invalid email format', () => {
      po.visit().typeEmail('notanemail').submit();
      po.hasText('Enter a valid email');
    });

    it('shows an error for an invalid phone number', () => {
      po.visit().typePhone('123').submit();
      po.hasText('Enter a valid Kenyan number');
    });

    it('shows an error when passwords do not match', () => {
      po.visit()
        .typeFirstName('John')
        .typeLastName('Doe')
        .typeEmail('john@example.com')
        .typePhone('0712345678')
        .typePassword('password123')
        .typeConfirmPassword('different123')
        .submit();
      po.hasText('Passwords do not match');
    });

    describe('already-registered email', () => {
      const existingEmail = `existing+${Date.now()}@example.com`;

      before(() => {
        cy.env(['API_BASE_URL', 'TEST_REGISTER_PASSWORD']).then(
          ({ API_BASE_URL: apiBaseUrl, TEST_REGISTER_PASSWORD: password }) => {
            cy.request('POST', `${apiBaseUrl}/auth/register`, {
              firstName: 'Test',
              lastName: 'User',
              email: existingEmail,
              phoneNumber: '0712345678',
              password,
              role: 'PLATFORM_USER',
            });
          },
        );
      });

      it('shows an error for an already-registered email', () => {
        cy.env(['TEST_REGISTER_PASSWORD']).then(({ TEST_REGISTER_PASSWORD: password }) => {
          po.visit()
            .typeFirstName('Test')
            .typeLastName('User')
            .typeEmail(existingEmail)
            .typePhone('0712345678')
            .typePassword(password)
            .typeConfirmPassword(password)
            .submit();
          po.urlIncludes('/auth/register');
        });
      });
    });
  });

  describe('navigation', () => {
    it('navigates to the login page via the "Sign in" link', () => {
      po.visit();
      cy.contains('Sign in').click();
      po.urlIncludes('/auth/login');
    });
  });

  describe('success', () => {
    it('registers a new account and logs in with the new credentials', () => {
      cy.env([
        'TEST_REGISTER_FIRST_NAME',
        'TEST_REGISTER_LAST_NAME',
        'TEST_REGISTER_PHONE',
        'TEST_REGISTER_PASSWORD',
      ]).then(
        ({
          TEST_REGISTER_FIRST_NAME: firstName,
          TEST_REGISTER_LAST_NAME: lastName,
          TEST_REGISTER_PHONE: phone,
          TEST_REGISTER_PASSWORD: password,
        }) => {
          const email = `testuser+${Date.now()}@example.com`;

          po.visit()
            .typeFirstName(firstName)
            .typeLastName(lastName)
            .typeEmail(email)
            .typePhone(phone)
            .typePassword(password)
            .typeConfirmPassword(password)
            .submit();
          po.urlIncludes('/auth/login');

          loginPo.typeEmail(email).typePassword(password).submit();
          cy.url().should('not.include', '/auth');
        },
      );
    });
  });
});
