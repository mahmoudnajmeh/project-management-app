// src/components/profile/ProfileForm.tsx
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Save, User, Mail } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import Input from '../common/Input';
import Button from '../common/Button';

const profileSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  email: z.string().email('Invalid email address'),
  username: z.string().min(3, 'Username must be at least 3 characters'),
});

type ProfileFormData = z.infer<typeof profileSchema>;

const ProfileForm: React.FC = () => {
  const { user, updateUser } = useAuth(); // Remove refreshUser - not needed
  const { success, error } = useToast();
  
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      email: user?.email || '',
      username: user?.username || '',
    },
  });

  React.useEffect(() => {
    if (user) {
      reset({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        username: user.username || '',
      });
    }
  }, [user, reset]);

  const onSubmit = async (data: ProfileFormData) => {
    try {
      const updateData = {
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
      };
      
      // Let updateUser handle everything - it updates local state immediately AND calls API
      await updateUser(updateData);
      
      success('Profile updated successfully!');
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to update profile');
    }
  };

  if (!user) return null;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Input
          {...register('firstName')}
          label="First Name"
          placeholder="John"
          leftIcon={<User className="h-4 w-4 text-gray-400" />}
          error={errors.firstName?.message}
        />
        <Input
          {...register('lastName')}
          label="Last Name"
          placeholder="Doe"
          leftIcon={<User className="h-4 w-4 text-gray-400" />}
          error={errors.lastName?.message}
        />
      </div>
      
      <Input
        {...register('email')}
        type="email"
        label="Email Address"
        placeholder="john@example.com"
        leftIcon={<Mail className="h-4 w-4 text-gray-400" />}
        error={errors.email?.message}
      />
      
      <Input
        {...register('username')}
        label="Username"
        placeholder="johndoe"
        leftIcon={<User className="h-4 w-4 text-gray-400" />}
        error={errors.username?.message}
        disabled
      />

      <div className="flex justify-end pt-4">
        <Button
          type="submit"
          isLoading={isSubmitting}
          className="flex items-center"
        >
          <Save className="h-4 w-4 mr-2" />
          Save Changes
        </Button>
      </div>
    </form>
  );
};

export default ProfileForm;