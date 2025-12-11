import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { userService } from '../../api';
import { User } from '../../types';
import { FiUser, FiMail, FiPhone, FiMapPin } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const ProfilePage: React.FC = () => {
  const { user: authUser } = useAuth();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (authUser) {
      loadUser();
    }
  }, [authUser]);

  const loadUser = async () => {
    try {
      if (authUser) {
        const data = await userService.getById(authUser.id);
        setUser(data);
      }
    } catch (error) {
      console.error('Error loading user:', error);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!user) {
    return (
      <div className="text-center py-12 text-gray-500">Usuário não encontrado</div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Meu Perfil</h1>
        <p className="text-gray-600 mt-1">Visualize suas informações pessoais</p>
      </div>

      {/* Profile Header */}
      <div className="card">
        <div className="flex items-center">
          <div className="flex items-center justify-center w-20 h-20 bg-primary-600 rounded-full">
            <span className="text-3xl font-bold text-white">
              {user.name.charAt(0).toUpperCase()}
            </span>
          </div>
          <div className="ml-6">
            <h2 className="text-2xl font-bold text-gray-900">{user.name}</h2>
            <p className="text-gray-600">@{user.username}</p>
            {user.role && (
              <span className="inline-block mt-2 badge badge-primary">{user.role.name}</span>
            )}
          </div>
        </div>
      </div>

      {/* Personal Information */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Informações Pessoais</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="flex items-start">
            <FiMail className="text-gray-400 mt-1 mr-3" size={20} />
            <div>
              <p className="text-sm text-gray-600">Email</p>
              <p className="font-medium text-gray-900">{user.email || 'Não informado'}</p>
            </div>
          </div>
          <div className="flex items-start">
            <FiPhone className="text-gray-400 mt-1 mr-3" size={20} />
            <div>
              <p className="text-sm text-gray-600">Telefone</p>
              <p className="font-medium text-gray-900">{user.phone || 'Não informado'}</p>
            </div>
          </div>
          <div className="flex items-start">
            <FiMapPin className="text-gray-400 mt-1 mr-3" size={20} />
            <div>
              <p className="text-sm text-gray-600">Localização</p>
              <p className="font-medium text-gray-900">
                {user.city && user.state
                  ? `${user.city}, ${user.state}`
                  : 'Não informado'}
              </p>
            </div>
          </div>
          <div className="flex items-start">
            <FiUser className="text-gray-400 mt-1 mr-3" size={20} />
            <div>
              <p className="text-sm text-gray-600">Idade</p>
              <p className="font-medium text-gray-900">{user.age ? `${user.age} anos` : 'Não informado'}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Formation Info */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Informações Formativas</h3>
        <div className="space-y-4">
          <div>
            <p className="text-sm text-gray-600">Etapa de Vida</p>
            <p className="font-medium text-gray-900">
              {user.lifeStage ? (
                <span className="badge badge-primary">{user.lifeStage}</span>
              ) : (
                'Não definido'
              )}
            </p>
          </div>
          {user.missionLocation && (
            <div>
              <p className="text-sm text-gray-600">Local de Missão</p>
              <p className="font-medium text-gray-900">{user.missionLocation.name}</p>
              {user.missionLocation.city && user.missionLocation.state && (
                <p className="text-sm text-gray-600">
                  {user.missionLocation.city}, {user.missionLocation.state}
                </p>
              )}
            </div>
          )}
          {user.mentor && (
            <div>
              <p className="text-sm text-gray-600">Mentor</p>
              <p className="font-medium text-gray-900">{user.mentor.name}</p>
            </div>
          )}
          {user.education && (
            <div>
              <p className="text-sm text-gray-600">Educação</p>
              <p className="font-medium text-gray-900">{user.education}</p>
            </div>
          )}
        </div>
      </div>

      {/* Formative Stages */}
      {user.formativeStages && user.formativeStages.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Minhas Etapas Formativas</h3>
          <div className="space-y-3">
            {user.formativeStages.map((stage) => (
              <div key={stage.id} className="p-4 bg-gray-50 rounded-lg">
                <p className="font-medium text-gray-900">{stage.name}</p>
                <div className="mt-2 flex items-center space-x-4 text-sm text-gray-600">
                  {stage.startDate && <span>Início: {stage.startDate}</span>}
                  {stage.endDate && <span>Fim: {stage.endDate}</span>}
                  {stage.durationMonths && <span>{stage.durationMonths} meses</span>}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
