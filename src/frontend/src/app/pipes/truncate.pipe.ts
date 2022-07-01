import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate'
})
export class TruncatePipe implements PipeTransform {

  transform(texte: string, size: number): unknown {
    return texte.length > size ? texte.substring(0, size) + '...' : texte;
  }

}
