import React, { useState, useEffect, useRef } from 'react';
import { X, Send, Paperclip, Smile, Check, CheckCheck } from 'lucide-react';
import { useChat } from '../../hooks/useChat';
import { useAuth } from '../../hooks/useAuth';
import Button from '../common/Button';
import Input from '../common/Input';

interface ChatModalProps {
  receiverId: number;
  receiverName: string;
  receiverAvatar?: string | null;
  isOpen: boolean;
  onClose: () => void;
}

const ChatModal: React.FC<ChatModalProps> = ({
  receiverId,
  receiverName,
  receiverAvatar,
  isOpen,
  onClose
}) => {
  const { user } = useAuth();
  const [message, setMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutRef = useRef<any>(null);

  const {
    messages,
    sendMessage: sendChatMessage,
    sendTyping,
    markAsRead,
    isConnected,
    typingUsers
  } = useChat({
    receiverId,
    onMessageReceived: () => {
      markAsRead();
    },
    onTyping: (typing, senderId) => {
      if (senderId === receiverId) {
        setIsTyping(typing);
      }
    }
  });

  useEffect(() => {
    if (isOpen && receiverId) {
      markAsRead();
    }
  }, [isOpen, receiverId, markAsRead]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!message.trim() || !user) return;

    sendChatMessage({
      type: 'CHAT',
      content: message,
      senderId: user.id,
      senderName: user.username,
      receiverId: receiverId,
      read: false
    });

    setMessage('');
  };

  const handleTyping = () => {
    if (!typingTimeoutRef.current) {
      sendTyping();
    }

    clearTimeout(typingTimeoutRef.current);
    typingTimeoutRef.current = setTimeout(() => {
      typingTimeoutRef.current = null;
    }, 3000);
  };

  const formatTime = (timestamp: string) => {
    return new Date(timestamp).toLocaleTimeString([], { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  const getInitials = (name: string) => {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={onClose}></div>
        
        <div className="relative transform overflow-hidden rounded-lg bg-white dark:bg-gray-800 shadow-xl w-full max-w-2xl">
          <div className="flex items-center justify-between border-b border-gray-200 dark:border-gray-700 px-6 py-4">
            <div className="flex items-center space-x-3">
              <div className="relative">
                {receiverAvatar ? (
                  <img
                    src={receiverAvatar}
                    alt={receiverName}
                    className="h-10 w-10 rounded-full object-cover border-2 border-white dark:border-gray-800"
                  />
                ) : (
                  <div className="h-10 w-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800">
                    <span className="text-sm font-bold text-white">
                      {getInitials(receiverName)}
                    </span>
                  </div>
                )}
                <div className={`absolute -bottom-1 -right-1 h-3 w-3 rounded-full border-2 border-white dark:border-gray-800 ${
                  isConnected ? 'bg-green-500' : 'bg-gray-400'
                }`}></div>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 dark:text-white">
                  {receiverName}
                </h3>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  {isConnected ? 'Online' : 'Connecting...'}
                  {typingUsers.includes(receiverId) && ' • Typing...'}
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
            >
              <X className="h-5 w-5 text-gray-500" />
            </button>
          </div>

          <div className="h-[500px] flex flex-col">
            <div className="flex-1 overflow-y-auto p-6 space-y-4">
              {messages.length === 0 ? (
                <div className="text-center py-12">
                  <div className="mx-auto h-12 w-12 text-gray-400 mb-4">
                    <Send className="h-12 w-12" />
                  </div>
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                    No messages yet
                  </h3>
                  <p className="text-gray-600 dark:text-gray-400">
                    Start a conversation with {receiverName}
                  </p>
                </div>
              ) : (
                messages.map((msg, index) => (
                  <div
                    key={msg.id || index}
                    className={`flex ${msg.senderId === user?.id ? 'justify-end' : 'justify-start'}`}
                  >
                    <div className={`max-w-xs lg:max-w-md ${msg.senderId === user?.id ? 'ml-12' : 'mr-12'}`}>
                      <div className={`rounded-lg px-4 py-2 ${
                        msg.senderId === user?.id
                          ? 'bg-primary-600 text-white rounded-br-none'
                          : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white rounded-bl-none'
                      }`}>
                        <p className="text-sm">{msg.content}</p>
                      </div>
                      <div className={`flex items-center mt-1 text-xs ${
                        msg.senderId === user?.id ? 'justify-end' : 'justify-start'
                      }`}>
                        <span className="text-gray-500 dark:text-gray-400">
                          {formatTime(msg.timestamp)}
                        </span>
                        {msg.senderId === user?.id && (
                          <span className="ml-1">
                            {msg.read ? (
                              <CheckCheck className="h-3 w-3 text-blue-500" />
                            ) : (
                              <Check className="h-3 w-3 text-gray-400" />
                            )}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))
              )}
              {isTyping && (
                <div className="flex justify-start">
                  <div className="max-w-xs lg:max-w-md mr-12">
                    <div className="bg-gray-100 dark:bg-gray-700 rounded-lg rounded-bl-none px-4 py-2">
                      <div className="flex space-x-1">
                        <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce"></div>
                        <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                        <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            <div className="border-t border-gray-200 dark:border-gray-700 p-4">
              <form onSubmit={handleSendMessage} className="flex items-center space-x-2">
                <button
                  type="button"
                  className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
                >
                  <Paperclip className="h-5 w-5 text-gray-500" />
                </button>
                <button
                  type="button"
                  className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
                >
                  <Smile className="h-5 w-5 text-gray-500" />
                </button>
                <div className="flex-1">
                  <Input
                    type="text"
                    value={message}
                    onChange={(e) => {
                      setMessage(e.target.value);
                      handleTyping();
                    }}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        handleSendMessage(e);
                      }
                    }}
                    placeholder="Type your message..."
                    className="w-full"
                  />
                </div>
                <Button
                  type="submit"
                  disabled={!message.trim() || !isConnected}
                  className="flex-shrink-0"
                >
                  <Send className="h-4 w-4" />
                </Button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatModal;