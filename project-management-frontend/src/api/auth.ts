import api from '.';
import type { LoginRequest, LoginResponse, RegisterRequest, AuthUser } from '../types/auth';

export const authApi = {
  login: (data: LoginRequest) => 
    api.post<LoginResponse>('/auth/login', data), 
    
  register: (data: RegisterRequest) => 
    api.post<AuthUser>('/auth/register', data), 
    
  getCurrentUser: () => 
    api.get<AuthUser>('/users/me'), 
};