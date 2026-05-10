import { BasePo } from '../../../../support/base.po';

export class RegisterPo extends BasePo {
  visit() {
    cy.visit('/auth/register');
    return this;
  }

  typeFirstName(name: string) {
    cy.get('[data-cy="first-name-input"]').type(name);
    return this;
  }

  typeLastName(name: string) {
    cy.get('[data-cy="last-name-input"]').type(name);
    return this;
  }

  typeEmail(email: string) {
    cy.get('[data-cy="email-input"]').type(email);
    return this;
  }

  typePhone(phone: string) {
    cy.get('[data-cy="phone-input"]').type(phone);
    return this;
  }

  typePassword(password: string) {
    cy.get('[data-cy="password-input"]').type(password);
    return this;
  }

  typeConfirmPassword(password: string) {
    cy.get('[data-cy="confirm-password-input"]').type(password);
    return this;
  }

  submit() {
    cy.get('[data-cy="register-submit"]').click();
    return this;
  }
}
