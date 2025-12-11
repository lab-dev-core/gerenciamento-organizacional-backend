import api from './axios';
import { Role, CreateRoleRequest, User } from '../types';

export const roleService = {
  getAll: async (): Promise<Role[]> => {
    const response = await api.get<Role[]>('/roles');
    return response.data;
  },

  getById: async (id: number): Promise<Role> => {
    const response = await api.get<Role>(`/roles/${id}`);
    return response.data;
  },

  create: async (role: CreateRoleRequest): Promise<Role> => {
    const response = await api.post<Role>('/roles', role);
    return response.data;
  },

  update: async (id: number, role: Partial<CreateRoleRequest>): Promise<Role> => {
    const response = await api.put<Role>(`/roles/${id}`, role);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/roles/${id}`);
  },

  getUsersByRole: async (id: number): Promise<User[]> => {
    const response = await api.get<User[]>(`/roles/${id}/users`);
    return response.data;
  },
};
