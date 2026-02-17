import React, { useState, useEffect } from 'react';
import { Clock, RefreshCw } from 'lucide-react';
import Card, { CardHeader, CardContent } from '../common/Card';
import api from '../../api';
import LoadingSpinner from '../common/LoadingSpinner';
import Button from '../common/Button';

interface Activity {
  id: number;
  type: string;
  action: string;
  content: string;
  createdAt: string;
  user: {
    id: number;
    name: string;
    firstName: string;
    lastName: string;
    profilePictureUrl?: string;
    profilePictureFileName?: string;
  };
  entity: {
    id: number;
    type: string;
  };
}

const RecentActivity: React.FC = () => {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [imageErrors, setImageErrors] = useState<Set<string>>(new Set());

  const fetchActivities = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await api.get('/activity/recent');
      console.log('Activities received:', response.data);
      setActivities(response.data);
    } catch (error: any) {
      console.error('Failed to fetch activities:', error);
      setError(error.response?.data?.message || 'Failed to load activities');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchActivities();
  }, []);

  const getProfilePictureUrl = (fileName: string | undefined) => {
    if (!fileName) return null;
    const baseURL = api.defaults.baseURL || 'http://localhost:8080/api';
    return `${baseURL}/users/profile-picture/${fileName}?t=${Date.now()}`;
  };

  const handleImageError = (activityId: string) => {
    setImageErrors(prev => new Set(prev).add(activityId));
  };

  const getInitials = (firstName: string, lastName: string) => {
    return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase() || 'U';
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    const diffWeeks = Math.floor(diffDays / 7);
    const diffMonths = Math.floor(diffDays / 30);
    const diffYears = Math.floor(diffDays / 365);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min${diffMins === 1 ? '' : 's'} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours === 1 ? '' : 's'} ago`;
    if (diffDays < 7) return `${diffDays} day${diffDays === 1 ? '' : 's'} ago`;
    if (diffWeeks < 4) return `${diffWeeks} week${diffWeeks === 1 ? '' : 's'} ago`;
    if (diffMonths < 12) return `${diffMonths} month${diffMonths === 1 ? '' : 's'} ago`;
    return `${diffYears} year${diffYears === 1 ? '' : 's'} ago`;
  };

  if (isLoading) {
    return (
      <Card>
        <CardHeader
          title="Recent Activity"
          subtitle="All team activity"
        />
        <CardContent>
          <div className="flex justify-center items-center py-8">
            <LoadingSpinner size="md" />
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader
        title="Recent Activity"
        subtitle="All team activity"
        action={
          <Button variant="ghost" size="sm" onClick={fetchActivities}>
            <RefreshCw className="h-4 w-4" />
          </Button>
        }
      />
      <CardContent>
        {error ? (
          <div className="text-center py-8">
            <div className="text-red-500 mb-4">{error}</div>
            <Button onClick={fetchActivities}>Try Again</Button>
          </div>
        ) : activities.length === 0 ? (
          <div className="text-center py-8">
            <Clock className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
              No recent activity
            </h3>
            <p className="text-gray-600 dark:text-gray-400">
              When team members create or update projects and tasks, they'll appear here
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {activities.map((activity) => {
              const activityId = `${activity.type}-${activity.id}-${activity.createdAt}`;
              const profileUrl = getProfilePictureUrl(activity.user.profilePictureFileName);
              const showImage = profileUrl && !imageErrors.has(activityId);
              
              return (
                <div key={activityId} className="flex items-start space-x-3">
                  {showImage ? (
                    <div className="flex-shrink-0">
                      <img 
                        src={profileUrl}
                        alt={activity.user.firstName}
                        className="h-8 w-8 rounded-full object-cover"
                        onError={() => handleImageError(activityId)}
                      />
                    </div>
                  ) : (
                    <div className="h-8 w-8 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center flex-shrink-0">
                      <span className="text-xs font-bold text-white">
                        {getInitials(activity.user.firstName, activity.user.lastName)}
                      </span>
                    </div>
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900 dark:text-white">
                      <span className="font-semibold">
                        {activity.user.firstName} {activity.user.lastName}
                      </span>{' '}
                      <span className="text-gray-600 dark:text-gray-400">
                        {activity.action}{' '}
                        <span className="font-medium text-primary-600 dark:text-primary-400">
                          {activity.content}
                        </span>
                      </span>
                    </p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                      {formatTime(activity.createdAt)}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default RecentActivity;