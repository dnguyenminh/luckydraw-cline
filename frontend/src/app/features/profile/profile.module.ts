import { NgModule, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

// Temporary placeholder component until we implement the real one
@Component({
  template: `
    <div class="container py-4">
      <div class="card">
        <div class="card-body">
          <h2 class="card-title">User Profile</h2>
          <p class="card-text">Profile Component - Coming Soon</p>
        </div>
      </div>
    </div>
  `
})
export class ProfileComponent {}

const routes: Routes = [
  {
    path: '',
    component: ProfileComponent
  }
];

@NgModule({
  declarations: [
    ProfileComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class ProfileModule { }