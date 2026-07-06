import { UserResponse } from './user.models';

export type RegisterUserRequest = {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
  role: string;
};

export type AuthRequest = {
  email: string;
  password: string;
};

export type AuthResponse = {
  user: UserResponse;
  accessToken: AccessTokenResponse;
};

export type AccessTokenResponse = {
  token: string;
  expiresIn: number;
};

export type ResetPasswordRequest = {
  token: string;
  password: string;
};
