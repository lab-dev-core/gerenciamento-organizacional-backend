import React, { useState, useEffect } from 'react';
import { meetingService } from '../../api';
import { FollowUpMeeting } from '../../types';
import { FiPlus, FiCalendar, FiClock } from 'react-icons/fi';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { format } from 'date-fns';

const MeetingsPage: React.FC = () => {
  const [meetings, setMeetings] = useState<FollowUpMeeting[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'upcoming' | 'completed'>('all');

  useEffect(() => {
    loadMeetings();
  }, [filter]);

  const loadMeetings = async () => {
    try {
      let data;
      if (filter === 'upcoming') {
        data = await meetingService.getUpcoming(30);
      } else if (filter === 'completed') {
        data = await meetingService.getByStatus('COMPLETED');
      } else {
        data = await meetingService.getMyMeetings();
      }
      setMeetings(data);
    } catch (error) {
      console.error('Error loading meetings:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const statusMap: { [key: string]: string } = {
      SCHEDULED: 'badge-primary',
      COMPLETED: 'badge-success',
      CANCELLED: 'badge-danger',
      RESCHEDULED: 'badge-warning',
    };
    return `badge ${statusMap[status] || 'badge-gray'}`;
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
          <h1 className="text-2xl font-bold text-gray-900">Reuniões de Acompanhamento</h1>
          <p className="text-gray-600 mt-1">Gerencie as reuniões com mentorados</p>
        </div>
        <button className="btn btn-primary flex items-center">
          <FiPlus className="mr-2" />
          Nova Reunião
        </button>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="flex space-x-2">
          <button
            onClick={() => setFilter('all')}
            className={`px-4 py-2 rounded-lg ${
              filter === 'all' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700'
            }`}
          >
            Todas
          </button>
          <button
            onClick={() => setFilter('upcoming')}
            className={`px-4 py-2 rounded-lg ${
              filter === 'upcoming' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700'
            }`}
          >
            Próximas
          </button>
          <button
            onClick={() => setFilter('completed')}
            className={`px-4 py-2 rounded-lg ${
              filter === 'completed' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700'
            }`}
          >
            Concluídas
          </button>
        </div>
      </div>

      <div className="space-y-4">
        {meetings.map((meeting) => (
          <div key={meeting.id} className="card hover:shadow-lg transition-shadow">
            <div className="flex items-start">
              <div className="flex items-center justify-center w-12 h-12 bg-primary-100 rounded-lg flex-shrink-0">
                <FiCalendar className="text-primary-700" size={24} />
              </div>
              <div className="ml-4 flex-1">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">{meeting.title}</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Mentorado: <span className="font-medium">{meeting.mentee.name}</span>
                    </p>
                  </div>
                  <span className={getStatusBadge(meeting.status)}>{meeting.status}</span>
                </div>
                <div className="mt-3 flex items-center space-x-4 text-sm text-gray-600">
                  <div className="flex items-center">
                    <FiClock className="mr-1" size={16} />
                    {format(new Date(meeting.scheduledDate), 'dd/MM/yyyy HH:mm')}
                  </div>
                  <span className="badge badge-primary">{meeting.meetingType}</span>
                </div>
                {meeting.objectives && (
                  <p className="text-sm text-gray-600 mt-2 line-clamp-2">{meeting.objectives}</p>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
      {meetings.length === 0 && (
        <div className="text-center py-12 text-gray-500">Nenhuma reunião encontrada</div>
      )}
    </div>
  );
};

export default MeetingsPage;
