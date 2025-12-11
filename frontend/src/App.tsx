import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import DashboardLayout from './components/layout/DashboardLayout';
import LoginPage from './pages/auth/LoginPage';
import InitAdminPage from './pages/auth/InitAdminPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import UsersPage from './pages/users/UsersPage';
import RolesPage from './pages/roles/RolesPage';
import LocationsPage from './pages/locations/LocationsPage';
import StagesPage from './pages/stages/StagesPage';
import DocumentsPage from './pages/documents/DocumentsPage';
import CategoriesPage from './pages/categories/CategoriesPage';
import MeetingsPage from './pages/meetings/MeetingsPage';
import ProfilePage from './pages/profile/ProfilePage';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/init-admin" element={<InitAdminPage />} />

          {/* Protected routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="users" element={<UsersPage />} />
            <Route path="roles" element={<RolesPage />} />
            <Route path="locations" element={<LocationsPage />} />
            <Route path="stages" element={<StagesPage />} />
            <Route path="documents" element={<DocumentsPage />} />
            <Route path="categories" element={<CategoriesPage />} />
            <Route path="meetings" element={<MeetingsPage />} />
            <Route path="profile" element={<ProfilePage />} />
          </Route>

          {/* Redirect root to dashboard */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* 404 */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
