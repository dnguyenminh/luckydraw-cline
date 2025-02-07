export interface Event {
  id: number;
  name: string;
  description: string;
  date: string;
  status: EventStatus;
  totalParticipants: number;
  totalRewards: number;
  remainingRewards: number;
  createdAt: string;
  updatedAt: string;
}

export enum EventStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface CreateEventRequest {
  name: string;
  description: string;
  date: string;
}

export interface UpdateEventRequest {
  name?: string;
  description?: string;
  date?: string;
  status?: EventStatus;
}

export interface EventSummary {
  totalParticipants: number;
  totalRewards: number;
  remainingRewards: number;
  drawnParticipants: number;
  remainingParticipants: number;
  completionPercentage: number;
}

export interface EventParticipant {
  id: number;
  participantId: number;
  eventId: number;
  name: string;
  employeeId: string;
  email: string;
  department: string;
  isDrawn: boolean;
  reward?: {
    id: number;
    name: string;
    value: number;
  };
  drawnAt?: string;
}

export interface DrawResult {
  participant: EventParticipant;
  reward: {
    id: number;
    name: string;
    value: number;
  };
  timestamp: string;
}