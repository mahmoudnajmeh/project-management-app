import React, { useState, useRef, useEffect } from 'react';
import { Upload, Camera, Trash2 } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { usersApi } from '../../api/users';
import { useToast } from '../../hooks/useToast';
import Button from '../common/Button';
import Modal from '../common/Modal';

const ProfilePicture: React.FC = () => {
  const { user, refreshUser } = useAuth();
  const { success, error } = useToast();
  const [isUploading, setIsUploading] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [profilePictureSrc, setProfilePictureSrc] = useState<string | null>(null);
  const [isConfirmDeleteModalOpen, setIsConfirmDeleteModalOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (user?.profilePictureFileName) {
      const src = `http://localhost:8080/api/users/profile-picture/${user.profilePictureFileName}?t=${Date.now()}`;
      setProfilePictureSrc(src);
    } else {
      setProfilePictureSrc(null);
    }
  }, [user?.profilePictureFileName]);

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      error('Please select an image file (JPG, PNG, GIF)');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      error('File size must be less than 10MB');
      return;
    }

    setIsUploading(true);
    try {
      await usersApi.uploadProfilePicture(file);
      await refreshUser();
      success('Profile picture updated successfully!');
    } catch (err: any) {
      console.error('Upload error:', err);
      error(err.response?.data?.error || 'Failed to upload profile picture');
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleDeleteConfirm = () => {
    setIsConfirmDeleteModalOpen(true);
  };

  const handleDelete = async () => {
    setIsDeleting(true);
    try {
      await usersApi.deleteProfilePicture();
      await refreshUser();
      success('Profile picture deleted successfully!');
      setIsConfirmDeleteModalOpen(false);
    } catch (err: any) {
      console.error('Delete error:', err);
      error(err.response?.data?.error || 'Failed to delete profile picture');
    } finally {
      setIsDeleting(false);
    }
  };

  const getInitials = () => {
    if (!user) return 'U';
    return `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase() || user.username?.[0].toUpperCase() || 'U';
  };

  return (
    <>
      <div className="flex flex-col items-center space-y-4">
        <div className="relative">
          {profilePictureSrc ? (
            <img
              src={profilePictureSrc}
              alt={user?.username || 'User'}
              className="h-32 w-32 rounded-full object-cover border-4 border-white dark:border-gray-800 shadow-lg"
            />
          ) : (
            <div className="h-32 w-32 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center border-4 border-white dark:border-gray-800 shadow-lg">
              <span className="text-4xl font-bold text-white">
                {getInitials()}
              </span>
            </div>
          )}
          
          <button
            onClick={() => fileInputRef.current?.click()}
            className="absolute bottom-0 right-0 h-10 w-10 rounded-full bg-primary-600 text-white flex items-center justify-center hover:bg-primary-700 transition-colors shadow-lg"
            title="Change photo"
            type="button"
          >
            <Camera className="h-5 w-5" />
          </button>
        </div>

        <div className="flex flex-col sm:flex-row gap-2 w-full">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            onChange={handleFileSelect}
            className="hidden"
          />
          <Button
            onClick={() => fileInputRef.current?.click()}
            isLoading={isUploading}
            variant="primary"
            className="flex-1"
          >
            <Upload className="h-4 w-4 mr-2" />
            {profilePictureSrc ? 'Change Photo' : 'Upload Photo'}
          </Button>
          {profilePictureSrc && (
            <Button
              onClick={handleDeleteConfirm}
              variant="danger"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Remove
            </Button>
          )}
        </div>

        <div className="text-center text-sm text-gray-500 dark:text-gray-400">
          <p>Upload a JPG, PNG, or GIF image (max. 10MB)</p>
          <p>Recommended size: 400x400 pixels</p>
        </div>
      </div>

      <Modal
        isOpen={isConfirmDeleteModalOpen}
        onClose={() => setIsConfirmDeleteModalOpen(false)}
        title="Confirm Delete"
        size="md"
      >
        <div className="space-y-4">
          <div className="flex items-center justify-center mb-4">
            <div className="h-12 w-12 rounded-full bg-red-100 dark:bg-red-900 flex items-center justify-center">
              <Trash2 className="h-6 w-6 text-red-600 dark:text-red-400" />
            </div>
          </div>
          
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white text-center">
            Delete Profile Picture
          </h3>
          
          <p className="text-gray-600 dark:text-gray-400 text-center">
            Are you sure you want to delete your profile picture? This action cannot be undone.
          </p>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => setIsConfirmDeleteModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              onClick={handleDelete}
              isLoading={isDeleting}
              disabled={isDeleting}
              className="flex items-center"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete Picture
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default ProfilePicture;