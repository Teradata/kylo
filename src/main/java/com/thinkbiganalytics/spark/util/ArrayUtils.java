package com.thinkbiganalytics.spark.util;

import javax.annotation.Nonnull;

/**
 * Static utility methods for arrays.
 */
public class ArrayUtils
{
    /**
     * Returns the index of the first occurrence of the specified array. The returned index is
     * relative to the offset in the source array, and will be -1 if the target is not found.
     *
     * @param sourceBytes array being searched
     * @param sourceOffset offset in source array
     * @param sourceLength number of elements to search
     * @param targetBytes array to be matched
     * @param targetOffset offset in target array
     * @param targetLength number of elements to match
     * @param fromIndex start position in source array, relative to its offset
     * @return index of the first occurrence, or -1 if not found
     * @throws IndexOutOfBoundsException if the offsets and lengths are greater than the size of the
     *     arrays
     */
    public static int indexOf (@Nonnull final byte[] sourceBytes, final int sourceOffset,
            final int sourceLength, @Nonnull final byte[] targetBytes, final int targetOffset,
            final int targetLength, final int fromIndex)
    {
        // Range checks
        if (fromIndex >= sourceLength) {
            return (targetLength == 0) ? sourceLength : -1;
        }
        else if (targetLength == 0) {
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

    private ArrayUtils ()
    {
        throw new UnsupportedOperationException();
    }
}
