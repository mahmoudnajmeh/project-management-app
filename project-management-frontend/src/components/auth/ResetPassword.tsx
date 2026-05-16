import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Lock, Eye, EyeOff, CheckCircle, XCircle } from 'lucide-react';
import api from '../../api/api';
import Button from '../common/Button';
import Input from '../common/Input';
import Card from '../common/Card';
import { useToast } from '../../hooks/useToast';

const ResetPassword: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();
  const { error, success } = useToast();
  
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isValidToken, setIsValidToken] = useState<boolean | null>(null);
  const [isSubmitted, setIsSubmitted] = useState(false);

  useEffect(() => {
    if (!token) {
      setIsValidToken(false);
      return;
    }

    const validateToken = async () => {
      try {
        const response = await api.get('/auth/validate-reset-token', {
          params: { token }
        });
        setIsValidToken(response.data.valid);
      } catch (err) {
        setIsValidToken(false);
      }
    };

    validateToken();
  }, [token]);

  const validatePassword = (pwd: string) => {
    const errors = [];
    if (pwd.length < 8) errors.push('At least 8 characters');
    if (!/[0-9]/.test(pwd)) errors.push('At least one number');
    if (!/[a-z]/.test(pwd)) errors.push('At least one lowercase letter');
    if (!/[A-Z]/.test(pwd)) errors.push('At least one uppercase letter');
    if (!/[^a-zA-Z0-9]/.test(pwd)) errors.push('At least one special character');
    return errors;
  };

  const getPasswordStrength = (pwd: string) => {
    const errors = validatePassword(pwd);
    if (pwd.length === 0) return { strength: 0, label: '', color: '' };
    if (errors.length === 0) return { strength: 100, label: 'Strong', color: 'text-green-600 dark:text-green-400' };
    if (errors.length <= 2) return { strength: 66, label: 'Medium', color: 'text-yellow-600 dark:text-yellow-400' };
    return { strength: 33, label: 'Weak', color: 'text-red-600 dark:text-red-400' };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const passwordErrors = validatePassword(password);
    if (passwordErrors.length > 0) {
      error(`Password must contain: ${passwordErrors.join(', ')}`);
      return;
    }

    if (password !== confirmPassword) {
      error('Passwords do not match');
      return;
    }

    setIsLoading(true);
    try {
      await api.post('/auth/reset-password', { token, newPassword: password, confirmPassword });
      setIsSubmitted(true);
    } catch (err: any) {
      error(err.response?.data?.error || 'Failed to reset password');
    } finally {
      setIsLoading(false);
    }
  };

  if (isValidToken === false) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
        <Card className="max-w-md w-full">
          <div className="p-6 text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900 mb-4">
              <XCircle className="h-6 w-6 text-red-600 dark:text-red-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
              Invalid Reset Link
            </h2>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              This password reset link is invalid or has expired.
            </p>
            <div className="space-y-3">
              <Button onClick={() => navigate('/forgot-password')} className="w-full">
                Request New Reset Link
              </Button>
              <Button variant="ghost" onClick={() => navigate('/login')} className="w-full">
                Back to Login
              </Button>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  if (isSubmitted) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
        <Card className="max-w-md w-full">
          <div className="p-6 text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 dark:bg-green-900 mb-4">
              <CheckCircle className="h-6 w-6 text-green-600 dark:text-green-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
              Password Reset Successfully!
            </h2>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              Your password has been reset. You can now log in with your new password.
            </p>
            <Button onClick={() => navigate('/login')} className="w-full">
              Go to Login
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  const passwordStrength = getPasswordStrength(password);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
      <Card className="max-w-md w-full">
        <div className="p-6">
          <div className="text-center mb-6">
            <h2 className="text-3xl font-bold text-gray-900 dark:text-white">
              Create New Password
            </h2>
            <p className="mt-2 text-gray-600 dark:text-gray-400">
              Enter your new password below.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                New Password
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-3 py-2 pl-10 pr-10 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
                  placeholder="Enter new password"
                  required
                />
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2"
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4 text-gray-400" />
                  ) : (
                    <Eye className="h-4 w-4 text-gray-400" />
                  )}
                </button>
              </div>
              {password && (
                <div className="mt-2">
                  <div className="h-1 w-full bg-gray-200 rounded-full overflow-hidden">
                    <div
                      className="h-full transition-all duration-300 rounded-full"
                      style={{
                        width: `${passwordStrength.strength}%`,
                        backgroundColor: passwordStrength.strength === 100 ? '#10B981' : passwordStrength.strength >= 66 ? '#F59E0B' : '#EF4444'
                      }}
                    />
                  </div>
                  <p className={`text-xs mt-1 ${passwordStrength.color}`}>
                    {passwordStrength.label} password
                  </p>
                  <ul className="text-xs text-gray-500 dark:text-gray-400 mt-1 space-y-0.5">
                    {validatePassword(password).map((err, i) => (
                      <li key={i} className="text-red-500">✗ {err}</li>
                    ))}
                    {validatePassword(password).length === 0 && password.length > 0 && (
                      <li className="text-green-500">✓ All requirements met!</li>
                    )}
                  </ul>
                </div>
              )}
            </div>

            <Input
              type={showConfirmPassword ? 'text' : 'password'}
              label="Confirm New Password"
              placeholder="Confirm your new password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
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
              required
            />

            {password && confirmPassword && password !== confirmPassword && (
              <p className="text-sm text-red-600 dark:text-red-400">
                Passwords do not match
              </p>
            )}

            <Button
              type="submit"
              isLoading={isLoading}
              disabled={isLoading || !password || !confirmPassword || password !== confirmPassword}
              className="w-full"
            >
              Reset Password
            </Button>

            <div className="text-center">
              <Link
                to="/login"
                className="inline-flex items-center text-sm text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300"
              >
                Back to Login
              </Link>
            </div>
          </form>
        </div>
      </Card>
    </div>
  );
};

export default ResetPassword;