import api from './api';

export const teamsApi = {
  createTeam: (data: { name: string; description: string }) => 
    api.post('/teams', data),
  
  getMyTeams: () => 
    api.get('/teams/my-teams'),
  
  getTeam: (teamId: number) => 
    api.get(`/teams/${teamId}`),
  
  updateTeam: (teamId: number, data: { name: string; description: string }) => 
    api.put(`/teams/${teamId}`, data),
  
  deleteTeam: (teamId: number) => 
    api.delete(`/teams/${teamId}`),
  
  getTeamMembers: (teamId: number) => 
    api.get(`/teams/${teamId}/members`),
  
  addMember: (teamId: number, userId: number) => 
    api.post(`/teams/${teamId}/members/${userId}`),
  
  removeMember: (teamId: number, userId: number) => 
    api.delete(`/teams/${teamId}/members/${userId}`),
  
  // Team photo endpoints
  uploadTeamPhoto: (teamId: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/teams/${teamId}/photo`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  
  getTeamPhoto: (teamId: number) => 
    `/api/teams/${teamId}/photo`,
  
  deleteTeamPhoto: (teamId: number) => 
    api.delete(`/teams/${teamId}/photo`),
};