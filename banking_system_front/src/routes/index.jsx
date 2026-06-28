import { Navigate, createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout';
import ProtectedRoute from '../components/ProtectedRoute';
import ErrorPage from '../pages/ErrorPage';
import LoginPage from '../pages/LoginPage';
import NotFoundPage from '../pages/NotFoundPage';
import PlaceholderPage from '../pages/PlaceholderPage';
import RegisterPage from '../pages/RegisterPage';
import UnauthorizedPage from '../pages/UnauthorizedPage';

const protectedFeatureRoutes = [
  {
    path: 'dashboard',
    title: 'Dashboard',
    description: 'Summary screen placeholder for authenticated users.',
  },
  {
    path: 'customers',
    title: 'Customers',
    description: 'Customer feature placeholder. Real customer screens can replace this later.',
  },
  {
    path: 'accounts',
    title: 'Accounts',
    description: 'Account feature placeholder. Real account screens can replace this later.',
  },
  {
    path: 'cards',
    title: 'Cards',
    description: 'Card feature placeholder. Real card screens can replace this later.',
  },
];

const managerFeatureRoutes = [
  {
    path: 'admin',
    title: 'Admin',
    description: 'Manager-only admin view placeholder.',
  },
  {
    path: 'admin/delete',
    title: 'Delete Records',
    description: 'Manager-only delete view placeholder.',
  },
];

function makePlaceholderRoute(route) {
  return {
    path: route.path,
    element: <PlaceholderPage title={route.title} description={route.description} />,
  };
}

export const appRoutes = [
  {
    path: '/',
    element: <Layout />,
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'unauthorized', element: <UnauthorizedPage /> },
      {
        element: <ProtectedRoute />,
        children: protectedFeatureRoutes.map(makePlaceholderRoute),
      },
      {
        element: <ProtectedRoute requiredRole="MANAGER" />,
        children: managerFeatureRoutes.map(makePlaceholderRoute),
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
];

export function createAppRouter() {
  return createBrowserRouter(appRoutes);
}
