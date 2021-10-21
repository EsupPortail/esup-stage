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

  constructor(private tokenService: TokenService, private messageService: MessageService, private loaderService: LoaderService) {}

  addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
    if (req.url.indexOf(environment.apiUrl) > -1) {
      if (token != null) {
        var char = (req.url.indexOf("?") > -1) ? "&" : "?";
        let headers = req.headers;
        return req.clone({url: req.url + char + "token=" + token});
      }
    }

    return req;
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const activeElement = document.activeElement;
    if (activeElement instanceof HTMLElement) {
      activeElement.blur();
    }
    this.loaderService.show();
    this.nbRequests++;
    if (this.tokenService.getToken()) {
      return next.handle(this.addToken(request, this.tokenService.getToken()))
        .pipe(
          catchError(error => this.handleError(error))
        )
        .pipe(
          finalize(() => {
            this.nbRequests--;
            if (this.nbRequests === 0) {
              if (activeElement instanceof HTMLElement) {
                activeElement.focus();
              }
              this.loaderService.hide();
            }
          })
        )
      ;
    }

    return next.handle(request).pipe(
      catchError(error => this.handleError(error))
    );
  }

  handleError(error: any): ObservableInput<any> {
    if (error.error && error.error.message) {
      this.messageService.setError(error.error.message);
    } else {
      switch (error.status) {
        case 400:
          this.messageService.setError("Données invalides");
          break;
        case 401:
          this.messageService.setError("Accès non autorisé");
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
