export declare namespace Common {
    interface Collection<T> {
    }
    interface Map<T> extends Collection<T> {
        [K: string]: T;
    }
    interface LabelValue {
        label: string;
        value: string;
        description?: string;
    }
}
