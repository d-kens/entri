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
  expiresIn: number,
  user: {
    role: string,
    email: string,
    externalKey: string,
  }
}

export type ForgotPasswordRequest = {
  phoneNumber: string
}

export type ResetPasswordRequest = {
  phoneNumber: string,
  otp: string,
  newPassword: string
}
