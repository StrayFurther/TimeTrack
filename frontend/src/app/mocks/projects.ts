import { Project } from "../models/project";
import { MOCK_BOOKINGS } from "./bookings";

export const MOCK_PROJECTS: Project[] = [
  {
    id: 1,
    name: 'Time Tracker App',
    description: 'An application to track work hours and tasks.',
    Bookings: MOCK_BOOKINGS.filter((booking) => booking.projectId === 1),
    createdAt: new Date('2023-09-01T08:00:00'),
    updatedAt: new Date('2023-10-01T12:00:00'),
  },
  {
    id: 2,
    name: 'E-Commerce Platform',
    description: 'A platform for online shopping.',
    Bookings: MOCK_BOOKINGS.filter((booking) => booking.projectId === 2),
    createdAt: new Date('2023-08-15T08:00:00'),
    updatedAt: new Date('2023-10-03T10:00:00'),
  },
];
