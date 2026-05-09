import { RegisterPo } from './register.po';

/**
 * E2E tests for the Register page.
 */
describe('Register page', () => {
  const po = new RegisterPo();

  beforeEach(() => {
    po.visit();
  });

  it('displays the registration form', () => {
    po.hasText('Create an Account');
    po.hasText('Create Account');
  });

  it('shows required-field errors when submitted empty', () => {
    po.submit();

    po.hasText('Required');           // first + last name fields
    po.hasText('Email is required');
    po.hasText('Phone number is required');
    po.hasText('Password is required');
  });

  it('shows an error when passwords do not match', () => {
    po
      .typeFirstName('John')
      .typeLastName('Doe')
      .typeEmail('john@example.com')
      .typePhone('0712345678')
      .typePassword('password123')
      .typeConfirmPassword('different123')
      .submit();

    po.hasText('Passwords do not match');
  });

  it('shows an error for an invalid phone number', () => {
    po.typePhone('123').submit();

    po.hasText('Enter a valid Kenyan number');
  });

  it('navigates back to the login page via the "Sign in" link', () => {
    cy.contains('Sign in').click();

    po.urlIncludes('/auth/login');
  });

  it('registers a new account and redirects to the login page', () => {
    const email = `testuser+${Date.now()}@example.com`;

    cy.env(['TEST_REGISTER_PASSWORD']).then(({ TEST_REGISTER_PASSWORD: password }) => {
      po.fillAndSubmit({
        firstName: 'Test',
        lastName: 'User',
        email,
        phone: '0712345678',
        password,
      });
    });

    po.urlIncludes('/auth/login');
  });
});
