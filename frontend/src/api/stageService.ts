import api from './axios';
import { FormativeStage, CreateStageRequest } from '../types';

export const stageService = {
  getAll: async (): Promise<FormativeStage[]> => {
    const response = await api.get<FormativeStage[]>('/stages');
    return response.data;
  },

  getById: async (id: number): Promise<FormativeStage> => {
    const response = await api.get<FormativeStage>(`/stages/${id}`);
    return response.data;
  },

  getByUser: async (userId: number): Promise<FormativeStage[]> => {
    const response = await api.get<FormativeStage[]>(`/stages/user/${userId}`);
    return response.data;
  },

  getActive: async (): Promise<FormativeStage[]> => {
    const response = await api.get<FormativeStage[]>('/stages/active');
    return response.data;
  },

  getActiveAtDate: async (date: string): Promise<FormativeStage[]> => {
    const response = await api.get<FormativeStage[]>(`/stages/active-at?date=${date}`);
    return response.data;
  },

  create: async (userId: number, stage: CreateStageRequest): Promise<FormativeStage> => {
    const response = await api.post<FormativeStage>(`/stages/user/${userId}`, stage);
    return response.data;
  },

  update: async (id: number, stage: Partial<CreateStageRequest>): Promise<FormativeStage> => {
    const response = await api.put<FormativeStage>(`/stages/${id}`, stage);
    return response.data;
  },

  complete: async (id: number): Promise<FormativeStage> => {
    const response = await api.put<FormativeStage>(`/stages/${id}/complete`);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/stages/${id}`);
  },

  getLongerThan: async (months: number): Promise<FormativeStage[]> => {
    const response = await api.get<FormativeStage[]>(`/stages/longer-than/${months}`);
    return response.data;
  },

  getRecentlyStarted: async (): Promise<FormativeStage[]> => {
    const response = await api.get<FormativeStage[]>('/stages/recently-started');
    return response.data;
  },

  getRecentlyCompleted: async (): Promise<FormativeStage[]> => {
    const response = await api.get<FormativeStage[]>('/stages/recently-completed');
    return response.data;
  },
};
