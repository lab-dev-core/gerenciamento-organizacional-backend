import React, { useState, useEffect } from 'react';
import { stageService } from '../../api';
import { FormativeStage } from '../../types';
import { FiPlus, FiTrendingUp } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { format } from 'date-fns';

const StagesPage: React.FC = () => {
  const [stages, setStages] = useState<FormativeStage[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadStages();
  }, []);

  const loadStages = async () => {
    try {
      const data = await stageService.getActive();
      setStages(data);
    } catch (error) {
      console.error('Error loading stages:', error);
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

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Etapas Formativas</h1>
          <p className="text-gray-600 mt-1">Acompanhe as etapas formativas ativas</p>
        </div>
        <button className="btn btn-primary flex items-center">
          <FiPlus className="mr-2" />
          Nova Etapa
        </button>
      </div>

      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Usuário</th>
                <th>Data Início</th>
                <th>Data Fim</th>
                <th>Duração (meses)</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {stages.map((stage) => (
                <tr key={stage.id}>
                  <td className="font-medium">{stage.name}</td>
                  <td>{stage.user?.name || '-'}</td>
                  <td>
                    {stage.startDate
                      ? format(new Date(stage.startDate), 'dd/MM/yyyy')
                      : '-'}
                  </td>
                  <td>
                    {stage.endDate ? format(new Date(stage.endDate), 'dd/MM/yyyy') : '-'}
                  </td>
                  <td>{stage.durationMonths || '-'}</td>
                  <td>
                    <span className="badge badge-success">Ativa</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {stages.length === 0 && (
            <div className="text-center py-8 text-gray-500">Nenhuma etapa ativa</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default StagesPage;
