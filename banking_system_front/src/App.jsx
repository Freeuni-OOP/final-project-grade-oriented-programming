import { Suspense } from 'react';
import { RouterProvider } from 'react-router-dom';
import { AuthProvider } from './components/AuthContext';
import ErrorBoundary from './components/ErrorBoundary';
import LoadingPage from './pages/LoadingPage';
import { createAppRouter } from './routes';

const router = createAppRouter();

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <Suspense fallback={<LoadingPage />}>
          <RouterProvider router={router} fallbackElement={<LoadingPage />} />
        </Suspense>
      </AuthProvider>
    </ErrorBoundary>
  );
}
