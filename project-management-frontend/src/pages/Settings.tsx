import React, { useState } from 'react';
import { 
  Bell, Globe, Users, Shield, ChevronRight, Download, Database, Lock
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../hooks/useToast';
import Card, { CardHeader, CardContent } from '../components/common/Card';
import Button from '../components/common/Button';

const Settings: React.FC = () => {
  const { user } = useAuth();
  const { success } = useToast();
  const [activeTab, setActiveTab] = useState('notifications');

  const [notifications, setNotifications] = useState({
    emailNotifications: true,
    taskAssignments: true,
    taskComments: true,
    projectUpdates: true,
    teamInvites: true,
    weeklyDigest: false,
    desktopNotifications: false,
    mentionAlerts: true,
  });


  const [privacy, setPrivacy] = useState({
    profileVisibility: 'team',
    showEmail: false,
    showActivity: true,
    allowTagging: true,
  });

  const [sound, setSound] = useState({
    enabled: true,
    volume: 70,
  });

  const [workingHours, setWorkingHours] = useState({
    enabled: false,
    start: '09:00',
    end: '17:00',
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    notifyOutside: true,
  });

  const [language, setLanguage] = useState('en');
  const [dateFormat, setDateFormat] = useState('MM/DD/YYYY');
  const [firstDayOfWeek, setFirstDayOfWeek] = useState('monday');

  const tabs = [
    { id: 'notifications', label: 'Notifications', icon: Bell },
    { id: 'privacy', label: 'Privacy', icon: Shield },
    { id: 'preferences', label: 'Preferences', icon: Globe },
    { id: 'team', label: 'Team Settings', icon: Users, adminOnly: true },
  ];

  const handleSaveNotifications = () => {
    localStorage.setItem('notificationSettings', JSON.stringify(notifications));
    success('Notification settings saved');
  };

  const handleSavePrivacy = () => {
    localStorage.setItem('privacySettings', JSON.stringify(privacy));
    success('Privacy settings saved');
  };

  const handleSavePreferences = () => {
    localStorage.setItem('language', language);
    localStorage.setItem('dateFormat', dateFormat);
    localStorage.setItem('firstDayOfWeek', firstDayOfWeek);
    localStorage.setItem('workingHours', JSON.stringify(workingHours));
    localStorage.setItem('sound', JSON.stringify(sound));
    success('Preferences saved');
  };

  const handleExportData = () => {
    const data = {
      user,
      notifications,
      privacy,
      preferences: { language, dateFormat, firstDayOfWeek, workingHours, sound },
      exportDate: new Date().toISOString(),
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `user-data-${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    URL.revokeObjectURL(url);
    success('Data exported successfully');
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          Settings
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">
          Configure your application preferences
        </p>
      </div>

      <div className="flex flex-col lg:flex-row gap-6">
        {/* Sidebar */}
        <div className="lg:w-64 space-y-1">
          {tabs.map(tab => {
            if (tab.adminOnly && user?.role !== 'ROLE_ADMIN') return null;
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center justify-between px-4 py-3 rounded-lg transition-colors ${
                  activeTab === tab.id
                    ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400'
                    : 'hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300'
                }`}
              >
                <div className="flex items-center">
                  <Icon className="h-5 w-5 mr-3" />
                  <span className="text-sm font-medium">{tab.label}</span>
                </div>
                <ChevronRight className={`h-4 w-4 ${
                  activeTab === tab.id ? 'opacity-100' : 'opacity-0'
                }`} />
              </button>
            );
          })}
        </div>

        {/* Content */}
        <div className="flex-1">
          {/* Notifications Tab */}
          {activeTab === 'notifications' && (
            <Card>
              <CardHeader title="Notification Settings" />
              <CardContent className="space-y-6">
                <div className="space-y-4">
                  <h3 className="font-medium text-gray-900 dark:text-white">Email Notifications</h3>
                  <div className="space-y-3">
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Email Notifications</span>
                      <input
                        type="checkbox"
                        checked={notifications.emailNotifications}
                        onChange={(e) => setNotifications({ ...notifications, emailNotifications: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Task Assignments</span>
                      <input
                        type="checkbox"
                        checked={notifications.taskAssignments}
                        onChange={(e) => setNotifications({ ...notifications, taskAssignments: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Task Comments</span>
                      <input
                        type="checkbox"
                        checked={notifications.taskComments}
                        onChange={(e) => setNotifications({ ...notifications, taskComments: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Project Updates</span>
                      <input
                        type="checkbox"
                        checked={notifications.projectUpdates}
                        onChange={(e) => setNotifications({ ...notifications, projectUpdates: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Team Invites</span>
                      <input
                        type="checkbox"
                        checked={notifications.teamInvites}
                        onChange={(e) => setNotifications({ ...notifications, teamInvites: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Mentions</span>
                      <input
                        type="checkbox"
                        checked={notifications.mentionAlerts}
                        onChange={(e) => setNotifications({ ...notifications, mentionAlerts: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Weekly Digest</span>
                      <input
                        type="checkbox"
                        checked={notifications.weeklyDigest}
                        onChange={(e) => setNotifications({ ...notifications, weeklyDigest: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                  </div>
                </div>

                <div className="space-y-4">
                  <h3 className="font-medium text-gray-900 dark:text-white">Desktop Notifications</h3>
                  <div className="space-y-3">
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Enable Desktop Notifications</span>
                      <input
                        type="checkbox"
                        checked={notifications.desktopNotifications}
                        onChange={(e) => setNotifications({ ...notifications, desktopNotifications: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                  </div>
                </div>

                <div className="space-y-4">
                  <h3 className="font-medium text-gray-900 dark:text-white">Notification Sounds</h3>
                  <div className="space-y-3">
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Play Sound</span>
                      <input
                        type="checkbox"
                        checked={sound.enabled}
                        onChange={(e) => setSound({ ...sound, enabled: e.target.checked })}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    
                    {sound.enabled && (
                      <div>
                        <label className="block text-sm text-gray-700 dark:text-gray-300 mb-2">
                          Volume: {sound.volume}%
                        </label>
                        <input
                          type="range"
                          min="0"
                          max="100"
                          value={sound.volume}
                          onChange={(e) => setSound({ ...sound, volume: parseInt(e.target.value) })}
                          className="w-full"
                        />
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button onClick={handleSaveNotifications}>
                    <Bell className="h-4 w-4 mr-2" />
                    Save Notification Settings
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Privacy Tab */}
          {activeTab === 'privacy' && (
            <Card>
              <CardHeader title="Privacy Settings" />
              <CardContent className="space-y-6">
                <div>
                  <h3 className="font-medium text-gray-900 dark:text-white mb-4">Profile Visibility</h3>
                  <select
                    value={privacy.profileVisibility}
                    onChange={(e) => setPrivacy({ ...privacy, profileVisibility: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                  >
                    <option value="public">Public - Anyone can see my profile</option>
                    <option value="team">Team Only - Only team members can see my profile</option>
                    <option value="private">Private - Only admins can see my profile</option>
                  </select>
                </div>

                <div className="space-y-3">
                  <label className="flex items-center justify-between">
                    <span className="text-sm text-gray-700 dark:text-gray-300">Show Email Address to Team Members</span>
                    <input
                      type="checkbox"
                      checked={privacy.showEmail}
                      onChange={(e) => setPrivacy({ ...privacy, showEmail: e.target.checked })}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                  </label>
                  <label className="flex items-center justify-between">
                    <span className="text-sm text-gray-700 dark:text-gray-300">Show Online Status</span>
                    <input
                      type="checkbox"
                      checked={privacy.showActivity}
                      onChange={(e) => setPrivacy({ ...privacy, showActivity: e.target.checked })}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                  </label>
                  <label className="flex items-center justify-between">
                    <span className="text-sm text-gray-700 dark:text-gray-300">Allow Others to Tag Me</span>
                    <input
                      type="checkbox"
                      checked={privacy.allowTagging}
                      onChange={(e) => setPrivacy({ ...privacy, allowTagging: e.target.checked })}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                  </label>
                </div>

                <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
                  <h3 className="font-medium text-gray-900 dark:text-white mb-4">Data & Privacy</h3>
                  <div className="space-y-3">
                    <Button variant="secondary" className="w-full justify-start" onClick={handleExportData}>
                      <Download className="h-4 w-4 mr-2" />
                      Export My Data
                    </Button>
                    <Button variant="secondary" className="w-full justify-start text-red-600 hover:text-red-700">
                      <Lock className="h-4 w-4 mr-2" />
                      Request Account Deletion
                    </Button>
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button onClick={handleSavePrivacy}>
                    <Shield className="h-4 w-4 mr-2" />
                    Save Privacy Settings
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Preferences Tab */}
          {activeTab === 'preferences' && (
            <Card>
              <CardHeader title="Preferences" />
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <h3 className="font-medium text-gray-900 dark:text-white mb-4">Language & Region</h3>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-sm text-gray-700 dark:text-gray-300 mb-2">Language</label>
                        <select
                          value={language}
                          onChange={(e) => setLanguage(e.target.value)}
                          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                        >
                          <option value="en">English</option>
                          <option value="es">Spanish</option>
                          <option value="fr">French</option>
                          <option value="de">German</option>
                          <option value="it">Italian</option>
                          <option value="pt">Portuguese</option>
                          <option value="nl">Dutch</option>
                          <option value="pl">Polish</option>
                          <option value="ru">Russian</option>
                          <option value="ja">Japanese</option>
                          <option value="ko">Korean</option>
                          <option value="zh">Chinese</option>
                        </select>
                      </div>

                      <div>
                        <label className="block text-sm text-gray-700 dark:text-gray-300 mb-2">Date Format</label>
                        <select
                          value={dateFormat}
                          onChange={(e) => setDateFormat(e.target.value)}
                          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                        >
                          <option value="MM/DD/YYYY">MM/DD/YYYY (12/31/2024)</option>
                          <option value="DD/MM/YYYY">DD/MM/YYYY (31/12/2024)</option>
                          <option value="YYYY-MM-DD">YYYY-MM-DD (2024-12-31)</option>
                        </select>
                      </div>

                      <div>
                        <label className="block text-sm text-gray-700 dark:text-gray-300 mb-2">First Day of Week</label>
                        <select
                          value={firstDayOfWeek}
                          onChange={(e) => setFirstDayOfWeek(e.target.value)}
                          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                        >
                          <option value="monday">Monday</option>
                          <option value="sunday">Sunday</option>
                          <option value="saturday">Saturday</option>
                        </select>
                      </div>
                    </div>
                  </div>

                  <div>
                    <h3 className="font-medium text-gray-900 dark:text-white mb-4">Working Hours</h3>
                    <div className="space-y-4">
                      <label className="flex items-center justify-between">
                        <span className="text-sm text-gray-700 dark:text-gray-300">Enable Working Hours</span>
                        <input
                          type="checkbox"
                          checked={workingHours.enabled}
                          onChange={(e) => setWorkingHours({ ...workingHours, enabled: e.target.checked })}
                          className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        />
                      </label>

                      {workingHours.enabled && (
                        <>
                          <div className="grid grid-cols-2 gap-3">
                            <div>
                              <label className="block text-xs text-gray-500 mb-1">Start Time</label>
                              <input
                                type="time"
                                value={workingHours.start}
                                onChange={(e) => setWorkingHours({ ...workingHours, start: e.target.value })}
                                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                              />
                            </div>
                            <div>
                              <label className="block text-xs text-gray-500 mb-1">End Time</label>
                              <input
                                type="time"
                                value={workingHours.end}
                                onChange={(e) => setWorkingHours({ ...workingHours, end: e.target.value })}
                                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                              />
                            </div>
                          </div>

                          <div>
                            <label className="block text-sm text-gray-700 dark:text-gray-300 mb-2">Time Zone</label>
                            <select
                              value={workingHours.timezone}
                              onChange={(e) => setWorkingHours({ ...workingHours, timezone: e.target.value })}
                              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                            >
                              <option value="America/New_York">Eastern Time (ET)</option>
                              <option value="America/Chicago">Central Time (CT)</option>
                              <option value="America/Denver">Mountain Time (MT)</option>
                              <option value="America/Los_Angeles">Pacific Time (PT)</option>
                              <option value="Europe/London">Greenwich Mean Time (GMT)</option>
                              <option value="Europe/Paris">Central European Time (CET)</option>
                              <option value="Asia/Tokyo">Japan Standard Time (JST)</option>
                              <option value="Asia/Shanghai">China Standard Time (CST)</option>
                              <option value="Australia/Sydney">Australian Eastern Time (AET)</option>
                            </select>
                          </div>

                          <label className="flex items-center justify-between">
                            <span className="text-sm text-gray-700 dark:text-gray-300">Notify me outside working hours</span>
                            <input
                              type="checkbox"
                              checked={workingHours.notifyOutside}
                              onChange={(e) => setWorkingHours({ ...workingHours, notifyOutside: e.target.checked })}
                              className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                            />
                          </label>
                        </>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex justify-end pt-4">
                  <Button onClick={handleSavePreferences}>
                    <Globe className="h-4 w-4 mr-2" />
                    Save Preferences
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Team Settings Tab (Admin only) */}
          {activeTab === 'team' && user?.role === 'ROLE_ADMIN' && (
            <Card>
              <CardHeader title="Team Settings" />
              <CardContent className="space-y-6">
                <div className="space-y-4">
                  <h3 className="font-medium text-gray-900 dark:text-white">Team Management</h3>
                  <div className="space-y-3">
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Allow Team Members to Invite Others</span>
                      <input
                        type="checkbox"
                        defaultChecked
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Require Admin Approval for New Members</span>
                      <input
                        type="checkbox"
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                    <label className="flex items-center justify-between">
                      <span className="text-sm text-gray-700 dark:text-gray-300">Allow Public Team Joining</span>
                      <input
                        type="checkbox"
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </label>
                  </div>
                </div>

                <div className="space-y-4">
                  <h3 className="font-medium text-gray-900 dark:text-white">Default Permissions</h3>
                  <div>
                    <label className="block text-sm text-gray-700 dark:text-gray-300 mb-2">Default New User Role</label>
                    <select className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800">
                      <option value="user">User (Can create and edit their own tasks)</option>
                      <option value="manager">Manager (Can create projects and manage team)</option>
                      <option value="admin">Admin (Full access)</option>
                    </select>
                  </div>
                </div>

                <div className="space-y-4">
                  <h3 className="font-medium text-gray-900 dark:text-white">Team Limits</h3>
                  <div>
                    <label className="block text-sm text-gray-700 dark:text-gray-300 mb-2">Maximum Team Size</label>
                    <input
                      type="number"
                      defaultValue={50}
                      min="1"
                      max="1000"
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800"
                    />
                  </div>
                </div>

                <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
                  <h3 className="font-medium text-gray-900 dark:text-white mb-4">Data Management</h3>
                  <div className="space-y-3">
                    <Button variant="secondary" className="w-full justify-start">
                      <Database className="h-4 w-4 mr-2" />
                      Export All Team Data
                    </Button>
                    <Button variant="secondary" className="w-full justify-start">
                      <Download className="h-4 w-4 mr-2" />
                      Download Activity Logs
                    </Button>
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button>
                    <Users className="h-4 w-4 mr-2" />
                    Save Team Settings
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

export default Settings;