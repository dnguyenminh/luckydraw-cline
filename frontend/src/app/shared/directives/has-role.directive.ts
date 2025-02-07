import { Directive, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';

@Directive({
  selector: '[appHasRole]'
})
export class HasRoleDirective implements OnDestroy {
  private destroy$ = new Subject<void>();
  private roles: string[] = [];
  private hasView = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  @Input()
  set appHasRole(roles: string | string[]) {
    this.roles = Array.isArray(roles) ? roles : [roles];
    this.updateView();
    
    // Subscribe to auth changes
    const user = this.authService.getCurrentUser();
    if (user) {
      this.updateView();
    }
  }

  private updateView(): void {
    const user = this.authService.getCurrentUser();
    const hasRole = user ? this.roles.some(role => user.roles.includes(role)) : false;

    if (hasRole && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!hasRole && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}