import { Pipe, PipeTransform } from '@angular/core';
import {data} from "../../en-json";

@Pipe({name: 'translate'})
export class TranslatePipe implements PipeTransform {
  transform(input: string) {
    return data[input];
  }
}