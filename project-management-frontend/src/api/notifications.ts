import api from '.';

export const notificationApi = {
  getNotifications: () =>
    api.get('/notifications'),
    
  getUnreadNotifications: () =>
    api.get('/notifications/unread'),
    
  getUnreadCount: () =>
    api.get('/notifications/count'),
    
  markAsRead: (notificationId: number) =>
    api.post(`/notifications/${notificationId}/read`),
    
  markAllAsRead: () =>
    api.post('/notifications/read-all'),
    
  deleteReadNotifications: () =>
    api.delete('/notifications/read'),
    
  deleteAllNotifications: () =>
    api.delete('/notifications'),
};