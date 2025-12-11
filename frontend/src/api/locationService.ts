import api from './axios';
import { MissionLocation, CreateLocationRequest, User } from '../types';

export const locationService = {
  getAll: async (): Promise<MissionLocation[]> => {
    const response = await api.get<MissionLocation[]>('/locations');
    return response.data;
  },

  getById: async (id: number): Promise<MissionLocation> => {
    const response = await api.get<MissionLocation>(`/locations/${id}`);
    return response.data;
  },

  getByCity: async (city: string): Promise<MissionLocation[]> => {
    const response = await api.get<MissionLocation[]>(`/locations/by-city/${city}`);
    return response.data;
  },

  getByState: async (state: string): Promise<MissionLocation[]> => {
    const response = await api.get<MissionLocation[]>(`/locations/by-state/${state}`);
    return response.data;
  },

  create: async (location: CreateLocationRequest): Promise<MissionLocation> => {
    const response = await api.post<MissionLocation>('/locations', location);
    return response.data;
  },

  update: async (id: number, location: Partial<CreateLocationRequest>): Promise<MissionLocation> => {
    const response = await api.put<MissionLocation>(`/locations/${id}`, location);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/locations/${id}`);
  },

  assignCoordinator: async (locationId: number, userId: number): Promise<MissionLocation> => {
    const response = await api.put<MissionLocation>(`/locations/${locationId}/coordinator/${userId}`);
    return response.data;
  },

  getUsersByLocation: async (id: number): Promise<User[]> => {
    const response = await api.get<User[]>(`/locations/${id}/users`);
    return response.data;
  },
};
