import React, { useState, useEffect } from 'react';
import { Calendar as CalendarIcon, ChevronLeft, ChevronRight, Plus, CheckCircle, Users, Clock, FolderKanban, Save, Edit, Trash2 } from 'lucide-react';
import Card from '../common/Card';
import Button from '../common/Button';
import Input from '../common/Input';
import Modal from '../common/Modal';
import LoadingSpinner from '../common/LoadingSpinner';
import { useToast } from '../../hooks/useToast';
import { format, startOfMonth, endOfMonth, startOfWeek, endOfWeek, addMonths, subMonths, addDays, isSameMonth, isSameDay } from 'date-fns';
import api from '../../api/api';

interface CalendarEvent {
  id: number;
  title: string;
  description?: string;
  date: Date;
  type: 'task' | 'project' | 'meeting' | 'custom';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?: string;
  color: string;
  projectName?: string;
  assignedTo?: string;
  projectId?: number;
  assignedUserId?: number;
  allDay?: boolean;
  endDate?: Date;
  eventDate: string;
  createdBy?: number;
}

interface NewEventForm {
  title: string;
  description: string;
  date: string;
  time: string;
  type: 'task' | 'project' | 'meeting' | 'custom';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  projectId?: string;
  assignedUserId?: string;
  allDay: boolean;
  endDate?: string;
  endTime?: string;
}

