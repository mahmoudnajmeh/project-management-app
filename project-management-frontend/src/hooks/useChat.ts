import { useEffect, useRef, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';
import { useAuth } from './useAuth';
import { useToast } from './useToast';

interface ChatMessage {
  id?: number;
  type: 'CHAT' | 'JOIN' | 'LEAVE' | 'TYPING' | 'READ_RECEIPT';
  content: string;
  senderId: number;
  senderName: string;
  senderAvatar?: string;
  receiverId?: number;
  roomId?: number;
  timestamp: string;
  read: boolean;
}

interface UseChatProps {
  receiverId?: number;
  roomId?: number;
  onMessageReceived?: (message: ChatMessage) => void;
  onTyping?: (isTyping: boolean, senderId: number) => void;
}

export const useChat = ({ receiverId, roomId, onMessageReceived, onTyping }: UseChatProps = {}) => {
  const { user } = useAuth();
  const { error } = useToast();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [typingUsers, setTypingUsers] = useState<Set<number>>(new Set());
  const stompClient = useRef<Client | null>(null);
  const reconnectAttempts = useRef(0);

  const connect = useCallback(() => {
    if (!user) {
      console.error('No user found');
      return;
    }

    if (stompClient.current?.connected) {
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token found');
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        if (str.includes('error') || str.includes('ERROR')) {
          console.error('STOMP Error:', str);
        }
      },
      onConnect: () => {
        setIsConnected(true);
        reconnectAttempts.current = 0;

        client.subscribe('/user/queue/errors', (message: IMessage) => {
          console.error('WebSocket error:', message.body);
        });

        if (receiverId) {
          client.subscribe(`/user/${user.id}/queue/messages`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              handleMessage(parsedMessage);
            } catch (e) {
              console.error('Error parsing message:', e);
            }
          });
          
          client.subscribe(`/user/${user.id}/queue/typing`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              handleMessage(parsedMessage);
            } catch (e) {
              console.error('Error parsing typing message:', e);
            }
          });
          
          client.subscribe(`/user/${user.id}/queue/read`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              handleMessage(parsedMessage);
            } catch (e) {
              console.error('Error parsing read receipt:', e);
            }
          });
        } else if (roomId) {
          client.subscribe(`/topic/room/${roomId}`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              handleMessage(parsedMessage);
            } catch (e) {
              console.error('Error parsing message:', e);
            }
          });
        } else {
          client.subscribe(`/topic/public`, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              handleMessage(parsedMessage);
            } catch (e) {
              console.error('Error parsing message:', e);
            }
          });
        }

        const joinMessage: ChatMessage = {
          type: 'JOIN',
          content: `${user.username} joined the chat`,
          senderId: user.id,
          senderName: user.username,
          timestamp: new Date().toISOString(),
          read: true
        };
        
        sendMessage(joinMessage);
      },
      onStompError: (frame) => {
        setIsConnected(false);
        if (reconnectAttempts.current < 5) {
          reconnectAttempts.current += 1;
          setTimeout(connect, 5000);
        } else {
          error('Failed to connect to chat server');
        }
      },
      onWebSocketError: (event) => {
        setIsConnected(false);
      },
      onDisconnect: () => {
        setIsConnected(false);
      }
    });

    stompClient.current = client;
    client.activate();
  }, [user, receiverId, roomId, error]);

  const disconnect = useCallback(() => {
    if (stompClient.current) {
      stompClient.current.deactivate();
      stompClient.current = null;
      setIsConnected(false);
    }
  }, []);

  const sendMessage = useCallback((message: Omit<ChatMessage, 'timestamp'>) => {
    if (!stompClient.current?.connected || !user) {
      return;
    }

    const fullMessage: ChatMessage = {
      ...message,
      timestamp: new Date().toISOString(),
      read: false
    };

    try {
      stompClient.current.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(fullMessage),
        headers: {
          'content-type': 'application/json'
        }
      });
      
      if (message.receiverId || message.roomId) {
        setMessages(prev => [...prev, fullMessage]);
      }
    } catch (err) {
      console.error('Error sending message:', err);
    }
  }, [user]);

  const sendTyping = useCallback(() => {
    if (!stompClient.current?.connected || !user || !receiverId) return;

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
    if (!stompClient.current?.connected || !user || !receiverId) return;

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
  }, [user, receiverId]);

  const handleMessage = useCallback((message: ChatMessage) => {
    if (message.type === 'TYPING') {
      setTypingUsers(prev => new Set([...prev, message.senderId]));
      setTimeout(() => {
        setTypingUsers(prev => {
          const newSet = new Set(prev);
          newSet.delete(message.senderId);
          return newSet;
        });
      }, 3000);
      onTyping?.(true, message.senderId);
    } else if (message.type === 'READ_RECEIPT') {
      setMessages(prev => prev.map(m => 
        m.senderId === message.senderId && m.receiverId === user?.id 
          ? { ...m, read: true }
          : m
      ));
    } else {
      setMessages(prev => [...prev, message]);
      onMessageReceived?.(message);
    }
  }, [user, onMessageReceived, onTyping]);

  useEffect(() => {
    if (user) {
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
    connect,
    disconnect
  };
};