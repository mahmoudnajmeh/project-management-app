import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { usersApi } from '../api/users';
import type { AuthUser } from '../types/auth';

export const useAuth = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [user, setUser] = useState<AuthUser | null>(null); 
  const [initialLoad, setInitialLoad] = useState(true);
  const navigate = useNavigate();

  const loadCurrentUser = useCallback(async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setInitialLoad(false);
      return;
    }

    try {
      setIsLoading(true);
      const response = await usersApi.getCurrentUser();
      setUser(response.data);
    } catch (error: any) {
      console.error('Failed to load user:', error);
    } finally {
      setIsLoading(false);
      setInitialLoad(false);
    }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      loadCurrentUser();
    } else {
      setInitialLoad(false);
    }
  }, [loadCurrentUser]);

  const login = async (username: string, password: string) => {
    setIsLoading(true);
    try {
      const response = await authApi.login({ username, password });
      const token = response.data.token;
      if (!token) {
        throw new Error('No token received from server');
      }
      
      localStorage.setItem('token', token);
      await loadCurrentUser();
      
      return response.data;
    } catch (error: any) {
      console.error('Login error:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => {
    setIsLoading(true);
    try {
      const response = await authApi.register(data);
      return response.data;
    } catch (error: any) {
      console.error('Register error:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null); 
    navigate('/login');
  };

  const getToken = () => {
    return localStorage.getItem('token');
  };

  const updateUser = async (updatedData: Partial<AuthUser>) => {
    try {
      setIsLoading(true);
      
      const previousUser = user;
      
      setUser(prev => prev ? { ...prev, ...updatedData } : null);
      
      if (updatedData.firstName || updatedData.lastName || updatedData.email) {
        const updatePayload = {
          firstName: updatedData.firstName,
          lastName: updatedData.lastName,
          email: updatedData.email,
        };
        
        try {
          const response = await usersApi.updateUser(updatePayload);
          setUser(response.data);
          return response.data;
        } catch (error) {
          console.error('API update failed, reverting:', error);
          setUser(previousUser);
          throw error;
        }
      } else {
        return user;
      }
    } catch (error) {
      console.error('Failed to update user:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    user,
    login,
    register,
    logout,
    getToken,
    isLoading,
    initialLoad,
    updateUser,
    refreshUser: loadCurrentUser,
  };
};