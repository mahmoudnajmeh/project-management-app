// src/types/auth.ts
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  message?: string;
  username?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  profilePictureUrl?: string;
  profilePictureFileName?: string | null;
  profilePictureContentType?: string | null;
  profilePictureSize?: number | null;
  profilePicturePath?: string | null;
  createdAt: string;
  updatedAt: string;
}
