export type CreateUserRequest = {
  userName: string,
  password: string,
  phoneNumber: string,
  email: string,
  role: string
}


export type UserResponse = {
  id: number,
  role: string,
  email: string,
  userName: string,
  phoneNumber: string
}
