import React, { useState, useEffect } from 'react';
import { documentService } from '../../api';
import { DocumentCategory } from '../../types';
import { FiPlus, FiFolder } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const CategoriesPage: React.FC = () => {
  const [categories, setCategories] = useState<DocumentCategory[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const data = await documentService.getAllCategories();
      setCategories(data);
    } catch (error) {
      console.error('Error loading categories:', error);
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
          <h1 className="text-2xl font-bold text-gray-900">Categorias de Documentos</h1>
          <p className="text-gray-600 mt-1">Organize os documentos em categorias</p>
        </div>
        <button className="btn btn-primary flex items-center">
          <FiPlus className="mr-2" />
          Nova Categoria
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {categories.map((category) => (
          <div key={category.id} className="card">
            <div className="flex items-start">
              <div className="flex items-center justify-center w-12 h-12 bg-purple-100 rounded-lg">
                <FiFolder className="text-purple-700" size={24} />
              </div>
              <div className="ml-4 flex-1">
                <h3 className="text-lg font-semibold text-gray-900">{category.name}</h3>
                <p className="text-sm text-gray-600 mt-1">{category.description || 'Sem descrição'}</p>
                {category.documents && (
                  <p className="text-sm text-primary-600 mt-2">
                    {category.documents.length} documento(s)
                  </p>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
      {categories.length === 0 && (
        <div className="text-center py-12 text-gray-500">Nenhuma categoria cadastrada</div>
      )}
    </div>
  );
};

export default CategoriesPage;
