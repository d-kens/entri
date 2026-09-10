import { GlobalErrorHandler } from './global-error-handler';

describe('GlobalErrorHandler', () => {
  let handler: GlobalErrorHandler;
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    handler = new GlobalErrorHandler();
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  it('logs the message of an Error instance', () => {
    const error = new Error('boom');

    handler.handleError(error);

    expect(consoleErrorSpy).toHaveBeenCalledWith('[GlobalErrorHandler]', 'boom', error);
  });

  it('stringifies and logs a non-Error thrown value', () => {
    handler.handleError('a plain string failure');

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      '[GlobalErrorHandler]',
      'a plain string failure',
      'a plain string failure',
    );
  });

  it('does not throw when handling an error', () => {
    expect(() => handler.handleError(new Error('boom'))).not.toThrow();
  });
});
