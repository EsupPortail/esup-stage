import {AbstractControl} from "@angular/forms";


function cronValidator(control: AbstractControl) {
  const cronRegex = /^(\*|([0-5]?\d))(\s+(\*|([01]?\d|2[0-3]))){1}(\s+(\*|([1-9]|[12]\d|3[01]))){1}(\s+(\*|(1[0-2]|0?[1-9]))){1}(\s+(\*|([0-6]))){1}$/;
  return cronRegex.test(control.value) ? null : { invalidCron: true };
}
