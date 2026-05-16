import { useState, useEffect } from 'react';
import { usersApi } from '../api/users';
import { useToast } from './useToast';

export const useTeam = () => {
  const [members, setMembers] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { error } = useToast();

  useEffect(() => {
    fetchTeamMembers();
  }, []);

  const fetchTeamMembers = async () => {
    try {
      setIsLoading(true);
      const response = await usersApi.getAllUsers();
      setMembers(response.data);
    } catch (err: any) {
      error('Failed to load team members');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return {
    members,
    isLoading,
    refetch: fetchTeamMembers,
  };
};