import api from './axios';
import { FollowUpMeeting, CreateMeetingRequest, MeetingStatistics, MeetingStatus } from '../types';

export const meetingService = {
  getAll: async (): Promise<FollowUpMeeting[]> => {
    const response = await api.get<FollowUpMeeting[]>('/follow-up');
    return response.data;
  },

  getMyMeetings: async (): Promise<FollowUpMeeting[]> => {
    const response = await api.get<FollowUpMeeting[]>('/follow-up/my-meetings');
    return response.data;
  },

  getByMentee: async (menteeId: number): Promise<FollowUpMeeting[]> => {
    const response = await api.get<FollowUpMeeting[]>(`/follow-up/mentee/${menteeId}`);
    return response.data;
  },

  getByStatus: async (status: MeetingStatus): Promise<FollowUpMeeting[]> => {
    const response = await api.get<FollowUpMeeting[]>(`/follow-up/status/${status}`);
    return response.data;
  },

  getUpcoming: async (days?: number): Promise<FollowUpMeeting[]> => {
    const url = days ? `/follow-up/upcoming?days=${days}` : '/follow-up/upcoming';
    const response = await api.get<FollowUpMeeting[]>(url);
    return response.data;
  },

  getByDateRange: async (startDate: string, endDate: string): Promise<FollowUpMeeting[]> => {
    const response = await api.get<FollowUpMeeting[]>(`/follow-up/date-range?startDate=${startDate}&endDate=${endDate}`);
    return response.data;
  },

  getById: async (id: number): Promise<FollowUpMeeting> => {
    const response = await api.get<FollowUpMeeting>(`/follow-up/${id}`);
    return response.data;
  },

  create: async (meeting: CreateMeetingRequest): Promise<FollowUpMeeting> => {
    const response = await api.post<FollowUpMeeting>('/follow-up', meeting);
    return response.data;
  },

  update: async (id: number, meeting: Partial<CreateMeetingRequest>): Promise<FollowUpMeeting> => {
    const response = await api.put<FollowUpMeeting>(`/follow-up/${id}`, meeting);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/follow-up/${id}`);
  },

  shareWithUser: async (id: number, userId: number): Promise<void> => {
    await api.post(`/follow-up/${id}/share/user/${userId}`);
  },

  shareWithRole: async (id: number, roleId: number): Promise<void> => {
    await api.post(`/follow-up/${id}/share/role/${roleId}`);
  },

  removeShareWithUser: async (id: number, userId: number): Promise<void> => {
    await api.delete(`/follow-up/${id}/share/user/${userId}`);
  },

  markAsCompleted: async (id: number): Promise<FollowUpMeeting> => {
    const response = await api.put<FollowUpMeeting>(`/follow-up/${id}/complete`);
    return response.data;
  },

  cancel: async (id: number): Promise<FollowUpMeeting> => {
    const response = await api.put<FollowUpMeeting>(`/follow-up/${id}/cancel`);
    return response.data;
  },

  getStatistics: async (): Promise<MeetingStatistics> => {
    const response = await api.get<MeetingStatistics>('/follow-up/statistics');
    return response.data;
  },
};
