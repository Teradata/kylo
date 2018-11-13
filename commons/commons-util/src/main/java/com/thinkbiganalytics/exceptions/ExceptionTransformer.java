package com.thinkbiganalytics.exceptions;

/*-
 * #%L
 * kylo-commons-util
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ExceptionTransformer<T extends RuntimeException> {

    private static final XLogger logger = XLoggerFactory.getXLogger(ExceptionTransformer.class);

    private final Class<? extends T> clazzT;
    private final Class<? extends Throwable> clazzCause;
    private final Class<? extends Throwable> clazzSubCause;

    public ExceptionTransformer(Class<? extends T> clazzT,
                                Class<? extends Throwable> clazzCause,
                                Class<? extends Throwable> clazzSubCause) {
        this.clazzT = clazzT;
        this.clazzCause = clazzCause;
        this.clazzSubCause = clazzSubCause;

    }

    public ExceptionTransformer(Class<? extends T> clazzT,
                                Class<? extends Throwable> clazzCause) {
        this(clazzT, clazzCause, null);
    }


    /**
     * check for clazzCause and if needed classSubClause occurring next, if occurring then return wrapped with new throwable T, else return the original throwable
     */
    public Throwable transformThrowable(Throwable throwable) {
        logger.entry(throwable);

        if (throwable.getCause() == null) {
            if (throwable.getClass() == this.clazzCause) {
                return wrapThrowable(throwable);
            } else {
                return throwable;
            }
        }

        int indexOfCause = ExceptionUtils.indexOfThrowable(throwable, this.clazzCause);
        if (indexOfCause != -1) {
            if (this.clazzSubCause != null) {
                if (ExceptionUtils.indexOfThrowable(throwable, this.clazzSubCause, indexOfCause) != -1) {
                    // subcause found
                    return wrapThrowable(throwable);
                } else {
                    // subcause not found
                    return throwable;
                }
            } else {
                // don't need to search for a subcause, but the cause does exist
                return wrapThrowable(throwable);
            } // end if
        } else {
            // cause not found
            return throwable;
        } // end if
    }

    /**
     * check for clazzCause and if needed classSubClause occurring next, if occurring then return wrapped with new throwable T, else return the original throwable
     */
    public Exception transformException(Exception exception) {
        logger.entry(exception);

        if (exception.getCause() == null) {
            if (exception.getClass() == this.clazzCause) {
                return wrapThrowable(exception);
            } else {
                return exception;
            }
        }

        int indexOfCause = ExceptionUtils.indexOfThrowable(exception, this.clazzCause);
        if (indexOfCause != -1) {
            if (this.clazzSubCause != null) {
                if (ExceptionUtils.indexOfThrowable(exception, this.clazzSubCause, indexOfCause) != -1) {
                    // subcause found
                    return wrapThrowable(exception);
                } else {
                    // subcause not found
                    return exception;
                }
            } else {
                // don't need to search for a subcause, but the cause does exist
                return wrapThrowable(exception);
            } // end if
        } else {
            // cause not found
            return exception;
        } // end if
    }


    public boolean causeInChain(Throwable throwable, Class<? extends Throwable> clazz) {
        return ExceptionUtils.indexOfThrowable(throwable, clazz) != -1;
    }


    public boolean causesInChain(Throwable throwable) {
        int indexOfCause = ExceptionUtils.indexOfThrowable(throwable, this.clazzCause);
        if (ExceptionUtils.indexOfThrowable(throwable, this.clazzSubCause, indexOfCause) != -1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean causesInChain(Throwable throwable, Class<? extends Throwable> clazz, Class<? extends Throwable> subClazz) {
        int indexOfCause = ExceptionUtils.indexOfThrowable(throwable, clazz);
        if (ExceptionUtils.indexOfThrowable(throwable, subClazz, indexOfCause) != -1) {
            return true;
        } else {
            return false;
        }
    }

    private T wrapThrowable(Throwable e) {
        try {
            final Constructor<? extends T> tConstructor = clazzT.getConstructor(Throwable.class);
            return tConstructor.newInstance(e);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException n) {
            throw new IllegalStateException(n);
        }
    }

    public Class<? extends T> getClassT() {
        return clazzT;
    }

    public Class<? extends Throwable> getClassCause() {
        return clazzCause;
    }

    public Class<? extends Throwable> getClassSubCause() {
        return clazzSubCause;
    }


}
