import api from './api';

export const calendarApi = {
  getAll: () => api.get('/calendar'),
  getMyEvents: () => api.get('/calendar/my-events'),
  getEventsInRange: (start: string, end: string) => 
    api.get(`/calendar/range?start=${start}&end=${end}`),
  getEventsByType: (type: string) => api.get(`/calendar/type/${type}`),
  getProjectEvents: (projectId: number) => api.get(`/calendar/project/${projectId}`),
  getMonthEvents: (year: number, month: number) => api.get(`/calendar/month/${year}/${month}`),
  getEvent: (id: number) => api.get(`/calendar/${id}`),
  create: (eventData: any) => api.post('/calendar', eventData),
  update: (id: number, eventData: any) => api.put(`/calendar/${id}`, eventData),
  delete: (id: number) => api.delete(`/calendar/${id}`),
};