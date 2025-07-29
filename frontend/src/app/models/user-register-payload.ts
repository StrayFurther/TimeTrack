export interface RegisterUserPayload {
  email: string;
  password: string;
  userName: string;
}

export function mapToRegisterUserPayload(formValues: any): RegisterUserPayload {
  return {
    email: formValues.email,
    password: formValues.password,
    userName: formValues.username,
  };
}
