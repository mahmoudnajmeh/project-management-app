export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  profilePictureUrl?: string;
  profilePictureFileName?: string;
  profilePictureContentType?: string;
  profilePictureSize?: number;
  createdAt: string;
  updatedAt: string;
}

export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
}

export interface ProfilePictureResponse {
  message: string;
  fileName: string;
  fileUrl: string;
}