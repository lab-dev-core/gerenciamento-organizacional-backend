import React, { useState, useEffect } from 'react';
import { documentService } from '../../api';
import { FormativeDocument } from '../../types';
import { FiPlus, FiBook, FiEye } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { format } from 'date-fns';

const DocumentsPage: React.FC = () => {
  const [documents, setDocuments] = useState<FormativeDocument[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      const data = await documentService.getAll();
      setDocuments(data);
    } catch (error) {
      console.error('Error loading documents:', error);
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
          <h1 className="text-2xl font-bold text-gray-900">Documentos Formativos</h1>
          <p className="text-gray-600 mt-1">Gerencie os documentos educacionais</p>
        </div>
        <button className="btn btn-primary flex items-center">
          <FiPlus className="mr-2" />
          Novo Documento
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {documents.map((doc) => (
          <div key={doc.id} className="card hover:shadow-lg transition-shadow">
            <div className="flex items-start">
              <div className="flex items-center justify-center w-12 h-12 bg-primary-100 rounded-lg flex-shrink-0">
                <FiBook className="text-primary-700" size={24} />
              </div>
              <div className="ml-4 flex-1 min-w-0">
                <h3 className="text-lg font-semibold text-gray-900 truncate">{doc.title}</h3>
                <p className="text-sm text-gray-600 mt-1 line-clamp-2">{doc.content.substring(0, 150)}...</p>
                <div className="mt-3 flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    {doc.documentType && (
                      <span className="badge badge-primary">{doc.documentType}</span>
                    )}
                    {doc.accessLevel && (
                      <span className="badge badge-gray">{doc.accessLevel}</span>
                    )}
                  </div>
                  <button className="text-primary-600 hover:text-primary-800 flex items-center text-sm">
                    <FiEye className="mr-1" size={16} />
                    Ver
                  </button>
                </div>
                {doc.lastModifiedDate && (
                  <p className="text-xs text-gray-500 mt-2">
                    Atualizado: {format(new Date(doc.lastModifiedDate), 'dd/MM/yyyy')}
                  </p>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
      {documents.length === 0 && (
        <div className="text-center py-12 text-gray-500">Nenhum documento cadastrado</div>
      )}
    </div>
  );
};

export default DocumentsPage;
