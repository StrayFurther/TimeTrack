import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, of, timer } from 'rxjs';
import { switchMap, map, catchError } from 'rxjs/operators';
import { UserService } from '../services/user/user.service';

export function emailTakenValidator(userService: UserService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) {
      return of(null);
    }
    return timer(300).pipe(
      switchMap(() =>
        userService.doesUserExist(control.value).pipe(
          map(exists => (exists ? { emailTaken: true } : null)),
          catchError(() => of(null))
        )
      )
    );
  };
}
