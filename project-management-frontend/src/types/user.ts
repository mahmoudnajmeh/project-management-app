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
  profilePicturePath?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ProfilePictureResponse {
  message: string;
  fileName: string;
  fileUrl: string;
}

export interface InvitationRequest {
  email: string;
  role: string;
}