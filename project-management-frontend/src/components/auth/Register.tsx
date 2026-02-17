import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { UserPlus, User, Mail, Lock, Hash, Eye, EyeOff } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import Input from '../common/Input';
import Button from '../common/Button';
import Card, { CardHeader, CardContent, CardFooter } from '../common/Card';

const passwordSchema = z.string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[0-9]/, 'Password must contain at least one number')
  .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
  .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
  .regex(/[^a-zA-Z0-9]/, 'Password must contain at least one special character');

const registerSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  email: z.string().email('Invalid email address'),
  password: passwordSchema,
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
});

type RegisterFormData = z.infer<typeof registerSchema>;

const Register: React.FC = () => {
  const { register: registerUser } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = React.useState(false);
  
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    watch,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const password = watch('password', '');

  const getPasswordStrength = () => {
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[^a-zA-Z0-9]/.test(password)) strength++;
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
    if (password.length === 0) return '';
    if (passwordStrength <= 2) return 'Weak';
    if (passwordStrength <= 3) return 'Fair';
    if (passwordStrength <= 4) return 'Good';
    return 'Strong';
  };

  const onSubmit = async (data: RegisterFormData) => {
    try {
      await registerUser(data);
      success('Account created successfully! Please sign in.');
      navigate('/login');
    } catch (err: any) {
      error(err.response?.data?.error || 'Registration failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 dark:from-gray-900 dark:to-gray-800 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <div className="flex justify-center">
            <div className="h-12 w-12 rounded-full bg-primary-600 flex items-center justify-center">
              <UserPlus className="h-6 w-6 text-white" />
            </div>
          </div>
          <h2 className="mt-6 text-3xl font-bold text-gray-900 dark:text-white">
            Create your account
          </h2>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
            Join thousands of teams using ProjectFlow
          </p>
        </div>

        <Card>
          <CardHeader title="Sign up for free" />
          <form onSubmit={handleSubmit(onSubmit)}>
            <CardContent className="space-y-4">
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
                {...register('username')}
                label="Username"
                placeholder="johndoe"
                leftIcon={<Hash className="h-4 w-4 text-gray-400" />}
                error={errors.username?.message}
              />
              <Input
                {...register('email')}
                type="email"
                label="Email Address"
                placeholder="john@example.com"
                leftIcon={<Mail className="h-4 w-4 text-gray-400" />}
                error={errors.email?.message}
              />
              <div className="space-y-2">
                <Input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  label="Password"
                  placeholder="Create a password"
                  leftIcon={<Lock className="h-4 w-4 text-gray-400" />}
                  rightIcon={
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="focus:outline-none"
                    >
                      {showPassword ? (
                        <EyeOff className="h-4 w-4 text-gray-400" />
                      ) : (
                        <Eye className="h-4 w-4 text-gray-400" />
                      )}
                    </button>
                  }
                  error={errors.password?.message}
                />
                
                {password && (
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
                      <div className={`flex items-center ${password.length >= 8 ? 'text-green-600' : 'text-gray-500'}`}>
                        <span className="mr-1">✓</span> 8+ characters
                      </div>
                      <div className={`flex items-center ${/[0-9]/.test(password) ? 'text-green-600' : 'text-gray-500'}`}>
                        <span className="mr-1">✓</span> Number
                      </div>
                      <div className={`flex items-center ${/[a-z]/.test(password) ? 'text-green-600' : 'text-gray-500'}`}>
                        <span className="mr-1">✓</span> Lowercase
                      </div>
                      <div className={`flex items-center ${/[A-Z]/.test(password) ? 'text-green-600' : 'text-gray-500'}`}>
                        <span className="mr-1">✓</span> Uppercase
                      </div>
                      <div className={`flex items-center ${/[^a-zA-Z0-9]/.test(password) ? 'text-green-600' : 'text-gray-500'}`}>
                        <span className="mr-1">✓</span> Special char
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
            <CardFooter>
              <Button
                type="submit"
                isLoading={isSubmitting}
                fullWidth
                className="mt-2"
              >
                Create Account
              </Button>
            </CardFooter>
          </form>
        </Card>

        <div className="text-center">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Already have an account?{' '}
            <Link
              to="/login"
              className="font-medium text-primary-600 hover:text-primary-500 dark:text-primary-400"
            >
              Sign in
            </Link>
          </p>
          <p className="mt-2 text-xs text-gray-500 dark:text-gray-500">
            By signing up, you agree to our Terms of Service and Privacy Policy
          </p>
        </div>

        <div className="mt-6">
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300 dark:border-gray-700"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-white dark:bg-gray-800 text-gray-500">
                Or sign up with
              </span>
            </div>
          </div>
          <div className="mt-6 grid grid-cols-2 gap-3">
            <button className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700">
              Google
            </button>
            <button className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700">
              GitHub
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;