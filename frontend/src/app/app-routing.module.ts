import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'events',
    loadChildren: () => import('./features/event/event.module').then(m => m.EventModule)
  },
  {
    path: 'admin',
    canActivate: [AuthGuard, AdminGuard],
    children: [
      {
        path: 'events',
        loadChildren: () => import('./features/admin/event-admin/event-admin.module').then(m => m.EventAdminModule)
      },
      {
        path: 'participants',
        loadChildren: () => import('./features/admin/participant-admin/participant-admin.module').then(m => m.ParticipantAdminModule)
      }
    ]
  },
  {
    path: 'profile',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/profile/profile.module').then(m => m.ProfileModule)
  },
  {
    path: '',
    redirectTo: 'events',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'events'
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      scrollPositionRestoration: 'enabled'
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }