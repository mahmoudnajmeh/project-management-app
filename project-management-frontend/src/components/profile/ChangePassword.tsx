import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Lock, Eye, EyeOff, Save, Key, X } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { usersApi } from '../../api/users';
import Input from '../common/Input';
import Button from '../common/Button';
import Card, { CardHeader, CardContent } from '../common/Card';

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[^a-zA-Z0-9]/, 'Password must contain at least one special character'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

type PasswordFormData = z.infer<typeof passwordSchema>;

const ChangePassword: React.FC = () => {
  const { user } = useAuth();
  const { success, error } = useToast();
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
    watch,
  } = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPassword = watch('newPassword', '');

  const getPasswordStrength = () => {
    let strength = 0;
    if (newPassword.length >= 8) strength++;
    if (/[0-9]/.test(newPassword)) strength++;
    if (/[a-z]/.test(newPassword)) strength++;
    if (/[A-Z]/.test(newPassword)) strength++;
    if (/[^a-zA-Z0-9]/.test(newPassword)) strength++;
    return strength;
  };

  const passwordStrength = getPasswordStrength();
  const strengthPercentage = (passwordStrength / 5) * 100;

  const getStrengthColor = () => {
    if (passwordStrength <= 2) return 'bg-red-500';
    if (passwordStrength <= 3) return 'bg-yellow-500';
    if (passwordStrength <= 4) return 'bg-blue-500';
    return 'bg-green-500';
  };

  const getStrengthText = () => {
    if (newPassword.length === 0) return '';
    if (passwordStrength <= 2) return 'Weak';
    if (passwordStrength <= 3) return 'Fair';
    if (passwordStrength <= 4) return 'Good';
    return 'Strong';
  };

  const onSubmit = async (data: PasswordFormData) => {
    try {
      await usersApi.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword,
      });
      
      success('Password changed successfully!');
      reset();
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to change password');
    }
  };

  const handleCancel = () => {
    reset();
    setShowCurrentPassword(false);
    setShowNewPassword(false);
    setShowConfirmPassword(false);
  };

  if (!user) return null;

  return (
    <Card>
      <CardHeader title="Change Password" />
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="flex items-center space-x-2 mb-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <Key className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            <p className="text-sm text-blue-700 dark:text-blue-300">
              Choose a strong password that you don't use elsewhere
            </p>
          </div>

          <Input
            {...register('currentPassword')}
            type={showCurrentPassword ? 'text' : 'password'}
            label="Current Password"
            placeholder="Enter your current password"
            leftIcon={<Lock className="h-4 w-4 text-gray-400" />}
            rightIcon={
              <button
                type="button"
                onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                className="focus:outline-none"
              >
                {showCurrentPassword ? (
                  <EyeOff className="h-4 w-4 text-gray-400" />
                ) : (
                  <Eye className="h-4 w-4 text-gray-400" />
                )}
              </button>
            }
            error={errors.currentPassword?.message}
          />

          <div className="space-y-2">
            <Input
              {...register('newPassword')}
              type={showNewPassword ? 'text' : 'password'}
              label="New Password"
              placeholder="Enter new password"
              leftIcon={<Lock className="h-4 w-4 text-gray-400" />}
              rightIcon={
                <button
                  type="button"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                  className="focus:outline-none"
                >
                  {showNewPassword ? (
                    <EyeOff className="h-4 w-4 text-gray-400" />
                  ) : (
                    <Eye className="h-4 w-4 text-gray-400" />
                  )}
                </button>
              }
              error={errors.newPassword?.message}
            />
            
            {newPassword && (
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div 
                      className={`h-full ${getStrengthColor()} transition-all duration-300`}
                      style={{ width: `${strengthPercentage}%` }}
                    />
                  </div>
                  <span className="ml-2 text-xs font-medium text-gray-600 dark:text-gray-400">
                    {getStrengthText()}
                  </span>
                </div>
                
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className={`flex items-center ${newPassword.length >= 8 ? 'text-green-600' : 'text-gray-500'}`}>
                    <span className="mr-1">✓</span> 8+ characters
                  </div>
                  <div className={`flex items-center ${/[0-9]/.test(newPassword) ? 'text-green-600' : 'text-gray-500'}`}>
                    <span className="mr-1">✓</span> Number
                  </div>
                  <div className={`flex items-center ${/[a-z]/.test(newPassword) ? 'text-green-600' : 'text-gray-500'}`}>
                    <span className="mr-1">✓</span> Lowercase
                  </div>
                  <div className={`flex items-center ${/[A-Z]/.test(newPassword) ? 'text-green-600' : 'text-gray-500'}`}>
                    <span className="mr-1">✓</span> Uppercase
                  </div>
                  <div className={`flex items-center ${/[^a-zA-Z0-9]/.test(newPassword) ? 'text-green-600' : 'text-gray-500'}`}>
                    <span className="mr-1">✓</span> Special char
                  </div>
                </div>
              </div>
            )}
          </div>

          <Input
            {...register('confirmPassword')}
            type={showConfirmPassword ? 'text' : 'password'}
            label="Confirm New Password"
            placeholder="Confirm your new password"
            leftIcon={<Lock className="h-4 w-4 text-gray-400" />}
            rightIcon={
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="focus:outline-none"
              >
                {showConfirmPassword ? (
                  <EyeOff className="h-4 w-4 text-gray-400" />
                ) : (
                  <Eye className="h-4 w-4 text-gray-400" />
                )}
              </button>
            }
            error={errors.confirmPassword?.message}
          />

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={handleCancel}
              className="flex items-center"
            >
              <X className="h-4 w-4 mr-2" />
              Cancel
            </Button>
            <Button
              type="submit"
              isLoading={isSubmitting}
              className="flex items-center"
            >
              <Save className="h-4 w-4 mr-2" />
              Change Password
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default ChangePassword;