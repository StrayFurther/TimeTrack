export interface Time {
  hours: number;
  minutes: number;
  seconds: number;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  Bookings: Booking[];
  createdAt?: Date;
  updatedAt?: Date;
}

export interface Booking {
  id: number;
  description: string;
  timeSpent: Time;
  createdAt: Date;
  projectId: number;
  bookerId?: number; // for future use, for example, to track who booked on the project.
  // also usable for a group feature
}
