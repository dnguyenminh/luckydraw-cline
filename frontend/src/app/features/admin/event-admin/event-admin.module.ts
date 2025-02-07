import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../../shared/shared.module';

import { EventListComponent } from './pages/event-list/event-list.component';
import { EventDetailComponent } from './pages/event-detail/event-detail.component';
import { EventFormComponent } from './pages/event-form/event-form.component';
import { AdminGuard } from '../../../core/guards/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: EventListComponent,
    canActivate: [AdminGuard]
  },
  {
    path: 'new',
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
  declarations: [
    EventListComponent,
    EventDetailComponent,
    EventFormComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ],
  providers: []
})
export class EventAdminModule { }