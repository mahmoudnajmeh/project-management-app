import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Calendar, ChevronLeft, Edit, Trash2, CheckCircle, MoreVertical } from 'lucide-react';
import { projectsApi } from '../../api/projects';
import { tasksApi } from '../../api/tasks';
import { useToast } from '../../hooks/useToast';
import Card from '../common/Card';
import Button from '../common/Button';
import LoadingSpinner from '../common/LoadingSpinner';
import Modal from '../common/Modal';

const ProjectDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { success, error } = useToast();
  
  const [project, setProject] = useState<any>(null);
  const [tasks, setTasks] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'overview' | 'tasks' | 'team'>('overview');
  const [isDeleting, setIsDeleting] = useState(false);
  const [isConfirmDeleteModalOpen, setIsConfirmDeleteModalOpen] = useState(false);

  useEffect(() => {
    if (!id) {
      error('Invalid project ID');
      navigate('/projects');
      return;
    }
    
    const projectId = parseInt(id);
    if (isNaN(projectId)) {
      error('Invalid project ID format');
      navigate('/projects');
      return;
    }
    
    fetchProjectDetails(projectId);
  }, [id]);

  const fetchProjectDetails = async (projectId: number) => {
    try {
      setIsLoading(true);
      
      const projectRes = await projectsApi.getById(projectId);
      setProject(projectRes.data);
      
      const tasksRes = await tasksApi.getByProject(projectId);
      const tasksData = tasksRes.data;
      
      if (Array.isArray(tasksData)) {
        setTasks(tasksData);
      } else if (tasksData && typeof tasksData === 'object') {
        setTasks([tasksData]);
      } else if (projectRes.data?.tasks && Array.isArray(projectRes.data.tasks)) {
        setTasks(projectRes.data.tasks);
      } else {
        setTasks([]);
      }
      
    } catch (err: any) {
      console.error('Error loading project:', err);
      
      if (err.response?.status === 401) {
        error('Your session has expired');
      } else if (err.response?.status === 403) {
        error('Access denied');
        setTimeout(() => navigate('/projects'), 2000);
      } else if (err.response?.status === 404) {
        error('Project not found');
        setTimeout(() => navigate('/projects'), 2000);
      } else {
        error('Failed to load project');
      }
      
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteConfirm = () => {
    setIsConfirmDeleteModalOpen(true);
  };

  const handleDeleteProject = async () => {
    if (!project) return;

    setIsDeleting(true);
    try {
      await projectsApi.delete(project.id);
      success('Project deleted successfully');
      setIsConfirmDeleteModalOpen(false);
      navigate('/projects');
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to delete project');
    } finally {
      setIsDeleting(false);
    }
  };

  const getStatusColor = (status: string) => {
    if (!status) return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    
    switch (status) {
      case 'COMPLETED': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'PLANNED': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'CANCELLED': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  const getProgress = () => {
    const tasksArray = Array.isArray(tasks) ? tasks : [];
    const completedTasks = tasksArray.filter(t => t?.status === 'DONE').length;
    return tasksArray.length > 0 ? Math.round((completedTasks / tasksArray.length) * 100) : 0;
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return 'Not set';
    
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
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

  const renderUserAvatar = (user: any, size: 'sm' | 'md' | 'lg' = 'md') => {
    const sizes = {
      sm: 'h-8 w-8 text-xs',
      md: 'h-10 w-10 text-sm',
      lg: 'h-12 w-12 text-base'
    };
    
    const profilePictureUrl = getProfilePictureUrl(user);
    
    if (profilePictureUrl) {
      return (
        <div className="relative">
          <img
            src={profilePictureUrl}
            alt={user?.username || 'User'}
            className={`${sizes[size]} rounded-full object-cover border-2 border-white dark:border-gray-800`}
            onError={(e) => {
              e.currentTarget.style.display = 'none';
              const fallback = document.getElementById(`fallback-${user.id}-${size}`);
              if (fallback) fallback.style.display = 'flex';
            }}
          />
          <div 
            id={`fallback-${user.id}-${size}`}
            className={`${sizes[size]} rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800 hidden`}
          >
            <span className="font-bold text-white">
              {getInitials(user?.firstName, user?.lastName)}
            </span>
          </div>
        </div>
      );
    }
    
    return (
      <div 
        className={`${sizes[size]} rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800`}
      >
        <span className="font-bold text-white">
          {getInitials(user?.firstName, user?.lastName)}
        </span>
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!project) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4">
        <Card className="max-w-md">
          <div className="p-6 text-center py-12">
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
              Project not found
            </h3>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              The project you're looking for doesn't exist or you don't have access to it.
            </p>
            <Button onClick={() => navigate('/projects')}>
              <ChevronLeft className="h-4 w-4 mr-2" />
              Back to Projects
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  const tasksArray = Array.isArray(tasks) ? tasks : [];

  const teamMembers = new Map();
  
  if (project.createdBy) {
    teamMembers.set(project.createdBy.id, {
      ...project.createdBy,
      role: 'Project Owner',
      isOwner: true,
      taskCount: 0
    });
  }
  
  tasksArray.forEach(task => {
    if (task?.assignedUser) {
      const userId = task.assignedUser.id;
      if (!teamMembers.has(userId)) {
        teamMembers.set(userId, {
          ...task.assignedUser,
          role: 'Contributor',
          isOwner: false,
          taskCount: 1
        });
      } else {
        const member = teamMembers.get(userId);
        member.taskCount += 1;
        teamMembers.set(userId, member);
      }
    }
  });

  const teamMembersArray = Array.from(teamMembers.values());

  return (
    <>
      <div className="space-y-6">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            <Button
              variant="ghost"
              onClick={() => navigate('/projects')}
              className="flex items-center"
            >
              <ChevronLeft className="h-4 w-4 mr-2" />
              Back to Projects
            </Button>
          </div>
          
          <div className="flex items-center space-x-3">
            <Button
              variant="secondary"
              onClick={() => navigate(`/projects/${project.id}/edit`)}
              className="flex items-center"
            >
              <Edit className="h-4 w-4 mr-2" />
              Edit Project
            </Button>
            <Button
              variant="danger"
              onClick={handleDeleteConfirm}
              className="flex items-center"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete
            </Button>
          </div>
        </div>

        <Card className="overflow-hidden">
          <div className="p-6 bg-gradient-to-r from-primary-50 to-primary-100 dark:from-primary-900/20 dark:to-primary-800/20">
            <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-3">
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(project.status)}`}>
                    {project.status?.replace('_', ' ') || 'Not Set'}
                  </span>
                  <span className="text-sm text-gray-600 dark:text-gray-400">
                    Created {formatDate(project.createdAt)}
                  </span>
                </div>
                <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
                  {project.name || 'Unnamed Project'}
                </h1>
                <p className="text-gray-700 dark:text-gray-300 text-lg">
                  {project.description || 'No description'}
                </p>
              </div>
              
              <div className="flex flex-col items-end space-y-3">
                <div className="text-right">
                  <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">Progress</div>
                  <div className="text-2xl font-bold text-gray-900 dark:text-white">
                    {getProgress()}%
                  </div>
                  <div className="w-32 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden mt-2">
                    <div
                      className="h-full bg-primary-600 rounded-full transition-all duration-300"
                      style={{ width: `${getProgress()}%` }}
                    />
                  </div>
                </div>
                
                <div className="text-right">
                  <div className="text-sm text-gray-600 dark:text-gray-400">Tasks</div>
                  <div className="text-2xl font-bold text-gray-900 dark:text-white">
                    {tasksArray.length}
                  </div>
                </div>
                
                <div className="text-right">
                  <div className="text-sm text-gray-600 dark:text-gray-400">Team Members</div>
                  <div className="text-2xl font-bold text-gray-900 dark:text-white">
                    {teamMembersArray.length}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Card>

        <div className="border-b border-gray-200 dark:border-gray-700">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setActiveTab('overview')}
              className={`py-3 px-1 border-b-2 font-medium text-sm ${activeTab === 'overview'
                ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'
              }`}
            >
              Overview
            </button>
            <button
              onClick={() => setActiveTab('tasks')}
              className={`py-3 px-1 border-b-2 font-medium text-sm ${activeTab === 'tasks'
                ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'
              }`}
            >
              Tasks ({tasksArray.length})
            </button>
            <button
              onClick={() => setActiveTab('team')}
              className={`py-3 px-1 border-b-2 font-medium text-sm ${activeTab === 'team'
                ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'
              }`}
            >
              Team ({teamMembersArray.length})
            </button>
          </nav>
        </div>

        <div className="space-y-6">
          {activeTab === 'overview' && (
            <>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Card>
                  <div className="p-6">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                      Project Owner
                    </h3>
                    <div className="mt-2">
                      {project.createdBy ? (
                        <div className="flex items-center space-x-4">
                          {renderUserAvatar(project.createdBy, 'lg')}
                          <div>
                            <h4 className="font-semibold text-gray-900 dark:text-white">
                              {project.createdBy.firstName || 'Unknown'} {project.createdBy.lastName || ''}
                            </h4>
                            <p className="text-sm text-gray-600 dark:text-gray-400">
                              @{project.createdBy.username || 'unknown'}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-500 mt-1">
                              {project.createdBy.role?.replace('ROLE_', '') || 'User'}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-500 mt-1">
                              {project.createdBy.email || ''}
                            </p>
                          </div>
                        </div>
                      ) : (
                        <p className="text-gray-500 dark:text-gray-400 text-center py-4">
                          No project owner information available
                        </p>
                      )}
                    </div>
                  </div>
                </Card>

                <Card>
                  <div className="p-6">
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                        Timeline
                      </h3>
                      <Calendar className="h-5 w-5 text-gray-400" />
                    </div>
                    <div className="space-y-3">
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Start Date</p>
                        <p className="font-medium text-gray-900 dark:text-white">
                          {formatDate(project.startDate)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">End Date</p>
                        <p className="font-medium text-gray-900 dark:text-white">
                          {formatDate(project.endDate)}
                        </p>
                      </div>
                      <div className="pt-3 border-t border-gray-200 dark:border-gray-700">
                        <p className="text-sm text-gray-600 dark:text-gray-400">Duration</p>
                        <p className="font-medium text-gray-900 dark:text-white">
                          {(() => {
                            try {
                              const start = project.startDate ? new Date(project.startDate) : null;
                              const end = project.endDate ? new Date(project.endDate) : null;
                              
                              if (!start || !end) return 'Not set';
                              
                              const diffTime = Math.abs(end.getTime() - start.getTime());
                              const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                              return `${diffDays} days`;
                            } catch (error) {
                              return 'Not set';
                            }
                          })()}
                        </p>
                      </div>
                    </div>
                  </div>
                </Card>

                <Card>
                  <div className="p-6">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                      Project Details
                    </h3>
                    <div className="space-y-4">
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Status</p>
                        <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium mt-1 ${getStatusColor(project.status)}`}>
                          {project.status?.replace('_', ' ') || 'Not Set'}
                        </span>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Last Updated</p>
                        <p className="font-medium text-gray-900 dark:text-white">
                          {formatDate(project.updatedAt)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Project ID</p>
                        <p className="font-mono text-sm text-gray-900 dark:text-white">
                          #{project.id}
                        </p>
                      </div>
                    </div>
                  </div>
                </Card>
              </div>

              <Card>
                <div className="p-6">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                    Description
                  </h3>
                  <div className="prose max-w-none dark:prose-invert">
                    <p className="text-gray-700 dark:text-gray-300 whitespace-pre-line">
                      {project.description || 'No description provided.'}
                    </p>
                  </div>
                </div>
              </Card>
            </>
          )}

          {activeTab === 'tasks' && (
            <Card>
              <div className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                      Tasks
                    </h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                      {tasksArray.length} tasks in this project
                    </p>
                  </div>
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => navigate(`/tasks?project=${project.id}`)}
                  >
                    Add Task
                  </Button>
                </div>
                <div className="mt-2">
                  {tasksArray.length === 0 ? (
                    <div className="text-center py-12">
                      <CheckCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                        No tasks yet
                      </h3>
                      <p className="text-gray-600 dark:text-gray-400 mb-6">
                        Get started by creating your first task for this project.
                      </p>
                      <Button
                        variant="primary"
                        onClick={() => navigate(`/tasks?project=${project.id}`)}
                      >
                        Create First Task
                      </Button>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {tasksArray.map((task, index) => (
                        <div
                          key={task.id || index}
                          className="flex items-center justify-between p-4 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                        >
                          <div className="flex items-center space-x-4">
                            <div className={`h-8 w-8 rounded-full flex items-center justify-center ${
                              task.status === 'DONE' 
                                ? 'bg-green-100 text-green-600 dark:bg-green-900 dark:text-green-400'
                                : 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400'
                            }`}>
                              <CheckCircle className="h-4 w-4" />
                            </div>
                            <div>
                              <h4 className="font-medium text-gray-900 dark:text-white">
                                {task.title || 'Untitled Task'}
                              </h4>
                              <p className="text-sm text-gray-600 dark:text-gray-400">
                                Due {formatDate(task.dueDate)}
                              </p>
                              {task.assignedUser && (
                                <div className="flex items-center space-x-2 mt-1">
                                  {renderUserAvatar(task.assignedUser, 'sm')}
                                  <p className="text-xs text-gray-500 dark:text-gray-500">
                                    {task.assignedUser.firstName} {task.assignedUser.lastName}
                                  </p>
                                </div>
                              )}
                            </div>
                          </div>
                          <div className="flex items-center space-x-4">
                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                              task.priority === 'HIGH' || task.priority === 'URGENT'
                                ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                                : task.priority === 'MEDIUM'
                                ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
                                : 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                            }`}>
                              {task.priority || 'MEDIUM'}
                            </span>
                            <button className="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded">
                              <MoreVertical className="h-4 w-4 text-gray-500" />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </Card>
          )}

          {activeTab === 'team' && (
            <Card>
              <div className="p-6">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Team Members
                </h3>
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-6">
                  {teamMembersArray.length} people working on this project
                </p>
                <div className="mt-2">
                  {teamMembersArray.length === 0 ? (
                    <div className="text-center py-12">
                      <div className="mx-auto h-12 w-12 text-gray-400 mb-4">
                        <CheckCircle className="h-12 w-12" />
                      </div>
                      <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                        No team members yet
                      </h3>
                      <p className="text-gray-600 dark:text-gray-400 mb-6">
                        Team members will appear here when tasks are assigned to them.
                      </p>
                      <Button
                        variant="primary"
                        onClick={() => navigate(`/tasks?project=${project.id}`)}
                      >
                        Add Task with Team Member
                      </Button>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {teamMembersArray
                        .sort((a, b) => {
                          if (a.isOwner && !b.isOwner) return -1;
                          if (!a.isOwner && b.isOwner) return 1;
                          return 0;
                        })
                        .map((member) => (
                          <div key={member.id} className="flex items-center justify-between p-4 rounded-lg border border-gray-200 dark:border-gray-700">
                            <div className="flex items-center space-x-4">
                              {renderUserAvatar(member, 'md')}
                              <div>
                                <h4 className="font-medium text-gray-900 dark:text-white">
                                  {member.firstName || 'Unknown'} {member.lastName || ''}
                                </h4>
                                <p className="text-sm text-gray-600 dark:text-gray-400">
                                  {member.isOwner ? 'Project Owner' : 'Contributor'} • @{member.username || 'unknown'}
                                </p>
                                {member.email && (
                                  <p className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                                    {member.email}
                                  </p>
                                )}
                              </div>
                            </div>
                            <div className="flex flex-col items-end">
                              {member.isOwner ? (
                                <span className="px-3 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-800 dark:bg-primary-900 dark:text-primary-200">
                                  Owner
                                </span>
                              ) : (
                                <span className="text-sm text-gray-500 dark:text-gray-400">
                                  {member.taskCount} {member.taskCount === 1 ? 'task' : 'tasks'}
                                </span>
                              )}
                            </div>
                          </div>
                        ))}
                    </div>
                  )}
                </div>
              </div>
            </Card>
          )}
        </div>
      </div>

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
              onClick={handleDeleteProject}
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

export default ProjectDetails;