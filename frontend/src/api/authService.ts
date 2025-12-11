import api from './axios';
import { LoginRequest, LoginResponse } from '../types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', credentials);
    return response.data;
  },

  createInitialAdmin: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/users/create-admin-init', credentials);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  },

  getStoredUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  storeAuth: (loginResponse: LoginResponse) => {
    localStorage.setItem('token', loginResponse.token);
    localStorage.setItem('user', JSON.stringify({
      id: loginResponse.id,
      username: loginResponse.username,
      name: loginResponse.name,
      roleName: loginResponse.roleName,
    }));
  },
};
