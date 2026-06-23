import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'boolean',
    standalone: false
})
export class BooleanPipe implements PipeTransform {

  transform(value: unknown, ...args: unknown[]): unknown {
    return value ? 'Oui' : 'Non';
  }

}
