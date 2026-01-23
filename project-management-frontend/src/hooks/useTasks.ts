import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tasksApi } from '../api/tasks';
import type { Task, TaskCreateRequest, TaskUpdateRequest } from '../types/task';
import { useToast } from './useToast';

export const useTasks = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const myTasksQuery = useQuery({
    queryKey: ['tasks', 'my-tasks'],
    queryFn: async () => {
      try {
        const response = await tasksApi.getMyTasks();
        const data = response.data;
        console.log('My tasks API response:', data);
        return Array.isArray(data) ? data : [];
      } catch (error: any) {
        console.error('Error fetching my tasks:', error);
        toast.error(error.response?.data?.message || 'Failed to load tasks');
        return [];
      }
    },
  });

  const tasksQuery = useQuery({
    queryKey: ['tasks', 'all'],
    queryFn: async () => {
      try {
        const response = await tasksApi.getAll();
        const data = response.data;
        console.log('All tasks API response:', data);
        return Array.isArray(data) ? data : [];
      } catch (error: any) {
        console.error('Error fetching all tasks:', error);
        toast.error(error.response?.data?.message || 'Failed to load tasks');
        return [];
      }
    },
  });

  const createTaskMutation = useMutation({
    mutationFn: (data: TaskCreateRequest) => 
      tasksApi.create(data).then(res => res.data),
    onSuccess: (newTask) => {
      queryClient.setQueryData(['tasks', 'my-tasks'], (old: Task[] = []) => [...old, newTask]);
      queryClient.setQueryData(['tasks', 'all'], (old: Task[] = []) => [...old, newTask]);
      toast.success('Task created successfully!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to create task');
    },
  });

  const updateTaskMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TaskUpdateRequest }) =>
      tasksApi.update(id, data).then(res => res.data),
    onSuccess: (updatedTask) => {
      queryClient.setQueryData(['tasks', 'my-tasks'], (old: Task[] = []) =>
        old.map(task => task.id === updatedTask.id ? updatedTask : task)
      );
      queryClient.setQueryData(['tasks', 'all'], (old: Task[] = []) =>
        old.map(task => task.id === updatedTask.id ? updatedTask : task)
      );
      toast.success('Task updated successfully!');
    },
  });

  const deleteTaskMutation = useMutation({
    mutationFn: (id: number) => tasksApi.delete(id),
    onSuccess: (_, id) => {
      queryClient.setQueryData(['tasks', 'my-tasks'], (old: Task[] = []) =>
        old.filter(task => task.id !== id)
      );
      queryClient.setQueryData(['tasks', 'all'], (old: Task[] = []) =>
        old.filter(task => task.id !== id)
      );
      toast.success('Task deleted successfully!');
    },
  });

  const refetchTasks = async () => {
    try {
      await Promise.all([
        myTasksQuery.refetch(),
        tasksQuery.refetch()
      ]);
    } catch (error) {
      console.error('Error refetching tasks:', error);
    }
  };

  return {
    tasks: tasksQuery.data || [],
    myTasks: myTasksQuery.data || [],
    isLoading: tasksQuery.isLoading || myTasksQuery.isLoading,
    refetch: refetchTasks,
    createTask: createTaskMutation.mutate,
    updateTask: updateTaskMutation.mutate,
    deleteTask: deleteTaskMutation.mutate,
    isCreating: createTaskMutation.isPending,
    isUpdating: updateTaskMutation.isPending,
    isDeleting: deleteTaskMutation.isPending,
  };
};