import api from '.';
import type { TaskCreateRequest, TaskUpdateRequest } from '../types/task';

export const tasksApi = {
  getAll: () => 
    api.get('/tasks'),
    
  getMyTasks: () => 
    api.get('/tasks/my-tasks'),
    
  getById: (id: number) => 
    api.get(`/tasks/${id}`),
    
  getByProject: (projectId: number) => 
    api.get(`/tasks/project/${projectId}`),
    
  create: (data: TaskCreateRequest) => 
    api.post('/tasks', data),
    
  update: (id: number, data: TaskUpdateRequest) => 
    api.put(`/tasks/${id}`, data),
    
  delete: (id: number) => 
    api.delete(`/tasks/${id}`),
};