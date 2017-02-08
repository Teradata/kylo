package com.thinkbiganalytics.spark.util;

/*-
 * #%L
 * thinkbig-commons-spark-repl
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

import javax.annotation.Nonnull;

/**
 * Static utility methods for arrays.
 */
public class ArrayUtils {

    /**
     * Instances of {@code ArrayUtils} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private ArrayUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the index of the first occurrence of the specified array. The returned index is relative to the offset in the
     * source array, and will be -1 if the target is not found.
     *
     * @param sourceBytes  array being searched
     * @param sourceOffset offset in source array
     * @param sourceLength number of elements to search
     * @param targetBytes  array to be matched
     * @param targetOffset offset in target array
     * @param targetLength number of elements to match
     * @param fromIndex    start position in source array, relative to its offset
     * @return index of the first occurrence, or -1 if not found
     * @throws IndexOutOfBoundsException if the offsets and lengths are greater than the size of the arrays
     */
    public static int indexOf(@Nonnull final byte[] sourceBytes, final int sourceOffset, final int sourceLength,
                              @Nonnull final byte[] targetBytes, final int targetOffset, final int targetLength,
                              final int fromIndex) {
        // Range checks
        if (fromIndex >= sourceLength) {
            return (targetLength == 0) ? sourceLength : -1;
        } else if (targetLength == 0) {
            return fromIndex;
        }

        // Search for a match starting at the specified index
        byte first = targetBytes[targetOffset];
        int sourceEnd = sourceOffset + sourceLength - targetLength;
        int targetEnd = targetOffset + targetLength;

        for (int i = sourceOffset + fromIndex; i <= sourceEnd; ++i) {
            if (sourceBytes[i] == first) {
                // Match remaining characters if the first one matches
                int j = i + 1;
                int k = targetOffset + 1;
                while (k < targetEnd && sourceBytes[j] == targetBytes[k]) {
                    ++j;
                    ++k;
                }

                // Return first match found
                if (k == targetEnd) {
                    return i - sourceOffset;
                }
            }
        }

        // Not found
        return -1;
    }
}
