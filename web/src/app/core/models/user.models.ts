export type RegisterMerchantRequest = {
  name: string,
  phoneNumber: string,
  password: string
}

export type UserResponse = {
  externalId: string,
  name: string,
  phoneNumber: string,
  roles: string[]
}

export type UpdateProfileRequest = {
  name: string,
}

export type ChangePasswordRequest = {
  currentPassword: string,
  newPassword: string,
}
