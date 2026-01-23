import React, { useState } from 'react';
import { Calendar, FolderKanban, MoreVertical, CheckCircle, Trash2 } from 'lucide-react';
import type { Project } from '../../types/project';
import { useNavigate } from 'react-router-dom';
import { useProjects } from '../../hooks/useProjects';
import { useTasks } from '../../hooks/useTasks';
import { useToast } from '../../hooks/useToast';
import Card from '../common/Card';
import Button from '../common/Button';
import Modal from '../common/Modal';

interface ProjectCardProps {
  project: Project;
  viewMode?: 'grid' | 'list';
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project, viewMode = 'grid' }) => {
  const navigate = useNavigate();
  const { deleteProject } = useProjects();
  const { tasks: allTasks } = useTasks();
  const { success, error } = useToast();
  const [showActions, setShowActions] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isConfirmDeleteModalOpen, setIsConfirmDeleteModalOpen] = useState(false);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'PLANNED': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'CANCELLED': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  const getProgress = () => {
    const projectTasks = allTasks?.filter(task => task.project?.id === project.id) || [];
    const totalTasks = projectTasks.length;
    const completedTasks = projectTasks.filter(t => t.status === 'DONE').length;
    return totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
  };

  const handleViewProject = () => {
    navigate(`/projects/${project.id}`);
  };

  const handleDeleteConfirm = () => {
    setIsConfirmDeleteModalOpen(true);
  };

  const handleDelete = async () => {
    setIsDeleting(true);
    try {
      await deleteProject(project.id);
      success('Project deleted successfully');
      setIsConfirmDeleteModalOpen(false);
    } catch (err: any) {
      error(err.message || 'Failed to delete project');
    } finally {
      setIsDeleting(false);
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch (error) {
      return 'Invalid date';
    }
  };

  const getInitials = (firstName: string = '', lastName: string = '') => {
    return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase() || 'U';
  };

  const getProfilePictureUrl = (user: any) => {
    if (!user) return null;
    
    const timestamp = Date.now();
    
    if (user.profilePictureFileName) {
      return `http://localhost:8080/api/users/profile-picture/${user.profilePictureFileName}?t=${timestamp}`;
    }
    
    if (user.profilePictureUrl && user.profilePictureUrl.startsWith('http')) {
      return `${user.profilePictureUrl}?t=${timestamp}`;
    }
    
    if (user.profilePictureUrl && !user.profilePictureUrl.startsWith('http')) {
      return `http://localhost:8080${user.profilePictureUrl}?t=${timestamp}`;
    }
    
    if (user.profilePicturePath) {
      return `http://localhost:8080${user.profilePicturePath}?t=${timestamp}`;
    }
    
    return null;
  };

  const renderUserAvatar = (user: any) => {
    const profilePictureUrl = getProfilePictureUrl(user);
    
    if (profilePictureUrl) {
      return (
        <div className="relative">
          <img
            src={profilePictureUrl}
            alt={user?.username || 'User'}
            className="h-10 w-10 rounded-full object-cover border-2 border-white dark:border-gray-800"
            onError={(e) => {
              e.currentTarget.style.display = 'none';
              const fallback = document.getElementById(`project-avatar-fallback-${user.id}`);
              if (fallback) fallback.style.display = 'flex';
            }}
          />
          <div 
            id={`project-avatar-fallback-${user.id}`}
            className="h-10 w-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800 hidden"
          >
            <span className="text-sm font-bold text-white">
              {getInitials(user?.firstName, user?.lastName)}
            </span>
          </div>
        </div>
      );
    }
    
    return (
      <div className="h-10 w-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800">
        <span className="text-sm font-bold text-white">
          {getInitials(user?.firstName, user?.lastName)}
        </span>
      </div>
    );
  };

  const projectTasks = allTasks?.filter(task => task.project?.id === project.id) || [];
  const completedTasksCount = projectTasks.filter(t => t.status === 'DONE').length;
  const progress = getProgress();

  if (viewMode === 'list') {
    return (
      <>
        <Card hover className="flex items-center justify-between p-4">
          <div className="flex items-center space-x-4 flex-1">
            <div className="h-10 w-10 rounded-lg bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
              <FolderKanban className="h-5 w-5 text-primary-600 dark:text-primary-400" />
            </div>
            <div className="flex-1">
              <h3 className="font-semibold text-gray-900 dark:text-white">
                {project.name}
              </h3>
              <p className="text-sm text-gray-600 dark:text-gray-400 mt-1 line-clamp-1">
                {project.description}
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-6">
            <div className="text-center">
              <p className="text-sm text-gray-600 dark:text-gray-400">Progress</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">
                {progress}%
              </p>
            </div>
            <div className="text-center">
              <p className="text-sm text-gray-600 dark:text-gray-400">Tasks</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">
                {projectTasks.length}
              </p>
            </div>
            <div className="text-center">
              <p className="text-sm text-gray-600 dark:text-gray-400">Status</p>
              <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(project.status)}`}>
                {project.status.replace('_', ' ')}
              </span>
            </div>
            <div className="relative">
              <button
                onClick={() => setShowActions(!showActions)}
                className="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
              >
                <MoreVertical className="h-4 w-4 text-gray-500" />
              </button>
              {showActions && (
                <div className="absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 z-10">
                  <div className="py-1">
                    <button
                      onClick={handleViewProject}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    >
                      View Details
                    </button>
                    <button
                      onClick={() => navigate(`/projects/${project.id}/edit`)}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    >
                      Edit Project
                    </button>
                    <button
                      onClick={handleDeleteConfirm}
                      className="block w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700"
                    >
                      Delete Project
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
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
              Delete Project
            </h3>
            
            <p className="text-gray-600 dark:text-gray-400 text-center">
              Are you sure you want to delete "{project.name}"? This action cannot be undone.
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
                Delete Project
              </Button>
            </div>
          </div>
        </Modal>
      </>
    );
  }

  return (
    <>
      <Card hover>
        <div className="p-6">
          <div className="flex items-start justify-between mb-4">
            <div className="h-12 w-12 rounded-lg bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
              <FolderKanban className="h-6 w-6 text-primary-600 dark:text-primary-400" />
            </div>
            <div className="relative">
              <button
                onClick={() => setShowActions(!showActions)}
                className="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
              >
                <MoreVertical className="h-4 w-4 text-gray-500" />
              </button>
              {showActions && (
                <div className="absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 z-10">
                  <div className="py-1">
                    <button
                      onClick={handleViewProject}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    >
                      View Details
                    </button>
                    <button
                      onClick={() => navigate(`/projects/${project.id}/edit`)}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    >
                      Edit Project
                    </button>
                    <button
                      onClick={handleDeleteConfirm}
                      className="block w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700"
                    >
                      Delete Project
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>

          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
            {project.name}
          </h3>
          <p className="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
            {project.description}
          </p>

          <div className="flex items-center justify-between mb-4">
            <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(project.status)}`}>
              {project.status.replace('_', ' ')}
            </span>
            <div className="flex items-center text-sm text-gray-500 dark:text-gray-400">
              {project.createdBy && (
                <div className="flex -space-x-2">
                  {renderUserAvatar(project.createdBy)}
                </div>
              )}
            </div>
          </div>

          <div className="mb-4">
            <div className="flex justify-between text-sm text-gray-600 dark:text-gray-400 mb-1">
              <span>Progress</span>
              <span>{progress}%</span>
            </div>
            <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div
                className="h-full bg-primary-600 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>

          <div className="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
            <div className="flex items-center">
              <Calendar className="h-4 w-4 mr-1" />
              <span>{formatDate(project.endDate)}</span>
            </div>
            <div className="flex items-center">
              <CheckCircle className="h-4 w-4 mr-1" />
              <span>{completedTasksCount}/{projectTasks.length}</span>
            </div>
          </div>

          <Button
            variant="secondary"
            fullWidth
            className="mt-4"
            onClick={handleViewProject}
          >
            View Project
          </Button>
        </div>
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
            Delete Project
          </h3>
          
          <p className="text-gray-600 dark:text-gray-400 text-center">
            Are you sure you want to delete "{project.name}"? This action cannot be undone.
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
              Delete Project
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default ProjectCard;