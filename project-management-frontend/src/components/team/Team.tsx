import React, { useState, useEffect } from 'react';
import { Search, Filter, UserPlus, Mail as MailIcon, Users, Calendar, Shield, CheckSquare, Trash2, Edit, Camera, X } from 'lucide-react';
import { usersApi } from '../../api/users';
import { tasksApi } from '../../api/tasks';
import { teamsApi } from '../../api/teams';
import { useToast } from '../../hooks/useToast';
import Button from '../common/Button';
import Input from '../common/Input';
import Modal from '../common/Modal';
import LoadingSpinner from '../common/LoadingSpinner';
import Card from '../common/Card';
import { useAuth } from '../../hooks/useAuth';
import ChatModal from './ChatModal';
import TeamPhoto from './TeamPhoto';

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
  profilePictureContentType?: string;
  lastActivity: string;
  createdAt: string;
  updatedAt: string;
}

interface Team {
  id: number;
  name: string;
  description: string;
  createdBy: TeamMember;
  members: TeamMember[];
  createdAt: string;
  teamPhotoFileName?: string;
  teamPhotoUrl?: string;
  teamPhotoPath?: string;
}

const Team: React.FC = () => {
  const [teamMembers, setTeamMembers] = useState<TeamMember[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [isLoading, setIsLoading] = useState(true);
  const [isAddMemberModalOpen, setIsAddMemberModalOpen] = useState(false);
  const [selectedMember, setSelectedMember] = useState<TeamMember | null>(null);
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
  const [isAssignTaskModalOpen, setIsAssignTaskModalOpen] = useState(false);
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDescription, setTaskDescription] = useState('');
  const [taskPriority, setTaskPriority] = useState('MEDIUM');
  const [taskDueDate, setTaskDueDate] = useState('');
  const [isCreatingTask, setIsCreatingTask] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [isSendingInvite, setIsSendingInvite] = useState(false);
  const [inviteRole, setInviteRole] = useState('ROLE_USER');
  const { error, success } = useToast();
  const { user: currentUser } = useAuth();
  const [chatModalOpen, setChatModalOpen] = useState(false);
  const [selectedChatMember, setSelectedChatMember] = useState<TeamMember | null>(null);
  
  const [isCreateTeamModalOpen, setIsCreateTeamModalOpen] = useState(false);
  const [newTeamName, setNewTeamName] = useState('');
  const [newTeamDescription, setNewTeamDescription] = useState('');
  const [isCreatingTeam, setIsCreatingTeam] = useState(false);
  
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [isTeamMembersModalOpen, setIsTeamMembersModalOpen] = useState(false);
  const [isEditTeamModalOpen, setIsEditTeamModalOpen] = useState(false);
  const [editTeamName, setEditTeamName] = useState('');
  const [editTeamDescription, setEditTeamDescription] = useState('');
  const [isUpdatingTeam, setIsUpdatingTeam] = useState(false);
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
  const [teamToDelete, setTeamToDelete] = useState<Team | null>(null);
  
  const [activeTab, setActiveTab] = useState<'members' | 'teams'>('members');

  const [isAddToTeamModalOpen, setIsAddToTeamModalOpen] = useState(false);
  const [selectedTeamForAdd, setSelectedTeamForAdd] = useState<Team | null>(null);
  const [availableMembers, setAvailableMembers] = useState<TeamMember[]>([]);

  const [isUploadPhotoModalOpen, setIsUploadPhotoModalOpen] = useState(false);
  const [selectedTeamForPhoto, setSelectedTeamForPhoto] = useState<Team | null>(null);
  const [teamPhotoFile, setTeamPhotoFile] = useState<File | null>(null);
  const [teamPhotoPreview, setTeamPhotoPreview] = useState<string | null>(null);
  const [isUploadingPhoto, setIsUploadingPhoto] = useState(false);

  const handleOpenAddToTeamModal = (team: Team) => {
    const teamMemberIds = team.members?.map(m => m.id) || [];
    const available = teamMembers.filter(member => !teamMemberIds.includes(member.id));
    setAvailableMembers(available);
    setSelectedTeamForAdd(team);
    setIsAddToTeamModalOpen(true);
  };

  const handleOpenEditTeamModal = (team: Team) => {
    setSelectedTeam(team);
    setEditTeamName(team.name);
    setEditTeamDescription(team.description || '');
    setIsEditTeamModalOpen(true);
  };

  const handleOpenDeleteConfirm = (team: Team) => {
    setTeamToDelete(team);
    setIsDeleteConfirmOpen(true);
  };

  const handleOpenUploadPhotoModal = (team: Team) => {
    setSelectedTeamForPhoto(team);
    setTeamPhotoFile(null);
    setTeamPhotoPreview(null);
    setIsUploadPhotoModalOpen(true);
  };

  const handleTeamPhotoSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setTeamPhotoFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setTeamPhotoPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleUploadTeamPhoto = async () => {
    if (!selectedTeamForPhoto || !teamPhotoFile) return;

    try {
      setIsUploadingPhoto(true);
      await teamsApi.uploadTeamPhoto(selectedTeamForPhoto.id, teamPhotoFile);
      
      success('Team photo uploaded successfully!');
      await fetchMyTeams();
      
      setIsUploadPhotoModalOpen(false);
      setTeamPhotoFile(null);
      setTeamPhotoPreview(null);
      setSelectedTeamForPhoto(null);
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to upload team photo');
    } finally {
      setIsUploadingPhoto(false);
    }
  };

  const handleDeleteTeamPhoto = async (team: Team) => {
    try {
      await teamsApi.deleteTeamPhoto(team.id);
      success('Team photo deleted successfully!');
      await fetchMyTeams();
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to delete team photo');
    }
  };

  useEffect(() => {
    fetchTeamMembers();
    fetchMyTeams();
  }, []);

  const fetchTeamMembers = async () => {
    try {
      setIsLoading(true);
      const response = await usersApi.getAllUsers();
      const transformedData = response.data.map((user: any) => ({
        ...user,
        profilePictureFileName: user.profilePictureFileName ?? undefined,
        lastActivity: user.lastActivity || user.updatedAt,
      }));
      
      const sortedData = [...transformedData].sort((a, b) => {
        if (currentUser && a.id === currentUser.id) return -1;
        if (currentUser && b.id === currentUser.id) return 1;
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      });
      
      setTeamMembers(sortedData);
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to load team members');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchMyTeams = async () => {
    try {
      const response = await teamsApi.getMyTeams();
      setTeams(response.data);
    } catch (err: any) {
      console.error('Failed to load teams:', err);
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

  const getOnlineStatus = (member: TeamMember) => {
    const isActive = getUserStatus(member);
    const lastActive = getLastActiveTime(member);
    
    if (isActive) return 'Online';
    return `Last seen ${lastActive}`;
  };

  const handleSendInvite = async () => {
    if (!inviteEmail.trim()) {
      error('Email address is required');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(inviteEmail)) {
      error('Please enter a valid email address');
      return;
    }

    try {
      setIsSendingInvite(true);
      
      const inviteData = {
        email: inviteEmail,
        role: inviteRole,
      };
      
      await usersApi.sendInvite(inviteData);
      
      success(`Invitation sent to ${inviteEmail} successfully!`);
      setInviteEmail('');
      setInviteRole('ROLE_USER');
      setIsAddMemberModalOpen(false);
      
    } catch (err: any) {
      console.error('Error sending invite:', err);
      
      if (err.response?.status === 400) {
        error(err.response?.data?.error || 'Failed to send invitation');
      } else if (err.response?.status === 409) {
        error('User with this email already exists');
      } else {
        error('Failed to send invitation');
      }
    } finally {
      setIsSendingInvite(false);
    }
  };

  const handleCreateTeam = async () => {
    if (!newTeamName.trim()) {
      error('Team name is required');
      return;
    }

    try {
      setIsCreatingTeam(true);
      const response = await teamsApi.createTeam({
        name: newTeamName,
        description: newTeamDescription
      });
      
      success('Team created successfully!');
      setIsCreateTeamModalOpen(false);
      setNewTeamName('');
      setNewTeamDescription('');
      
      await fetchMyTeams();
      
      setActiveTab('teams');
    } catch (err: any) {
      console.error('Error creating team:', err);
      error(err.response?.data?.error || 'Failed to create team');
    } finally {
      setIsCreatingTeam(false);
    }
  };

  const handleUpdateTeam = async () => {
    if (!selectedTeam) return;
    
    if (!editTeamName.trim()) {
      error('Team name is required');
      return;
    }

    try {
      setIsUpdatingTeam(true);
      await teamsApi.updateTeam(selectedTeam.id, {
        name: editTeamName,
        description: editTeamDescription
      });
      
      success('Team updated successfully!');
      await fetchMyTeams();
      
      setIsEditTeamModalOpen(false);
      setSelectedTeam(null);
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to update team');
    } finally {
      setIsUpdatingTeam(false);
    }
  };

  const handleDeleteTeam = async () => {
    if (!teamToDelete) return;

    try {
      await teamsApi.deleteTeam(teamToDelete.id);
      success('Team deleted successfully!');
      await fetchMyTeams();
      
      setIsDeleteConfirmOpen(false);
      setTeamToDelete(null);
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to delete team');
    }
  };

  const handleViewTeamMembers = async (team: Team) => {
    try {
      const response = await teamsApi.getTeamMembers(team.id);
      
      setSelectedTeam({
        ...team,
        members: response.data
      });
      setIsTeamMembersModalOpen(true);
    } catch (err: any) {
      error('Failed to load team members');
    }
  };

  const handleAddToTeam = async (teamId: number, userId: number) => {
    try {
      await teamsApi.addMember(teamId, userId);
      success('Member added to team successfully!');
      
      if (selectedTeam && selectedTeam.id === teamId) {
        const response = await teamsApi.getTeamMembers(teamId);
        setSelectedTeam({
          ...selectedTeam,
          members: response.data
        });
      }
      
      await fetchMyTeams();
      
      setIsAddToTeamModalOpen(false);
      setSelectedTeamForAdd(null);
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to add member to team');
    }
  };

  const handleRemoveFromTeam = async (teamId: number, userId: number) => {
    try {
      await teamsApi.removeMember(teamId, userId);
      success('Member removed from team successfully!');
      
      if (selectedTeam && selectedTeam.id === teamId) {
        const response = await teamsApi.getTeamMembers(teamId);
        setSelectedTeam({
          ...selectedTeam,
          members: response.data
        });
      }
      
      await fetchMyTeams();
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to remove member from team');
    }
  };

  const roleOptions = [
    { value: 'all', label: 'All Roles' },
    { value: 'ROLE_USER', label: 'User' },
    { value: 'ROLE_ADMIN', label: 'Admin' }
  ];

  const statusOptions = [
    { value: 'all', label: 'All Status' },
    { value: 'online', label: 'Online' },
    { value: 'offline', label: 'Offline' }
  ];

  const priorityOptions = [
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' }
  ];

  const filteredMembers = teamMembers.filter((member) => {
    const fullName = `${member.firstName} ${member.lastName}`.toLowerCase();
    const matchesSearch = fullName.includes(searchQuery.toLowerCase()) ||
      member.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      member.username.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole = roleFilter === 'all' || member.role === roleFilter;
    const isOnline = getUserStatus(member);
    const matchesStatus = statusFilter === 'all' || 
      (statusFilter === 'online' && isOnline) || 
      (statusFilter === 'offline' && !isOnline);
    
    return matchesSearch && matchesRole && matchesStatus;
  });

  const getInitials = (firstName: string = '', lastName: string = '') => {
    return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase() || 'U';
  };

  const getProfilePictureUrl = (member: TeamMember) => {
    const timestamp = Date.now();

    if (member.profilePictureFileName) {
      return `/api/users/profile-picture/${member.profilePictureFileName}?t=${timestamp}`;
    }

    if (member.profilePictureUrl && member.profilePictureUrl.startsWith('http')) {
      return `${member.profilePictureUrl}?t=${timestamp}`;
    }

    if (member.profilePictureUrl && !member.profilePictureUrl.startsWith('http')) {
      return `/api${member.profilePictureUrl}?t=${timestamp}`;
    }

    if (member.profilePicturePath) {
      return `/api${member.profilePicturePath}?t=${timestamp}`;
    }

    return null;
  };

  const getTeamPhotoUrl = (team: Team) => {
    console.log('Team photo data:', {
      id: team.id,
      teamPhotoFileName: team.teamPhotoFileName,
      teamPhotoUrl: team.teamPhotoUrl
    });
    
    if (team.teamPhotoFileName) {
      const url = `/api/teams/${team.id}/photo?t=${Date.now()}`;
      console.log('Generated team photo URL:', url);
      return url;
    }
    return null;
  };

  const formatRole = (role: string) => {
    return role.replace('ROLE_', '');
  };

  const formatDate = (dateString: string) => {
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

  const handleViewProfile = (member: TeamMember) => {
    setSelectedMember(member);
    setIsProfileModalOpen(true);
  };

  const handleCloseProfileModal = () => {
    setSelectedMember(null);
    setIsProfileModalOpen(false);
  };

  const handleAssignTaskClick = () => {
    if (selectedMember) {
      setIsAssignTaskModalOpen(true);
    }
  };

  const handleCloseAssignTaskModal = () => {
    setIsAssignTaskModalOpen(false);
    setTaskTitle('');
    setTaskDescription('');
    setTaskPriority('MEDIUM');
    setTaskDueDate('');
  };

  const handleCreateTask = async () => {
    if (!selectedMember) return;
    
    if (!taskTitle.trim()) {
      error('Task title is required');
      return;
    }

    if (!taskDescription.trim()) {
      error('Task description is required');
      return;
    }

    try {
      setIsCreatingTask(true);
      
      const taskData = {
        title: taskTitle,
        description: taskDescription,
        projectId: 1,
        assignedUserId: selectedMember.id,
        priority: taskPriority as any,
        dueDate: taskDueDate ? `${taskDueDate}T23:59:59` : undefined,
      };

      await tasksApi.create(taskData);
      
      success(`Task assigned to ${selectedMember.firstName} ${selectedMember.lastName} successfully!`);
      
      handleCloseAssignTaskModal();
      handleCloseProfileModal();
    } catch (err: any) {
      console.error('Error creating task:', err);
      error(err.response?.data?.error || 'Failed to assign task');
    } finally {
      setIsCreatingTask(false);
    }
  };

  const stats = {
    total: teamMembers.length,
    admins: teamMembers.filter(m => m.role === 'ROLE_ADMIN').length,
    users: teamMembers.filter(m => m.role === 'ROLE_USER').length,
    online: teamMembers.filter(m => getUserStatus(m)).length,
    offline: teamMembers.filter(m => !getUserStatus(m)).length,
    teams: teams.length
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Team Collaboration
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Manage members and teams
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => setIsCreateTeamModalOpen(true)}>
            <Users className="h-4 w-4 mr-2" />
            Create Team
          </Button>
          <Button onClick={() => setIsAddMemberModalOpen(true)}>
            <UserPlus className="h-4 w-4 mr-2" />
            Invite Member
          </Button>
        </div>
      </div>

      <div className="border-b border-gray-200 dark:border-gray-700">
        <nav className="flex -mb-px space-x-8">
          <button
            onClick={() => setActiveTab('members')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'members'
                ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'
            }`}
          >
            All Members ({stats.total})
          </button>
          <button
            onClick={() => setActiveTab('teams')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'teams'
                ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'
            }`}
          >
            My Teams ({stats.teams})
          </button>
        </nav>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-6 gap-4">
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Total Members</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.total}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Admins</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.admins}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Users</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.users}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Online</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.online}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Offline</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.offline}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Teams</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.teams}</p>
        </div>
      </div>

      {activeTab === 'members' && (
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <Input
              placeholder="Search by name, email, or username..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              leftIcon={<Search className="h-4 w-4 text-gray-400" />}
            />
          </div>
          <div className="flex items-center gap-2">
            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              className="rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm"
            >
              {roleOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm"
            >
              {statusOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <button className="p-2 border border-gray-300 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800">
              <Filter className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {activeTab === 'members' ? (
        filteredMembers.length === 0 ? (
          <Card>
            <div className="p-6 text-center">
              <div className="mx-auto h-12 w-12 text-gray-400 mb-4">
                <Users className="h-12 w-12" />
              </div>
              <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                No members found
              </h3>
              <p className="text-gray-600 dark:text-gray-400 mb-6">
                {teamMembers.length === 0
                  ? 'Your team is empty. Start by inviting team members.'
                  : 'No members match your search criteria.'}
              </p>
              <Button onClick={() => setIsAddMemberModalOpen(true)}>
                <UserPlus className="h-4 w-4 mr-2" />
                Invite Team Members
              </Button>
            </div>
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredMembers.map((member) => {
              const profilePictureUrl = getProfilePictureUrl(member);
              const isOnline = getUserStatus(member);
              const onlineStatus = getOnlineStatus(member);
              const memberTeams = teams.filter(team => 
                team.members?.some(m => m.id === member.id) || team.createdBy?.id === member.id
              );

              return (
                <Card key={member.id} hover>
                  <div className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center space-x-4">
                        <div className="relative">
                          <div className="relative">
                            {profilePictureUrl ? (
                              <img
                                src={profilePictureUrl}
                                alt={member.username}
                                className="h-12 w-12 rounded-full object-cover border-2 border-white dark:border-gray-800"
                                onError={(e) => {
                                  e.currentTarget.style.display = 'none';
                                  const fallback = e.currentTarget.nextElementSibling as HTMLElement;
                                  if (fallback) fallback.style.display = 'flex';
                                }}
                              />
                            ) : null}

                            <div
                              className={`h-12 w-12 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800 ${
                                profilePictureUrl ? 'hidden' : 'flex'
                              }`}
                            >
                              <span className="text-sm font-bold text-white">
                                {getInitials(member.firstName, member.lastName)}
                              </span>
                            </div>
                          </div>
                          <div className={`absolute -bottom-1 -right-1 h-4 w-4 rounded-full border-2 border-white dark:border-gray-800 ${
                            isOnline ? 'bg-green-500' : 'bg-gray-400'
                          }`}></div>
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-900 dark:text-white">
                            {member.firstName} {member.lastName}
                          </h3>
                          <p className="text-sm text-gray-600 dark:text-gray-400">
                            @{member.username}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="space-y-3">
                      <div className="flex items-center text-sm">
                        <MailIcon className="h-4 w-4 text-gray-400 mr-2 flex-shrink-0" />
                        <span className="text-gray-600 dark:text-gray-400 truncate">{member.email}</span>
                      </div>
                      <div className="flex items-center text-sm">
                        <Shield className="h-4 w-4 text-gray-400 mr-2 flex-shrink-0" />
                        <span className="text-gray-600 dark:text-gray-400">{formatRole(member.role)}</span>
                      </div>
                      <div className="flex items-center text-sm">
                        <Calendar className="h-4 w-4 text-gray-400 mr-2 flex-shrink-0" />
                        <span className="text-gray-600 dark:text-gray-400">{onlineStatus}</span>
                      </div>
                      {memberTeams.length > 0 && (
                        <div className="flex items-center text-sm">
                          <Users className="h-4 w-4 text-gray-400 mr-2 flex-shrink-0" />
                          <span className="text-gray-600 dark:text-gray-400">
                            {memberTeams.length} team{memberTeams.length !== 1 ? 's' : ''}
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
                      <div className="flex justify-between">
                        <div>
                          <p className="text-sm text-gray-600 dark:text-gray-400">User ID</p>
                          <p className="font-medium text-gray-900 dark:text-white">#{member.id}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm text-gray-600 dark:text-gray-400">Status</p>
                          <p className={`font-medium ${
                            isOnline ? 'text-green-600 dark:text-green-400' : 'text-gray-600 dark:text-gray-400'
                          }`}>
                            {isOnline ? 'Online' : 'Offline'}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="mt-6 flex space-x-3">
                      <Button 
                        variant="secondary" 
                        size="sm" 
                        className="flex-1"
                        onClick={() => {
                          setSelectedChatMember(member);
                          setChatModalOpen(true);
                        }}
                      >
                        Message
                      </Button>
                      <Button
                        variant="primary"
                        size="sm"
                        className="flex-1"
                        onClick={() => handleViewProfile(member)}
                      >
                        View Profile
                      </Button>
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        )
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {teams.length === 0 ? (
            <Card className="col-span-full">
              <div className="p-12 text-center">
                <div className="mx-auto h-16 w-16 text-gray-400 mb-4">
                  <Users className="h-16 w-16" />
                </div>
                <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-2">
                  No teams yet
                </h3>
                <p className="text-gray-600 dark:text-gray-400 mb-6">
                  Create your first team to start collaborating
                </p>
                <Button onClick={() => setIsCreateTeamModalOpen(true)}>
                  <Users className="h-4 w-4 mr-2" />
                  Create Team
                </Button>
              </div>
            </Card>
          ) : (
            teams.map((team) => {
              const creatorProfilePic = getProfilePictureUrl(team.createdBy);
              const teamPhotoUrl = getTeamPhotoUrl(team);
              const isCreator = team.createdBy?.id === currentUser?.id;
              
              return (
                <Card key={team.id} hover>
                  <div className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center space-x-3">
                        <div className="relative group">
                          <TeamPhoto 
                            teamId={team.id} 
                            alt={team.name} 
                            className="h-12 w-12 rounded-lg object-cover"
                          />
                          <div
                            className={`h-12 w-12 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center ${
                              teamPhotoUrl ? 'hidden' : 'flex'
                            }`}
                          >
                            <Users className="h-6 w-6 text-white" />
                          </div>
                          {isCreator && (
                            <button
                              onClick={() => handleOpenUploadPhotoModal(team)}
                              className="absolute -top-2 -right-2 p-1 bg-primary-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                              title="Upload team photo"
                            >
                              <Camera className="h-3 w-3" />
                            </button>
                          )}
                        </div>
                        <div className="flex-1">
                          <h3 className="font-semibold text-gray-900 dark:text-white">
                            {team.name}
                          </h3>
                          <div className="flex items-center space-x-2 mt-1">
                            {creatorProfilePic ? (
                              <img
                                src={creatorProfilePic}
                                alt={team.createdBy?.username}
                                className="h-5 w-5 rounded-full object-cover"
                              />
                            ) : (
                              <div className="h-5 w-5 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
                                <span className="text-xs font-medium text-primary-600 dark:text-primary-400">
                                  {getInitials(team.createdBy?.firstName, team.createdBy?.lastName)}
                                </span>
                              </div>
                            )}
                            <p className="text-sm text-gray-600 dark:text-gray-400">
                              {team.createdBy?.firstName} {team.createdBy?.lastName}
                            </p>
                          </div>
                        </div>
                      </div>
                      
                      {isCreator && (
                        <div className="flex space-x-1">
                          <button
                            onClick={() => handleOpenEditTeamModal(team)}
                            className="p-1 text-gray-500 hover:text-primary-600 dark:text-gray-400 dark:hover:text-primary-400"
                            title="Edit team"
                          >
                            <Edit className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleOpenDeleteConfirm(team)}
                            className="p-1 text-gray-500 hover:text-red-600 dark:text-gray-400 dark:hover:text-red-400"
                            title="Delete team"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      )}
                    </div>

                    <p className="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
                      {team.description || 'No description provided'}
                    </p>

                    <div className="space-y-2 mb-4">
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600 dark:text-gray-400">Members:</span>
                        <span className="font-medium text-gray-900 dark:text-white">
                          {team.members?.length || 0}
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600 dark:text-gray-400">Created:</span>
                        <span className="font-medium text-gray-900 dark:text-white">
                          {formatDate(team.createdAt)}
                        </span>
                      </div>
                    </div>

                    <div className="mt-6 flex space-x-3">
                      <Button
                        variant="secondary"
                        size="sm"
                        className="flex-1"
                        onClick={() => handleViewTeamMembers(team)}
                      >
                        <Users className="h-4 w-4 mr-2" />
                        View Members
                      </Button>
                      {isCreator && (
                        <Button
                          variant="primary"
                          size="sm"
                          className="flex-1"
                          onClick={() => handleOpenAddToTeamModal(team)}
                        >
                          <UserPlus className="h-4 w-4 mr-2" />
                          Add Member
                        </Button>
                      )}
                    </div>
                  </div>
                </Card>
              );
            })
          )}
        </div>
      )}

      <Modal
        isOpen={isAddMemberModalOpen}
        onClose={() => setIsAddMemberModalOpen(false)}
        title={selectedTeam ? `Add Member to ${selectedTeam.name}` : "Invite Team Member"}
        size="md"
      >
        <div className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Email Address
            </label>
            <Input
              type="email"
              value={inviteEmail}
              onChange={(e) => setInviteEmail(e.target.value)}
              placeholder="Enter email address"
              leftIcon={<MailIcon className="h-4 w-4 text-gray-400" />}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Role
            </label>
            <select
              value={inviteRole}
              onChange={(e) => setInviteRole(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
            >
              <option value="ROLE_USER">User</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </div>

          {selectedTeam && (
            <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
              <p className="text-sm text-blue-700 dark:text-blue-300">
                This user will be added to team "{selectedTeam.name}" after registration.
              </p>
            </div>
          )}

          <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
            <p className="text-sm text-blue-700 dark:text-blue-300">
              The invitation will be sent to the provided email address. The user will receive a link to register and join.
            </p>
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setIsAddMemberModalOpen(false);
                setSelectedTeam(null);
              }}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={handleSendInvite}
              isLoading={isSendingInvite}
              disabled={isSendingInvite}
            >
              <MailIcon className="h-4 w-4 mr-2" />
              Send Invitation
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isCreateTeamModalOpen}
        onClose={() => {
          setIsCreateTeamModalOpen(false);
          setNewTeamName('');
          setNewTeamDescription('');
        }}
        title="Create New Team"
        size="md"
      >
        <div className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Team Name *
            </label>
            <Input
              type="text"
              value={newTeamName}
              onChange={(e) => setNewTeamName(e.target.value)}
              placeholder="Enter team name"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Description
            </label>
            <textarea
              value={newTeamDescription}
              onChange={(e) => setNewTeamDescription(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
              rows={3}
              placeholder="Describe your team's purpose..."
            />
          </div>

          <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
            <p className="text-sm text-blue-700 dark:text-blue-300">
              You will be the team creator and first member. You can add more members after creation.
            </p>
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setIsCreateTeamModalOpen(false);
                setNewTeamName('');
                setNewTeamDescription('');
              }}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={handleCreateTeam}
              isLoading={isCreatingTeam}
              disabled={!newTeamName.trim() || isCreatingTeam}
            >
              <Users className="h-4 w-4 mr-2" />
              Create Team
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isEditTeamModalOpen}
        onClose={() => {
          setIsEditTeamModalOpen(false);
          setSelectedTeam(null);
        }}
        title="Edit Team"
        size="md"
      >
        <div className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Team Name *
            </label>
            <Input
              type="text"
              value={editTeamName}
              onChange={(e) => setEditTeamName(e.target.value)}
              placeholder="Enter team name"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Description
            </label>
            <textarea
              value={editTeamDescription}
              onChange={(e) => setEditTeamDescription(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
              rows={3}
              placeholder="Describe your team's purpose..."
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setIsEditTeamModalOpen(false);
                setSelectedTeam(null);
              }}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={handleUpdateTeam}
              isLoading={isUpdatingTeam}
              disabled={!editTeamName.trim() || isUpdatingTeam}
            >
              <Edit className="h-4 w-4 mr-2" />
              Update Team
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isDeleteConfirmOpen}
        onClose={() => {
          setIsDeleteConfirmOpen(false);
          setTeamToDelete(null);
        }}
        title="Delete Team"
        size="sm"
      >
        <div className="space-y-6">
          <p className="text-gray-600 dark:text-gray-400">
            Are you sure you want to delete "{teamToDelete?.name}"? This action cannot be undone.
          </p>

          <div className="flex justify-end space-x-3">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setIsDeleteConfirmOpen(false);
                setTeamToDelete(null);
              }}
            >
              Cancel
            </Button>
            <Button
              type="button"
              variant="danger"
              onClick={handleDeleteTeam}
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete Team
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isUploadPhotoModalOpen}
        onClose={() => {
          setIsUploadPhotoModalOpen(false);
          setSelectedTeamForPhoto(null);
          setTeamPhotoFile(null);
          setTeamPhotoPreview(null);
        }}
        title={`Upload Team Photo - ${selectedTeamForPhoto?.name}`}
        size="md"
      >
        <div className="space-y-6">
          <div className="flex flex-col items-center justify-center">
            {teamPhotoPreview ? (
              <div className="relative">
                <img
                  src={teamPhotoPreview}
                  alt="Team photo preview"
                  className="w-48 h-48 rounded-lg object-cover border-2 border-gray-200 dark:border-gray-700"
                />
                <button
                  onClick={() => {
                    setTeamPhotoFile(null);
                    setTeamPhotoPreview(null);
                  }}
                  className="absolute -top-2 -right-2 p-1 bg-red-500 text-white rounded-full"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            ) : (
              <div className="w-48 h-48 rounded-lg bg-gray-100 dark:bg-gray-800 border-2 border-dashed border-gray-300 dark:border-gray-700 flex flex-col items-center justify-center">
                <Camera className="h-12 w-12 text-gray-400 mb-2" />
                <p className="text-sm text-gray-500 dark:text-gray-400">No photo selected</p>
              </div>
            )}
          </div>

          <div>
            <input
              type="file"
              accept="image/*"
              onChange={handleTeamPhotoSelect}
              className="block w-full text-sm text-gray-500 dark:text-gray-400
                file:mr-4 file:py-2 file:px-4
                file:rounded-full file:border-0
                file:text-sm file:font-semibold
                file:bg-primary-50 file:text-primary-700
                dark:file:bg-primary-900 dark:file:text-primary-300
                hover:file:bg-primary-100 dark:hover:file:bg-primary-800"
            />
          </div>

          {selectedTeamForPhoto?.teamPhotoFileName && (
            <div className="flex justify-center">
              <Button
                variant="danger"
                size="sm"
                onClick={async () => {
                  if (selectedTeamForPhoto) {
                    await handleDeleteTeamPhoto(selectedTeamForPhoto);
                    setIsUploadPhotoModalOpen(false);
                  }
                }}
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Delete Current Photo
              </Button>
            </div>
          )}

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setIsUploadPhotoModalOpen(false);
                setSelectedTeamForPhoto(null);
                setTeamPhotoFile(null);
                setTeamPhotoPreview(null);
              }}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={handleUploadTeamPhoto}
              isLoading={isUploadingPhoto}
              disabled={!teamPhotoFile || isUploadingPhoto}
            >
              <Camera className="h-4 w-4 mr-2" />
              Upload Photo
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isTeamMembersModalOpen}
        onClose={() => {
          setIsTeamMembersModalOpen(false);
          setSelectedTeam(null);
        }}
        title={`${selectedTeam?.name} - Members`}
        size="lg"
      >
        {selectedTeam && (
          <div className="space-y-4">
            {selectedTeam.members?.length === 0 ? (
              <div className="text-center py-8">
                <Users className="h-12 w-12 mx-auto text-gray-400 mb-3" />
                <p className="text-gray-600 dark:text-gray-400">No members in this team yet</p>
                {selectedTeam.createdBy?.id === currentUser?.id && (
                  <Button
                    variant="primary"
                    size="sm"
                    className="mt-4"
                    onClick={() => {
                      setIsTeamMembersModalOpen(false);
                      handleOpenAddToTeamModal(selectedTeam);
                    }}
                  >
                    <UserPlus className="h-4 w-4 mr-2" />
                    Add Members
                  </Button>
                )}
              </div>
            ) : (
              <div className="divide-y divide-gray-200 dark:divide-gray-700">
                {selectedTeam.members?.map((member) => {
                  const profilePictureUrl = getProfilePictureUrl(member);
                  const isOnline = getUserStatus(member);
                  
                  return (
                    <div key={member.id} className="py-3 flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <div className="relative">
                          {profilePictureUrl ? (
                            <img
                              src={profilePictureUrl}
                              alt={member.username}
                              className="h-10 w-10 rounded-full object-cover"
                              onError={(e) => {
                                e.currentTarget.style.display = 'none';
                                const fallback = e.currentTarget.nextElementSibling as HTMLElement;
                                if (fallback) fallback.style.display = 'flex';
                              }}
                            />
                          ) : null}
                          <div
                            className={`h-10 w-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center ${
                              profilePictureUrl ? 'hidden' : 'flex'
                            }`}
                          >
                            <span className="text-sm font-bold text-white">
                              {getInitials(member.firstName, member.lastName)}
                            </span>
                          </div>
                          <div className={`absolute -bottom-1 -right-1 h-3 w-3 rounded-full border-2 border-white dark:border-gray-800 ${
                            isOnline ? 'bg-green-500' : 'bg-gray-400'
                          }`}></div>
                        </div>
                        <div>
                          <p className="font-medium text-gray-900 dark:text-white">
                            {member.firstName} {member.lastName}
                            {member.id === selectedTeam.createdBy?.id && (
                              <span className="ml-2 text-xs bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200 px-2 py-0.5 rounded-full">
                                Creator
                              </span>
                            )}
                          </p>
                          <p className="text-sm text-gray-600 dark:text-gray-400">
                            @{member.username}
                          </p>
                        </div>
                      </div>
                      
                      {selectedTeam.createdBy?.id === currentUser?.id && member.id !== currentUser.id && (
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleRemoveFromTeam(selectedTeam.id, member.id)}
                        >
                          Remove
                        </Button>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </Modal>

      <Modal
        isOpen={isAddToTeamModalOpen}
        onClose={() => {
          setIsAddToTeamModalOpen(false);
          setSelectedTeamForAdd(null);
        }}
        title={`Add Members to ${selectedTeamForAdd?.name}`}
        size="lg"
      >
        <div className="space-y-4">
          {availableMembers.length === 0 ? (
            <div className="text-center py-8">
              <Users className="h-12 w-12 mx-auto text-gray-400 mb-3" />
              <p className="text-gray-600 dark:text-gray-400">No available members to add</p>
              <p className="text-sm text-gray-500 dark:text-gray-500 mt-1">
                All users are already members of this team
              </p>
            </div>
          ) : (
            <div className="divide-y divide-gray-200 dark:divide-gray-700 max-h-96 overflow-y-auto">
              {availableMembers.map((member) => {
                const profilePictureUrl = getProfilePictureUrl(member);
                const isOnline = getUserStatus(member);
                
                return (
                  <div key={member.id} className="py-3 flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="relative">
                        {profilePictureUrl ? (
                          <img
                            src={profilePictureUrl}
                            alt={member.username}
                            className="h-10 w-10 rounded-full object-cover"
                            onError={(e) => {
                              e.currentTarget.style.display = 'none';
                              const fallback = e.currentTarget.nextElementSibling as HTMLElement;
                              if (fallback) fallback.style.display = 'flex';
                            }}
                          />
                        ) : null}
                        <div
                          className={`h-10 w-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center ${
                            profilePictureUrl ? 'hidden' : 'flex'
                          }`}
                        >
                          <span className="text-sm font-bold text-white">
                            {getInitials(member.firstName, member.lastName)}
                          </span>
                        </div>
                        <div className={`absolute -bottom-1 -right-1 h-3 w-3 rounded-full border-2 border-white dark:border-gray-800 ${
                          isOnline ? 'bg-green-500' : 'bg-gray-400'
                        }`}></div>
                      </div>
                      <div>
                        <p className="font-medium text-gray-900 dark:text-white">
                          {member.firstName} {member.lastName}
                        </p>
                        <p className="text-sm text-gray-600 dark:text-gray-400">
                          @{member.username} • {member.email}
                        </p>
                      </div>
                    </div>
                    
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => handleAddToTeam(selectedTeamForAdd!.id, member.id)}
                    >
                      <UserPlus className="h-4 w-4 mr-2" />
                      Add to Team
                    </Button>
                  </div>
                );
              })}
            </div>
          )}
          
          <div className="flex justify-end pt-4">
            <Button
              variant="secondary"
              onClick={() => {
                setIsAddToTeamModalOpen(false);
                setSelectedTeamForAdd(null);
              }}
            >
              Close
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isProfileModalOpen}
        onClose={handleCloseProfileModal}
        title="Member Profile"
        size="lg"
      >
        {selectedMember && (
          <div className="space-y-6">
            <div className="flex flex-col items-center space-y-4">
              <div className="relative">
                {(() => {
                  const profilePictureUrl = getProfilePictureUrl(selectedMember);
                  const isOnline = getUserStatus(selectedMember);
                  return (
                    <>
                      {profilePictureUrl ? (
                        <img
                          src={profilePictureUrl}
                          alt={selectedMember.username}
                          className="h-32 w-32 rounded-full object-cover border-4 border-white dark:border-gray-800"
                        />
                      ) : (
                        <div className="h-32 w-32 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-4 border-white dark:border-gray-800">
                          <span className="text-3xl font-bold text-white">
                            {getInitials(selectedMember.firstName, selectedMember.lastName)}
                          </span>
                        </div>
                      )}
                      <div className={`absolute bottom-2 right-2 h-5 w-5 rounded-full border-2 border-white dark:border-gray-800 ${
                        isOnline ? 'bg-green-500' : 'bg-gray-400'
                      }`}></div>
                    </>
                  );
                })()}
              </div>
              <div className="text-center">
                <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                  {selectedMember.firstName} {selectedMember.lastName}
                </h2>
                <p className="text-gray-600 dark:text-gray-400">
                  @{selectedMember.username}
                </p>
                <div className="mt-2 flex items-center justify-center space-x-2">
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                    selectedMember.role === 'ROLE_ADMIN' 
                      ? 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200' 
                      : 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
                  }`}>
                    {formatRole(selectedMember.role)}
                  </span>
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                    getUserStatus(selectedMember)
                      ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                      : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
                  }`}>
                    {getUserStatus(selectedMember) ? 'Online' : 'Offline'}
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div>
                  <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Contact Information</h3>
                  <div className="mt-2 space-y-2">
                    <div className="flex items-center">
                      <MailIcon className="h-4 w-4 text-gray-400 mr-2" />
                      <span className="text-gray-900 dark:text-white">{selectedMember.email}</span>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Account Information</h3>
                  <div className="mt-2 space-y-2">
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">User ID:</span>
                      <span className="font-medium text-gray-900 dark:text-white">#{selectedMember.id}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">Status:</span>
                      <span className={`font-medium ${
                        getUserStatus(selectedMember) ? 'text-green-600 dark:text-green-400' : 'text-gray-600 dark:text-gray-400'
                      }`}>
                        {getUserStatus(selectedMember) ? 'Online' : 'Offline'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="space-y-4">
                <div>
                  <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Member Since</h3>
                  <p className="mt-2 text-gray-900 dark:text-white">{formatDate(selectedMember.createdAt)}</p>
                </div>

                <div>
                  <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Last Activity</h3>
                  <p className="mt-2 text-gray-900 dark:text-white">{getOnlineStatus(selectedMember)}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    Account updated: {formatDate(selectedMember.updatedAt)}
                  </p>
                </div>
              </div>
            </div>

            <div className="pt-6 border-t border-gray-200 dark:border-gray-700">
              <div className="flex space-x-3">
                <Button 
                  variant="secondary" 
                  className="flex-1"
                  onClick={() => {
                    setSelectedChatMember(selectedMember);
                    setChatModalOpen(true);
                    handleCloseProfileModal();
                  }}
                >
                  <MailIcon className="h-4 w-4 mr-2" />
                  Send Message
                </Button>
                <Button 
                  variant="primary" 
                  className="flex-1"
                  onClick={handleAssignTaskClick}
                >
                  <CheckSquare className="h-4 w-4 mr-2" />
                  Assign Task
                </Button>
              </div>
            </div>
          </div>
        )}
      </Modal>

      <Modal
        isOpen={isAssignTaskModalOpen}
        onClose={handleCloseAssignTaskModal}
        title="Assign New Task"
        size="lg"
      >
        {selectedMember && (
          <div className="space-y-6">
            <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
              <div className="flex items-center space-x-3">
                {(() => {
                  const profilePictureUrl = getProfilePictureUrl(selectedMember);
                  const isOnline = getUserStatus(selectedMember);
                  return (
                    <div className="relative">
                      {profilePictureUrl ? (
                        <img
                          src={profilePictureUrl}
                          alt={selectedMember.username}
                          className="h-10 w-10 rounded-full object-cover border-2 border-white dark:border-gray-800"
                        />
                      ) : (
                        <div className="h-10 w-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800">
                          <span className="text-sm font-bold text-white">
                            {getInitials(selectedMember.firstName, selectedMember.lastName)}
                          </span>
                        </div>
                      )}
                      <div className={`absolute -bottom-1 -right-1 h-3 w-3 rounded-full border-2 border-white dark:border-gray-800 ${
                        isOnline ? 'bg-green-500' : 'bg-gray-400'
                      }`}></div>
                    </div>
                  );
                })()}
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Assigning task to:</p>
                  <p className="font-medium text-gray-900 dark:text-white">
                    {selectedMember.firstName} {selectedMember.lastName}
                  </p>
                  <p className={`text-xs ${getUserStatus(selectedMember) ? 'text-green-600 dark:text-green-400' : 'text-gray-600 dark:text-gray-400'}`}>
                    {getUserStatus(selectedMember) ? '● Online' : '● Offline'}
                  </p>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Task Title *
                </label>
                <Input
                  type="text"
                  value={taskTitle}
                  onChange={(e) => setTaskTitle(e.target.value)}
                  placeholder="Enter task title"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Description *
                </label>
                <textarea
                  value={taskDescription}
                  onChange={(e) => setTaskDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
                  rows={3}
                  placeholder="Describe the task..."
                  required
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Priority
                  </label>
                  <select
                    value={taskPriority}
                    onChange={(e) => setTaskPriority(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
                  >
                    {priorityOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Due Date
                  </label>
                  <Input
                    type="date"
                    value={taskDueDate}
                    onChange={(e) => setTaskDueDate(e.target.value)}
                    leftIcon={<Calendar className="h-4 w-4 text-gray-400" />}
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={handleCloseAssignTaskModal}
              >
                Cancel
              </Button>
              <Button
                type="button"
                variant="primary"
                onClick={handleCreateTask}
                isLoading={isCreatingTask}
                disabled={isCreatingTask}
              >
                <CheckSquare className="h-4 w-4 mr-2" />
                Assign Task
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {selectedChatMember && (
        <ChatModal
          receiverId={selectedChatMember.id}
          receiverName={`${selectedChatMember.firstName} ${selectedChatMember.lastName}`}
          receiverAvatar={getProfilePictureUrl(selectedChatMember)}
          isOpen={chatModalOpen}
          onClose={() => {
            setChatModalOpen(false);
            setSelectedChatMember(null);
          }}
        />
      )}
    </div>
  );
};

export default Team;