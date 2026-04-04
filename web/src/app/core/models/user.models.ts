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
