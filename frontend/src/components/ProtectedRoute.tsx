import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAppSelector } from '../store';

export const ProtectedRoute: React.FC<{ role?: 'admin' | 'user' }> = ({ role }) => {
  const { token, userRole } = useAppSelector((s) => s.auth);
  if (!token) return <Navigate to="/auth/login" replace />;
  if (role && userRole !== role) return <Navigate to="/" replace />;
  return <Outlet />;
};

export default ProtectedRoute;
