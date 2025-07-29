export type UserDetailResponse = {
  userName: string;
  email: string;
  role: string;
};

export type CompleteUserDetailResponse = {
  userDetails: UserDetailResponse;
  profilePic: Blob | null;
}
