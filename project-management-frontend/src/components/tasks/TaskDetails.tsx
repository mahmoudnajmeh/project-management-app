import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { tasksApi } from '../../api/tasks';
import { useToast } from '../../hooks/useToast';
import Card from '../common/Card';
import Button from '../common/Button';
import LoadingSpinner from '../common/LoadingSpinner';
import { ChevronLeft } from 'lucide-react';

const TaskDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { error } = useToast();
  const [task, setTask] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchTask = async () => {
      if (!id) return;
      try {
        setIsLoading(true);
        const response = await tasksApi.getById(parseInt(id));
        setTask(response.data);
      } catch (err: any) {
        console.error('Error loading task:', err);
        error('Failed to load task');
        navigate('/tasks');
      } finally {
        setIsLoading(false);
      }
    };

    fetchTask();
  }, [id, navigate, error]);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!task) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-600 dark:text-gray-400">Task not found</p>
        <Button onClick={() => navigate('/tasks')} className="mt-4">
          Back to Tasks
        </Button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <Button
        variant="ghost"
        onClick={() => navigate('/tasks')}
        className="mb-4"
      >
        <ChevronLeft className="h-4 w-4 mr-2" />
        Back to Tasks
      </Button>

      <Card>
        <div className="p-6">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
            {task.title}
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            {task.description}
          </p>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Status</p>
              <p className="font-medium">{task.status}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Priority</p>
              <p className="font-medium">{task.priority}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Due Date</p>
              <p className="font-medium">{new Date(task.dueDate).toLocaleDateString()}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Assigned To</p>
              <p className="font-medium">
                {task.assignedUser?.firstName} {task.assignedUser?.lastName}
              </p>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default TaskDetails;