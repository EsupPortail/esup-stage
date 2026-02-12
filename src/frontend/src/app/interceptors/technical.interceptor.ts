import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, ObservableInput } from 'rxjs';
import { TokenService } from "../services/token.service";
import { environment } from "../../environments/environment";
import { catchError, finalize } from "rxjs/operators";
import { MessageService } from "../services/message.service";
import { LoaderService } from "../services/loader.service";

@Injectable()
export class TechnicalInterceptor implements HttpInterceptor {

  private nbRequests: number = 0;
  private currentActiveElement :any;

  constructor(private tokenService: TokenService, private messageService: MessageService, private loaderService: LoaderService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const inputs = ['input', 'select', 'button', 'textarea'];
    if (document.activeElement instanceof HTMLElement && inputs.indexOf(document.activeElement.tagName.toLowerCase()) > -1) {
      this.currentActiveElement = document.activeElement;
      this.currentActiveElement.blur();
    }
    setTimeout(() => {
      this.loaderService.show();
    });
    this.nbRequests++;
    return next.handle(request)
      .pipe(
        catchError(error => this.handleError(error))
      )
      .pipe(
        finalize(() => {
          this.nbRequests--;
          if (this.nbRequests === 0) {
            if (this.currentActiveElement) {
              this.currentActiveElement.focus();
              this.currentActiveElement = undefined;
            }
            setTimeout(() => {
              this.loaderService.hide();
            });
          }
        })
      )
    ;
  }

  handleError(error: any): ObservableInput<any> {
    if (error?.status === 401) {
      const authReason = error?.headers?.get?.("X-Auth-Reason");
      if (authReason === "idle") {
        this.messageService.setWarning("Vous avez été déconnecté suite à une période d'inactivité prolongée");
      } else {
        this.messageService.setWarning("Session non authentifiée. Merci de vous reconnecter.");
      }
      throw error;
    }
    if (error.error instanceof Blob) {
      error.error.text().then((data: any) => {
        const message = JSON.parse(data).message;
        this.messageService.setError(message);
      })
    }
    else if (error.error && error.error.message) {
      this.messageService.setError(error.error.message);
    } else {
      switch (error.status) {
        case 400:
          this.messageService.setError("Données invalides");
          break;
        case 401:
          break;
        case 403:
          this.messageService.setError("Accès interdit");
          break;
        case 404:
          this.messageService.setError("Ressource introuvable");
          break;
        case 500:
          this.messageService.setError("Une erreur technique est survenue");
          break;
        default:
          this.messageService.setError("Une erreur non prévue est survenue");
          break;
      }
    }
    throw error;
  }
}
