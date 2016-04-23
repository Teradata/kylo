package com.thinkbiganalytics.spark.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility methods for classes.
 */
public class ClassUtils {

    /**
     * Invokes the method with the specified name and matching argument types.
     *
     * @param <R>  return type of the method
     * @param obj  object containing the method to find
     * @param name name of the method
     * @param args arguments for the method call
     * @return result of the method call
     * @throws IllegalArgumentException      if the method throws an exception
     * @throws IllegalStateException         if the method is inaccessible
     * @throws UnsupportedOperationException if the method cannot be found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <R> R invoke(@Nonnull final Object obj, @Nonnull final String name, final Object... args) {
        // Find a method matching the specified arguments
        Method method = null;
        Method[] methods = obj.getClass().getMethods();

        for (int i = 0; i < methods.length && method == null; ++i) {
            if (methods[i].getName().equals(name)
                && argumentTypesEqual(methods[i].getParameterTypes(), args)) {
                method = methods[i];
            }
        }

        // Throw an exception if the method doesn't exist
        if (method == null) {
            throw new UnsupportedOperationException("Method does not exist: " + toString(obj.getClass(), name, args));
        }

        // Invoke the method
        try {
            return (R) method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Checks if the specified arguments match the specified method.
     *
     * @param types parameter types to match
     * @param args  arguments to match
     * @return {@code true} if the arguments match the method, {@code false} otherwise
     */
    private static boolean argumentTypesEqual(@Nonnull final Class<?>[] types, @Nonnull final Object[] args) {
        // Verify lengths are equal
        if (types.length != args.length) {
            return false;
        }

        // Verify method parameters are assignable from arguments
        for (int i = 0; i < types.length; ++i) {
            if (!isAssignableFrom(types[i], args[i].getClass())) {
                return false;
            }
        }

        // Method and arguments match
        return true;
    }

    /**
     * Checks if the first argument is either the same as, or is a superclass or subinterface of, the second argument. Unlike
     * {@link Class#isAssignableFrom(Class)}, this method assumes that primitive types and their corresponding {@code Object} type
     * are equal.
     *
     * @param to   class being assigned to
     * @param from class being assigned from
     * @return {@code true} if the first argument is assignable from the second argument, {@code false} otherwise
     */
    private static boolean isAssignableFrom(@Nonnull final Class<?> to, @Nonnull final Class<?> from) {
        return (to.isAssignableFrom(from) || (to == Integer.TYPE && from == Integer.class));
    }

    /**
     * Creates a string representing the specified method.
     *
     * @param cls    class containing the method
     * @param method name of the method
     * @param args   arguments for the method call
     * @return method string
     */
    @Nonnull
    private static String toString(@Nonnull final Class<?> cls, @Nonnull final String method, final Object... args) {
        StringBuilder string = new StringBuilder();
        string.append(cls.getName());
        string.append('.');
        string.append(method);
        string.append('(');

        for (int i = 0; i < args.length; ++i) {
            if (i > 0) {
                string.append(',');
            }
            string.append(args[i].getClass().getName());
        }

        string.append(')');
        return string.toString();
    }

    /**
     * Instances of {@code ClassUtils} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private ClassUtils() {
        throw new UnsupportedOperationException();
    }
}
