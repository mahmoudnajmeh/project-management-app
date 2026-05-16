import React, { useState } from 'react';
import { 
  Download, PieChart, TrendingUp, 
  CheckSquare, FolderKanban
} from 'lucide-react';
import { useProjects } from '../hooks/useProjects';
import { useTasks } from '../hooks/useTasks';
import Card, { CardHeader, CardContent } from '../components/common/Card';
import Button from '../components/common/Button';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { useToast } from '../hooks/useToast';

const Reports: React.FC = () => {
  const [dateRange, setDateRange] = useState<'week' | 'month' | 'quarter' | 'year'>('month');
  const [isExporting, setIsExporting] = useState(false);
  const { projects, isLoading: projectsLoading } = useProjects();
  const { tasks, isLoading: tasksLoading } = useTasks();
  const { success, error } = useToast();
  
  const isLoading = projectsLoading || tasksLoading;

  const totalProjects = projects?.length || 0;
  const completedProjects = projects?.filter(p => p.status === 'COMPLETED').length || 0;
  const inProgressProjects = projects?.filter(p => p.status === 'IN_PROGRESS').length || 0;
  const plannedProjects = projects?.filter(p => p.status === 'PLANNED').length || 0;

  const totalTasks = tasks?.length || 0;
  const completedTasks = tasks?.filter(t => t.status === 'DONE').length || 0;
  const inProgressTasks = tasks?.filter(t => t.status === 'IN_PROGRESS').length || 0;
  const todoTasks = tasks?.filter(t => t.status === 'TODO').length || 0;
  const overdueTasks = tasks?.filter(t => new Date(t.dueDate) < new Date() && t.status !== 'DONE').length || 0;

  const urgentTasks = tasks?.filter(t => t.priority === 'URGENT').length || 0;
  const highTasks = tasks?.filter(t => t.priority === 'HIGH').length || 0;
  const mediumTasks = tasks?.filter(t => t.priority === 'MEDIUM').length || 0;
  const lowTasks = tasks?.filter(t => t.priority === 'LOW').length || 0;

  const projectCompletionRate = totalProjects > 0 ? Math.round((completedProjects / totalProjects) * 100) : 0;
  const taskCompletionRate = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

  const handleExport = async (format: 'csv' | 'pdf' = 'csv') => {
    try {
      setIsExporting(true);
      
      const reportData = {
        generatedAt: new Date().toISOString(),
        dateRange,
        summary: {
          totalProjects,
          completedProjects,
          inProgressProjects,
          plannedProjects,
          totalTasks,
          completedTasks,
          inProgressTasks,
          todoTasks,
          overdueTasks,
          projectCompletionRate,
          taskCompletionRate,
        },
        priorityDistribution: {
          urgent: urgentTasks,
          high: highTasks,
          medium: mediumTasks,
          low: lowTasks,
        },
        projects: projects?.map(p => ({
          id: p.id,
          name: p.name,
          status: p.status,
          createdAt: p.createdAt,
        })),
        tasks: tasks?.map(t => ({
          id: t.id,
          title: t.title,
          status: t.status,
          priority: t.priority,
          dueDate: t.dueDate,
          assignedTo: t.assignedUser ? `${t.assignedUser.firstName} ${t.assignedUser.lastName}` : 'Unassigned',
          project: t.project.name,
        })),
      };

      if (format === 'csv') {
        exportToCSV(reportData);
      } else {
        exportToPDF(reportData);
      }
      
      success(`Report exported as ${format.toUpperCase()} successfully!`);
    } catch (err) {
      error('Failed to export report');
      console.error('Export error:', err);
    } finally {
      setIsExporting(false);
    }
  };

  const exportToCSV = (data: any) => {
    const csvRows = [];
    
    csvRows.push('Report Generated,' + new Date(data.generatedAt).toLocaleString());
    csvRows.push('Date Range,' + data.dateRange);
    csvRows.push('');
    
    csvRows.push('SUMMARY');
    csvRows.push('Metric,Value');
    csvRows.push(`Total Projects,${data.summary.totalProjects}`);
    csvRows.push(`Completed Projects,${data.summary.completedProjects}`);
    csvRows.push(`In Progress Projects,${data.summary.inProgressProjects}`);
    csvRows.push(`Planned Projects,${data.summary.plannedProjects}`);
    csvRows.push(`Project Completion Rate,${data.summary.projectCompletionRate}%`);
    csvRows.push('');
    csvRows.push(`Total Tasks,${data.summary.totalTasks}`);
    csvRows.push(`Completed Tasks,${data.summary.completedTasks}`);
    csvRows.push(`In Progress Tasks,${data.summary.inProgressTasks}`);
    csvRows.push(`To Do Tasks,${data.summary.todoTasks}`);
    csvRows.push(`Overdue Tasks,${data.summary.overdueTasks}`);
    csvRows.push(`Task Completion Rate,${data.summary.taskCompletionRate}%`);
    csvRows.push('');
    
    csvRows.push('PRIORITY DISTRIBUTION');
    csvRows.push('Priority,Count');
    csvRows.push(`Urgent,${data.priorityDistribution.urgent}`);
    csvRows.push(`High,${data.priorityDistribution.high}`);
    csvRows.push(`Medium,${data.priorityDistribution.medium}`);
    csvRows.push(`Low,${data.priorityDistribution.low}`);
    csvRows.push('');
    
    csvRows.push('TASKS');
    csvRows.push('ID,Title,Status,Priority,Due Date,Assigned To,Project');
    data.tasks.forEach((task: any) => {
      csvRows.push(`${task.id},"${task.title}",${task.status},${task.priority},${new Date(task.dueDate).toLocaleDateString()},${task.assignedTo},${task.project}`);
    });
    csvRows.push('');
    
    csvRows.push('PROJECTS');
    csvRows.push('ID,Name,Status,Created Date');
    data.projects.forEach((project: any) => {
      csvRows.push(`${project.id},"${project.name}",${project.status},${new Date(project.createdAt).toLocaleDateString()}`);
    });

    const csvContent = csvRows.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `project-report-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const exportToPDF = (data: any) => {
    const printWindow = window.open('', '_blank');
    if (!printWindow) {
      error('Please allow pop-ups to export PDF');
      return;
    }

    const htmlContent = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Project Report</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 40px; }
          h1 { color: #333; border-bottom: 2px solid #4f46e5; padding-bottom: 10px; }
          h2 { color: #4f46e5; margin-top: 30px; }
          table { border-collapse: collapse; width: 100%; margin: 20px 0; }
          th { background: #4f46e5; color: white; padding: 10px; text-align: left; }
          td { padding: 8px; border-bottom: 1px solid #ddd; }
          .summary-box { display: inline-block; margin: 10px; padding: 15px; background: #f3f4f6; border-radius: 8px; }
          .metric { font-size: 24px; font-weight: bold; color: #4f46e5; }
          .label { color: #6b7280; font-size: 14px; }
        </style>
      </head>
      <body>
        <h1>Project Performance Report</h1>
        <p>Generated: ${new Date(data.generatedAt).toLocaleString()}</p>
        <p>Date Range: ${data.dateRange}</p>
        
        <h2>Summary</h2>
        <div style="display: flex; flex-wrap: wrap;">
          <div class="summary-box">
            <div class="metric">${data.summary.totalProjects}</div>
            <div class="label">Total Projects</div>
          </div>
          <div class="summary-box">
            <div class="metric">${data.summary.projectCompletionRate}%</div>
            <div class="label">Project Completion</div>
          </div>
          <div class="summary-box">
            <div class="metric">${data.summary.totalTasks}</div>
            <div class="label">Total Tasks</div>
          </div>
          <div class="summary-box">
            <div class="metric">${data.summary.taskCompletionRate}%</div>
            <div class="label">Task Completion</div>
          </div>
        </div>
        
        <h2>Tasks</h2>
        <table>
          <tr>
            <th>Title</th>
            <th>Status</th>
            <th>Priority</th>
            <th>Due Date</th>
            <th>Assigned To</th>
            <th>Project</th>
          </tr>
          ${data.tasks.map((task: any) => `
            <tr>
              <td>${task.title}</td>
              <td>${task.status}</td>
              <td>${task.priority}</td>
              <td>${new Date(task.dueDate).toLocaleDateString()}</td>
              <td>${task.assignedTo}</td>
              <td>${task.project}</td>
            </tr>
          `).join('')}
        </table>
        
        <h2>Projects</h2>
        <table>
          <tr>
            <th>Name</th>
            <th>Status</th>
            <th>Created Date</th>
          </tr>
          ${data.projects.map((project: any) => `
            <tr>
              <td>${project.name}</td>
              <td>${project.status}</td>
              <td>${new Date(project.createdAt).toLocaleDateString()}</td>
            </tr>
          `).join('')}
        </table>
      </body>
      </html>
    `;

    printWindow.document.write(htmlContent);
    printWindow.document.close();
    printWindow.focus();
    printWindow.print();
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
            Reports & Analytics
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Track your team's progress and performance
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <select
            value={dateRange}
            onChange={(e) => setDateRange(e.target.value as any)}
            className="px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-sm"
          >
            <option value="week">Last 7 days</option>
            <option value="month">Last 30 days</option>
            <option value="quarter">Last 90 days</option>
            <option value="year">Last 12 months</option>
          </select>
          <Button 
            variant="secondary" 
            size="sm"
            onClick={() => handleExport('csv')}
            disabled={isExporting}
            className="flex items-center"
            >
            {isExporting ? (
                <>
                <LoadingSpinner size="sm" />
                <span className="ml-2">Exporting...</span>
                </>
            ) : (
                <>
                <Download className="h-4 w-4 mr-2" />
                Export
                </>
            )}
            </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <FolderKanban className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <span className="text-2xl font-bold text-gray-900 dark:text-white">{totalProjects}</span>
            </div>
            <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Projects</h3>
            <div className="mt-2 flex items-center text-sm">
              <span className="text-green-600 dark:text-green-400">+{completedProjects}</span>
              <span className="text-gray-500 dark:text-gray-500 ml-1">completed</span>
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <CheckSquare className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <span className="text-2xl font-bold text-gray-900 dark:text-white">{totalTasks}</span>
            </div>
            <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Tasks</h3>
            <div className="mt-2 flex items-center text-sm">
              <span className="text-green-600 dark:text-green-400">{completedTasks}</span>
              <span className="text-gray-500 dark:text-gray-500 ml-1">done</span>
              <span className="text-red-600 dark:text-red-400 ml-3">{overdueTasks}</span>
              <span className="text-gray-500 dark:text-gray-500 ml-1">overdue</span>
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
                <TrendingUp className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
              <span className="text-2xl font-bold text-gray-900 dark:text-white">{projectCompletionRate}%</span>
            </div>
            <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">Project Completion</h3>
            <div className="mt-2 w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div 
                className="h-full bg-purple-600 rounded-full"
                style={{ width: `${projectCompletionRate}%` }}
              />
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
                <PieChart className="h-6 w-6 text-orange-600 dark:text-orange-400" />
              </div>
              <span className="text-2xl font-bold text-gray-900 dark:text-white">{taskCompletionRate}%</span>
            </div>
            <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">Task Completion</h3>
            <div className="mt-2 w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div 
                className="h-full bg-orange-600 rounded-full"
                style={{ width: `${taskCompletionRate}%` }}
              />
            </div>
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader title="Project Status" />
          <CardContent>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600 dark:text-gray-400">Completed</span>
                  <span className="font-medium text-gray-900 dark:text-white">{completedProjects}</span>
                </div>
                <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-green-500 rounded-full"
                    style={{ width: totalProjects > 0 ? `${(completedProjects / totalProjects) * 100}%` : '0%' }}
                  />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600 dark:text-gray-400">In Progress</span>
                  <span className="font-medium text-gray-900 dark:text-white">{inProgressProjects}</span>
                </div>
                <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-blue-500 rounded-full"
                    style={{ width: totalProjects > 0 ? `${(inProgressProjects / totalProjects) * 100}%` : '0%' }}
                  />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600 dark:text-gray-400">Planned</span>
                  <span className="font-medium text-gray-900 dark:text-white">{plannedProjects}</span>
                </div>
                <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-yellow-500 rounded-full"
                    style={{ width: totalProjects > 0 ? `${(plannedProjects / totalProjects) * 100}%` : '0%' }}
                  />
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader title="Task Priority" />
          <CardContent>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600 dark:text-gray-400">Urgent</span>
                  <span className="font-medium text-gray-900 dark:text-white">{urgentTasks}</span>
                </div>
                <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-red-500 rounded-full"
                    style={{ width: totalTasks > 0 ? `${(urgentTasks / totalTasks) * 100}%` : '0%' }}
                  />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600 dark:text-gray-400">High</span>
                  <span className="font-medium text-gray-900 dark:text-white">{highTasks}</span>
                </div>
                <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-orange-500 rounded-full"
                    style={{ width: totalTasks > 0 ? `${(highTasks / totalTasks) * 100}%` : '0%' }}
                  />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600 dark:text-gray-400">Medium</span>
                  <span className="font-medium text-gray-900 dark:text-white">{mediumTasks}</span>
                </div>
                <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-yellow-500 rounded-full"
                    style={{ width: totalTasks > 0 ? `${(mediumTasks / totalTasks) * 100}%` : '0%' }}
                  />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600 dark:text-gray-400">Low</span>
                  <span className="font-medium text-gray-900 dark:text-white">{lowTasks}</span>
                </div>
                <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-green-500 rounded-full"
                    style={{ width: totalTasks > 0 ? `${(lowTasks / totalTasks) * 100}%` : '0%' }}
                  />
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader title="Task Progress" />
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="inline-flex items-center justify-center h-16 w-16 rounded-full bg-gray-100 dark:bg-gray-800 mb-2">
                <span className="text-xl font-bold text-gray-900 dark:text-white">{todoTasks}</span>
              </div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">To Do</p>
            </div>
            <div className="text-center">
              <div className="inline-flex items-center justify-center h-16 w-16 rounded-full bg-blue-100 dark:bg-blue-900 mb-2">
                <span className="text-xl font-bold text-blue-600 dark:text-blue-400">{inProgressTasks}</span>
              </div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">In Progress</p>
            </div>
            <div className="text-center">
              <div className="inline-flex items-center justify-center h-16 w-16 rounded-full bg-green-100 dark:bg-green-900 mb-2">
                <span className="text-xl font-bold text-green-600 dark:text-green-400">{completedTasks}</span>
              </div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Completed</p>
            </div>
            <div className="text-center">
              <div className="inline-flex items-center justify-center h-16 w-16 rounded-full bg-red-100 dark:bg-red-900 mb-2">
                <span className="text-xl font-bold text-red-600 dark:text-red-400">{overdueTasks}</span>
              </div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Overdue</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Reports;