import { Booking } from "../models/project";

export const MOCK_BOOKINGS: Booking[] = [
  {
    id: 1,
    description: 'Implemented login feature',
    timeSpent: { hours: 3, minutes: 15, seconds: 0 },
    createdAt: new Date('2023-10-01T10:00:00'),
    projectId: 1,
    bookerId: 101,
  },
  {
    id: 2,
    description: 'Fixed bug in payment module',
    timeSpent: { hours: 1, minutes: 45, seconds: 0 },
    createdAt: new Date('2023-10-02T14:30:00'),
    projectId: 1,
    bookerId: 102,
  },
  {
    id: 3,
    description: 'Designed database schema',
    timeSpent: { hours: 4, minutes: 0, seconds: 0 },
    createdAt: new Date('2023-10-03T09:00:00'),
    projectId: 2,
    bookerId: 103,
  },
];
