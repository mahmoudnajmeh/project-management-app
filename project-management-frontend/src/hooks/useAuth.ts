import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { usersApi } from '../api/users';
import { oauthApi } from '../api/oauth';
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

  const oauthLogin = async (provider: string) => {
    try {
      setIsLoading(true);
      const response = await oauthApi.getAuthorizationUrl(provider);
      window.location.href = response.data.url;
    } catch (error: any) {
      console.error('OAuth login error:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const handleOAuthRedirect = async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const username = urlParams.get('username');

    if (token && username) {
      localStorage.setItem('token', token);
      localStorage.setItem('username', username);
      await loadCurrentUser();
      return true;
    }
    return false;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
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
    
    const updatePayload: any = {};
    if (updatedData.firstName !== undefined) updatePayload.firstName = updatedData.firstName;
    if (updatedData.lastName !== undefined) updatePayload.lastName = updatedData.lastName;
    if (updatedData.email !== undefined) updatePayload.email = updatedData.email;
    if (updatedData.username !== undefined) updatePayload.username = updatedData.username;
    
    if (Object.keys(updatePayload).length > 0) {
      try {
        const response = await usersApi.updateUser(updatePayload);
        
        // If username changed, log out and require re-login
        if (updatedData.username && updatedData.username !== previousUser?.username) {
          // Show message to user
          alert('Username changed! Please log in again with your new username.');
          // Clear token and log out
          localStorage.removeItem('token');
          localStorage.removeItem('username');
          localStorage.removeItem('email');
          setUser(null);
          navigate('/login');
          return null;
        }
        
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
    oauthLogin,
    handleOAuthRedirect,
    logout,
    getToken,
    isLoading,
    initialLoad,
    updateUser,
    refreshUser: loadCurrentUser,
  };
};