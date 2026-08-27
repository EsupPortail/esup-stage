import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'roleLibelle',
    standalone: false
})
export class RoleLibellePipe implements PipeTransform {

  transform(value: any, ...args: unknown[]): unknown {
    return value.libelle;
  }

}
