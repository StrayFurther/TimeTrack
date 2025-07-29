export interface UserLoginPayload {
  email: string;
  password: string;
}

export function mapToUserLoginPayload(formValues: any): UserLoginPayload {
  return {
    email: formValues.email,
    password: formValues.password,
  };
}
