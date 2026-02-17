import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Calendar, User } from 'lucide-react';
import { useTasks } from '../../hooks/useTasks';
import { useAuth } from '../../hooks/useAuth';
import Input from '../common/Input';
import Button from '../common/Button';
import type { Priority, TaskCreateRequest, TaskUpdateRequest } from '../../types/task';
import api from '../../api';

const taskSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().min(1, 'Description is required'),
  projectId: z.string().min(1, 'Project is required'),
  assignedUserId: z.string().min(1, 'Assignee is required'),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'URGENT']),
  dueDate: z.string().optional(),
});

type TaskFormData = z.infer<typeof taskSchema>;

interface TaskFormProps {
  task?: any;
  onSuccess?: () => void;
}

interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}

const TaskForm: React.FC<TaskFormProps> = ({ task, onSuccess }) => {
  const { createTask, updateTask, isCreating, isUpdating } = useTasks();
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(false);
  
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<TaskFormData>({
    resolver: zodResolver(taskSchema),
    defaultValues: task ? {
      title: task.title,
      description: task.description,
      projectId: task.project?.id?.toString() || '1',
      assignedUserId: task.assignedUser?.id?.toString() || currentUser?.id?.toString() || '',
      priority: task.priority || 'MEDIUM',
      dueDate: task.dueDate ? new Date(task.dueDate).toISOString().split('T')[0] : '',
    } : {
      title: '',
      description: '',
      projectId: '1',
      assignedUserId: currentUser?.id?.toString() || '',
      priority: 'MEDIUM',
      dueDate: '',
    },
  });

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setIsLoadingUsers(true);
      const response = await api.get('/users');
      setUsers(response.data || []);
    } catch (error) {
      console.error('Failed to load users:', error);
    } finally {
      setIsLoadingUsers(false);
    }
  };

  const onSubmit = async (data: TaskFormData) => {
    try {
      const taskData: TaskCreateRequest | TaskUpdateRequest = {
        title: data.title,
        description: data.description,
        projectId: parseInt(data.projectId),
        assignedUserId: parseInt(data.assignedUserId),
        priority: data.priority as Priority,
      };
      
      if (data.dueDate) {
        taskData.dueDate = `${data.dueDate}T23:59:59`;
      }
      
      if (task) {
        await updateTask({ id: task.id, data: taskData as TaskUpdateRequest });
      } else {
        await createTask(taskData as TaskCreateRequest);
      }
      onSuccess?.();
    } catch (error: any) {
      console.error('Failed to save task:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        {...register('title')}
        label="Task Title"
        placeholder="Enter task title"
        error={errors.title?.message}
      />
      
      <div>
        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          Description
        </label>
        <textarea
          {...register('description')}
          rows={3}
          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
          placeholder="Describe the task..."
        />
        {errors.description && (
          <p className="mt-1 text-sm text-red-600 dark:text-red-400">
            {errors.description.message}
          </p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Input
          {...register('projectId')}
          label="Project ID"
          placeholder="Enter project ID"
          defaultValue="1"
          error={errors.projectId?.message}
        />
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Priority
          </label>
          <select
            {...register('priority')}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
          {errors.priority && (
            <p className="mt-1 text-sm text-red-600 dark:text-red-400">
              {errors.priority.message}
            </p>
          )}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Assign To
          </label>
          <div className="relative">
            <div className="absolute left-3 top-1/2 transform -translate-y-1/2">
              <User className="h-4 w-4 text-gray-400" />
            </div>
            <select
              {...register('assignedUserId')}
              className="w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
              disabled={isLoadingUsers}
            >
              <option value="">Select user...</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.firstName} {user.lastName} ({user.username})
                </option>
              ))}
            </select>
          </div>
          {errors.assignedUserId && (
            <p className="mt-1 text-sm text-red-600 dark:text-red-400">
              {errors.assignedUserId.message}
            </p>
          )}
        </div>
        
        <Input
          {...register('dueDate')}
          type="date"
          label="Due Date"
          leftIcon={<Calendar className="h-4 w-4 text-gray-400" />}
          error={errors.dueDate?.message}
        />
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <Button
          type="button"
          variant="secondary"
          onClick={onSuccess}
        >
          Cancel
        </Button>
        <Button
          type="submit"
          isLoading={isCreating || isUpdating}
        >
          {task ? 'Update Task' : 'Create Task'}
        </Button>
      </div>
    </form>
  );
};

export default TaskForm;