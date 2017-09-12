/**
 * Representation of a SELECT statement.
 */
export interface TeradataScript {

    /** GROUP BY clause */
    groupBy: string;

    /** HAVING or QUALIFY conditional-expression */
    having: string;

    /** Keywords between 'SELECT' and column list */
    keywordList: string;

    /** List of expressions */
    selectList: string;

    /** WHERE qualification */
    where: string;
}
