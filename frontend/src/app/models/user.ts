export enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER',
}

export type User = {
  userName: string;
  email: string;
  role: Role;
  profilePic?: string | null;
};

export function stringToEnum(value: string): Role | undefined {
  if (Object.values(Role).includes(value as Role)) {
    return value as Role;
  }
  return undefined; // Return undefined if the string doesn't match any enum value
}

export function getRoleEnumValues(): string[] {
  return Object.values(Role).filter(value => typeof value === 'string');
}
