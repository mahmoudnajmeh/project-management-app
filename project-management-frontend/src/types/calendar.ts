export interface CalendarEvent {
  id: number;
  title: string;
  description?: string;
  type: 'task' | 'project' | 'meeting' | 'custom';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?: string;
  eventDate: string;
  endDate?: string;
  allDay: boolean;
  color?: string;
  projectId?: number;
  assignedUserId?: number;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CalendarEventCreateRequest {
  title: string;
  description?: string;
  type: 'task' | 'project' | 'meeting' | 'custom';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?: string;
  eventDate: string;
  endDate?: string;
  allDay?: boolean;
  color?: string;
  projectId?: number;
  assignedUserId?: number;
}

export interface CalendarEventUpdateRequest {
  title?: string;
  description?: string;
  type?: 'task' | 'project' | 'meeting' | 'custom';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?: string;
  eventDate?: string;
  endDate?: string;
  allDay?: boolean;
  color?: string;
  projectId?: number;
  assignedUserId?: number;
}