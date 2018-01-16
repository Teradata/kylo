
export type StatusState
    = 'Initial'
    | 'Loading'
    | 'Disabled'
    | 'Failed'
    | 'Invalid'
    | 'Valid';

export class Status {
    public state?: StatusState;

    constructor(
        state?: StatusState,
    ) {
        this.state = state ? state : null;
    }
}
