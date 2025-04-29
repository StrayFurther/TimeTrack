import {Component, inject} from '@angular/core';
import {Priority, Ticket, TicketStatus} from '../../../models/ticket';
import {TICKETS} from '../../../mocks/tickets';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle, MatCardSubtitle } from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {CommonModule, DatePipe} from '@angular/common';
import {MatIcon, MatIconModule} from '@angular/material/icon';
import {Router} from '@angular/router';

@Component({
  selector: 'ticket-overview',
  imports: [
    MatCard,
    MatCardSubtitle,
    MatCardTitle,
    MatCardHeader,
    MatCardContent,
    MatButton,
    MatCardActions,
    DatePipe,
    CommonModule,
    MatIcon
  ],
  templateUrl: './ticket-overview.component.html',
  styleUrl: './ticket-overview.component.scss'
})
export class TicketOverviewComponent {
  tickets: Ticket[] = TICKETS
  router = inject(Router)

  getPriorityClass(priority?: Priority): string {
    switch (priority) {
      case 'High':
        return 'priority-high';
      case 'Medium':
        return 'priority-medium';
      case 'Low':
        return 'priority-low';
      default:
        return '';
    }
  }

  getPriorityIcon(priority?: Priority): string {
    switch (priority) {
      case 'High':
        return 'priority_high'; // Urgent icon
      case 'Medium':
        return 'arrow_upward'; // Moderate importance
      case 'Low':
        return 'low_priority'; // Low importance
      default:
        return 'help'; // Default or unknown priority
    }
  }

  getStatusClass(status?: TicketStatus): string {
    switch (status) {
      case TicketStatus.New:
        return 'status-new';
      case TicketStatus.InProgress:
        return 'status-in-progress';
      case TicketStatus.Completed:
        return 'status-completed';
      case TicketStatus.PendingReview:
        return 'status-pending-review';
      case TicketStatus.OnHold:
        return 'status-on-hold';
      case TicketStatus.Cancelled:
        return 'status-cancelled';
      case TicketStatus.Reopened:
        return 'status-reopened';
      case TicketStatus.Blocked:
        return 'status-blocked';
      default:
        return '';
    }
  }

  onClickTicketName(id: number) {
    this.router.navigate(['ticket', id])
  }
}
