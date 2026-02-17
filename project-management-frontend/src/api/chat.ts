import api from '.';

export const chatApi = {
  getConversation: (userId1: number, userId2: number) =>
    api.get(`/chat/conversation/${userId1}/${userId2}`),
    
  getUnreadCount: (userId: number) =>
    api.get(`/chat/unread/${userId}`),
    
  getUnreadCountFromUser: (receiverId: number, senderId: number) =>
    api.get(`/chat/unread/${receiverId}/${senderId}`),
    
  markAsRead: (receiverId: number, senderId: number) =>
    api.post(`/chat/read/${receiverId}/${senderId}`),
    
  getUnreadMessages: (userId: number) =>
    api.get(`/chat/messages/unread/${userId}`),
  
  uploadFiles: (formData: FormData) =>
    api.post('/chat/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
};