import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { TokenService } from "../services/token.service";
import { environment } from "../../environments/environment";
import { catchError } from "rxjs/operators";

@Injectable()
export class TechnicalInterceptor implements HttpInterceptor {

  constructor(private tokenService: TokenService) {}

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
    if (this.tokenService.getToken()) {
      return next.handle(this.addToken(request, this.tokenService.getToken()));
    }

    return next.handle(request).pipe(
      catchError(error => {
        return throwError(error);
      })
    );
  }
}
