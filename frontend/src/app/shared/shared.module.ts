import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

// Pipes
import { TruncatePipe } from './pipes/truncate.pipe';
import { RelativeTimePipe } from './pipes/relative-time.pipe';
import { FileSizePipe } from './pipes/file-size.pipe';

// Components
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { AlertComponent } from './components/alert/alert.component';
import { SpinnerComponent } from './components/spinner/spinner.component';
import { EmptyStateComponent } from './components/empty-state/empty-state.component';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { FileUploadComponent } from './components/file-upload/file-upload.component';
import { ConfirmModalComponent } from './components/confirm-modal/confirm-modal.component';

// Directives
import { ClickStopPropagationDirective } from './directives/click-stop-propagation.directive';
import { HasRoleDirective } from './directives/has-role.directive';

@NgModule({
  declarations: [
    // Pipes
    TruncatePipe,
    RelativeTimePipe,
    FileSizePipe,
    
    // Components
    LoadingSpinnerComponent,
    ConfirmDialogComponent,
    AlertComponent,
    SpinnerComponent,
    EmptyStateComponent,
    PageHeaderComponent,
    FileUploadComponent,
    ConfirmModalComponent,
    
    // Directives
    ClickStopPropagationDirective,
    HasRoleDirective
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    NgbModule
  ],
  exports: [
    // Angular Modules
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    NgbModule,
    
    // Pipes
    TruncatePipe,
    RelativeTimePipe,
    FileSizePipe,
    
    // Components
    LoadingSpinnerComponent,
    ConfirmDialogComponent,
    AlertComponent,
    SpinnerComponent,
    EmptyStateComponent,
    PageHeaderComponent,
    FileUploadComponent,
    ConfirmModalComponent,
    
    // Directives
    ClickStopPropagationDirective,
    HasRoleDirective
  ]
})
export class SharedModule { }