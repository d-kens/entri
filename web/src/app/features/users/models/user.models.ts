export type UserResponse = {
  role: string;
  email: string;
  externalKey: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  enabled: boolean;
};

export type UpdateUserRequest = {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
};

export type ChangePasswordRequest = {
  currentPassword: string;
  newPassword: string;
};
