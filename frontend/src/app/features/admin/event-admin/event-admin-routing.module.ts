import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { EventListComponent } from './pages/event-list/event-list.component';
import { EventFormComponent } from './pages/event-form/event-form.component';
import { EventDetailComponent } from './pages/event-detail/event-detail.component';
import { AdminGuard } from '@app/core/guards/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: EventListComponent,
    canActivate: [AdminGuard]
  },
  {
    path: 'create',
    component: EventFormComponent,
    canActivate: [AdminGuard]
  },
  {
    path: ':id',
    component: EventDetailComponent,
    canActivate: [AdminGuard]
  },
  {
    path: ':id/edit',
    component: EventFormComponent,
    canActivate: [AdminGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EventAdminRoutingModule { }