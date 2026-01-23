import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Calendar } from 'lucide-react';
import { useProjects } from '../../hooks/useProjects';
import Input from '../common/Input';
import Button from '../common/Button';
import type { ProjectCreateRequest, ProjectUpdateRequest, ProjectStatus } from '../../types/project';

const projectSchema = z.object({
  name: z.string().min(1, 'Project name is required'),
  description: z.string().min(1, 'Description is required'),
  status: z.enum(['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']).optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
});

type ProjectFormData = z.infer<typeof projectSchema>;

interface ProjectFormProps {
  project?: any;
  onSuccess?: () => void;
}

const ProjectForm: React.FC<ProjectFormProps> = ({ project, onSuccess }) => {
  const { createProject, updateProject, isCreating, isUpdating } = useProjects();
  
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProjectFormData>({
    resolver: zodResolver(projectSchema),
    defaultValues: project ? {
      name: project.name,
      description: project.description,
      status: project.status || 'PLANNED',
      startDate: project.startDate ? new Date(project.startDate).toISOString().split('T')[0] : '',
      endDate: project.endDate ? new Date(project.endDate).toISOString().split('T')[0] : '',
    } : {
      name: '',
      description: '',
      status: 'PLANNED',
      startDate: '',
      endDate: '',
    },
  });

  const onSubmit = async (data: ProjectFormData) => {
    try {
      const projectData: ProjectCreateRequest | ProjectUpdateRequest = {
        name: data.name,
        description: data.description,
        status: data.status as ProjectStatus,
      };
      
      if (data.startDate) {
        projectData.startDate = `${data.startDate}T00:00:00`;
      }
      
      if (data.endDate) {
        projectData.endDate = `${data.endDate}T23:59:59`;
      }

      if (project) {
        await updateProject({ id: project.id, data: projectData as ProjectUpdateRequest });
      } else {
        await createProject(projectData as ProjectCreateRequest);
      }
      onSuccess?.();
    } catch (error: any) {
      console.error('Failed to save project:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        {...register('name')}
        label="Project Name"
        placeholder="Enter project name"
        error={errors.name?.message}
      />
      
      <div>
        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          Description
        </label>
        <textarea
          {...register('description')}
          rows={3}
          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
          placeholder="Describe the project..."
        />
        {errors.description && (
          <p className="mt-1 text-sm text-red-600 dark:text-red-400">
            {errors.description.message}
          </p>
        )}
      </div>

      {project && (
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Status
          </label>
          <select
            {...register('status')}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
          >
            <option value="PLANNED">Planned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
          {errors.status && (
            <p className="mt-1 text-sm text-red-600 dark:text-red-400">
              {errors.status.message}
            </p>
          )}
        </div>
      )}

      <div className="grid grid-cols-2 gap-4">
        <Input
          {...register('startDate')}
          type="date"
          label="Start Date"
          leftIcon={<Calendar className="h-4 w-4 text-gray-400" />}
          error={errors.startDate?.message}
        />
        <Input
          {...register('endDate')}
          type="date"
          label="End Date"
          leftIcon={<Calendar className="h-4 w-4 text-gray-400" />}
          error={errors.endDate?.message}
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
          {project ? 'Update Project' : 'Create Project'}
        </Button>
      </div>
    </form>
  );
};

export default ProjectForm;