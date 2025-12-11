import React, { useState, useEffect } from 'react';
import { roleService } from '../../api';
import { Role, CreateRoleRequest } from '../../types';
import { FiPlus, FiEdit2, FiTrash2, FiCheck, FiX } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Modal from '../../components/common/Modal';

const RolesPage: React.FC = () => {
  const [roles, setRoles] = useState<Role[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [formData, setFormData] = useState<CreateRoleRequest>({
    name: '',
    description: '',
    canManageUsers: false,
    canManageRoles: false,
    canManageStages: false,
    canManageDocuments: false,
  });

  useEffect(() => {
    loadRoles();
  }, []);

  const loadRoles = async () => {
    try {
      const data = await roleService.getAll();
      setRoles(data);
    } catch (error) {
      console.error('Error loading roles:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenModal = (role?: Role) => {
    if (role) {
      setEditingRole(role);
      setFormData({
        name: role.name,
        description: role.description || '',
        canManageUsers: role.canManageUsers,
        canManageRoles: role.canManageRoles,
        canManageStages: role.canManageStages,
        canManageDocuments: role.canManageDocuments,
      });
    } else {
      setEditingRole(null);
      setFormData({
        name: '',
        description: '',
        canManageUsers: false,
        canManageRoles: false,
        canManageStages: false,
        canManageDocuments: false,
      });
    }
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingRole) {
        await roleService.update(editingRole.id, formData);
      } else {
        await roleService.create(formData);
      }
      await loadRoles();
      setIsModalOpen(false);
    } catch (error) {
      console.error('Error saving role:', error);
      alert('Erro ao salvar perfil');
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Tem certeza que deseja excluir este perfil?')) {
      try {
        await roleService.delete(id);
        await loadRoles();
      } catch (error) {
        console.error('Error deleting role:', error);
        alert('Erro ao excluir perfil');
      }
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
          <h1 className="text-2xl font-bold text-gray-900">Perfis</h1>
          <p className="text-gray-600 mt-1">Gerencie os perfis e permissões do sistema</p>
        </div>
        <button onClick={() => handleOpenModal()} className="btn btn-primary flex items-center">
          <FiPlus className="mr-2" />
          Novo Perfil
        </button>
      </div>

      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Descrição</th>
                <th>Usuários</th>
                <th>Perfis</th>
                <th>Etapas</th>
                <th>Documentos</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {roles.map((role) => (
                <tr key={role.id}>
                  <td className="font-medium">{role.name}</td>
                  <td>{role.description || '-'}</td>
                  <td>
                    {role.canManageUsers ? (
                      <FiCheck className="text-green-600" size={20} />
                    ) : (
                      <FiX className="text-gray-400" size={20} />
                    )}
                  </td>
                  <td>
                    {role.canManageRoles ? (
                      <FiCheck className="text-green-600" size={20} />
                    ) : (
                      <FiX className="text-gray-400" size={20} />
                    )}
                  </td>
                  <td>
                    {role.canManageStages ? (
                      <FiCheck className="text-green-600" size={20} />
                    ) : (
                      <FiX className="text-gray-400" size={20} />
                    )}
                  </td>
                  <td>
                    {role.canManageDocuments ? (
                      <FiCheck className="text-green-600" size={20} />
                    ) : (
                      <FiX className="text-gray-400" size={20} />
                    )}
                  </td>
                  <td className="text-right">
                    <button
                      onClick={() => handleOpenModal(role)}
                      className="text-primary-600 hover:text-primary-800 mr-3"
                    >
                      <FiEdit2 size={18} />
                    </button>
                    <button
                      onClick={() => handleDelete(role.id)}
                      className="text-red-600 hover:text-red-800"
                    >
                      <FiTrash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingRole ? 'Editar Perfil' : 'Novo Perfil'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">Nome *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="input"
              required
            />
          </div>
          <div>
            <label className="label">Descrição</label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="input"
              rows={3}
            />
          </div>
          <div className="space-y-2">
            <label className="label">Permissões</label>
            <div className="space-y-2">
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.canManageUsers}
                  onChange={(e) => setFormData({ ...formData, canManageUsers: e.target.checked })}
                  className="mr-2"
                />
                Gerenciar Usuários
              </label>
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.canManageRoles}
                  onChange={(e) => setFormData({ ...formData, canManageRoles: e.target.checked })}
                  className="mr-2"
                />
                Gerenciar Perfis
              </label>
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.canManageStages}
                  onChange={(e) => setFormData({ ...formData, canManageStages: e.target.checked })}
                  className="mr-2"
                />
                Gerenciar Etapas
              </label>
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.canManageDocuments}
                  onChange={(e) => setFormData({ ...formData, canManageDocuments: e.target.checked })}
                  className="mr-2"
                />
                Gerenciar Documentos
              </label>
            </div>
          </div>
          <div className="flex justify-end space-x-3 mt-6">
            <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary">
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary">
              {editingRole ? 'Salvar' : 'Criar'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default RolesPage;
