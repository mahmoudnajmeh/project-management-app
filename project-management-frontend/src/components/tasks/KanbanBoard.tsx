import React from 'react';
import type { Task } from '../../types/task';
import TaskCard from './TaskCard';

interface KanbanBoardProps {
  tasks: Task[];
}

const KanbanBoard: React.FC<KanbanBoardProps> = ({ tasks }) => {
  const columns = [
    { id: 'TODO', title: 'To Do', color: 'bg-gray-100 dark:bg-gray-800' },
    { id: 'IN_PROGRESS', title: 'In Progress', color: 'bg-blue-50 dark:bg-blue-900/20' },
    { id: 'REVIEW', title: 'Review', color: 'bg-purple-50 dark:bg-purple-900/20' },
    { id: 'DONE', title: 'Done', color: 'bg-green-50 dark:bg-green-900/20' },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {columns.map((column) => {
        const columnTasks = tasks.filter(task => task.status === column.id);
        return (
          <div key={column.id} className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-semibold text-gray-900 dark:text-white">
                {column.title}
              </h3>
              <span className="text-sm text-gray-500 dark:text-gray-400">
                {columnTasks.length}
              </span>
            </div>
            <div className={`${column.color} rounded-lg p-4 min-h-[200px] space-y-4`}>
              {columnTasks.map((task) => (
                <TaskCard key={task.id} task={task} />
              ))}
              {columnTasks.length === 0 && (
                <div className="text-center py-8 text-gray-400 dark:text-gray-500">
                  No tasks
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default KanbanBoard;