import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'roleLibelle'
})
export class RoleLibellePipe implements PipeTransform {

  transform(value: any, ...args: unknown[]): unknown {
    return value.libelle;
  }

}
