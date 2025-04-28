import { Ticket, TicketStatus, Priority } from '../models/ticket';

export const TICKETS: Ticket[] = [
  {
    id: 1,
    name: 'Fix login issue',
    description: 'Resolve the bug preventing users from logging in.',
    Bookings: [
      {
        id: 101,
        description: 'Initial investigation',
        timeSpent: { hours: 2, minutes: 30, seconds: 0 },
        createdAt: new Date('2023-10-01T10:00:00'),
      },
    ],
    status: TicketStatus.InProgress,
    priority: Priority.High,
    createdAt: new Date('2023-09-30T08:00:00'),
    updatedAt: new Date('2023-10-01T12:00:00'),
  },
  {
    id: 2,
    name: 'Add dark mode',
    description: 'Implement dark mode for the application.',
    Bookings: [
      {
        id: 102,
        description: 'Design mockups',
        timeSpent: { hours: 1, minutes: 45, seconds: 0 },
        createdAt: new Date('2023-10-02T09:00:00'),
      },
    ],
    status: TicketStatus.PendingReview,
    priority: Priority.Medium,
    createdAt: new Date('2023-10-01T14:00:00'),
    updatedAt: new Date('2023-10-02T10:45:00'),
  },
  {
    id: 3,
    name: 'Update documentation',
    description: 'Revise and update the project documentation.',
    Bookings: [],
    status: TicketStatus.New,
    priority: Priority.Low,
    createdAt: new Date('2023-10-03T08:00:00'),
    updatedAt: new Date('2023-10-03T08:00:00'),
  },
  {
    id: 4,
    name: 'Fix payment gateway',
    description: 'Resolve issues with the payment gateway integration.',
    Bookings: [
      {
        id: 103,
        description: 'Debugging payment errors',
        timeSpent: { hours: 3, minutes: 15, seconds: 0 },
        createdAt: new Date('2023-10-04T11:00:00'),
      },
    ],
    status: TicketStatus.Blocked,
    priority: Priority.High,
    createdAt: new Date('2023-10-03T10:00:00'),
    updatedAt: new Date('2023-10-04T12:15:00'),
  },
];
