export type AuthUser = {
  id: string;
  email?: string | null;
  lineUserId?: string | null;
  authProvider?: string;
  displayName?: string | null;
  name?: string | null;
  phone?: string | null;
  defaultPickupLocation?: string | null;
  profileImageUrl?: string | null;
  isAdmin: boolean;
  createdAt?: string;
};

export type AuthSession = {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
};
