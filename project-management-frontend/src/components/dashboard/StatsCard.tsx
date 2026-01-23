import React from 'react';
import type { LucideIcon } from 'lucide-react';
import { cn } from '../../utils/helpers';

interface StatsCardProps {
  title: string;
  value: string;
  change: string;
  icon: LucideIcon;
  color: 'primary' | 'success' | 'warning' | 'danger';
}

const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  change,
  icon: Icon,
  color,
}) => {
  const colors = {
    primary: 'bg-primary-100 text-primary-600 dark:bg-primary-900 dark:text-primary-400',
    success: 'bg-green-100 text-green-600 dark:bg-green-900 dark:text-green-400',
    warning: 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900 dark:text-yellow-400',
    danger: 'bg-red-100 text-red-600 dark:bg-red-900 dark:text-red-400',
  };

  const changeColors = {
    primary: 'text-primary-600 dark:text-primary-400',
    success: 'text-green-600 dark:text-green-400',
    warning: 'text-yellow-600 dark:text-yellow-400',
    danger: 'text-red-600 dark:text-red-400',
  };

  const isPositive = !change.startsWith('-');

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600 dark:text-gray-400">
            {title}
          </p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white mt-2">
            {value}
          </p>
        </div>
        <div className={cn('p-3 rounded-full', colors[color])}>
          <Icon className="h-6 w-6" />
        </div>
      </div>
      <div className="mt-4 flex items-center">
        <span
          className={cn(
            'text-sm font-medium',
            isPositive ? changeColors[color] : 'text-gray-600 dark:text-gray-400'
          )}
        >
          {isPositive && '+'}
          {change}
        </span>
        <span className="text-sm text-gray-500 dark:text-gray-400 ml-2">
          from last period
        </span>
      </div>
    </div>
  );
};

export default StatsCard;