import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';
import LoadingPage from '../pages/LoadingPage';

export default function ProtectedRoute({ requiredRole }) {
  const { isAuthenticated, authority, ready } = useAuth();

  // Wait for token validation so private pages do not flash before redirecting.
  if (!ready) {
    return <LoadingPage message="Checking your session..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && authority !== requiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
