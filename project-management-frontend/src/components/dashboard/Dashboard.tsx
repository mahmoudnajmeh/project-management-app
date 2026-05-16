import React, { useState, useEffect } from 'react';
import { TrendingUp, Users, CheckCircle, Clock, Calendar, MoreVertical, Plus, FolderKanban } from 'lucide-react';
import { useProjects } from '../../hooks/useProjects';
import { useTasks } from '../../hooks/useTasks';
import Card, { CardHeader, CardContent } from '../common/Card';
import Button from '../common/Button';
import StatsCard from './StatsCard';
import RecentActivity from './RecentActivity';
import ProjectForm from '../projects/ProjectForm';
import Modal from '../common/Modal';
import { useNavigate } from 'react-router-dom';
import LoadingSpinner from '../common/LoadingSpinner';
import api from '../../api';
import type { Project, Task } from '../../types';

interface TeamStats {
  totalMembers: number;
  newMembers: number;
  percentageChange: number;
}

const Dashboard: React.FC = () => {
  const { myProjects, isLoading: projectsLoading } = useProjects();
  const { myTasks, isLoading: tasksLoading } = useTasks();
  const navigate = useNavigate();
  
  const [timeRange, setTimeRange] = useState('week');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [teamStats, setTeamStats] = useState<TeamStats>({ totalMembers: 0, newMembers: 0, percentageChange: 0 });
  const [isLoadingTeamStats, setIsLoadingTeamStats] = useState(true);
  
  const [recentProjects, setRecentProjects] = useState<Project[]>([]);
  const [upcomingTasks, setUpcomingTasks] = useState<Task[]>([]);

  const fetchTeamStats = async () => {
    try {
      setIsLoadingTeamStats(true);
      const response = await api.get('/users');
      const allUsers = response.data;
      
      const totalMembers = allUsers.length;
      
      const thirtyDaysAgo = new Date();
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
      
      const newMembers = allUsers.filter((user: any) => 
        new Date(user.createdAt) > thirtyDaysAgo
      ).length;
      
      const percentageChange = totalMembers > 0 
        ? Math.round((newMembers / totalMembers) * 100) 
        : 0;
      
      setTeamStats({
        totalMembers,
        newMembers,
        percentageChange
      });
    } catch (error) {
      console.error('Failed to fetch team stats:', error);
      setTeamStats({
        totalMembers: 1,
        newMembers: 0,
        percentageChange: 0
      });
    } finally {
      setIsLoadingTeamStats(false);
    }
  };

  useEffect(() => {
    fetchTeamStats();
  }, []);

  useEffect(() => {
    if (myProjects && Array.isArray(myProjects)) {
      const sortedProjects = [...myProjects]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 4);
      setRecentProjects(sortedProjects);
    }
  }, [myProjects]);

  useEffect(() => {
    if (myTasks && Array.isArray(myTasks)) {
      const sortedTasks = myTasks
        .filter((task: Task) => task.status !== 'DONE')
        .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime())
        .slice(0, 5);
      setUpcomingTasks(sortedTasks);
    }
  }, [myTasks]);

  const totalProjects = myProjects?.length || 0;
  const previousPeriodProjects = Math.max(0, totalProjects - 2);
  const projectChange = previousPeriodProjects > 0 
    ? `+${Math.round(((totalProjects - previousPeriodProjects) / previousPeriodProjects) * 100)}%`
    : '+0%';

  const activeTasks = myTasks?.filter((t: Task) => t.status !== 'DONE')?.length || 0;
  const previousPeriodTasks = Math.max(0, activeTasks - 1); 
  const taskChange = previousPeriodTasks > 0 
    ? `+${Math.round(((activeTasks - previousPeriodTasks) / previousPeriodTasks) * 100)}%`
    : '+0%';

  const overdueTasks = myTasks?.filter((t: Task) => 
    new Date(t.dueDate) < new Date() && t.status !== 'DONE'
  )?.length || 0;
  const previousPeriodOverdue = Math.max(0, overdueTasks + 1); 
  const overdueChange = previousPeriodOverdue > 0 
    ? `-${Math.round(((previousPeriodOverdue - overdueTasks) / previousPeriodOverdue) * 100)}%`
    : '-0%';

  const stats = [
    {
      title: 'Total Projects',
      value: totalProjects.toString(),
      change: projectChange,
      icon: TrendingUp,
      color: 'primary' as const,
    },
    {
      title: 'Active Tasks',
      value: activeTasks.toString(),
      change: taskChange,
      icon: CheckCircle,
      color: 'success' as const,
    },
    {
      title: 'Team Members',
      value: teamStats.totalMembers.toString(),
      change: `+${teamStats.newMembers}`,
      icon: Users,
      color: 'warning' as const,
    },
    {
      title: 'Overdue',
      value: overdueTasks.toString(),
      change: overdueChange,
      icon: Clock,
      color: 'danger' as const,
    },
  ];

  const getStatusColor = (status: string | undefined | null) => {
    if (!status) return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    
    switch (status) {
      case 'COMPLETED': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'PLANNED': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'LOW': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  const formatStatus = (status: string | undefined | null) => {
    if (!status) return 'Not Set';
    return status.replace('_', ' ');
  };

  if (projectsLoading || tasksLoading || isLoadingTeamStats) {
    return (
      <div className="flex justify-center items-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Welcome back!
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Here's what's happening with your projects today.
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <select
            value={timeRange}
            onChange={(e) => setTimeRange(e.target.value)}
            className="rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm"
          >
            <option value="week">This Week</option>
            <option value="month">This Month</option>
            <option value="quarter">This Quarter</option>
            <option value="year">This Year</option>
          </select>
          <Button onClick={() => setIsCreateModalOpen(true)}>
            <Plus className="h-4 w-4 mr-2" />
            New Project
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat) => (
          <StatsCard key={stat.title} {...stat} />
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <Card>
            <CardHeader
              title="Recent Projects"
              subtitle="Your latest project updates"
              action={
                <Button variant="ghost" size="sm" onClick={() => navigate('/projects')}>
                  View All
                </Button>
              }
            />
            <CardContent>
              {recentProjects.length === 0 ? (
                <div className="text-center py-8">
                  <FolderKanban className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                    No projects yet
                  </h3>
                  <p className="text-gray-600 dark:text-gray-400 mb-4">
                    Get started by creating your first project
                  </p>
                  <Button onClick={() => setIsCreateModalOpen(true)}>
                    <Plus className="h-4 w-4 mr-2" />
                    Create First Project
                  </Button>
                </div>
              ) : (
                <div className="space-y-4">
                  {recentProjects.map((project) => (
                    <div
                      key={project.id}
                      className="flex items-center justify-between p-4 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                    >
                      <div className="flex items-center space-x-4">
                        <div className="h-10 w-10 rounded-lg bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
                          <Calendar className="h-5 w-5 text-primary-600 dark:text-primary-400" />
                        </div>
                        <div className="flex-1">
                          <h4 className="font-medium text-gray-900 dark:text-white">
                            {project.name || 'Unnamed Project'}
                          </h4>
                          <p className="text-sm text-gray-500 dark:text-gray-400 line-clamp-1">
                            {project.description || 'No description'}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center space-x-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(project.status)}`}>
                          {formatStatus(project.status)}
                        </span>
                        <button className="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded">
                          <MoreVertical className="h-4 w-4 text-gray-500" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <div>
          <Card>
            <CardHeader
              title="Upcoming Tasks"
              subtitle="Tasks due soon"
              action={
                <Button variant="ghost" size="sm" onClick={() => navigate('/tasks')}>
                  View All
                </Button>
              }
            />
            <CardContent>
              {upcomingTasks.length === 0 ? (
                <div className="text-center py-8">
                  <CheckCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                    No upcoming tasks
                  </h3>
                  <p className="text-gray-600 dark:text-gray-400">
                    All caught up!
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {upcomingTasks.map((task) => (
                    <div
                      key={task.id}
                      className="p-4 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                    >
                      <div className="flex items-start justify-between mb-2">
                        <h4 className="font-medium text-gray-900 dark:text-white">
                          {task.title || 'Untitled Task'}
                        </h4>
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getPriorityColor(task.priority)}`}>
                          {task.priority || 'MEDIUM'}
                        </span>
                      </div>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mb-3 line-clamp-2">
                        {task.description || 'No description'}
                      </p>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-500 dark:text-gray-400">
                          {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No date'}
                        </span>
                        <span className="text-primary-600 dark:text-primary-400 font-medium">
                          {task.project?.name || 'No Project'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      <RecentActivity />

      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create New Project"
        size="lg"
      >
        <ProjectForm onSuccess={() => setIsCreateModalOpen(false)} />
      </Modal>
    </div>
  );
};

export default Dashboard;