import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectsApi } from '../api/projects';
import type { Project, ProjectCreateRequest, ProjectUpdateRequest } from '../types/project';
import { useToast } from './useToast';

export const useProjects = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const projectsQuery = useQuery({
    queryKey: ['projects'],
    queryFn: async () => {
      try {
        const response = await projectsApi.getAll();
        return response.data || [];
      } catch (error) {
        console.error('Error fetching projects:', error);
        return [];
      }
    },
  });

  const myProjectsQuery = useQuery({
    queryKey: ['my-projects'],
    queryFn: async () => {
      try {
        const response = await projectsApi.getMyProjects();
        return response.data || [];
      } catch (error) {
        console.error('Error fetching my projects:', error);
        return [];
      }
    },
  });

  const createProjectMutation = useMutation({
    mutationFn: (data: ProjectCreateRequest) => 
      projectsApi.create(data).then(res => res.data),
    onSuccess: (newProject) => {
      queryClient.setQueryData(['projects'], (old: Project[] = []) => [...old, newProject]);
      queryClient.setQueryData(['my-projects'], (old: Project[] = []) => [...old, newProject]);
      toast.success('Project created successfully!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to create project');
    },
  });

  const updateProjectMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ProjectUpdateRequest }) =>
      projectsApi.update(id, data).then(res => res.data),
    onSuccess: (updatedProject) => {
      queryClient.setQueryData(['projects'], (old: Project[] = []) =>
        old.map(project => project.id === updatedProject.id ? updatedProject : project)
      );
      queryClient.setQueryData(['my-projects'], (old: Project[] = []) =>
        old.map(project => project.id === updatedProject.id ? updatedProject : project)
      );
      toast.success('Project updated successfully!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to update project');
    },
  });

  const deleteProjectMutation = useMutation({
    mutationFn: (id: number) => projectsApi.delete(id),
    onSuccess: (_, id) => {
      queryClient.setQueryData(['projects'], (old: Project[] = []) =>
        old.filter(project => project.id !== id)
      );
      queryClient.setQueryData(['my-projects'], (old: Project[] = []) =>
        old.filter(project => project.id !== id)
      );
      toast.success('Project deleted successfully!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to delete project');
    },
  });

  return {
    projects: projectsQuery.data || [],
    myProjects: myProjectsQuery.data || [],
    isLoading: projectsQuery.isLoading || myProjectsQuery.isLoading,
    error: projectsQuery.error || myProjectsQuery.error,
    createProject: createProjectMutation.mutate,
    updateProject: updateProjectMutation.mutate,
    deleteProject: deleteProjectMutation.mutate,
    isCreating: createProjectMutation.isPending,
    isUpdating: updateProjectMutation.isPending,
    isDeleting: deleteProjectMutation.isPending,
  };
};