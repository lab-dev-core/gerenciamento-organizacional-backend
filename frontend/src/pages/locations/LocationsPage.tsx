import React, { useState, useEffect } from 'react';
import { locationService } from '../../api';
import { MissionLocation } from '../../types';
import { FiPlus, FiMapPin } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const LocationsPage: React.FC = () => {
  const [locations, setLocations] = useState<MissionLocation[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadLocations();
  }, []);

  const loadLocations = async () => {
    try {
      const data = await locationService.getAll();
      setLocations(data);
    } catch (error) {
      console.error('Error loading locations:', error);
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
          <h1 className="text-2xl font-bold text-gray-900">Locais de Missão</h1>
          <p className="text-gray-600 mt-1">Gerencie os locais de missão</p>
        </div>
        <button className="btn btn-primary flex items-center">
          <FiPlus className="mr-2" />
          Novo Local
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {locations.map((location) => (
          <div key={location.id} className="card">
            <div className="flex items-start">
              <div className="flex items-center justify-center w-12 h-12 bg-primary-100 rounded-lg">
                <FiMapPin className="text-primary-700" size={24} />
              </div>
              <div className="ml-4 flex-1">
                <h3 className="text-lg font-semibold text-gray-900">{location.name}</h3>
                <p className="text-sm text-gray-600 mt-1">{location.description || 'Sem descrição'}</p>
                <div className="mt-3 space-y-1 text-sm text-gray-600">
                  {location.city && location.state && (
                    <p>
                      {location.city}, {location.state}
                    </p>
                  )}
                  {location.coordinator && (
                    <p className="text-primary-600">Coordenador: {location.coordinator.name}</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
      {locations.length === 0 && (
        <div className="text-center py-12 text-gray-500">Nenhum local de missão cadastrado</div>
      )}
    </div>
  );
};

export default LocationsPage;
