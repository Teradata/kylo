export interface GroupPrincipal {

/**
 * Metadata for a user group in Kylo.
 *
 * @typedef {Object} GroupPrincipal
 * @property {string|null} description a human-readable summary
 * @property {number} memberCount number of users and groups within the group
 * @property {string} systemName unique name
 * @property {string|null} title human-readable name
 */
    description: string|null,
    memberCount: number,
    systemName: string,
    title: string|null
}