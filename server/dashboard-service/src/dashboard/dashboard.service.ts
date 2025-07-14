import { Injectable } from '@nestjs/common';

@Injectable()
export class DashboardService {
  // Simulação de comunicação com o user-service
  private users = [
    { id: '1', email: 'test1@example.com', plan: 'free', online: true, lastLogin: new Date() },
    { id: '2', email: 'test2@example.com', plan: 'premium', online: false, lastLogin: new Date(Date.now() - 86400000) },
  ];

  getActiveNow() {
    return { count: this.users.filter(u => u.online).length };
  }

  getActiveToday() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return { count: this.users.filter(u => u.lastLogin >= today).length };
  }

  getUsers() {
    return this.users;
  }

  grantFreePlan(id: string) {
    const user = this.users.find(u => u.id === id);
    if (user) {
      user.plan = 'premium'; // Simula a concessão de um plano
      return { message: `Free plan granted to user ${id}` };
    }
    return { message: `User ${id} not found` };
  }
}
