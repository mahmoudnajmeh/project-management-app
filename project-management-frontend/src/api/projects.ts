import api from '.';
import type { Project, ProjectCreateRequest, ProjectUpdateRequest } from '../types/project';

export const projectsApi = {
  getById: (id: number) => 
    api.get<Project>(`/projects/${id}`),
    
  getAll: () => 
    api.get<Project[]>('/projects'),
    
  getMyProjects: () => 
    api.get<Project[]>('/projects/my-projects'),
    
  create: (data: ProjectCreateRequest) => 
    api.post<Project>('/projects', data),
    
  update: (id: number, data: ProjectUpdateRequest) => 
    api.put<Project>(`/projects/${id}`, data),
    
  delete: (id: number) => 
    api.delete(`/projects/${id}`),
    
  search: (name: string) => 
    api.get<Project[]>(`/projects/search?name=${name}`),
};