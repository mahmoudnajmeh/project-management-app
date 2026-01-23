import api from '.';
import type { AuthUser } from '../types/auth';
import type { ProfilePictureResponse } from '../types/user';
import type { UserUpdateRequest } from '../types/user';

export const usersApi = {
  getCurrentUser: () => 
    api.get<AuthUser>('/users/me'),
    
  getAllUsers: () =>
    api.get<AuthUser[]>('/users'),
    
  updateUser: (data: UserUpdateRequest) => 
    api.put<AuthUser>('/users/me', data),
    
  deleteUser: () => 
    api.delete('/users/me'),
    
  uploadProfilePicture: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    
    return api.post<ProfilePictureResponse>('/users/me/profile-picture', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  
  deleteProfilePicture: () => 
    api.delete('/users/me/profile-picture'),
    
  sendInvite: (data: { email: string; role: string }) =>
    api.post('/users/invite', data),
};