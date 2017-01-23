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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.RecordReader;

import java.io.IOException;

/**
 * EscapedLineReader gets around Omniture's pesky escaped tabs and newlines.
 * For more information about format, please refer to Omniture Documentation at
 * https://marketing.adobe.com/resources/help/en_US/sc/clickstream/analytics_clickstream.pdf.
 */
public class OmnitureDataFileRecordReader implements RecordReader<LongWritable, Text> {

    private static final Log LOG = LogFactory.getLog(OmnitureDataFileRecordReader.class);

    private int maxLineLength;
    private long start;
    private long pos;
    private long end;
    private CompressionCodecFactory compressionCodecs;
    private EscapedLineReader lineReader;

    public OmnitureDataFileRecordReader(Configuration job, FileSplit split)
        throws IOException {

        this.maxLineLength = job.getInt("mapred.escapedlinereader.maxlength", Integer.MAX_VALUE);
        this.start = split.getStart();
        this.end = start + split.getLength();
        final Path file = split.getPath();
        this.compressionCodecs = new CompressionCodecFactory(job);
        final CompressionCodec codec = compressionCodecs.getCodec(file);

        // Open the file and seek to the start of the split
        FileSystem fs = file.getFileSystem(job);
        FSDataInputStream fileIn = fs.open(split.getPath());
        boolean skipFirstLine = false;
        if (codec != null) {
            lineReader = new EscapedLineReader(codec.createInputStream(fileIn), job);
            end = Long.MAX_VALUE;
        } else {
            if (start != 0) {
                skipFirstLine = true;
                --start;
                fileIn.seek(start);
            }
            lineReader = new EscapedLineReader(fileIn, job);
        }
        if (skipFirstLine) {
            start += lineReader.readLine(new Text(), 0, (int) Math.min((long) Integer.MAX_VALUE, end - start));
        }
        this.pos = start;
    }

    public boolean next(LongWritable key, Text value) throws IOException {
        while (pos <= end) {
            key.set(pos);

            int newSize = lineReader.readLine(value, maxLineLength, Math.max((int) Math.min(Integer.MAX_VALUE, end - pos), maxLineLength));
            pos += newSize;

            if (newSize == 0) {
                return false;
            }

            String line = value.toString().replaceAll("\\\\\\\\", "").replaceAll("\\\\\t", " ").replaceAll("\\\\(\n|\r|\r\n)", " ");
            value.set(line);

            if (newSize < maxLineLength) {
                return true;
            }

            // line too long. try again
            LOG.info("Skipped line of size " + newSize + " at pos " + (pos - newSize));
        }

        return false;
    }

    public LongWritable createKey() {
        return new LongWritable();
    }

    public Text createValue() {
        return new Text();
    }

    public long getPos() throws IOException {
        return pos;
    }

    public void close() throws IOException {
        if (lineReader != null) {
            lineReader.close();
        }
    }

    public float getProgress() throws IOException {
        if (start == end) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (pos - start) / (float) (end - start));
        }
    }
}
