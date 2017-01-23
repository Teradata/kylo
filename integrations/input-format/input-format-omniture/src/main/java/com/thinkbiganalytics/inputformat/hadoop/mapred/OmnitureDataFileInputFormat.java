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

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;

import java.io.IOException;

/**
 * A tool for Hadoop eco system that makes it easier to parse Omniture Click Stream data.
 * It consists of custom input format and line reader. Works identically to TextInputFormat except for
 * the fact that it uses a EscapedLineReader which gets around Omniture's pesky escaped tabs and newlines.
 * For more information about format, please refer to Omniture Documentation at
 * https://marketing.adobe.com/resources/help/en_US/sc/clickstream/analytics_clickstream.pdf.
 */
public class OmnitureDataFileInputFormat extends TextInputFormat {

    @Override
    public RecordReader<LongWritable, Text> getRecordReader(
        org.apache.hadoop.mapred.InputSplit genericSplit, JobConf job,
        Reporter reporter)
        throws IOException {

        reporter.setStatus(genericSplit.toString());
        return new OmnitureDataFileRecordReader(job, (FileSplit) genericSplit);
    }
}
