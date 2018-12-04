export interface UserPrincipal {
    /**
     * Metadata for a user with access to Kylo.
     *
     * @typedef {Object} UserPrincipal
     * @property {string|null} displayName display name for this user
     * @property {string|null} email email address for this user
     * @property {boolean} enabled indicates if user is active or disabled
     * @property {Array.<string>} groups system names of groups the user belongs to
     * @property {string} systemName username for this user
     */

    displayName: string|null,
    email: string|null,
    enabled: boolean,
    groups: string[],
    systemName: string
}