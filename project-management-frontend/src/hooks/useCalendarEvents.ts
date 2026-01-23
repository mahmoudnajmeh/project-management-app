import { useState, useEffect, useCallback } from 'react';
import { calendarApi } from '../api/calendar';
import { useToast } from './useToast';

export const useCalendarEvents = () => {
  const [events, setEvents] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { error } = useToast();

  const fetchEvents = useCallback(async () => {
    try {
      setIsLoading(true);
      const response = await calendarApi.getMyEvents();
      const eventsWithDates = response.data.map((event: any) => ({
        ...event,
        date: new Date(event.eventDate),
      }));
      setEvents(eventsWithDates);
    } catch (err: any) {
      error('Failed to load calendar events');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }, [error]);

  const createEvent = async (eventData: any) => {
    try {
      const response = await calendarApi.create(eventData);
      const newEvent = {
        ...response.data,
        date: new Date(response.data.eventDate),
      };
      setEvents(prev => [...prev, newEvent]);
      return response.data;
    } catch (err: any) {
      throw err;
    }
  };

  const updateEvent = async (id: number, eventData: any) => {
    try {
      const response = await calendarApi.update(id, eventData);
      const updatedEvent = {
        ...response.data,
        date: new Date(response.data.eventDate),
      };
      setEvents(prev => prev.map(event => 
        event.id === id ? updatedEvent : event
      ));
      return response.data;
    } catch (err: any) {
      throw err;
    }
  };

  const deleteEvent = async (id: number) => {
    try {
      await calendarApi.delete(id);
      setEvents(prev => prev.filter(event => event.id !== id));
    } catch (err: any) {
      throw err;
    }
  };

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  return {
    events,
    isLoading,
    fetchEvents,
    createEvent,
    updateEvent,
    deleteEvent,
  };
};