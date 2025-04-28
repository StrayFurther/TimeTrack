export interface Time {
  hours: number;
  minutes: number;
  seconds: number;
}

export interface Booking {
  id: number;
  description: string;
  timeSpent: Time;
  createdAt: Date;
  bookerId?: number; // for future use, for example, to track who booked the ticket.
  // also usable for a group feature
}

export enum TicketStatus {
  New = 'New',
  InProgress = 'In Progress',
  Completed = 'Completed',
  PendingReview = 'Pending Review',
  OnHold = 'On Hold',
  Cancelled = 'Cancelled',
  Reopened = 'Reopened',
  Blocked = 'Blocked',
}

export enum Priority {
  High = 'High',
  Medium = 'Medium',
  Low = 'Low',
}

export interface Ticket {
  id: number;
  name: string;
  description: string;
  Bookings: Booking[];
  status?: TicketStatus;
  priority?: Priority;
  createdAt?: Date;
  updatedAt?: Date;
}
