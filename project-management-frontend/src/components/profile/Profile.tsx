// src/components/profile/Profile.tsx
import React, { useEffect, useState } from 'react';
import { User, Mail, Calendar, Shield, Camera } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import Card, { CardHeader, CardContent } from '../common/Card';
import ProfilePicture from './ProfilePicture';
import ProfileForm from './ProfileForm';

const Profile: React.FC = () => {
  const { user, initialLoad } = useAuth();
  const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null);

  // Update profile image URL when user changes
  useEffect(() => {
    if (user?.profilePictureFileName) {
      const timestamp = Date.now();
      const src = `http://localhost:8080/api/users/profile-picture/${user.profilePictureFileName}?t=${timestamp}`;
      setProfileImageUrl(src);
    } else if (user?.profilePictureUrl) {
      setProfileImageUrl(user.profilePictureUrl);
    } else {
      setProfileImageUrl(null);
    }
  }, [user]);

  // Show loading state
  if (initialLoad) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-600 dark:text-gray-400">
          Please log in to view your profile
        </p>
      </div>
    );
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getInitials = () => {
    return `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase() || user.username?.[0].toUpperCase() || 'U';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          Profile Settings
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">
          Manage your account settings and preferences
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Profile Info */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader title="Personal Information" />
            <CardContent>
              <ProfileForm />
            </CardContent>
          </Card>

          <Card>
            <CardHeader title="Account Security" />
            <CardContent>
              <div className="space-y-4">
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white mb-2">
                    Change Password
                  </h4>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                    Update your password regularly to keep your account secure.
                  </p>
                  <button className="btn-primary">
                    Change Password
                  </button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column - Profile Overview */}
        <div className="space-y-6">
          {/* Profile Picture */}
          <Card>
            <CardHeader title="Profile Picture" />
            <CardContent>
              <ProfilePicture />
            </CardContent>
          </Card>

          {/* Account Overview */}
          <Card>
            <CardHeader title="Account Overview" />
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center space-x-3">
                  <div className="relative">
                    {profileImageUrl ? (
                      <img
                        src={profileImageUrl}
                        alt={user.username}
                        className="h-10 w-10 rounded-full object-cover border-2 border-white dark:border-gray-800"
                        onError={(e) => {
                          e.currentTarget.style.display = 'none';
                          const parent = e.currentTarget.parentElement;
                          if (parent) {
                            const fallback = parent.querySelector('.profile-initials') as HTMLElement;
                            if (fallback) fallback.style.display = 'flex';
                          }
                        }}
                      />
                    ) : null}
                    
                    {/* Fallback initials */}
                    <div 
                      className={`profile-initials h-10 w-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-2 border-white dark:border-gray-800 ${
                        profileImageUrl ? 'hidden' : 'flex'
                      }`}
                    >
                      <span className="text-sm font-bold text-white">
                        {getInitials()}
                      </span>
                    </div>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900 dark:text-white">
                      {user.firstName} {user.lastName}
                    </p>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      @{user.username}
                    </p>
                  </div>
                </div>

                <div className="space-y-3">
                  <div className="flex items-center text-sm">
                    <Mail className="h-4 w-4 text-gray-400 mr-2 flex-shrink-0" />
                    <span className="text-gray-600 dark:text-gray-400 truncate">{user.email}</span>
                  </div>
                  <div className="flex items-center text-sm">
                    <Shield className="h-4 w-4 text-gray-400 mr-2 flex-shrink-0" />
                    <span className="text-gray-600 dark:text-gray-400">
                      {user.role ? user.role.replace('ROLE_', '') : 'User'}
                    </span>
                  </div>
                  <div className="flex items-center text-sm">
                    <Calendar className="h-4 w-4 text-gray-400 mr-2 flex-shrink-0" />
                    <span className="text-gray-600 dark:text-gray-400">
                      Joined {user.createdAt ? formatDate(user.createdAt) : 'Recently'}
                    </span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Profile;