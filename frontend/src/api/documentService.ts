import api from './axios';
import { FormativeDocument, CreateDocumentRequest, DocumentCategory, CreateCategoryRequest, DocumentSearchParams, DocumentReadingProgress, UpdateReadingProgressRequest, LifeStage } from '../types';

export const documentService = {
  // Documents
  getAll: async (): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>('/documents');
    return response.data;
  },

  getById: async (id: number): Promise<FormativeDocument> => {
    const response = await api.get<FormativeDocument>(`/documents/${id}`);
    return response.data;
  },

  create: async (document: CreateDocumentRequest): Promise<FormativeDocument> => {
    const response = await api.post<FormativeDocument>('/documents', document);
    return response.data;
  },

  update: async (id: number, document: Partial<CreateDocumentRequest>): Promise<FormativeDocument> => {
    const response = await api.put<FormativeDocument>(`/documents/${id}`, document);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/documents/${id}`);
  },

  uploadAttachment: async (id: number, file: File): Promise<FormativeDocument> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<FormativeDocument>(`/documents/${id}/attachment`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getByStage: async (stageName: LifeStage): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>(`/documents/by-stage/${stageName}`);
    return response.data;
  },

  getByLocation: async (locationId: number): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>(`/documents/by-location/${locationId}`);
    return response.data;
  },

  grantAccessToUser: async (documentId: number, userId: number): Promise<void> => {
    await api.post(`/documents/${documentId}/access/user/${userId}`);
  },

  // Categories
  getAllCategories: async (): Promise<DocumentCategory[]> => {
    const response = await api.get<DocumentCategory[]>('/categories');
    return response.data;
  },

  getRootCategories: async (): Promise<DocumentCategory[]> => {
    const response = await api.get<DocumentCategory[]>('/categories/root');
    return response.data;
  },

  getCategoryById: async (id: number): Promise<DocumentCategory> => {
    const response = await api.get<DocumentCategory>(`/categories/${id}`);
    return response.data;
  },

  getSubcategories: async (id: number): Promise<DocumentCategory[]> => {
    const response = await api.get<DocumentCategory[]>(`/categories/${id}/subcategories`);
    return response.data;
  },

  createCategory: async (category: CreateCategoryRequest): Promise<DocumentCategory> => {
    const response = await api.post<DocumentCategory>('/categories', category);
    return response.data;
  },

  updateCategory: async (id: number, category: Partial<CreateCategoryRequest>): Promise<DocumentCategory> => {
    const response = await api.put<DocumentCategory>(`/categories/${id}`, category);
    return response.data;
  },

  deleteCategory: async (id: number): Promise<void> => {
    await api.delete(`/categories/${id}`);
  },

  addDocumentToCategory: async (categoryId: number, documentId: number): Promise<void> => {
    await api.post(`/categories/${categoryId}/documents/${documentId}`);
  },

  removeDocumentFromCategory: async (categoryId: number, documentId: number): Promise<void> => {
    await api.delete(`/categories/${categoryId}/documents/${documentId}`);
  },

  // Search
  search: async (params: DocumentSearchParams): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>('/search/documents', { params });
    return response.data;
  },

  searchByContent: async (text: string): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>(`/search/content?text=${text}`);
    return response.data;
  },

  getRecent: async (): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>('/search/recent');
    return response.data;
  },

  getMostViewed: async (): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>('/search/most-viewed');
    return response.data;
  },

  getRecommended: async (): Promise<FormativeDocument[]> => {
    const response = await api.get<FormativeDocument[]>('/search/recommended');
    return response.data;
  },

  // Reading Progress
  getProgress: async (documentId: number): Promise<DocumentReadingProgress> => {
    const response = await api.get<DocumentReadingProgress>(`/reading-progress/document/${documentId}`);
    return response.data;
  },

  updateProgress: async (documentId: number, progress: UpdateReadingProgressRequest): Promise<DocumentReadingProgress> => {
    const response = await api.post<DocumentReadingProgress>(`/reading-progress/document/${documentId}`, progress);
    return response.data;
  },

  getCompleted: async (): Promise<DocumentReadingProgress[]> => {
    const response = await api.get<DocumentReadingProgress[]>('/reading-progress/completed');
    return response.data;
  },

  getInProgress: async (): Promise<DocumentReadingProgress[]> => {
    const response = await api.get<DocumentReadingProgress[]>('/reading-progress/in-progress');
    return response.data;
  },

  getRecentlyViewed: async (): Promise<DocumentReadingProgress[]> => {
    const response = await api.get<DocumentReadingProgress[]>('/reading-progress/recent');
    return response.data;
  },

  resetProgress: async (documentId: number): Promise<void> => {
    await api.delete(`/reading-progress/document/${documentId}`);
  },
};
