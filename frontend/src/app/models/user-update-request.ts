export type RegularUserUpdatePayload = {
  userName: string;
  password: string;
};

export type AdminUserUpdatePayload = {
  userName: string;
  password: string;
  email: string;
  role: string;
};

export function mapToRegularUserUpdatePayload(formValues: any): RegularUserUpdatePayload {
  return {
    userName: formValues.userName,
    password: formValues.password,
  };
}

export function mapToAdminUserUpdatePayload(formValues: any): AdminUserUpdatePayload {
  return {
    userName: formValues.userName,
    password: formValues.password,
    email: formValues.email,
    role: formValues.role,
  };
}
