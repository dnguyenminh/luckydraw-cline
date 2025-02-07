import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate'
})
export class TruncatePipe implements PipeTransform {
  transform(value: string, limit: number = 50, completeWords: boolean = false, ellipsis: string = '...'): string {
    if (!value) return '';
    if (value.length <= limit) return value;

    if (completeWords) {
      const words = value.substring(0, limit).split(' ');
      words.pop();
      return `${words.join(' ')}${ellipsis}`;
    }

    return `${value.substring(0, limit)}${ellipsis}`;
  }
}