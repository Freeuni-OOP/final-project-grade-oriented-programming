import { Navigate, createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout';
import ProtectedRoute from '../components/ProtectedRoute';
import ErrorPage from '../pages/ErrorPage';
import LoginPage from '../pages/LoginPage';
import CustomersPage from '../pages/CustomersPage';
import ManagerCustomersPage from '../pages/ManagerCustomersPage';
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
        children: [
          ...protectedFeatureRoutes.map(makePlaceholderRoute),
          { path: 'customers', element: <CustomersPage /> },
        ],
      },
      {
        element: <ProtectedRoute requiredRole="MANAGER" />,
        children: [
          { path: 'admin', element: <ManagerCustomersPage title="Admin" /> },
          { path: 'admin/delete', element: <ManagerCustomersPage title="Delete Records" /> },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
];

export function createAppRouter() {
  return createBrowserRouter(appRoutes);
}
