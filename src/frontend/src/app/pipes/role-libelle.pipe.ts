import { Pipe, PipeTransform } from '@angular/core';
import { Role } from "../constants/role";

@Pipe({
  name: 'roleLibelle'
})
export class RoleLibellePipe implements PipeTransform {

  transform(value: string, ...args: unknown[]): unknown {
    return Role[value] ? Role[value].libelle : '';
  }

}
