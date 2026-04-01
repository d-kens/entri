export type RegisterMerchantRequest = {
  name: string,
  phoneNumber: string
}

export type UserResponse = {
  externalId: string,
  name: string,
  phoneNumber: string,
  roles: string[]
}
