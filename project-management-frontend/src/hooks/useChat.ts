import { useEffect, useRef, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';
import { useAuth } from './useAuth';
import { useToast } from './useToast';
import { chatApi } from '../api/chat';

interface ChatMessage {
  id?: number;
  type: 'CHAT' | 'JOIN' | 'LEAVE' | 'TYPING' | 'READ_RECEIPT' | 'NOTIFICATION' | 'TASK_CREATED' | 'TASK_UPDATED' | 'TASK_DELETED' | 'TASK_ASSIGNED' | 'TASK_COMPLETED' | 'PROJECT_CREATED' | 'PROJECT_UPDATED' | 'PROJECT_DELETED';
  content: string;
  senderId: number;
  senderName: string;
  senderAvatar?: string;
  receiverId?: number;
  roomId?: number;
  timestamp: string;
  read: boolean;
  entityId?: number;
  entityType?: string;
}

interface UseChatProps {
  receiverId?: number;
  onMessageReceived?: (message: ChatMessage) => void;
  onNotification?: (notification: any) => void;
}

export const useChat = ({ receiverId, onMessageReceived, onNotification }: UseChatProps = {}) => {
  const { user } = useAuth();
  const { error } = useToast();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [typingUsers, setTypingUsers] = useState<Set<number>>(new Set());
  const stompClient = useRef<Client | null>(null);
  const reconnectAttempts = useRef(0);
  const isConnecting = useRef(false);
  const hasLoadedInitialMessages = useRef(false);

  const loadExistingMessages = useCallback(async () => {
    if (!user || !receiverId || hasLoadedInitialMessages.current) return;

    try {
      const response = await chatApi.getConversation(user.id, receiverId);
      const existingMessages = response.data.map((msg: any) => ({
        id: msg.id,
        type: msg.type,
        content: msg.content,
        senderId: msg.senderId,
        senderName: msg.senderName,
        senderAvatar: msg.senderAvatar,
        receiverId: msg.receiverId,
        timestamp: msg.timestamp,
        read: msg.read,
        entityId: msg.entityId,
        entityType: msg.entityType
      }));
      
      setMessages(existingMessages);
      hasLoadedInitialMessages.current = true;
    } catch (err) {
      console.error('Error loading existing messages:', err);
    }
  }, [user, receiverId]);

  useEffect(() => {
    if (receiverId) {
      hasLoadedInitialMessages.current = false;
      loadExistingMessages();
    }
  }, [receiverId, loadExistingMessages]);

  const connect = useCallback(() => {
    if (!user || !user.id) {
      console.warn('No user or user ID found, skipping WebSocket connection');
      return;
    }

    if (stompClient.current?.connected || isConnecting.current) {
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token found');
      return;
    }

    isConnecting.current = true;

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        if (str.includes('ERROR:') || str.includes('error:')) {
          console.error('STOMP Error:', str);
        }
      },
      onConnect: () => {
        setIsConnected(true);
        reconnectAttempts.current = 0;
        isConnecting.current = false;

        client.subscribe('/user/queue/errors', (message: IMessage) => {
          console.error('WebSocket error:', message.body);
        });

        client.subscribe(`/user/${user.id}/queue/notifications`, (message: IMessage) => {
          try {
            const parsedMessage = JSON.parse(message.body);
            console.log('WebSocket notification received in useChat:', parsedMessage);
            
            onNotification?.(parsedMessage);
          } catch (e) {
            console.error('Error parsing notification:', e);
          }
        });

        if (receiverId) {
          client.subscribe(`/user/${user.id}/queue/messages`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              if (parsedMessage.type === 'CHAT' && 
                  (parsedMessage.senderId === receiverId || parsedMessage.receiverId === receiverId)) {
                handleMessage(parsedMessage);
              }
            } catch (e) {
              console.error('Error parsing message:', e);
            }
          });
          
          client.subscribe(`/user/${user.id}/queue/typing`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              if (parsedMessage.type === 'TYPING' && parsedMessage.senderId === receiverId) {
                handleMessage(parsedMessage);
              }
            } catch (e) {
              console.error('Error parsing typing message:', e);
            }
          });
          
          client.subscribe(`/user/${user.id}/queue/read`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              if (parsedMessage.type === 'READ_RECEIPT' && parsedMessage.senderId === receiverId) {
                handleMessage(parsedMessage);
              }
            } catch (e) {
              console.error('Error parsing read receipt:', e);
            }
          });
        }
      },
      onStompError: () => {
        setIsConnected(false);
        isConnecting.current = false;
        if (reconnectAttempts.current < 5) {
          reconnectAttempts.current += 1;
          setTimeout(connect, 5000);
        } else {
          error('Failed to connect to chat server');
        }
      },
      onWebSocketError: () => {
        setIsConnected(false);
        isConnecting.current = false;
      },
      onDisconnect: () => {
        setIsConnected(false);
        isConnecting.current = false;
      }
    });

    stompClient.current = client;
    client.activate();
  }, [user, receiverId, error, onNotification]);

  const disconnect = useCallback(() => {
    if (stompClient.current) {
      stompClient.current.deactivate();
      stompClient.current = null;
      setIsConnected(false);
      isConnecting.current = false;
    }
  }, []);

  const sendMessage = useCallback(async (message: Omit<ChatMessage, 'timestamp'>) => {
    if (!stompClient.current?.connected || !user) {
      console.warn('Cannot send message: WebSocket not connected or no user');
      return;
    }

    const fullMessage: ChatMessage = {
      ...message,
      timestamp: new Date().toISOString(),
      read: false
    };

    try {
      setMessages(prev => [...prev, fullMessage]);
      
      stompClient.current.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(fullMessage),
        headers: {
          'content-type': 'application/json'
        }
      });
    } catch (err) {
      console.error('Error sending message:', err);
      setMessages(prev => prev.filter(m => m !== fullMessage));
    }
  }, [user]);

  const sendTyping = useCallback(() => {
    if (!stompClient.current?.connected || !user || !receiverId) {
      return;
    }

    const typingMessage: ChatMessage = {
      type: 'TYPING',
      content: '',
      senderId: user.id,
      senderName: user.username,
      receiverId: receiverId,
      timestamp: new Date().toISOString(),
      read: true
    };

    stompClient.current.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify(typingMessage)
    });
  }, [user, receiverId]);

  const markAsRead = useCallback(() => {
    if (!stompClient.current?.connected || !user || !receiverId) {
      return;
    }

    const readMessage: ChatMessage = {
      type: 'READ_RECEIPT',
      content: '',
      senderId: user.id,
      senderName: user.username,
      receiverId: receiverId,
      timestamp: new Date().toISOString(),
      read: true
    };

    stompClient.current.publish({
      destination: '/app/chat.read',
      body: JSON.stringify(readMessage)
    });

    if (receiverId) {
      chatApi.markAsRead(user.id, receiverId).catch(console.error);
    }
  }, [user, receiverId]);

  const handleMessage = useCallback((message: ChatMessage) => {
    if (message.type === 'TYPING') {
      setTypingUsers(prev => {
        const newSet = new Set(prev);
        newSet.add(message.senderId);
        return newSet;
      });
      
      const typingTimer = setTimeout(() => {
        setTypingUsers(prev => {
          const newSet = new Set(prev);
          newSet.delete(message.senderId);
          return newSet;
        });
      }, 3000);
      
      return () => clearTimeout(typingTimer);
    } else if (message.type === 'READ_RECEIPT') {
      setMessages(prev => prev.map(m => 
        m.senderId === message.senderId && m.receiverId === user?.id 
          ? { ...m, read: true }
          : m
      ));
    } else if (message.type === 'CHAT') {
      setMessages(prev => {
        const exists = prev.some(m => 
          m.id === message.id || 
          (m.senderId === message.senderId && 
           m.receiverId === message.receiverId && 
           m.content === message.content && 
           Math.abs(new Date(m.timestamp).getTime() - new Date(message.timestamp).getTime()) < 1000)
        );
        return exists ? prev : [...prev, message];
      });
      onMessageReceived?.(message);
    }
  }, [user, onMessageReceived]);

  useEffect(() => {
    if (user && user.id) {
      connect();
    }
    
    return () => {
      disconnect();
    };
  }, [connect, disconnect, user]);

  return {
    messages,
    sendMessage,
    sendTyping,
    markAsRead,
    isConnected,
    typingUsers: Array.from(typingUsers),
    loadExistingMessages,
    connect,
    disconnect
  };
};