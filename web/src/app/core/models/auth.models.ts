export type AuthRequest = {
  phoneNumber: string,
  password: string
}


export type AuthResponse = {
  accessToken: string,
  externalId: string,
  roles: string[],
  name: string,
}
