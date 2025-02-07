import { NgModule, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

// Temporary placeholder component until we implement the real one
@Component({
  template: '<div>Participant Admin Component - Coming Soon</div>'
})
export class ParticipantAdminComponent {}

const routes: Routes = [
  {
    path: '',
    component: ParticipantAdminComponent
  }
];

@NgModule({
  declarations: [
    ParticipantAdminComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class ParticipantAdminModule { }