package com.thinkbiganalytics.alerts.api.core;
/*-
 * #%L
 * thinkbig-alerts-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;

import org.joda.time.DateTime;

import java.net.URI;

/**
 * Created by sr186054 on 9/25/17.
 */
public class AlertCriteriaInput {

    private Integer limit = Integer.MAX_VALUE;
    private URI type;
    private String subtype;
    private Alert.State state;
    private Alert.Level level;
    private DateTime before;
    private DateTime after;

    private DateTime modifiedBefore;
    private DateTime modifiedAfter;

    private boolean cleared;
    private boolean asServiceAccount;
    private boolean onlyIfChangesDetected;

    public Integer getLimit() {
        return limit;
    }

    public URI getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public Alert.State getState() {
        return state;
    }

    public Alert.Level getLevel() {
        return level;
    }

    public DateTime getBefore() {
        return before;
    }

    public DateTime getAfter() {
        return after;
    }

    public boolean isCleared() {
        return cleared;
    }

    public boolean isAsServiceAccount() {
        return asServiceAccount;
    }

    public boolean isOnlyIfChangesDetected() {
        return onlyIfChangesDetected;
    }

    public DateTime getModifiedBefore() {
        return modifiedBefore;
    }

    public DateTime getModifiedAfter() {
        return modifiedAfter;
    }

    public AlertCriteriaInput() {
    }


    public AlertCriteriaInput(Integer limit, URI type, String subtype, Alert.State state, Alert.Level level, DateTime before, DateTime after, DateTime modifiedBefore, DateTime modifiedAfter,
                              boolean cleared, boolean asServiceAccount, boolean onlyIfChangesDetected) {
        this.limit = limit;
        this.type = type;
        this.subtype = subtype;
        this.state = state;
        this.level = level;
        this.before = before;
        this.after = after;
        this.modifiedBefore = modifiedBefore;
        this.modifiedAfter = modifiedAfter;
        this.cleared = cleared;
        this.asServiceAccount = asServiceAccount;
        this.onlyIfChangesDetected = onlyIfChangesDetected;
    }


    public static class Builder {

        private Integer limit = Integer.MAX_VALUE;
        private URI type;
        private String subtype;
        private Alert.State state;
        private Alert.Level level;
        private DateTime before;
        private DateTime after;
        private DateTime modifiedBefore;
        private DateTime modifiedAfter;
        private boolean cleared;
        private boolean asServiceAccount;
        private boolean onlyIfChangesDetected;

        public Builder limit(Integer limit) {
            if (limit != null) {
                this.limit = limit;
            }
            return this;
        }

        public Builder type(String type) {
            if (type != null) {
                try {
                    this.type = URI.create(type);
                } catch (IllegalArgumentException e) {

                }
            }
            return this;
        }

        public Builder type(URI uri) {
            this.type = uri;
            return this;
        }

        public Builder state(String stateStr) {
            if (stateStr != null) {
                try {
                    this.state = Alert.State.valueOf(stateStr.toUpperCase());
                } catch (IllegalArgumentException e) {

                }
            }
            return this;
        }

        public Builder state(Alert.State state) {
            this.state = state;
            return this;
        }

        public Builder level(String levelStr) {
            if (levelStr != null) {
                try {
                    this.level = Alert.Level.valueOf(levelStr.toUpperCase());
                } catch (IllegalArgumentException e) {

                }
            }
            return this;
        }

        public Builder level(Alert.Level level) {
            this.level = level;
            return this;
        }

        public Builder before(String before) {
            if (before != null) {
                try {
                    this.before = Formatters.parseDateTime(before);
                } catch (IllegalArgumentException e) {

                }
            }
            return this;
        }

        public Builder after(String after) {
            if (after != null) {
                try {
                    this.after = Formatters.parseDateTime(after);
                } catch (IllegalArgumentException e) {

                }
            }
            return this;
        }

        public Builder modifiedBefore(String before) {
            if (before != null) {
                try {
                    this.modifiedBefore = Formatters.parseDateTime(before);
                } catch (IllegalArgumentException e) {

                }
            }
            return this;
        }

        public Builder modifiedAfter(String after) {
            if (after != null) {
                try {
                    this.modifiedAfter = Formatters.parseDateTime(after);
                } catch (IllegalArgumentException e) {

                }
            }
            return this;
        }

        public Builder cleared(boolean cleared) {
            this.cleared = cleared;
            return this;
        }

        public Builder cleared(String cleared) {
            if (cleared != null) {
                this.cleared = Boolean.parseBoolean(cleared);
            }
            return this;
        }

        public Builder subtype(String subtype) {
            this.subtype = subtype;
            return this;
        }

        public Builder asServiceAccount(boolean asServiceAccount) {
            this.asServiceAccount = asServiceAccount;
            return this;
        }

        public Builder onlyIfChangesDetected(boolean onlyIfChangesDetected) {
            this.onlyIfChangesDetected = onlyIfChangesDetected;
            return this;
        }


        public void applyToCriteria(AlertCriteria criteria) {
            AlertCriteriaInput input = build();
            criteria.state(input.getState());
            criteria.type(input.getType());
            criteria.subtype(input.getSubtype());
            criteria.level(input.getLevel());
            criteria.limit(input.getLimit());
            criteria.before(input.getBefore());
            criteria.after(input.getAfter());
            criteria.modifiedBefore(input.getModifiedBefore());
            criteria.modifiedAfter(input.getModifiedAfter());
            criteria.includedCleared(input.isCleared());
            criteria.asServiceAccount(input.isAsServiceAccount());
            criteria.onlyIfChangesDetected(input.isOnlyIfChangesDetected());
        }


        public AlertCriteriaInput build() {
            AlertCriteriaInput
                input =
                new AlertCriteriaInput(this.limit, this.type, this.subtype, this.state, this.level, this.before, this.after, this.modifiedBefore, this.modifiedAfter, this.cleared,
                                       this.asServiceAccount, this.onlyIfChangesDetected);
            return input;
        }

    }


}
