export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: Priority;
  dueDate: string;
  createdAt: string;
  updatedAt: string;
  project: {
    id: number;
    name: string;
  };
  assignedUser: {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
  };
}

export interface TaskCreateRequest {
  title: string;
  description: string;
  projectId: number;
  assignedUserId: number;
  priority?: Priority;
  dueDate?: string;
}

export interface TaskUpdateRequest {
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: Priority;
  dueDate?: string;
  assignedUserId?: number;
}