import React, { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

const ProtectedRoute: React.FC = () => {
  const { getToken, isLoading, user, initialLoad } = useAuth();
  const location = useLocation();
  const [checkedAuth, setCheckedAuth] = useState(false);
  
  useEffect(() => {
    const timer = setTimeout(() => {
      setCheckedAuth(true);
    }, 100);
    
    return () => clearTimeout(timer);
  }, []);

  if (isLoading || initialLoad || !checkedAuth) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  const token = getToken();
  
  console.log('ProtectedRoute - Final check:', {
    token: token ? 'exists' : 'missing',
    user: user ? 'exists' : 'missing',
    from: location.pathname
  });

  if (!token) {
    console.log('No token, redirecting to login');
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;