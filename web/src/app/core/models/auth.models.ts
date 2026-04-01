export type AuthRequest = {
  phoneNumber: string,
  code?: string
}

export type AccessToken = {
  accessToken: string
}

export type JWTPayload = {
  exp?: number;
  [key: string]: any;
};

export type ForgotPasswordPayload = {
  phoneNumber: string
}

export type ResetPasswordPayload = {
  token: string,
  newPassword: string
}
