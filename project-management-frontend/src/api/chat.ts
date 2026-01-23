import api from '.';

export const chatApi = {
  getConversation: (userId1: number, userId2: number) =>
    api.get(`/api/chat/conversation/${userId1}/${userId2}`),
    
  getUnreadCount: (senderId: number) =>
    api.get(`/api/chat/unread/${senderId}`),
    
  markAsRead: (senderId: number, receiverId: number) =>
    api.post(`/api/chat/read/${senderId}/${receiverId}`),
};