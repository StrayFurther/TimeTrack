export type UserDetailResponse = {
  userName: string;
  email: string;
  role: string;
  profilePic?: string | null;
};

export type CompleteUserDetailResponse = {
  userDetails: UserDetailResponse;
  profilePic: Blob | null;
}
