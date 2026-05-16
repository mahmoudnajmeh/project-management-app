import React, { useState, useRef, useEffect } from 'react';
import { Calendar, User, AlertCircle, MoreVertical, CheckCircle, Clock, Trash2 } from 'lucide-react';
import type { Task } from '../../types/task';
import { useTasks } from '../../hooks/useTasks';
import { useToast } from '../../hooks/useToast';
import { useAuth } from '../../hooks/useAuth';
import Card from '../common/Card';
import Modal from '../common/Modal';
import Button from '../common/Button';

interface TaskCardProps {
  task: Task;
}

const TaskCard: React.FC<TaskCardProps> = ({ task }) => {
  const { updateTask, deleteTask } = useTasks();
  const { success, error } = useToast();
  const { user } = useAuth();
  const [showActions, setShowActions] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isConfirmDeleteModalOpen, setIsConfirmDeleteModalOpen] = useState(false);
  const actionsRef = useRef<HTMLDivElement>(null);

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'URGENT': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      case 'HIGH': return 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'LOW': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DONE': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'REVIEW': return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
      case 'TODO': return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };


  const canModifyTask = () => {
    if (!user) return false;
    
    if (task.assignedUser && task.assignedUser.id === user.id) {
      return true;
    }
    return false;
  };


  const canUpdateStatus = () => {
    if (!user) return false;
    

    if (task.assignedUser && task.assignedUser.id === user.id) {
      return true;
    }
    
    return false;
  };

  const handleStatusChange = async (newStatus: string) => {
    if (!canUpdateStatus()) {
      error('You are not authorized to update this task');
      return;
    }
    
    try {
      await updateTask({ id: task.id, data: { status: newStatus as any } });
      success('Task status updated');
      setShowActions(false);
    } catch (err: any) {
      if (err.response?.status === 403) {
        error('You are not authorized to update this task');
      } else {
        error('Failed to update task status');
      }
    }
  };

  const handleDeleteConfirm = () => {
    if (!canModifyTask()) {
      error('You are not authorized to delete this task');
      return;
    }
    setIsConfirmDeleteModalOpen(true);
    setShowActions(false);
  };

  const handleDelete = async () => {
    setIsDeleting(true);
    try {
      await deleteTask(task.id);
      success('Task deleted successfully');
      setIsConfirmDeleteModalOpen(false);
    } catch (err: any) {
      if (err.response?.status === 403) {
        error('You are not authorized to delete this task');
      } else {
        error(err.message || 'Failed to delete task');
      }
    } finally {
      setIsDeleting(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
    });
  };

  const isOverdue = new Date(task.dueDate) < new Date() && task.status !== 'DONE';

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (actionsRef.current && !actionsRef.current.contains(event.target as Node)) {
        setShowActions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleMenuItemClick = (action: () => void) => {
    action();
  };

  const isCurrentUserAssigned = user && task.assignedUser && task.assignedUser.id === user.id;

  return (
    <>
      <Card hover className="p-4">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <span className={`badge ${getStatusColor(task.status)}`}>
                {task.status.replace('_', ' ')}
              </span>
              <span className={`badge ${getPriorityColor(task.priority)}`}>
                {task.priority}
              </span>
              {isOverdue && (
                <span className="badge bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 flex items-center">
                  <AlertCircle className="h-3 w-3 mr-1" />
                  Overdue
                </span>
              )}
            </div>
            
            <h3 className="font-semibold text-gray-900 dark:text-white mb-1">
              {task.title}
            </h3>
            <p className="text-gray-600 dark:text-gray-400 text-sm mb-3">
              {task.description}
            </p>

            <div className="flex items-center justify-between text-sm">
              <div className="flex items-center space-x-4">
                <div className="flex items-center text-gray-500 dark:text-gray-400">
                  <Calendar className="h-4 w-4 mr-1" />
                  <span>{formatDate(task.dueDate)}</span>
                </div>
                <div className="flex items-center text-gray-500 dark:text-gray-400">
                  <User className="h-4 w-4 mr-1" />
                  <span>
                    {task.assignedUser.firstName} {task.assignedUser.lastName}
                    {isCurrentUserAssigned && (
                      <span className="ml-1 text-xs text-primary-600 dark:text-primary-400">(You)</span>
                    )}
                  </span>
                </div>
              </div>
              <div className="flex items-center space-x-2">
                <span className="text-primary-600 dark:text-primary-400 font-medium">
                  {task.project.name}
                </span>
                {canModifyTask() && (
                  <div className="relative" ref={actionsRef}>
                    <button
                      onClick={() => setShowActions(!showActions)}
                      className="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
                    >
                      <MoreVertical className="h-4 w-4 text-gray-500" />
                    </button>
                    {showActions && (
                      <div className="absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 z-10">
                        <div className="py-1">
                          <div className="border-t border-gray-200 dark:border-gray-700">
                            <button
                              onClick={() => handleMenuItemClick(() => handleStatusChange('TODO'))}
                              className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                            >
                              Mark as To Do
                            </button>
                            <button
                              onClick={() => handleMenuItemClick(() => handleStatusChange('IN_PROGRESS'))}
                              className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                            >
                              Mark as In Progress
                            </button>
                            <button
                              onClick={() => handleMenuItemClick(() => handleStatusChange('DONE'))}
                              className="block w-full text-left px-4 py-2 text-sm text-green-600 dark:text-green-400 hover:bg-gray-100 dark:hover:bg-gray-700"
                            >
                              Mark as Done
                            </button>
                          </div>
                          <div className="border-t border-gray-200 dark:border-gray-700">
                            <button
                              onClick={handleDeleteConfirm}
                              className="block w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700"
                            >
                              Delete Task
                            </button>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {task.status !== 'DONE' && canUpdateStatus() && (
          <div className="flex items-center space-x-2 mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
            <button
              onClick={() => handleStatusChange('IN_PROGRESS')}
              className="flex-1 flex items-center justify-center px-3 py-1.5 text-sm bg-blue-50 text-blue-700 dark:bg-blue-900 dark:text-blue-300 rounded hover:bg-blue-100 dark:hover:bg-blue-800"
            >
              <Clock className="h-3 w-3 mr-1" />
              Start
            </button>
            <button
              onClick={() => handleStatusChange('DONE')}
              className="flex-1 flex items-center justify-center px-3 py-1.5 text-sm bg-green-50 text-green-700 dark:bg-green-900 dark:text-green-300 rounded hover:bg-green-100 dark:hover:bg-green-800"
            >
              <CheckCircle className="h-3 w-3 mr-1" />
              Complete
            </button>
          </div>
        )}
      </Card>

      <Modal
        isOpen={isConfirmDeleteModalOpen}
        onClose={() => setIsConfirmDeleteModalOpen(false)}
        title="Confirm Delete"
        size="md"
      >
        <div className="space-y-4">
          <div className="flex items-center justify-center mb-4">
            <div className="h-12 w-12 rounded-full bg-red-100 dark:bg-red-900 flex items-center justify-center">
              <Trash2 className="h-6 w-6 text-red-600 dark:text-red-400" />
            </div>
          </div>
          
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white text-center">
            Delete Task
          </h3>
          
          <p className="text-gray-600 dark:text-gray-400 text-center">
            Are you sure you want to delete "{task.title}"? This action cannot be undone.
          </p>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => setIsConfirmDeleteModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              onClick={handleDelete}
              isLoading={isDeleting}
              disabled={isDeleting}
              className="flex items-center"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete Task
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default TaskCard;