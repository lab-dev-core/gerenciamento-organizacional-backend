import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import {
  FiHome,
  FiUsers,
  FiShield,
  FiMapPin,
  FiTrendingUp,
  FiBook,
  FiFolder,
  FiCalendar,
  FiUser,
  FiLogOut,
  FiMenu,
  FiX,
} from 'react-icons/fi';

const DashboardLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    { path: '/dashboard', icon: FiHome, label: 'Dashboard' },
    { path: '/dashboard/users', icon: FiUsers, label: 'Usuários' },
    { path: '/dashboard/roles', icon: FiShield, label: 'Perfis' },
    { path: '/dashboard/locations', icon: FiMapPin, label: 'Locais de Missão' },
    { path: '/dashboard/stages', icon: FiTrendingUp, label: 'Etapas Formativas' },
    { path: '/dashboard/documents', icon: FiBook, label: 'Documentos' },
    { path: '/dashboard/categories', icon: FiFolder, label: 'Categorias' },
    { path: '/dashboard/meetings', icon: FiCalendar, label: 'Acompanhamentos' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-gray-600 bg-opacity-75 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        ></div>
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-white shadow-lg transform ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0 transition-transform duration-300 ease-in-out`}
      >
        <div className="flex flex-col h-full">
          {/* Logo */}
          <div className="flex items-center justify-between h-16 px-6 border-b border-gray-200">
            <div className="flex items-center">
              <div className="flex items-center justify-center w-8 h-8 bg-primary-600 rounded">
                <span className="text-sm font-bold text-white">GF</span>
              </div>
              <span className="ml-2 text-lg font-semibold text-gray-900">Gestão Formativa</span>
            </div>
            <button
              onClick={() => setSidebarOpen(false)}
              className="lg:hidden text-gray-500 hover:text-gray-700"
            >
              <FiX size={24} />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-4 overflow-y-auto">
            <div className="space-y-1">
              {menuItems.map((item) => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  end={item.path === '/dashboard'}
                  className={({ isActive }) =>
                    `flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-colors ${
                      isActive
                        ? 'bg-primary-50 text-primary-700'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`
                  }
                  onClick={() => setSidebarOpen(false)}
                >
                  <item.icon className="mr-3" size={20} />
                  {item.label}
                </NavLink>
              ))}
            </div>
          </nav>

          {/* User section */}
          <div className="border-t border-gray-200 p-4">
            <div className="flex items-center mb-3">
              <div className="flex items-center justify-center w-10 h-10 bg-primary-100 rounded-full">
                <FiUser className="text-primary-700" size={20} />
              </div>
              <div className="ml-3 flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">{user?.name}</p>
                <p className="text-xs text-gray-500 truncate">{user?.roleName}</p>
              </div>
            </div>
            <div className="space-y-1">
              <NavLink
                to="/dashboard/profile"
                className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 rounded-lg hover:bg-gray-100"
                onClick={() => setSidebarOpen(false)}
              >
                <FiUser className="mr-3" size={18} />
                Perfil
              </NavLink>
              <button
                onClick={handleLogout}
                className="w-full flex items-center px-4 py-2 text-sm font-medium text-red-600 rounded-lg hover:bg-red-50"
              >
                <FiLogOut className="mr-3" size={18} />
                Sair
              </button>
            </div>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <div className="lg:pl-64">
        {/* Top bar */}
        <header className="h-16 bg-white shadow-sm border-b border-gray-200">
          <div className="flex items-center justify-between h-full px-4 sm:px-6">
            <button
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden text-gray-500 hover:text-gray-700"
            >
              <FiMenu size={24} />
            </button>
            <div className="flex items-center lg:hidden">
              <div className="flex items-center justify-center w-8 h-8 bg-primary-600 rounded">
                <span className="text-sm font-bold text-white">GF</span>
              </div>
            </div>
            <div></div>
          </div>
        </header>

        {/* Page content */}
        <main className="p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
