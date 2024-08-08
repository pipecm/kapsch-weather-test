import { Component } from '@angular/core';
import { ForecastService } from './services/forecast.service';
import { saveAs } from "file-saver";
  
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent {
  postData = {
    latitude: '',
    longitude: ''
  };
  
  constructor(private service:ForecastService) {}
  
  createPost() {
    this.service.generateCsvFile(this.postData).subscribe(
      (response: any) => {
        const blob = new Blob([response], { type: 'application/octet-stream' });
        const fileName = 'forecast.csv';
        saveAs(blob, fileName);
      }
    );
  }
}