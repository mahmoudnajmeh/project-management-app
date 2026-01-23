import type { Task } from './task';

export type ProjectStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Project {
  id: number;
  name: string;
  description: string;
  status: ProjectStatus;
  startDate: string;
  endDate: string;
  createdAt: string;
  updatedAt: string;
  createdBy: {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
  };
  tasks: Task[];
}

export interface ProjectCreateRequest {
  name: string;
  description: string;
  startDate?: string;
  endDate?: string;
}

export interface ProjectUpdateRequest {
  name?: string;
  description?: string;
  status?: ProjectStatus;
  startDate?: string;
  endDate?: string;
}