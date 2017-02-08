/*
 * MIT License
 *
 * Copyright (c) 2016 mikes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.thinkbiganalytics.inputformat.hadoop.mapred;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.io.InputStream;

/**
 * A class that provides an escaped line reader from an input stream.
 */
public class EscapedLineReader {

    private static final byte DEFAULT_ESCAPE_CHARACTER = '\\';
    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private InputStream in;
    private byte[] buffer;
    // the number of bytes in the real buffer
    private int bufferLength;
    // the current position of the buffer
    private int bufferPos;
    private byte escapeChar;

    /**
     * Create a multi-line reader that reads from the given stream using the
     * given buffer-size.
     *
     * @param in         The input stream
     * @param bufferSize Size of the read buffer
     */
    public EscapedLineReader(InputStream in, int bufferSize, byte escapeChar) {
        this.escapeChar = escapeChar;
        this.in = in;
        this.bufferSize = bufferSize;
        this.buffer = new byte[this.bufferSize];
    }

    /**
     * Create a multi-line reader that reads from the given stream using the
     * default buffer-size (64K).
     *
     * @param in The input stream
     */
    public EscapedLineReader(InputStream in, byte escapeChar) {
        this(in, DEFAULT_BUFFER_SIZE, escapeChar);
    }

    public EscapedLineReader(InputStream in) {
        this(in, DEFAULT_BUFFER_SIZE, DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * Create a multi-line reader that reads from the given stream using the
     * <code>io.file.buffer.size</code> specified in the given
     * <code>Configuration</code>.
     *
     * @param in   input stream
     * @param conf configuration
     */
    public EscapedLineReader(InputStream in, Configuration conf) throws IOException {
        this(in, conf.getInt("io.file.buffer.size", DEFAULT_BUFFER_SIZE), DEFAULT_ESCAPE_CHARACTER);
    }

    public EscapedLineReader(InputStream in, Configuration conf, byte escapeChar) throws IOException {
        this(in, conf.getInt("io.file.buffer.size", DEFAULT_BUFFER_SIZE), escapeChar);
    }

    /**
     * Close the underlying stream.
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Read one line from the InputStream into the given Text. A line
     * can be terminated by one of the following: '\n' (LF), '\r' (CR),
     * or '\r\n' (CR+LF).  Will ignore any of these termination characters
     * if they are proceeded by a designated escape character. EOF also
     * terminates an otherwise unterminated line.
     *
     * @param str               the object to store the given line (without the newline)
     * @param maxLineLength     the maximum number of bytes to store into str; the rest will be silently discarded.
     * @param maxBytesToConsume the maximum number of bytes to consume in this call.  This is only a hint, because if the line crosses this threshold, we allow it to happen.  It can overshoot
     *                          potentially by as much as one buffer length.
     * @return the number of bytes read including the (longest) newline found
     */
    public int readLine(Text str, int maxLineLength, int maxBytesToConsume)
        throws IOException {
        /* We're reading data from in, but the head of the stream may be
         * already buffered in buffer, so we have several cases:
	     * 1. No newline characters are in the buffer, so we need to copy
	     *    everything and read another buffer from the stream.
	     * 2. An unambiguously terminated line is in buffer, so we just
	     *    copy to str.
	     * 3. Ambiguously terminated line is in buffer, i.e. buffer ends
	     *    in CR.  In this case we copy everything up to CR to str, but
	     *    we also need to see what follows CR: if it's LF, then we
	     *    need consume LF as well, so next call to readLine will read
	     *    from after that.
	     * We use a flag prevCharCR to signal if previous character was CR
	     * and, if it happens to be at the end of the buffer, delay
	     * consuming it until we have a chance to look at the char that
	     * follows.
	     */
        str.clear();
        int txtLength = 0; // tracks str.getLength() as an optimization
        int newLineLength = 0; // length of the terminating newline
        boolean prevCharCR = false; // true if prev char was \r
        long bytesConsumed = 0;

        do {
            int startPos = bufferPos; // starting from where we left off
            if (bufferPos >= bufferLength) {
                startPos = bufferPos = 0;
                if (prevCharCR) {
                    ++bytesConsumed; // account for CR from previous read
                }
                bufferLength = in.read(buffer);
                if (bufferLength <= 0) {
                    break; // EOF
                }
            }
            for (; bufferPos < bufferLength; ++bufferPos) {
                boolean escaped = false;
                if (prevCharCR && bufferPos > 1) {
                    escaped = (buffer[bufferPos - 2] == escapeChar);
                }
                if (!prevCharCR && bufferPos > 0) {
                    escaped = (buffer[bufferPos - 1] == escapeChar);
                }

                if (buffer[bufferPos] == LF && !escaped) {
                    newLineLength = prevCharCR ? 2 : 1;
                    ++bufferPos; // at next loop proceed from following byte
                    break;
                }
                if (prevCharCR && !escaped) { // CR + notLF, we are at notLF
                    newLineLength = 1;
                    break;
                }
                prevCharCR = (buffer[bufferPos] == CR);
                //prevCharCR = (buffer[bufferPos] == CR && !escaped);
            }
            int readLength = bufferPos - startPos;
            if (prevCharCR && newLineLength == 0) {
                --readLength;
            }
            bytesConsumed += readLength;
            int appendLength = readLength - newLineLength;
            if (appendLength > maxLineLength - txtLength) {
                appendLength = maxLineLength - txtLength;
            }
            if (appendLength > 0) {
                str.append(buffer, startPos, appendLength);
                txtLength += appendLength;
            }
        } while (newLineLength == 0 && bytesConsumed < maxBytesToConsume);

        if (bytesConsumed > (long) Integer.MAX_VALUE) {
            throw new IOException("Too many bytes before newline: " + bytesConsumed);
        }

        return (int) bytesConsumed;
    }

    /**
     * Read from the InputStream into the given Text.
     *
     * @param str           the object to store the given line
     * @param maxLineLength the maximum number of bytes to store into str
     * @return the number of bytes read including newline
     * @throws IOException if the underlying stream throws
     */
    public int readLine(Text str, int maxLineLength) throws IOException {
        return readLine(str, maxLineLength, Integer.MAX_VALUE);
    }

    /**
     * Read from the InputStream into the given Text.
     *
     * @param str the object to store the given line
     * @return the number of bytes read including newline
     * @throws IOException if the underlying stream throws
     */
    public int readLine(Text str) throws IOException {
        return readLine(str, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}
