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

const isDevelopment = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

let globalStompClient: Client | null = null;
let globalConnectionAttempts = 0;
let globalIsConnected = false;
const globalSubscriptions = new Set<string>();

export const useChat = ({ receiverId, onMessageReceived, onNotification }: UseChatProps = {}) => {
  const { user } = useAuth();
  const { error } = useToast();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isConnected, setIsConnected] = useState(globalIsConnected);
  const [typingUsers, setTypingUsers] = useState<Set<number>>(new Set());
  const stompClient = useRef<Client | null>(null);
  const reconnectAttempts = useRef(0);
  const isConnecting = useRef(false);
  const hasLoadedInitialMessages = useRef(false);
  const maxReconnectAttempts = 10;
  const mounted = useRef(true);
  const heartbeatInterval = useRef<ReturnType<typeof setInterval> | null>(null);
  const connectionTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);
  const subscriptionsRef = useRef<Set<string>>(new Set());

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

  const startHeartbeat = useCallback(() => {
    if (heartbeatInterval.current) {
      clearInterval(heartbeatInterval.current);
    }
    
    heartbeatInterval.current = setInterval(() => {
      if (stompClient.current?.connected && user) {
        stompClient.current.publish({
          destination: '/app/heartbeat',
          body: JSON.stringify({ 
            userId: user.id, 
            timestamp: new Date().toISOString() 
          })
        });
        if (isDevelopment) {
          console.log('Heartbeat sent');
        }
      }
    }, 25000);
  }, [user]);

  const disconnect = useCallback(() => {
    subscriptionsRef.current.clear();
  }, []);

  const connect = useCallback(() => {
    if (!user || !user.id) {
      return;
    }

    if (globalStompClient?.connected) {
      stompClient.current = globalStompClient;
      setIsConnected(true);
      
      if (receiverId && !globalSubscriptions.has(`messages-${receiverId}`)) {
        globalSubscriptions.add(`messages-${receiverId}`);
        globalStompClient.subscribe(`/user/${user.id}/queue/messages`, (message: IMessage) => {
          if (!mounted.current) return;
          
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
      }
      
      if (receiverId && !globalSubscriptions.has(`typing-${receiverId}`)) {
        globalSubscriptions.add(`typing-${receiverId}`);
        globalStompClient.subscribe(`/user/${user.id}/queue/typing`, (message: IMessage) => {
          if (!mounted.current || !receiverId) return;
          
          try {
            const parsedMessage = JSON.parse(message.body);
            if (parsedMessage.type === 'TYPING' && parsedMessage.senderId === receiverId) {
              handleMessage(parsedMessage);
            }
          } catch (e) {
            console.error('Error parsing typing message:', e);
          }
        });
      }
      
      if (receiverId && !globalSubscriptions.has(`read-${receiverId}`)) {
        globalSubscriptions.add(`read-${receiverId}`);
        globalStompClient.subscribe(`/user/${user.id}/queue/read`, (message: IMessage) => {
          if (!mounted.current || !receiverId) return;
          
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
      
      return;
    }

    if (isConnecting.current) {
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token found');
      return;
    }

    isConnecting.current = true;

    if (isDevelopment) {
      console.log('🔌 Creating WebSocket connection...');
    }

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        if (isDevelopment && (str.includes('ERROR') || str.includes('error'))) {
          console.log('STOMP:', str);
        }
      },
      onConnect: () => {
        if (!mounted.current) return;
        
        if (isDevelopment) {
          console.log('✅ WebSocket Connected successfully at', new Date().toLocaleTimeString());
        }
        
        globalStompClient = client;
        globalIsConnected = true;
        stompClient.current = client;
        setIsConnected(true);
        reconnectAttempts.current = 0;
        isConnecting.current = false;
        
        startHeartbeat();

        if (connectionTimeout.current) {
          clearTimeout(connectionTimeout.current);
        }
        connectionTimeout.current = setTimeout(() => {
          if (stompClient.current?.connected) {
            if (isDevelopment) {
              console.log('Connection timeout - sending ping');
            }
            stompClient.current.publish({
              destination: '/app/ping',
              body: JSON.stringify({ userId: user.id, timestamp: new Date().toISOString() })
            });
          }
        }, 30000);

        client.subscribe('/user/queue/errors', (message: IMessage) => {
          console.error('❌ Server error:', message.body);
        });

        client.subscribe(`/user/${user.id}/queue/notifications`, (message: IMessage) => {
          if (!mounted.current) return;
          
          try {
            if (connectionTimeout.current) {
              clearTimeout(connectionTimeout.current);
              connectionTimeout.current = setTimeout(() => {
                if (stompClient.current?.connected) {
                  stompClient.current.publish({
                    destination: '/app/ping',
                    body: JSON.stringify({ userId: user.id, timestamp: new Date().toISOString() })
                  });
                }
              }, 30000);
            }

            const parsedMessage = JSON.parse(message.body);
            
            if (isDevelopment) {
              console.log('📨 Notification received:', parsedMessage.type, 'at', new Date().toLocaleTimeString());
            }
            
            const messageTime = parsedMessage.timestamp || parsedMessage.createdAt;
            if (messageTime) {
              const msgTime = new Date(messageTime).getTime();
              const now = new Date().getTime();
              const ageInSeconds = (now - msgTime) / 1000;
              
              if (ageInSeconds > 10) {
                if (isDevelopment) {
                  console.log(`⏰ Ignoring old notification (${ageInSeconds.toFixed(1)} seconds old)`);
                }
                return;
              }
            }
            
            onNotification?.(parsedMessage);
          } catch (e) {
            console.error('Error parsing notification:', e);
          }
        });

        if (receiverId) {
          if (isDevelopment) {
            console.log(`📝 Subscribing to messages for receiver ${receiverId}`);
          }
          
          client.subscribe(`/user/${user.id}/queue/messages`, (message: IMessage) => {
            if (!mounted.current) return;
            
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
            if (!mounted.current) return;
            
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
            if (!mounted.current) return;
            
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

        client.publish({
          destination: '/app/user.connect',
          body: JSON.stringify({ 
            userId: user.id, 
            timestamp: new Date().toISOString() 
          })
        });
      },
      onStompError: (frame) => {
        if (!mounted.current) return;
        
        console.error('❌ STOMP error:', frame);
        setIsConnected(false);
        globalIsConnected = false;
        isConnecting.current = false;
        
        if (reconnectAttempts.current < maxReconnectAttempts) {
          reconnectAttempts.current += 1;
          if (isDevelopment) {
            console.log(`🔄 Reconnecting... Attempt ${reconnectAttempts.current}/${maxReconnectAttempts}`);
          }
          setTimeout(connect, 5000);
        }
      },
      onWebSocketError: (event) => {
        if (!mounted.current) return;
        
        console.error('❌ WebSocket error:', event);
        setIsConnected(false);
        globalIsConnected = false;
        isConnecting.current = false;
      },
      onDisconnect: () => {
        if (!mounted.current) return;
        
        if (isDevelopment) {
          console.log('🔌 WebSocket Disconnected at', new Date().toLocaleTimeString());
        }
        
        setIsConnected(false);
        globalIsConnected = false;
        isConnecting.current = false;
        
        if (heartbeatInterval.current) {
          clearInterval(heartbeatInterval.current);
        }
        
        if (user && reconnectAttempts.current < maxReconnectAttempts) {
          reconnectAttempts.current += 1;
          if (isDevelopment) {
            console.log(`🔄 Reconnecting... Attempt ${reconnectAttempts.current}/${maxReconnectAttempts}`);
          }
          setTimeout(connect, 5000);
        }
      }
    });

    globalStompClient = client;
    stompClient.current = client;
    client.activate();
  }, [user, receiverId, onNotification, startHeartbeat]);

  const sendMessage = useCallback(async (message: Omit<ChatMessage, 'timestamp'>) => {
    if (!stompClient.current?.connected || !user) {
      console.warn('Cannot send message: WebSocket not connected');
      connect();
      return false;
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
      
      return true;
    } catch (err) {
      console.error('Error sending message:', err);
      setMessages(prev => prev.filter(m => m !== fullMessage));
      return false;
    }
  }, [user, connect]);

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
      
      setTimeout(() => {
        setTypingUsers(prev => {
          const newSet = new Set(prev);
          newSet.delete(message.senderId);
          return newSet;
        });
      }, 3000);
      
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
    mounted.current = true;
    
    if (user && user.id) {
      const timer = setTimeout(() => {
        if (mounted.current) {
          connect();
        }
      }, 100);
      
      return () => {
        clearTimeout(timer);
        mounted.current = false;
      };
    }
    
    return () => {
      mounted.current = false;
    };
  }, [user]); 

  useEffect(() => {
    if (user && user.id && receiverId && stompClient.current?.connected) {
      connect();
    }
  }, [receiverId]);

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