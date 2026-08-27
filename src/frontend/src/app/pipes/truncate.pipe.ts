import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'truncate',
    standalone: false
})
export class TruncatePipe implements PipeTransform {

  transform(texte: string, size: number): unknown {
    return texte.length > size ? texte.substring(0, size) + '...' : texte;
  }

}
