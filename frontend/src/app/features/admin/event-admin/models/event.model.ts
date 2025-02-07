export interface Event {
  id: number;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  defaultSpins: number;
  maxSpinsPerDay: number;
  active: boolean;
  rewards?: Reward[];
  createdAt: string;
  updatedAt: string;
}

export interface Reward {
  id: number;
  name: string;
  description?: string;
  quantity: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EventListResponse {
  content: Event[];
  total: number;
  page: number;
  size: number;
}