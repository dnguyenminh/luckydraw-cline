import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-file-upload',
  template: `
    <div class="file-upload">
      <div class="upload-area"
           [class.dragging]="isDragging"
           (dragover)="onDragOver($event)"
           (dragleave)="onDragLeave($event)"
           (drop)="onDrop($event)">
        
        <div class="upload-content">
          <i class="bi bi-cloud-upload"></i>
          <p class="upload-message">
            {{ message }}
            <span class="text-muted">or drag and drop</span>
          </p>
          <input type="file"
                 #fileInput
                 [multiple]="multiple"
                 [accept]="accept"
                 (change)="onFileSelected($event)"
                 class="file-input">
          <button type="button" 
                  class="btn btn-primary" 
                  (click)="fileInput.click()">
            Choose File{{ multiple ? 's' : '' }}
          </button>
        </div>

        <div class="selected-files" *ngIf="files.length > 0">
          <div class="file-item" *ngFor="let file of files; let i = index">
            <i class="bi bi-file-earmark"></i>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">({{ file.size | fileSize }})</span>
            <button type="button" 
                    class="btn-close" 
                    (click)="removeFile(i)">
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .file-upload {
      width: 100%;
    }

    .upload-area {
      border: 2px dashed #ccc;
      border-radius: 4px;
      padding: 2rem;
      text-align: center;
      transition: all 0.3s ease;
      background: #f8f9fa;
    }

    .upload-area.dragging {
      border-color: var(--primary-color);
      background: #e9ecef;
    }

    .upload-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
    }

    .upload-message {
      margin: 0;
      color: #6c757d;
    }

    .file-input {
      display: none;
    }

    i.bi-cloud-upload {
      font-size: 2.5rem;
      color: var(--primary-color);
    }

    .selected-files {
      margin-top: 1rem;
      text-align: left;
    }

    .file-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem;
      background: white;
      border-radius: 4px;
      margin-bottom: 0.5rem;
    }

    .file-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-size {
      color: #6c757d;
      font-size: 0.875rem;
    }

    .btn-close {
      padding: 0.25rem;
    }
  `]
})
export class FileUploadComponent {
  @Input() message = 'Click to upload files';
  @Input() accept = '*/*';
  @Input() multiple = false;
  @Output() filesChanged = new EventEmitter<File[]>();

  files: File[] = [];
  isDragging = false;

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files) {
      this.handleFiles(files);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.handleFiles(input.files);
    }
  }

  removeFile(index: number): void {
    this.files.splice(index, 1);
    this.filesChanged.emit(this.files);
  }

  private handleFiles(fileList: FileList): void {
    if (this.multiple) {
      this.files.push(...Array.from(fileList));
    } else {
      this.files = [fileList[0]];
    }
    this.filesChanged.emit(this.files);
  }
}