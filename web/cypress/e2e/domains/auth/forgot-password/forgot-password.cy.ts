import { ForgotPasswordPo } from './forgot-password.po';

/**
 * E2E test for the Forget Password Page
 */

describe('Forgot Password Page', () => {
  const po = new ForgotPasswordPo();

  describe("validation", () => {
    it('shows required error when submitted empty', () => {
      po.visit().submit();
      po.hasText('Email is required')
    })

    it('shows format error for an invalid email', () => {
      po.visit().typeEmail('notanemail').submit();
      po.hasText('Enter a valid email')
    })
  })

  describe('navigation', () => {
    it('goes back to login when the link is clicked', () => {
      po.visit();
      cy.contains('Back to sign in').click();
      po.urlIncludes('/auth/login')
    })
  })

  describe('success', () => {
    it('shows confirmation and redirects to login after submitting a valid email', () => {
      po.visit().typeEmail('test@example.com').submit();
      po.hasText('A reset link has successfully been sent');
      po.urlIncludes('/auth/login');
    })
  })
})
