import React, { useEffect, useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { userService, documentService, meetingService, stageService } from '../../api';
import { FiUsers, FiBook, FiCalendar, FiTrendingUp } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';

interface DashboardStats {
  totalUsers: number;
  totalDocuments: number;
  upcomingMeetings: number;
  activeStages: number;
}

const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats>({
    totalUsers: 0,
    totalDocuments: 0,
    upcomingMeetings: 0,
    activeStages: 0,
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const [users, documents, meetings, stages] = await Promise.all([
        userService.getAll().catch(() => []),
        documentService.getAll().catch(() => []),
        meetingService.getUpcoming(7).catch(() => []),
        stageService.getActive().catch(() => []),
      ]);

      setStats({
        totalUsers: users.length,
        totalDocuments: documents.length,
        upcomingMeetings: meetings.length,
        activeStages: stages.length,
      });
    } catch (error) {
      console.error('Error loading stats:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const statCards = [
    {
      title: 'Total de Usuários',
      value: stats.totalUsers,
      icon: FiUsers,
      color: 'bg-blue-500',
      bgColor: 'bg-blue-50',
      textColor: 'text-blue-700',
    },
    {
      title: 'Documentos',
      value: stats.totalDocuments,
      icon: FiBook,
      color: 'bg-green-500',
      bgColor: 'bg-green-50',
      textColor: 'text-green-700',
    },
    {
      title: 'Reuniões (7 dias)',
      value: stats.upcomingMeetings,
      icon: FiCalendar,
      color: 'bg-purple-500',
      bgColor: 'bg-purple-50',
      textColor: 'text-purple-700',
    },
    {
      title: 'Etapas Ativas',
      value: stats.activeStages,
      icon: FiTrendingUp,
      color: 'bg-orange-500',
      bgColor: 'bg-orange-50',
      textColor: 'text-orange-700',
    },
  ];

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Bem-vindo, {user?.name}!</h1>
        <p className="mt-2 text-gray-600">Aqui está um resumo do seu sistema de gestão formativa.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, index) => (
          <div key={index} className="card">
            <div className="flex items-center">
              <div className={`flex items-center justify-center w-12 h-12 rounded-lg ${stat.bgColor}`}>
                <stat.icon className={stat.textColor} size={24} />
              </div>
              <div className="ml-4">
                <p className="text-sm text-gray-600">{stat.title}</p>
                <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Ações Rápidas</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className="btn btn-outline flex items-center justify-center">
            <FiUsers className="mr-2" />
            Adicionar Usuário
          </button>
          <button className="btn btn-outline flex items-center justify-center">
            <FiBook className="mr-2" />
            Novo Documento
          </button>
          <button className="btn btn-outline flex items-center justify-center">
            <FiCalendar className="mr-2" />
            Agendar Reunião
          </button>
        </div>
      </div>

      {/* Info Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Sobre o Sistema</h3>
          <p className="text-gray-600 text-sm leading-relaxed">
            O Sistema de Gestão Formativa é uma plataforma completa para gerenciar membros da comunidade,
            acompanhar estágios formativos, compartilhar documentos educacionais e coordenar reuniões de
            acompanhamento entre mentores e mentorados.
          </p>
        </div>
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Suas Permissões</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Perfil:</span>
              <span className="font-medium text-gray-900">{user?.roleName}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Usuário:</span>
              <span className="font-medium text-gray-900">{user?.username}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