const Calendar: React.FC = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [viewMode, setViewMode] = useState<'month' | 'week' | 'day'>('month');
  const [filterType, setFilterType] = useState<string>('all');
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [isNewEventModalOpen, setIsNewEventModalOpen] = useState(false);
  const [isConfirmDeleteModalOpen, setIsConfirmDeleteModalOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null);
  
  const { success, error } = useToast();

  const [newEventForm, setNewEventForm] = useState<NewEventForm>({
    title: '',
    description: '',
    date: format(new Date(), 'yyyy-MM-dd'),
    time: format(new Date(), 'HH:mm'),
    type: 'custom',
    priority: 'MEDIUM',
    allDay: false,
  });

  const loadCalendarEvents = async () => {
    try {
      const response = await api.get('/calendar/my-events');
      const calendarEvents = response.data.map((event: any) => ({
        id: event.id,
        title: event.title,
        description: event.description,
        date: new Date(event.eventDate),
        eventDate: event.eventDate,
        type: event.type,
        priority: event.priority,
        status: event.status,
        color: event.color || getEventColor(event.type, event.priority),
        allDay: event.allDay || false,
        projectId: event.projectId,
        assignedUserId: event.assignedUserId,
        createdBy: event.createdBy,
        ...(event.endDate && { endDate: new Date(event.endDate) }),
      }));
      return calendarEvents;
    } catch (err) {
      console.error('Error loading calendar events:', err);
      return [];
    }
  };

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setIsLoading(true);
        const calendarEvents = await loadCalendarEvents();
        setEvents(calendarEvents);
      } catch (err) {
        console.error('Error fetching events:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchEvents();
  }, []);

  const getEventColor = (type: string, priority?: string) => {
    switch (type) {
      case 'task':
        switch (priority) {
          case 'URGENT': return 'bg-red-500';
          case 'HIGH': return 'bg-orange-500';
          case 'MEDIUM': return 'bg-yellow-500';
          default: return 'bg-blue-500';
        }
      case 'project':
        return 'bg-purple-500';
      case 'meeting':
        return 'bg-pink-500';
      case 'custom':
        return 'bg-teal-500';
      default:
        return 'bg-gray-500';
    }
  };

  const getEventIcon = (type: string) => {
    switch (type) {
      case 'task': return <CheckCircle className="h-3 w-3" />;
      case 'project': return <FolderKanban className="h-3 w-3" />;
      case 'meeting': return <Users className="h-3 w-3" />;
      default: return <Clock className="h-3 w-3" />;
    }
  };

  const handleAddEventClick = () => {
    const defaultDate = selectedDate || new Date();
    setNewEventForm({
      title: '',
      description: '',
      date: format(defaultDate, 'yyyy-MM-dd'),
      time: format(defaultDate, 'HH:mm'),
      type: 'custom',
      priority: 'MEDIUM',
      allDay: false,
    });
    setIsNewEventModalOpen(true);
    setIsEditMode(false);
  };

  const handleEditEventClick = (event: CalendarEvent) => {
    setSelectedEvent(event);
    setNewEventForm({
      title: event.title,
      description: event.description || '',
      date: format(event.date, 'yyyy-MM-dd'),
      time: format(event.date, 'HH:mm'),
      type: event.type,
      priority: event.priority || 'MEDIUM',
      allDay: event.allDay || false,
      projectId: event.projectId?.toString(),
      assignedUserId: event.assignedUserId?.toString(),
      ...(event.endDate && {
        endDate: format(event.endDate, 'yyyy-MM-dd'),
        endTime: format(event.endDate, 'HH:mm'),
      }),
    });
    setIsNewEventModalOpen(true);
    setIsEditMode(true);
  };

  const handleNewEventSubmit = async () => {
    try {
      if (!newEventForm.title.trim()) {
        error('Please enter an event title');
        return;
      }

      const eventDateTime = `${newEventForm.date}T${newEventForm.time}:00`;
      const endDateTime = newEventForm.endDate && newEventForm.endTime 
        ? `${newEventForm.endDate}T${newEventForm.endTime}:00`
        : null;

      const eventData = {
        title: newEventForm.title,
        description: newEventForm.description,
        type: newEventForm.type,
        priority: newEventForm.priority,
        eventDate: eventDateTime,
        allDay: newEventForm.allDay,
        ...(endDateTime && { endDate: endDateTime }),
        ...(newEventForm.projectId && { projectId: parseInt(newEventForm.projectId) }),
        ...(newEventForm.assignedUserId && { assignedUserId: parseInt(newEventForm.assignedUserId) }),
      };

      if (isEditMode && selectedEvent) {
        const response = await api.put(`/calendar/${selectedEvent.id}`, eventData);
        const updatedEvent = {
          id: response.data.id,
          title: response.data.title,
          description: response.data.description,
          date: new Date(response.data.eventDate),
          eventDate: response.data.eventDate,
          type: response.data.type,
          priority: response.data.priority,
          color: response.data.color || getEventColor(response.data.type, response.data.priority),
          allDay: response.data.allDay || false,
          ...(response.data.endDate && { endDate: new Date(response.data.endDate) }),
        };

        setEvents(prev => prev.map(event => 
          event.id === selectedEvent.id ? updatedEvent : event
        ));
        success('Event updated successfully!');
      } else {
        const response = await api.post('/calendar', eventData);
        const newEvent = {
          id: response.data.id,
          title: response.data.title,
          description: response.data.description,
          date: new Date(response.data.eventDate),
          eventDate: response.data.eventDate,
          type: response.data.type,
          priority: response.data.priority,
          color: response.data.color || getEventColor(response.data.type, response.data.priority),
          allDay: response.data.allDay || false,
          ...(response.data.endDate && { endDate: new Date(response.data.endDate) }),
        };

        setEvents(prev => [...prev, newEvent]);
        success('Event created successfully!');
      }

      setIsNewEventModalOpen(false);
      resetNewEventForm();
      setSelectedEvent(null);
    } catch (err: any) {
      error(err.response?.data?.message || err.message || 'Failed to save event');
    }
  };

  const resetNewEventForm = () => {
    setNewEventForm({
      title: '',
      description: '',
      date: format(new Date(), 'yyyy-MM-dd'),
      time: format(new Date(), 'HH:mm'),
      type: 'custom',
      priority: 'MEDIUM',
      allDay: false,
    });
  };

  const handleEventClick = (event: CalendarEvent) => {
    setSelectedEvent(event);
    setIsEventModalOpen(true);
  };

  const handleDeleteConfirm = () => {
    setIsConfirmDeleteModalOpen(true);
  };

  const handleDeleteEvent = async () => {
    if (!selectedEvent) return;
    
    try {
      await api.delete(`/calendar/${selectedEvent.id}`);
      setEvents(prev => prev.filter(e => e.id !== selectedEvent.id));
      success('Event deleted successfully!');
      setIsEventModalOpen(false);
      setIsConfirmDeleteModalOpen(false);
      setSelectedEvent(null);
    } catch (err: any) {
      error(err.response?.data?.message || err.message || 'Failed to delete event');
    }
  };

  const getPriorityLabel = (priority?: string) => {
    switch (priority) {
      case 'URGENT': return 'Urgent';
      case 'HIGH': return 'High';
      case 'MEDIUM': return 'Medium';
      case 'LOW': return 'Low';
      default: return 'Normal';
    }
  };

  const goToPreviousMonth = () => {
    setCurrentDate(subMonths(currentDate, 1));
  };

  const goToNextMonth = () => {
    setCurrentDate(addMonths(currentDate, 1));
  };

  const goToToday = () => {
    const today = new Date();
    setCurrentDate(today);
    setSelectedDate(today);
  };

  const getDaysInMonth = () => {
    const monthStart = startOfMonth(currentDate);
    const monthEnd = endOfMonth(currentDate);
    const startDate = startOfWeek(monthStart);
    const endDate = endOfWeek(monthEnd);

    const days = [];
    let day = startDate;

    while (day <= endDate) {
      days.push(day);
      day = addDays(day, 1);
    }

    return days;
  };

  const getEventsForDate = (date: Date) => {
    const dateStr = format(date, 'yyyy-MM-dd');
    
    const foundEvents = events.filter(event => {
      const eventDateStr = format(event.date, 'yyyy-MM-dd');
      const sameDate = eventDateStr === dateStr;
      const matchesFilter = filterType === 'all' || event.type === filterType;
      return sameDate && matchesFilter;
    });
    
    return foundEvents;
  };

  const getWeekEvents = () => {
    const weekStart = startOfWeek(selectedDate);
    const weekDays = [];
    
    for (let i = 0; i < 7; i++) {
      const day = addDays(weekStart, i);
      const dayEvents = getEventsForDate(day);
      weekDays.push({
        date: day,
        events: dayEvents,
      });
    }
    
    return weekDays;
  };

  const renderMonthView = () => {
    const days = getDaysInMonth();
    const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    const isToday = (date: Date) => {
      const today = new Date();
      return date.getDate() === today.getDate() &&
             date.getMonth() === today.getMonth() &&
             date.getFullYear() === today.getFullYear();
    };

    return (
      <div className="grid grid-cols-7 gap-1">
        {weekDays.map((day) => (
          <div key={day} className="text-center font-semibold text-gray-700 dark:text-gray-300 py-2">
            {day}
          </div>
        ))}
        {days.map((day, index) => {
          const dayEvents = getEventsForDate(day);
          const today = isToday(day);
          const isSelected = isSameDay(day, selectedDate);
          const isCurrentMonth = isSameMonth(day, currentDate);

          return (
            <div
              key={index}
              className={`min-h-24 p-1 border border-gray-200 dark:border-gray-700 ${
                !isCurrentMonth ? 'bg-gray-50 dark:bg-gray-900' : ''
              } ${today ? 'bg-blue-50 dark:bg-blue-900/20' : ''} ${
                isSelected ? 'ring-2 ring-primary-500' : ''
              }`}
              onClick={() => {
                setSelectedDate(day);
                if (viewMode !== 'day') setViewMode('day');
              }}
            >
              <div className="flex justify-between items-center mb-1">
                <span className={`text-sm font-medium ${
                  today 
                    ? 'bg-primary-600 text-white rounded-full h-6 w-6 flex items-center justify-center' 
                    : isCurrentMonth 
                      ? 'text-gray-900 dark:text-white' 
                      : 'text-gray-400 dark:text-gray-600'
                }`}>
                  {format(day, 'd')}
                </span>
                {dayEvents.length > 0 && (
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    {dayEvents.length}
                  </span>
                )}
              </div>
              <div className="space-y-1 max-h-20 overflow-y-auto">
                {dayEvents.slice(0, 3).map((event) => (
                  <div
                    key={event.id}
                    className={`${event.color} text-white text-xs p-1 rounded truncate cursor-pointer hover:opacity-90`}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleEventClick(event);
                    }}
                  >
                    <div className="flex items-center">
                      {getEventIcon(event.type)}
                      <span className="ml-1 truncate">{event.title}</span>
                    </div>
                  </div>
                ))}
                {dayEvents.length > 3 && (
                  <div className="text-xs text-gray-500 dark:text-gray-400 text-center">
                    +{dayEvents.length - 3} more
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  const renderWeekView = () => {
    const weekDays = getWeekEvents();
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    const isToday = (date: Date) => {
      const today = new Date();
      return date.getDate() === today.getDate() &&
             date.getMonth() === today.getMonth() &&
             date.getFullYear() === today.getFullYear();
    };

    return (
      <div className="grid grid-cols-7 gap-4">
        {weekDays.map((day, index) => {
          const today = isToday(day.date);
          const isSelected = isSameDay(day.date, selectedDate);

          return (
            <div key={index} className={`${isSelected ? 'bg-blue-50 dark:bg-blue-900/20' : ''}`}>
              <div className={`text-center font-semibold p-2 ${today ? 'text-primary-600 dark:text-primary-400' : 'text-gray-700 dark:text-gray-300'}`}>
                <div className="text-sm">{dayNames[index].substring(0, 3)}</div>
                <div className="text-lg">{format(day.date, 'd')}</div>
              </div>
              <div className="mt-2 space-y-2">
                {day.events.map((event) => (
                  <div
                    key={event.id}
                    className={`${event.color} text-white p-2 rounded text-sm cursor-pointer hover:opacity-90`}
                    onClick={() => handleEventClick(event)}
                  >
                    <div className="font-medium truncate">{event.title}</div>
                    <div className="text-xs opacity-90 mt-1">
                      {format(event.date, 'h:mm a')}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  const renderEventList = () => {
    const dayEvents = getEventsForDate(selectedDate).sort((a, b) => a.date.getTime() - b.date.getTime());

    const isToday = (date: Date) => {
      const today = new Date();
      return date.getDate() === today.getDate() &&
             date.getMonth() === today.getMonth() &&
             date.getFullYear() === today.getFullYear();
    };

    if (dayEvents.length === 0) {
      return (
        <div className="text-center py-8">
          <CalendarIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
            No events scheduled
          </h3>
          <p className="text-gray-600 dark:text-gray-400">
            {isToday(selectedDate)
              ? "You're all caught up for today!"
              : `No events on ${format(selectedDate, 'MMMM d, yyyy')}`}
          </p>
          <div className="mt-6">
            <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
              Total events in calendar: {events.length}
            </p>
            <Button 
              variant="primary" 
              onClick={handleAddEventClick}
            >
              <Plus className="h-4 w-4 mr-2" />
              Add Event
            </Button>
          </div>
        </div>
      );
    }

    return (
      <div className="space-y-3">
        {dayEvents.map((event) => (
          <Card 
            key={event.id} 
            hover 
            className="p-4 cursor-pointer"
            onClick={() => handleEventClick(event)}
          >
            <div className="flex items-start justify-between">
              <div className="flex items-start space-x-3">
                <div className={`${event.color} h-10 w-10 rounded-lg flex items-center justify-center`}>
                  {getEventIcon(event.type)}
                </div>
                <div>
                  <h4 className="font-semibold text-gray-900 dark:text-white">
                    {event.title}
                  </h4>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                    {event.description || 'No description'}
                  </p>
                  <div className="flex items-center space-x-4 mt-2">
                    <div className="flex items-center text-sm text-gray-500 dark:text-gray-400">
                      <Clock className="h-3 w-3 mr-1" />
                      {format(event.date, 'h:mm a')}
                    </div>
                    {event.priority && (
                      <span className={`px-2 py-1 rounded-full text-xs ${
                        event.priority === 'URGENT' || event.priority === 'HIGH'
                          ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                          : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
                      }`}>
                        {getPriorityLabel(event.priority)}
                      </span>
                    )}
                  </div>
                </div>
              </div>
              <div className="text-right">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleEditEventClick(event);
                  }}
                >
                  <Edit className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </Card>
        ))}
      </div>
    );
  };

  const filterOptions = [
    { value: 'all', label: 'All Events' },
    { value: 'task', label: 'Tasks' },
    { value: 'project', label: 'Projects' },
    { value: 'custom', label: 'Custom Events' },
    { value: 'meeting', label: 'Meetings' },
  ];

  const eventTypeOptions = [
    { value: 'custom', label: 'Custom Event' },
    { value: 'task', label: 'Task' },
    { value: 'project', label: 'Project' },
    { value: 'meeting', label: 'Meeting' },
  ];

  const priorityOptions = [
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' },
  ];

  const isToday = (date: Date) => {
    const today = new Date();
    return date.getDate() === today.getDate() &&
           date.getMonth() === today.getMonth() &&
           date.getFullYear() === today.getFullYear();
  };

  const isFuture = (date: Date) => {
    const today = new Date();
    return date > today;
  };

  const stats = {
    total: events.length,
    tasks: events.filter(e => e.type === 'task').length,
    projects: events.filter(e => e.type === 'project').length,
    meetings: events.filter(e => e.type === 'meeting').length,
    custom: events.filter(e => e.type === 'custom').length,
    today: events.filter(e => isToday(e.date)).length,
    upcoming: events.filter(e => {
      const weekFromNow = new Date();
      weekFromNow.setDate(weekFromNow.getDate() + 7);
      return isFuture(e.date) && e.date <= weekFromNow;
    }).length,
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
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Calendar
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            View and manage your schedule
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button onClick={goToToday}>
            Today
          </Button>
          <Button onClick={handleAddEventClick}>
            <Plus className="h-4 w-4 mr-2" />
            Add Event
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Total Events</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {stats.total}
          </p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Tasks</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {stats.tasks}
          </p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Projects</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {stats.projects}
          </p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Meetings</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {stats.meetings}
          </p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <p className="text-sm text-gray-600 dark:text-gray-400">Custom</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {stats.custom}
          </p>
        </div>
      </div>

      <Card>
        <div className="p-6">
          <div className="flex flex-col lg:flex-row lg:items-center justify-between mb-6">
            <div className="flex items-center space-x-4 mb-4 lg:mb-0">
              <div className="flex items-center space-x-2">
                <button
                  onClick={goToPreviousMonth}
                  className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800"
                >
                  <ChevronLeft className="h-5 w-5" />
                </button>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white min-w-48 text-center">
                  {format(currentDate, 'MMMM yyyy')}
                </h2>
                <button
                  onClick={goToNextMonth}
                  className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800"
                >
                  <ChevronRight className="h-5 w-5" />
                </button>
              </div>
              <div className="text-lg font-semibold text-gray-900 dark:text-white">
                {selectedDate && format(selectedDate, 'EEEE, MMMM d, yyyy')}
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex border border-gray-300 dark:border-gray-700 rounded-lg overflow-hidden">
                <button
                  onClick={() => setViewMode('month')}
                  className={`px-4 py-2 text-sm ${viewMode === 'month' ? 'bg-gray-100 dark:bg-gray-800' : ''}`}
                >
                  Month
                </button>
                <button
                  onClick={() => setViewMode('week')}
                  className={`px-4 py-2 text-sm ${viewMode === 'week' ? 'bg-gray-100 dark:bg-gray-800' : ''}`}
                >
                  Week
                </button>
                <button
                  onClick={() => setViewMode('day')}
                  className={`px-4 py-2 text-sm ${viewMode === 'day' ? 'bg-gray-100 dark:bg-gray-800' : ''}`}
                >
                  Day
                </button>
              </div>
              <select
                value={filterType}
                onChange={(e) => setFilterType(e.target.value)}
                className="rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm"
              >
                {filterOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="mb-8">
            {viewMode === 'month' && renderMonthView()}
            {viewMode === 'week' && renderWeekView()}
            {viewMode === 'day' && renderEventList()}
          </div>

          <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Events for {format(selectedDate, 'MMMM d, yyyy')}
              </h3>
              <div className="flex items-center space-x-4">
                <div className="flex items-center space-x-2">
                  <div className="flex items-center">
                    <div className="h-3 w-3 rounded-full bg-blue-500 mr-1"></div>
                    <span className="text-xs text-gray-600 dark:text-gray-400">Tasks</span>
                  </div>
                  <div className="flex items-center">
                    <div className="h-3 w-3 rounded-full bg-purple-500 mr-1"></div>
                    <span className="text-xs text-gray-600 dark:text-gray-400">Projects</span>
                  </div>
                  <div className="flex items-center">
                    <div className="h-3 w-3 rounded-full bg-teal-500 mr-1"></div>
                    <span className="text-xs text-gray-600 dark:text-gray-400">Custom</span>
                  </div>
                  <div className="flex items-center">
                    <div className="h-3 w-3 rounded-full bg-pink-500 mr-1"></div>
                    <span className="text-xs text-gray-600 dark:text-gray-400">Meetings</span>
                  </div>
                </div>
              </div>
            </div>
            {renderEventList()}
          </div>
        </div>
      </Card>

      <Modal
        isOpen={isNewEventModalOpen}
        onClose={() => {
          setIsNewEventModalOpen(false);
          resetNewEventForm();
          setSelectedEvent(null);
        }}
        title={isEditMode ? "Edit Event" : "Add New Event"}
        size="lg"
      >
        <div className="space-y-4">
          <Input
            label="Event Title"
            placeholder="Enter event title"
            value={newEventForm.title}
            onChange={(e) => setNewEventForm({...newEventForm, title: e.target.value})}
            required
          />
          
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Description
            </label>
            <textarea
              value={newEventForm.description}
              onChange={(e) => setNewEventForm({...newEventForm, description: e.target.value})}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white transition-colors"
              placeholder="Describe the event..."
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Date
              </label>
              <input
                type="date"
                value={newEventForm.date}
                onChange={(e) => setNewEventForm({...newEventForm, date: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Time
              </label>
              <input
                type="time"
                value={newEventForm.time}
                onChange={(e) => setNewEventForm({...newEventForm, time: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
                required
              />
            </div>
          </div>

          <div className="flex items-center">
            <input
              type="checkbox"
              id="allDay"
              checked={newEventForm.allDay}
              onChange={(e) => setNewEventForm({...newEventForm, allDay: e.target.checked})}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
            <label htmlFor="allDay" className="ml-2 block text-sm text-gray-700 dark:text-gray-300">
              All-day event
            </label>
          </div>

          {!newEventForm.allDay && (
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  End Date (Optional)
                </label>
                <input
                  type="date"
                  value={newEventForm.endDate || ''}
                  onChange={(e) => setNewEventForm({...newEventForm, endDate: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  End Time (Optional)
                </label>
                <input
                  type="time"
                  value={newEventForm.endTime || ''}
                  onChange={(e) => setNewEventForm({...newEventForm, endTime: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
                />
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Event Type
              </label>
              <select
                value={newEventForm.type}
                onChange={(e) => setNewEventForm({...newEventForm, type: e.target.value as any})}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
              >
                {eventTypeOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Priority
              </label>
              <select
                value={newEventForm.priority}
                onChange={(e) => setNewEventForm({...newEventForm, priority: e.target.value as any})}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white"
              >
                {priorityOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Project ID (Optional)"
              placeholder="Enter project ID"
              value={newEventForm.projectId || ''}
              onChange={(e) => setNewEventForm({...newEventForm, projectId: e.target.value})}
              type="number"
            />
            <Input
              label="Assigned User ID (Optional)"
              placeholder="Enter user ID"
              value={newEventForm.assignedUserId || ''}
              onChange={(e) => setNewEventForm({...newEventForm, assignedUserId: e.target.value})}
              type="number"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setIsNewEventModalOpen(false);
                resetNewEventForm();
                setSelectedEvent(null);
              }}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={handleNewEventSubmit}
              className="flex items-center"
            >
              <Save className="h-4 w-4 mr-2" />
              {isEditMode ? 'Update Event' : 'Create Event'}
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isEventModalOpen}
        onClose={() => {
          setIsEventModalOpen(false);
          setSelectedEvent(null);
        }}
        title="Event Details"
        size="md"
      >
        {selectedEvent && (
          <div className="space-y-4">
            <div className="flex items-center space-x-3">
              <div className={`${selectedEvent.color} h-12 w-12 rounded-lg flex items-center justify-center`}>
                {getEventIcon(selectedEvent.type)}
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                  {selectedEvent.title}
                </h3>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  {selectedEvent.type.charAt(0).toUpperCase() + selectedEvent.type.slice(1)} • {format(selectedEvent.date, 'MMM d, yyyy')}
                </p>
              </div>
            </div>

            {selectedEvent.description && (
              <div>
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Description</h4>
                <p className="text-gray-600 dark:text-gray-400">{selectedEvent.description}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Date & Time</h4>
                <p className="text-gray-900 dark:text-white">{format(selectedEvent.date, 'EEEE, MMMM d, yyyy')}</p>
                <p className="text-sm text-gray-600 dark:text-gray-400">{format(selectedEvent.date, 'h:mm a')}</p>
              </div>
              
              {selectedEvent.priority && (
                <div>
                  <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Priority</h4>
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                    selectedEvent.priority === 'URGENT' || selectedEvent.priority === 'HIGH'
                      ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                      : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
                  }`}>
                    {getPriorityLabel(selectedEvent.priority)}
                  </span>
                </div>
              )}
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <Button
                variant="danger"
                onClick={handleDeleteConfirm}
                className="flex items-center"
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Delete
              </Button>
              <Button
                variant="secondary"
                onClick={() => {
                  setIsEventModalOpen(false);
                  handleEditEventClick(selectedEvent);
                }}
                className="flex items-center"
              >
                <Edit className="h-4 w-4 mr-2" />
                Edit
              </Button>
            </div>
          </div>
        )}
      </Modal>

      <Modal
        isOpen={isConfirmDeleteModalOpen}
        onClose={() => setIsConfirmDeleteModalOpen(false)}
        title="Confirm Delete"
        size="md"
      >
        <div className="space-y-4">
          <div className="flex items-center justify-center mb-4">
            <div className="h-12 w-12 rounded-full bg-red-100 dark:bg-red-900 flex items-center justify-center">
              <Trash2 className="h-6 w-6 text-red-600 dark:text-red-400" />
            </div>
          </div>
          
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white text-center">
            Delete Event
          </h3>
          
          <p className="text-gray-600 dark:text-gray-400 text-center">
            Are you sure you want to delete <span className="font-semibold text-gray-900 dark:text-white">"{selectedEvent?.title}"</span>? This action cannot be undone.
          </p>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => setIsConfirmDeleteModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              onClick={handleDeleteEvent}
              className="flex items-center"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete Event
            </Button>
          </div>
        </div>
      </Modal>

      <Card>
        <div className="p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Calendar Legend
          </h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="flex items-center space-x-2">
              <div className="h-4 w-4 rounded bg-blue-500"></div>
              <span className="text-sm text-gray-600 dark:text-gray-400">Normal Task</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="h-4 w-4 rounded bg-yellow-500"></div>
              <span className="text-sm text-gray-600 dark:text-gray-400">Medium Priority</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="h-4 w-4 rounded bg-orange-500"></div>
              <span className="text-sm text-gray-600 dark:text-gray-400">High Priority</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="h-4 w-4 rounded bg-red-500"></div>
              <span className="text-sm text-gray-600 dark:text-gray-400">Urgent Task</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="h-4 w-4 rounded bg-purple-500"></div>
              <span className="text-sm text-gray-600 dark:text-gray-400">Project</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="h-4 w-4 rounded bg-teal-500"></div>
              <span className="text-sm text-gray-600 dark:text-gray-400">Custom Event</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="h-4 w-4 rounded bg-pink-500"></div>
              <span className="text-sm text-gray-600 dark:text-gray-400">Meeting</span>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default Calendar;