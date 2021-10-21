import { Pipe, PipeTransform } from '@angular/core';
import { ContenuService } from "../services/contenu.service";

@Pipe({
  name: 'contenu',
  pure: false,
})
export class ContenuPipe implements PipeTransform {

  constructor(private contenuService: ContenuService) {}

  transform(value: any): unknown {
    const contenu = this.contenuService.contenus.find((c: any) => c.code === value);
    return contenu ? contenu.texte : value;
  }

}
