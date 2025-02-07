import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { SharedModule } from '../../shared/shared.module';
import { EventListComponent } from './components/event-list/event-list.component';

const routes: Routes = [
  {
    path: '',
    component: EventListComponent,
    data: { title: 'Events' }
  }
];

@NgModule({
  declarations: [
    EventListComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgbModule,
    SharedModule,
    RouterModule.forChild(routes)
  ],
  providers: []
})
export class EventModule { }