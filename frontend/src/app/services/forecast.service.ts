import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../environments/environment';
  
@Injectable({
  providedIn: 'root'
})
export class ForecastService {
  private url = `${environment.apiBaseUrl}/forecast-app/v1/forecast`;
   
  constructor(private httpClient: HttpClient) { }
  
  generateCsvFile(data: any): Observable<ArrayBuffer> {
    const options: {
        headers?: HttpHeaders;
        observe?: 'body';
        params?: HttpParams;
        reportProgress?: boolean;
        responseType: 'arraybuffer';
        withCredentials?: boolean;
    } = {
        responseType: 'arraybuffer'
    };

    return this.httpClient
      .post(this.url, data, options)
      .pipe(
        map((file: ArrayBuffer) => {
            return file;
        })
      );
  }
}