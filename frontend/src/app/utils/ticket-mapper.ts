import { TicketStatus, Priority, Ticket } from '../models/ticket';

export function mapStatus(status: string): TicketStatus {
  if (Object.values(TicketStatus).includes(status as TicketStatus)) {
    return status as TicketStatus;
  }
  throw new Error(`Invalid status: ${status}`);
}

export function mapPriority(priority: string): Priority {
  if (Object.values(Priority).includes(priority as Priority)) {
    return priority as Priority;
  }
  throw new Error(`Invalid priority: ${priority}`);
}

export function mapBackendTicket(data: any): Ticket {
  return {
    ...data,
    status: Object.values(TicketStatus).includes(data.status) ? (data.status as TicketStatus) : undefined,
    priority: Object.values(Priority).includes(data.priority) ? (data.priority as Priority) : undefined,
    createdAt: data.createdAt ? new Date(data.createdAt) : undefined,
    updatedAt: data.updatedAt ? new Date(data.updatedAt) : undefined,
  };
}
