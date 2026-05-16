import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Layout from './components/layout/Layout';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import Dashboard from './components/dashboard/Dashboard';
import ProjectList from './components/projects/ProjectList';
import ProjectDetails from './components/projects/ProjectDetails';
import EditProject from './components/projects/EditProject';
import TaskList from './components/tasks/TaskList';
import Profile from './components/profile/Profile';
import Team from './components/team/Team';
import Calendar from './components/calendar/Calendar';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import TaskDetails from './components/tasks/TaskDetails';
import ForgotPassword from './components/auth/ForgotPassword';
import ResetPassword from './components/auth/ResetPassword';
import OAuth2Redirect from './components/auth/OAuth2Redirect';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      retry: 1,
    },
  },
});

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <Router>
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />
              <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
              <Route element={<ProtectedRoute />}>
                <Route path="/" element={<Layout />}>
                  <Route index element={<Navigate to="/dashboard" replace />} />
                  <Route path="dashboard" element={<Dashboard />} />
                  <Route path="projects" element={<ProjectList />} />
                  <Route path="projects/:id" element={<ProjectDetails />} />
                  <Route path="projects/:id/edit" element={<EditProject />} />
                  <Route path="tasks" element={<TaskList />} />
                  <Route path="tasks/:id" element={<TaskDetails />} />
                  <Route path="profile" element={<Profile />} />
                  <Route path="calendar" element={<Calendar />} />
                  <Route path="reports" element={<Reports />} />
                  <Route path="settings" element={<Settings />} />
                  <Route path="settings" element={<div>Settings</div>} />
                  <Route path="team" element={<Team />} />
                </Route>
              </Route>
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </Router>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;