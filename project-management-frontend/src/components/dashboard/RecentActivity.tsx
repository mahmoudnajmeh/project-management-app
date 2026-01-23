import React from 'react';
import { CheckCircle, UserPlus, FileText, MessageSquare, GitBranch } from 'lucide-react';
import Card, { CardHeader, CardContent } from '../common/Card';

const RecentActivity: React.FC = () => {
  const activities = [
    {
      id: 1,
      user: 'Alex Johnson',
      action: 'completed a task',
      target: 'Design System Update',
      time: '5 min ago',
      icon: CheckCircle,
      color: 'text-green-500 bg-green-100 dark:bg-green-900',
    },
    {
      id: 2,
      user: 'Sarah Miller',
      action: 'added a new project',
      target: 'Mobile App Redesign',
      time: '1 hour ago',
      icon: FileText,
      color: 'text-blue-500 bg-blue-100 dark:bg-blue-900',
    },
    {
      id: 3,
      user: 'Mike Chen',
      action: 'commented on',
      target: 'Q3 Planning Document',
      time: '2 hours ago',
      icon: MessageSquare,
      color: 'text-purple-500 bg-purple-100 dark:bg-purple-900',
    },
    {
      id: 4,
      user: 'Emma Wilson',
      action: 'created a branch for',
      target: 'Feature/Auth-Improvements',
      time: '3 hours ago',
      icon: GitBranch,
      color: 'text-orange-500 bg-orange-100 dark:bg-orange-900',
    },
    {
      id: 5,
      user: 'David Park',
      action: 'joined the team',
      target: '',
      time: '5 hours ago',
      icon: UserPlus,
      color: 'text-pink-500 bg-pink-100 dark:bg-pink-900',
    },
  ];

  return (
    <Card>
      <CardHeader
        title="Recent Activity"
        subtitle="Team updates from the last 24 hours"
      />
      <CardContent>
        <div className="space-y-4">
          {activities.map((activity) => (
            <div key={activity.id} className="flex items-start space-x-3">
              <div className={`p-2 rounded-full ${activity.color}`}>
                <activity.icon className="h-4 w-4" />
              </div>
              <div className="flex-1">
                <p className="text-sm text-gray-900 dark:text-white">
                  <span className="font-semibold">{activity.user}</span>{' '}
                  {activity.action}{' '}
                  {activity.target && (
                    <span className="font-semibold text-primary-600 dark:text-primary-400">
                      {activity.target}
                    </span>
                  )}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  {activity.time}
                </p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default RecentActivity;