export class ArrayUtils {
    remove(array: any, element: any) {
        for (var i = 0; i < array.length; i++) {
            if (array[i]._id === element._id) {
                array.splice(i, 1);
                break;
            }
        }
    }

    add(array: any, element: any) {
        this.remove(array, element);
        array.push(element);
    }

}