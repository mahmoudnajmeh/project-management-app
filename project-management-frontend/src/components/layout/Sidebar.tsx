import React, { useState, useEffect } from 'react';
import {
  LayoutDashboard,
  FolderKanban,
  CheckSquare,
  Users,
  Calendar,
  BarChart3,
  Settings,
  Plus,
  Clock,
} from 'lucide-react';
import { NavLink, useNavigate } from 'react-router-dom';
import { cn } from '../../utils/helpers';
import { useProjects } from '../../hooks/useProjects';
import { usersApi } from '../../api/users';
import { useToast } from '../../hooks/useToast';
import Button from '../common/Button';
import Modal from '../common/Modal';
import ProjectForm from '../projects/ProjectForm';

interface SidebarProps {
  isOpen?: boolean;
  onClose?: () => void;
}

interface TeamMember {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  profilePictureFileName?: string;
  profilePictureUrl?: string;
  profilePicturePath?: string;
  lastActivity: string;
  createdAt: string;
  updatedAt: string;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen = true, onClose }) => {
  const navigate = useNavigate();
  const { myProjects, isLoading: projectsLoading } = useProjects();
  const { success } = useToast();
  const [teamMembers, setTeamMembers] = useState<TeamMember[]>([]);
  const [isLoadingMembers, setIsLoadingMembers] = useState(false);
  const [isNewProjectModalOpen, setIsNewProjectModalOpen] = useState(false);

  const navItems = [
    { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard' },
    { icon: FolderKanban, label: 'Projects', path: '/projects' },
    { icon: CheckSquare, label: 'Tasks', path: '/tasks' },
    { icon: Users, label: 'Team', path: '/team' },
    { icon: Calendar, label: 'Calendar', path: '/calendar' },
    { icon: BarChart3, label: 'Reports', path: '/reports' },
    { icon: Settings, label: 'Settings', path: '/settings' },
  ];

  useEffect(() => {
    fetchTeamMembers();
  }, []);

  const fetchTeamMembers = async () => {
    try {
      setIsLoadingMembers(true);
      const response = await usersApi.getAllUsers();
      const transformedData = response.data.map((user: any) => ({
        ...user,
        profilePictureFileName: user.profilePictureFileName ?? undefined,
        lastActivity: user.lastActivity || user.updatedAt,
      }));
      
      setTeamMembers(transformedData);
    } catch (err: any) {
      console.error('Failed to load team members:', err);
    } finally {
      setIsLoadingMembers(false);
    }
  };

  const getInitials = (firstName: string = '', lastName: string = '') => {
    return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase() || 'U';
  };

  const getProfilePictureUrl = (member: TeamMember) => {
    const timestamp = Date.now();

    if (member.profilePictureFileName) {
      return `http://localhost:8080/api/users/profile-picture/${member.profilePictureFileName}?t=${timestamp}`;
    }

    if (member.profilePictureUrl && member.profilePictureUrl.startsWith('http')) {
      return `${member.profilePictureUrl}?t=${timestamp}`;
    }

    if (member.profilePictureUrl && !member.profilePictureUrl.startsWith('http')) {
      return `http://localhost:8080${member.profilePictureUrl}?t=${timestamp}`;
    }

    if (member.profilePicturePath) {
      return `http://localhost:8080${member.profilePicturePath}?t=${timestamp}`;
    }

    return null;
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'bg-green-500';
      case 'IN_PROGRESS': return 'bg-blue-500';
      case 'PLANNED': return 'bg-yellow-500';
      case 'CANCELLED': return 'bg-red-500';
      default: return 'bg-gray-500';
    }
  };

  const getUserStatus = (member: TeamMember) => {
    if (!member.lastActivity) return false;
    
    const lastActive = new Date(member.lastActivity);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - lastActive.getTime()) / (1000 * 60));
    
    return diffInMinutes < 15;
  };

  const getLastActiveTime = (member: TeamMember) => {
    if (!member.lastActivity) return 'Never';
    
    const lastActive = new Date(member.lastActivity);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - lastActive.getTime()) / (1000 * 60));
    
    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes} min ago`;
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours} hours ago`;
    
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays === 1) return 'Yesterday';
    if (diffInDays < 7) return `${diffInDays} days ago`;
    if (diffInDays < 30) return `${Math.floor(diffInDays / 7)} weeks ago`;
    
    return `${Math.floor(diffInDays / 30)} months ago`;
  };

  const handleNewProjectClick = () => {
    setIsNewProjectModalOpen(true);
  };

  const handleProjectClick = (projectId: number) => {
    navigate(`/projects/${projectId}`);
    if (onClose) onClose();
  };

  const handleNewProjectSuccess = () => {
    setIsNewProjectModalOpen(false);
    success('Project created successfully!');
  };

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      const now = new Date();
      const diffTime = Math.abs(now.getTime() - date.getTime());
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays === 0) return 'Today';
      if (diffDays === 1) return 'Yesterday';
      if (diffDays < 7) return `${diffDays} days ago`;
      if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
      return `${Math.floor(diffDays / 30)} months ago`;
    } catch (error) {
      return '';
    }
  };

  const getRecentProjects = () => {
    if (!myProjects || !Array.isArray(myProjects)) return [];
    
    return [...myProjects]
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 5);
  };

  const getOnlineTeamMembers = () => {
    return teamMembers
      .filter(member => getUserStatus(member))
      .slice(0, 5);
  };

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && onClose && (
        <div
          className="fixed inset-0 z-30 bg-black bg-opacity-50 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={cn(
          'fixed lg:sticky top-16 lg:top-0 left-0 z-40 h-[calc(100vh-4rem)] w-64 border-r border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 transition-transform lg:translate-x-0 overflow-y-auto',
          isOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="h-full px-3 py-4">
          {/* Quick Actions */}
          <div className="mb-6">
            <Button
              onClick={handleNewProjectClick}
              className="w-full"
            >
              <Plus className="h-4 w-4 mr-2" />
              New Project
            </Button>
          </div>

          {/* Navigation */}
          <nav>
            <div className="mb-4">
              <h3 className="px-3 mb-2 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Navigation
              </h3>
              <ul className="space-y-1">
                {navItems.map((item) => (
                  <li key={item.path}>
                    <NavLink
                      to={item.path}
                      className={({ isActive }) =>
                        cn(
                          'flex items-center px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                          isActive
                            ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400'
                            : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800'
                        )
                      }
                      onClick={onClose}
                    >
                      <item.icon className="h-4 w-4 mr-3" />
                      {item.label}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>

            {/* Recent Projects */}
            <div className="mb-4">
              <div className="flex items-center justify-between px-3 mb-2">
                <h3 className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Recent Projects
                </h3>
                <span className="text-xs text-gray-400">
                  {projectsLoading ? '...' : myProjects?.length || 0}
                </span>
              </div>
              <ul className="space-y-1">
                {projectsLoading ? (
                  <li className="px-3 py-2">
                    <div className="animate-pulse flex items-center">
                      <div className="h-2 w-2 rounded-full bg-gray-300 mr-3"></div>
                      <div className="h-2 bg-gray-300 rounded w-24"></div>
                    </div>
                  </li>
                ) : getRecentProjects().length === 0 ? (
                  <li className="px-3 py-2">
                    <p className="text-xs text-gray-500 dark:text-gray-400 text-center">
                      No projects yet
                    </p>
                  </li>
                ) : (
                  getRecentProjects().map((project) => (
                    <li key={project.id}>
                      <button
                        onClick={() => handleProjectClick(project.id)}
                        className="flex items-center w-full px-3 py-2 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors text-left"
                      >
                        <div className="flex items-center w-full">
                          <span
                            className={`w-2 h-2 rounded-full ${getStatusColor(project.status)} mr-3 flex-shrink-0`}
                          />
                          <span className="truncate flex-1">{project.name}</span>
                          <span className="text-xs text-gray-500 dark:text-gray-400 ml-2 flex-shrink-0">
                            {formatDate(project.createdAt)}
                          </span>
                        </div>
                      </button>
                    </li>
                  ))
                )}
              </ul>
            </div>

            {/* Online Team Members */}
            <div>
              <div className="flex items-center justify-between px-3 mb-2">
                <h3 className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Online Members
                </h3>
                <span className="text-xs text-gray-400">
                  {isLoadingMembers ? '...' : getOnlineTeamMembers().length}
                </span>
              </div>
              <div className="px-3">
                <div className="flex flex-col space-y-2">
                  {isLoadingMembers ? (
                    <div className="space-y-2">
                      {[1, 2, 3].map((i) => (
                        <div key={i} className="animate-pulse flex items-center">
                          <div className="h-8 w-8 rounded-full bg-gray-300 mr-3"></div>
                          <div className="flex-1">
                            <div className="h-2 bg-gray-300 rounded w-16 mb-1"></div>
                            <div className="h-2 bg-gray-300 rounded w-12"></div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : getOnlineTeamMembers().length === 0 ? (
                    <p className="text-xs text-gray-500 dark:text-gray-400 text-center py-2">
                      No online members
                    </p>
                  ) : (
                    getOnlineTeamMembers().map((member) => {
                      const profilePictureUrl = getProfilePictureUrl(member);
                      const isOnline = getUserStatus(member);
                      const lastActive = getLastActiveTime(member);

                      return (
                        <button
                          key={member.id}
                          onClick={() => navigate(`/team`)}
                          className="flex items-center w-full p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                        >
                          <div className="relative">
                            {profilePictureUrl ? (
                              <img
                                src={profilePictureUrl}
                                alt={member.username}
                                className="h-8 w-8 rounded-full object-cover border-2 border-white dark:border-gray-800"
                                onError={(e) => {
                                  e.currentTarget.style.display = 'none';
                                  const fallback = e.currentTarget.nextElementSibling as HTMLElement;
                                  if (fallback) fallback.style.display = 'flex';
                                }}
                              />
                            ) : null}

                            <div
                              className={`h-8 w-8 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800 ${
                                profilePictureUrl ? 'hidden' : 'flex'
                              }`}
                            >
                              <span className="text-xs font-bold text-white">
                                {getInitials(member.firstName, member.lastName)}
                              </span>
                            </div>
                            
                            <div
                              className={`absolute -bottom-1 -right-1 h-2 w-2 rounded-full border-2 border-white dark:border-gray-800 ${
                                isOnline ? 'bg-green-500' : 'bg-gray-400'
                              }`}
                            />
                          </div>
                          <div className="ml-3 text-left">
                            <p className="text-xs font-medium text-gray-900 dark:text-white truncate">
                              {member.firstName} {member.lastName}
                            </p>
                            <div className="flex items-center">
                              <Clock className="h-2 w-2 text-gray-400 mr-1" />
                              <p className="text-xs text-gray-500 dark:text-gray-400">
                                {lastActive}
                              </p>
                            </div>
                          </div>
                        </button>
                      );
                    })
                  )}
                </div>
              </div>
            </div>
          </nav>
        </div>
      </aside>

      {/* New Project Modal */}
      <Modal
        isOpen={isNewProjectModalOpen}
        onClose={() => setIsNewProjectModalOpen(false)}
        title="Create New Project"
        size="lg"
      >
        <ProjectForm onSuccess={handleNewProjectSuccess} />
      </Modal>
    </>
  );
};

export default Sidebar;