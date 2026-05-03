export type RegisterUserRequest = {
  firstName: string,
  lastName: string,
  email: string,
  phoneNumber: string,
  password: string,
  role: string
}

export type AuthRequest = {
  email: string,
  password: string
}

export type AuthResponse = {
  accessToken: string,
  externalId: string,
  roles: string[],
  permissions: string[],
  name: string,
}

export type ForgotPasswordRequest = {
  phoneNumber: string
}

export type ResetPasswordRequest = {
  phoneNumber: string,
  otp: string,
  newPassword: string
}
