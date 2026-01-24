export type AuthRequest = {
  email: string,
  password: string
}

export type AccessToken = {
  accessToken: string
}

export type JWTPayload = {
  exp?: number;
  [key: string]: any;
};

export type ResetPasswordPayload = {
  token: string,
  newPassword: string
}
