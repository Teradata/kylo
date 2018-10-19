import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name:'charactersPipe'})
export class CharactersPipe implements PipeTransform {
    transform(input: any, chars: any): string {
        if (input == null || input == undefined) {
            input = '';
        }
        if (isNaN(chars)) {
            return input;
        }
        if (chars) {
            if (input.length > chars) {
                input = input.substring(0, chars) + "...";
            }
        }
        return input;
    }
}