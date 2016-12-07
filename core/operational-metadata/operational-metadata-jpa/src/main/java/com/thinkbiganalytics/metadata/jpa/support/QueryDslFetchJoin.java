package com.thinkbiganalytics.metadata.jpa.support;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.EntityPath;

/**
 * Created by sr186054 on 12/6/16.
 */
public class QueryDslFetchJoin {
        public final EntityPath joinPath;
        public final EntityPath alias;
        public final Join type;
        public static enum Join{
            INNER,LEFT,RIGHT,JOIN,INNER_ALIAS,LEFT_ALIAS,RIGHT_ALIAS,JOIN_ALIAS
        }

        private QueryDslFetchJoin(EntityPath joinPath, Join type) {
            this.joinPath = joinPath;
            this.alias = null;
            this.type = type;
        }

        private QueryDslFetchJoin(EntityPath joinPath, EntityPath alias,Join type) {
            this.joinPath = joinPath;
            this.alias = alias;
            this.type = type;
        }

        public static QueryDslFetchJoin innerJoin(EntityPath path) {
            return new QueryDslFetchJoin(path, Join.INNER);
        }

        public static QueryDslFetchJoin join(EntityPath path) {
            return new QueryDslFetchJoin(path, Join.JOIN);
        }

        public static QueryDslFetchJoin leftJoin(EntityPath path) {
            return new QueryDslFetchJoin(path, Join.LEFT);
        }

        public static QueryDslFetchJoin rightJoin(EntityPath path) {
            return new QueryDslFetchJoin(path, Join.RIGHT);
        }




    public static QueryDslFetchJoin innerJoin(EntityPath path, EntityPath alias) {
        return new QueryDslFetchJoin(path, alias, Join.INNER_ALIAS);
    }

    public static QueryDslFetchJoin join(EntityPath path, EntityPath alias) {
        return new QueryDslFetchJoin(path,alias, Join.JOIN_ALIAS);
    }

    public static QueryDslFetchJoin leftJoin(EntityPath path, EntityPath alias) {
        return new QueryDslFetchJoin(path,alias, Join.LEFT_ALIAS);
    }

    public static QueryDslFetchJoin rightJoin(EntityPath path, EntityPath alias) {
        return new QueryDslFetchJoin(path,alias, Join.RIGHT_ALIAS);
    }


}
