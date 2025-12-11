import React, { useState, useEffect } from 'react';
import { userService, roleService, locationService } from '../../api';
import { User, Role, MissionLocation, LifeStage, CreateUserRequest } from '../../types';
import { FiPlus, FiEdit2, FiTrash2, FiSearch } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Modal from '../../components/common/Modal';

const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [locations, setLocations] = useState<MissionLocation[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formData, setFormData] = useState<CreateUserRequest>({
    username: '',
    password: '',
    name: '',
    email: '',
    city: '',
    state: '',
    age: undefined,
    phone: '',
    education: '',
    lifeStage: undefined,
    roleId: undefined,
    missionLocationId: undefined,
    mentorId: undefined,
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [usersData, rolesData, locationsData] = await Promise.all([
        userService.getAll(),
        roleService.getAll(),
        locationService.getAll(),
      ]);
      setUsers(usersData);
      setRoles(rolesData);
      setLocations(locationsData);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenModal = (user?: User) => {
    if (user) {
      setEditingUser(user);
      setFormData({
        username: user.username,
        password: '', // Don't show existing password
        name: user.name,
        email: user.email || '',
        city: user.city || '',
        state: user.state || '',
        age: user.age,
        phone: user.phone || '',
        education: user.education || '',
        lifeStage: user.lifeStage,
        roleId: user.role?.id,
        missionLocationId: user.missionLocation?.id,
        mentorId: user.mentor?.id,
      });
    } else {
      setEditingUser(null);
      setFormData({
        username: '',
        password: '',
        name: '',
        email: '',
        city: '',
        state: '',
        age: undefined,
        phone: '',
        education: '',
        lifeStage: undefined,
        roleId: undefined,
        missionLocationId: undefined,
        mentorId: undefined,
      });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingUser(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingUser) {
        await userService.update(editingUser.id, formData);
      } else {
        await userService.create(formData);
      }
      await loadData();
      handleCloseModal();
    } catch (error) {
      console.error('Error saving user:', error);
      alert('Erro ao salvar usuário');
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Tem certeza que deseja excluir este usuário?')) {
      try {
        await userService.delete(id);
        await loadData();
      } catch (error) {
        console.error('Error deleting user:', error);
        alert('Erro ao excluir usuário');
      }
    }
  };

  const filteredUsers = users.filter(
    (user) =>
      user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Usuários</h1>
          <p className="text-gray-600 mt-1">Gerencie os usuários do sistema</p>
        </div>
        <button onClick={() => handleOpenModal()} className="btn btn-primary flex items-center">
          <FiPlus className="mr-2" />
          Novo Usuário
        </button>
      </div>

      {/* Search */}
      <div className="card">
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <FiSearch className="text-gray-400" size={20} />
          </div>
          <input
            type="text"
            placeholder="Buscar usuários..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input pl-10"
          />
        </div>
      </div>

      {/* Users Table */}
      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Usuário</th>
                <th>Email</th>
                <th>Etapa de Vida</th>
                <th>Perfil</th>
                <th>Local de Missão</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((user) => (
                <tr key={user.id}>
                  <td className="font-medium">{user.name}</td>
                  <td>{user.username}</td>
                  <td>{user.email || '-'}</td>
                  <td>
                    {user.lifeStage ? (
                      <span className="badge badge-primary">{user.lifeStage}</span>
                    ) : (
                      '-'
                    )}
                  </td>
                  <td>{user.role?.name || '-'}</td>
                  <td>{user.missionLocation?.name || '-'}</td>
                  <td className="text-right">
                    <button
                      onClick={() => handleOpenModal(user)}
                      className="text-primary-600 hover:text-primary-800 mr-3"
                    >
                      <FiEdit2 size={18} />
                    </button>
                    <button
                      onClick={() => handleDelete(user.id)}
                      className="text-red-600 hover:text-red-800"
                    >
                      <FiTrash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filteredUsers.length === 0 && (
            <div className="text-center py-8 text-gray-500">Nenhum usuário encontrado</div>
          )}
        </div>
      </div>

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title={editingUser ? 'Editar Usuário' : 'Novo Usuário'}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
              <label className="label">Usuário *</label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                className="input"
                required
              />
            </div>
            <div>
              <label className="label">Senha {!editingUser && '*'}</label>
              <input
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                className="input"
                required={!editingUser}
                placeholder={editingUser ? 'Deixe em branco para manter' : ''}
              />
            </div>
            <div>
              <label className="label">Email</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="input"
              />
            </div>
            <div>
              <label className="label">Cidade</label>
              <input
                type="text"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                className="input"
              />
            </div>
            <div>
              <label className="label">Estado</label>
              <input
                type="text"
                value={formData.state}
                onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                className="input"
              />
            </div>
            <div>
              <label className="label">Idade</label>
              <input
                type="number"
                value={formData.age || ''}
                onChange={(e) =>
                  setFormData({ ...formData, age: e.target.value ? parseInt(e.target.value) : undefined })
                }
                className="input"
              />
            </div>
            <div>
              <label className="label">Telefone</label>
              <input
                type="text"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="input"
              />
            </div>
            <div>
              <label className="label">Educação</label>
              <input
                type="text"
                value={formData.education}
                onChange={(e) => setFormData({ ...formData, education: e.target.value })}
                className="input"
              />
            </div>
            <div>
              <label className="label">Etapa de Vida</label>
              <select
                value={formData.lifeStage || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    lifeStage: e.target.value ? (e.target.value as LifeStage) : undefined,
                  })
                }
                className="input"
              >
                <option value="">Selecione...</option>
                {Object.values(LifeStage).map((stage) => (
                  <option key={stage} value={stage}>
                    {stage}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Perfil</label>
              <select
                value={formData.roleId || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    roleId: e.target.value ? parseInt(e.target.value) : undefined,
                  })
                }
                className="input"
              >
                <option value="">Selecione...</option>
                {roles.map((role) => (
                  <option key={role.id} value={role.id}>
                    {role.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Local de Missão</label>
              <select
                value={formData.missionLocationId || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    missionLocationId: e.target.value ? parseInt(e.target.value) : undefined,
                  })
                }
                className="input"
              >
                <option value="">Selecione...</option>
                {locations.map((location) => (
                  <option key={location.id} value={location.id}>
                    {location.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex justify-end space-x-3 mt-6">
            <button type="button" onClick={handleCloseModal} className="btn btn-secondary">
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary">
              {editingUser ? 'Salvar' : 'Criar'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default UsersPage;
