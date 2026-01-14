export interface AuthRequest {
  email: string,
  password: string
}

export interface AccessToken {
  accessToken: string
}

export type JWTPayload = {
  exp?: number;
  [key: string]: any;
};
