import React, { useState, useEffect } from 'react';
import { Menu, Bell, Search, Sun, Moon, User, CheckCheck, Trash2 } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useChat } from '../../hooks/useChat';
import { chatApi } from '../../api/chat';
import { notificationApi } from '../../api/notifications';
import { usersApi } from '../../api/users';
import ChatModal from '../team/ChatModal';

interface HeaderProps {
  onMenuClick?: () => void;
}

interface Notification {
  id: number;
  type: string;
  content: string;
  senderId?: number;
  senderName?: string;
  timestamp: string;
  read: boolean;
  entityId?: number;
  entityType?: string;
}

const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [selectedChatMember, setSelectedChatMember] = useState<{ 
    id: number; 
    name: string; 
    avatar?: string | null 
  } | null>(null);
  const [chatModalOpen, setChatModalOpen] = useState(false);
  const [userProfiles, setUserProfiles] = useState<Record<number, { avatar?: string }>>({});
  
  const { connect, disconnect } = useChat({
    onMessageReceived: (message) => {
      console.log('Chat message received:', message);
      if (message.type === 'CHAT') {
        addNotification({
          id: Date.now(),
          type: 'MESSAGE',
          content: `New message from ${message.senderName}`,
          senderId: message.senderId,
          senderName: message.senderName,
          timestamp: message.timestamp,
          read: false
        });
      }
    },
    onNotification: (notification) => {
      console.log('WebSocket notification received:', notification);
      
      if (notification.type && notification.content) {
        if (notification.type === 'UNREAD_COUNT_UPDATE') {
          setUnreadCount(notification.unreadCount || 0);
        } else {
          addNotification({
            id: notification.id || Date.now(),
            type: notification.type,
            content: notification.content,
            senderId: notification.senderId,
            senderName: notification.senderName,
            entityId: notification.entityId,
            entityType: notification.entityType,
            timestamp: notification.timestamp || notification.createdAt || new Date().toISOString(),
            read: notification.read || false
          });
        }
      }
    }
  });

  const addNotification = (notification: Notification) => {
    setNotifications(prev => {
      const exists = prev.some(n => 
        n.id === notification.id || 
        (n.type === notification.type && 
         n.senderId === notification.senderId && 
         n.content === notification.content &&
         Math.abs(new Date(n.timestamp).getTime() - new Date(notification.timestamp).getTime()) < 5000)
      );
      return exists ? prev : [notification, ...prev];
    });
    
    if (!notification.read) {
      setUnreadCount(prev => prev + 1);
    }
  };

  useEffect(() => {
    if (user) {
      connect();
      loadAllNotifications();
      loadAllUsers(); // Load all users to get their profile pictures
    }
    
    return () => {
      disconnect();
    };
  }, [user]);

  const loadAllUsers = async () => {
    if (!user) return;
    
    try {
      const response = await usersApi.getAllUsers();
      const profiles: Record<number, { avatar?: string }> = {};
      
      response.data.forEach((user: any) => {
        if (user.profilePictureFileName) {
          profiles[user.id] = {
            avatar: `/api/users/profile-picture/${user.profilePictureFileName}`
          };
        }
      });
      
      setUserProfiles(profiles);
    } catch (error) {
      console.error('Error loading users:', error);
    }
  };

  const loadAllNotifications = async () => {
    await fetchDatabaseNotifications();
    await fetchChatNotifications();
    await fetchUnreadCount();
  };

  const toggleDarkMode = () => {
    setIsDarkMode(!isDarkMode);
    if (isDarkMode) {
      document.documentElement.classList.remove('dark');
    } else {
      document.documentElement.classList.add('dark');
    }
  };

  const fetchDatabaseNotifications = async () => {
    if (!user) return;
    
    try {
      const response = await notificationApi.getNotifications();
      const dbNotifications = response.data.map((notif: any) => ({
        id: notif.id,
        type: notif.type,
        content: notif.content,
        senderId: notif.senderId,
        senderName: notif.senderName,
        entityId: notif.entityId,
        entityType: notif.entityType,
        timestamp: notif.createdAt,
        read: notif.read
      }));
      
      setNotifications(prev => {
        const newNotifs = [...dbNotifications];
        prev.forEach(n => {
          if (!newNotifs.some(dbn => dbn.id === n.id)) {
            newNotifs.push(n);
          }
        });
        return newNotifs;
      });
    } catch (error) {
      console.error('Error fetching database notifications:', error);
    }
  };

  const fetchUnreadCount = async () => {
    if (!user) return;
    
    try {
      const dbCount = await notificationApi.getUnreadCount();
      const chatCount = await chatApi.getUnreadCount(user.id);
      const totalUnread = (dbCount.data || 0) + (chatCount.data || 0);
      setUnreadCount(totalUnread);
    } catch (error) {
      console.error('Error fetching unread count:', error);
      const localUnread = notifications.filter(n => !n.read).length;
      setUnreadCount(localUnread);
    }
  };

  const fetchChatNotifications = async () => {
    if (!user) return;
    
    try {
      const response = await chatApi.getUnreadMessages(user.id);
      const chatNotifications = response.data.map((msg: any) => ({
        id: msg.id || Date.now(),
        type: 'MESSAGE',
        content: `New message from ${msg.senderName}`,
        senderId: msg.senderId,
        senderName: msg.senderName,
        timestamp: msg.timestamp,
        read: msg.read
      }));
      
      setNotifications(prev => {
        const newNotifs = [...chatNotifications];
        prev.forEach(n => {
          if (!newNotifs.some(cn => cn.id === n.id)) {
            newNotifs.push(n);
          }
        });
        return newNotifs;
      });
    } catch (error) {
      console.error('Error fetching chat notifications:', error);
    }
  };

  const markNotificationAsRead = async (notification: Notification) => {
    try {
      if (notification.type.includes('PROJECT') || notification.type.includes('TASK')) {
        await notificationApi.markAsRead(notification.id);
      } else if (notification.type === 'MESSAGE' && notification.senderId && user) {
        await chatApi.markAsRead(user.id, notification.senderId);
      }
      
      setNotifications(prev => 
        prev.map(notif => 
          notif.id === notification.id ? { ...notif, read: true } : notif
        )
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.read) {
      markNotificationAsRead(notification);
    }
    
    if (notification.type === 'MESSAGE' && notification.senderId && notification.senderName) {
      // Try to get the sender's profile picture from cached data
      const senderAvatar = userProfiles[notification.senderId]?.avatar;
      
      setSelectedChatMember({
        id: notification.senderId,
        name: notification.senderName,
        avatar: senderAvatar
      });
      setChatModalOpen(true);
    } else if (notification.type.includes('TASK') && notification.entityId) {
      window.location.href = `/tasks/${notification.entityId}`;
    } else if (notification.type.includes('PROJECT') && notification.entityId) {
      window.location.href = `/projects/${notification.entityId}`;
    }
    
    setShowNotifications(false);
  };

  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      if (user) {
        const unreadMessages = notifications.filter(n => n.type === 'MESSAGE' && !n.read && n.senderId);
        for (const msg of unreadMessages) {
          if (msg.senderId) {
            await chatApi.markAsRead(user.id, msg.senderId);
          }
        }
      }
      
      setNotifications(prev => prev.map(notif => ({ ...notif, read: true })));
      setUnreadCount(0);
    } catch (error) {
      console.error('Error marking all as read:', error);
    }
  };

  const clearAllNotifications = async () => {
    try {
      await notificationApi.deleteAllNotifications();
      setNotifications([]);
      setUnreadCount(0);
    } catch (error) {
      console.error('Error clearing all notifications:', error);
    }
  };

  const getNotificationIcon = (type: string) => {
    if (type === 'MESSAGE') return '💬';
    if (type.includes('PROJECT')) return '📋';
    if (type.includes('TASK')) {
      if (type === 'TASK_COMPLETED') return '✅';
      if (type === 'TASK_REASSIGNED') return '🔄';
      return '📝';
    }
    return '🔔';
  };

  const getInitials = () => {
    if (!user) return 'U';
    return `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase() || 'U';
  };

  const getProfilePictureUrl = () => {
    if (!user) return undefined;
    const timestamp = Date.now();
    if (user.profilePictureFileName) {
      return `/api/users/profile-picture/${user.profilePictureFileName}?t=${timestamp}`;
    }
    return undefined;
  };

  const formatTime = (timestamp: string) => {
    try {
      const date = new Date(timestamp);
      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays === 1) return 'Yesterday';
      return `${diffDays}d ago`;
    } catch (error) {
      return 'Recently';
    }
  };

  const profilePictureUrl = getProfilePictureUrl();

  return (
    <>
      <header className="sticky top-0 z-40 border-b border-gray-200 dark:border-gray-800 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md">
        <div className="px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center">
              <button
                onClick={onMenuClick}
                className="p-2 rounded-md text-gray-500 hover:text-gray-600 dark:text-gray-400 dark:hover:text-gray-300 lg:hidden"
              >
                <Menu className="h-5 w-5" />
              </button>
              
              <div className="hidden lg:flex items-center space-x-4">
                <h1 className="text-xl font-semibold text-gray-900 dark:text-white">
                  ProjectFlow
                </h1>
              </div>
            </div>

            <div className="flex-1 max-w-2xl mx-4 hidden md:block">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="search"
                  placeholder="Search projects, tasks, or team members..."
                  className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-300 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <button
                onClick={toggleDarkMode}
                className="p-2 rounded-md text-gray-500 hover:text-gray-600 dark:text-gray-400 dark:hover:text-gray-300"
                aria-label="Toggle dark mode"
              >
                {isDarkMode ? (
                  <Sun className="h-5 w-5" />
                ) : (
                  <Moon className="h-5 w-5" />
                )}
              </button>

              <div className="relative">
                <button
                  onClick={() => {
                    setShowNotifications(!showNotifications);
                    if (!showNotifications) {
                      loadAllNotifications();
                    }
                  }}
                  className="p-2 rounded-md text-gray-500 hover:text-gray-600 dark:text-gray-400 dark:hover:text-gray-300 relative"
                  aria-label="Notifications"
                >
                  <Bell className="h-5 w-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 h-5 w-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </button>

                {showNotifications && (
                  <>
                    <div
                      className="fixed inset-0 z-10"
                      onClick={() => setShowNotifications(false)}
                    />
                    <div className="absolute right-0 mt-2 w-96 max-h-96 overflow-y-auto rounded-md shadow-lg py-1 bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 z-20">
                      <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
                        <div>
                          <p className="text-sm font-medium text-gray-900 dark:text-white">
                            Notifications
                          </p>
                          {unreadCount > 0 && (
                            <p className="text-xs text-gray-500 dark:text-gray-400">
                              {unreadCount} unread notification{unreadCount !== 1 ? 's' : ''}
                            </p>
                          )}
                        </div>
                        <div className="flex space-x-2">
                          {unreadCount > 0 && (
                            <button
                              onClick={markAllAsRead}
                              className="text-xs text-primary-600 dark:text-primary-400 hover:text-primary-800 dark:hover:text-primary-300"
                              title="Mark all as read"
                            >
                              <CheckCheck className="h-4 w-4" />
                            </button>
                          )}
                          <button
                            onClick={clearAllNotifications}
                            className="text-xs text-red-600 dark:text-red-400 hover:text-red-800 dark:hover:text-red-300"
                            title="Clear all notifications"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </div>
                      
                      {notifications.length === 0 ? (
                        <div className="px-4 py-6 text-center">
                          <Bell className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                          <p className="text-sm text-gray-500 dark:text-gray-400">
                            No notifications
                          </p>
                        </div>
                      ) : (
                        notifications.map((notification) => (
                          <div
                            key={notification.id}
                            className={`px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer border-l-4 ${
                              !notification.read 
                                ? 'border-primary-500 bg-blue-50 dark:bg-blue-900/20' 
                                : 'border-transparent'
                            }`}
                            onClick={() => handleNotificationClick(notification)}
                          >
                            <div className="flex items-start">
                              <div className="flex-shrink-0 pt-1">
                                <span className="text-lg">{getNotificationIcon(notification.type)}</span>
                              </div>
                              <div className="ml-3 flex-1">
                                <p className="text-sm text-gray-900 dark:text-white">
                                  {notification.content}
                                </p>
                                {notification.senderName && (
                                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                                    {notification.type === 'MESSAGE' ? 'From: ' : 'By: '}{notification.senderName}
                                  </p>
                                )}
                                <div className="flex justify-between items-center mt-1">
                                  <p className="text-xs text-gray-500 dark:text-gray-400">
                                    {formatTime(notification.timestamp)}
                                  </p>
                                  {!notification.read && (
                                    <div className="flex-shrink-0">
                                      <div className="h-2 w-2 bg-primary-500 rounded-full"></div>
                                    </div>
                                  )}
                                </div>
                              </div>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </>
                )}
              </div>

              <div className="relative">
                <button
                  onClick={() => setShowProfileMenu(!showProfileMenu)}
                  className="flex items-center space-x-2 p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-800"
                >
                  {profilePictureUrl ? (
                    <img
                      src={profilePictureUrl}
                      alt={user?.username || 'User'}
                      className="h-8 w-8 rounded-full object-cover"
                    />
                  ) : (
                    <div className="h-8 w-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
                      <span className="text-sm font-medium text-primary-600 dark:text-primary-400">
                        {getInitials()}
                      </span>
                    </div>
                  )}
                  <div className="hidden md:block text-left">
                    <p className="text-sm font-medium text-gray-900 dark:text-white">
                      {user?.firstName} {user?.lastName}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      {user?.role?.replace('ROLE_', '')}
                    </p>
                  </div>
                </button>

                {showProfileMenu && (
                  <>
                    <div
                      className="fixed inset-0 z-10"
                      onClick={() => setShowProfileMenu(false)}
                    />
                    <div className="absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 z-20">
                      <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700">
                        <p className="text-sm font-medium text-gray-900 dark:text-white">
                          {user?.firstName} {user?.lastName}
                        </p>
                        <p className="text-xs text-gray-500 dark:text-gray-400 truncate">
                          {user?.email}
                        </p>
                      </div>
                      <a
                        href="/profile"
                        className="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                      >
                        <User className="inline-block h-4 w-4 mr-2" />
                        Your Profile
                      </a>
                      <button
                        onClick={() => {
                          setShowProfileMenu(false);
                          setShowNotifications(true);
                        }}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                      >
                        <Bell className="inline-block h-4 w-4 mr-2" />
                        Notifications
                        {unreadCount > 0 && (
                          <span className="ml-2 inline-flex items-center justify-center h-5 w-5 bg-red-500 text-white text-xs rounded-full">
                            {unreadCount}
                          </span>
                        )}
                      </button>
                      <button
                        onClick={logout}
                        className="block w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700"
                      >
                        Sign out
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      {selectedChatMember && (
        <ChatModal
          receiverId={selectedChatMember.id}
          receiverName={selectedChatMember.name}
          receiverAvatar={selectedChatMember.avatar}
          isOpen={chatModalOpen}
          onClose={() => {
            setChatModalOpen(false);
            setSelectedChatMember(null);
            loadAllNotifications();
          }}
        />
      )}
    </>
  );
};

export default Header;