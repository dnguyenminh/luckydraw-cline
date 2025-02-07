import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { LoadingService } from './core/services/loading.service';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  isLoading$!: Observable<boolean>;
  isMenuCollapsed = true;

  constructor(
    private loadingService: LoadingService,
    private router: Router,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.isLoading$ = this.loadingService.isLoading$;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}