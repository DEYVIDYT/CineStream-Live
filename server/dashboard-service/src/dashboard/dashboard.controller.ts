import { Controller, Get, Param, Post } from '@nestjs/common';
import { DashboardService } from './dashboard.service';

@Controller('dashboard')
export class DashboardController {
  constructor(private readonly dashboardService: DashboardService) {}

  @Get('stats/active-now')
  getActiveNow() {
    return this.dashboardService.getActiveNow();
  }

  @Get('stats/active-today')
  getActiveToday() {
    return this.dashboardService.getActiveToday();
  }

  @Get('users')
  getUsers() {
    return this.dashboardService.getUsers();
  }

  @Post('users/:id/grant-free-plan')
  grantFreePlan(@Param('id') id: string) {
    return this.dashboardService.grantFreePlan(id);
  }
}
