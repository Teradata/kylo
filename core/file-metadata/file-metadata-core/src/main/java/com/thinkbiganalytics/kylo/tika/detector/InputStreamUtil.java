package com.thinkbiganalytics.kylo.tika.detector;

/*-
 * #%L
 * kylo-file-metadata-core
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Read an input stream up to a given length
 */
public class InputStreamUtil {

    public static byte[] readHeader(InputStream stream, int len) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("InputStream is missing");
        } else {
            InputStream inputStream = stream;
            if(!stream.markSupported()){
                inputStream = new BufferedInputStream(stream);
            }

            try {
                inputStream.mark(len);
                byte[] bytes = new byte[len];
                int totalRead = 0;

                for (int lastRead = inputStream.read(bytes); lastRead != -1; lastRead = inputStream.read(bytes, totalRead, bytes.length - totalRead)) {
                    totalRead += lastRead;
                    if (totalRead == bytes.length) {
                        return bytes;
                    }
                }

                byte[] shorter = new byte[totalRead];
                System.arraycopy(bytes, 0, shorter, 0, totalRead);
                return shorter;
            }finally {
                if(inputStream != null) {
                    inputStream.reset();
                }
            }
        }
    }

    public static InputStream asStream(byte[] bytes){
        return new ByteArrayInputStream(bytes);
    }

    public  static InputStream readHeaderAsStream(InputStream stream, int len)throws IOException{

        byte[] bytes = readHeader(stream,len);
        return new ByteArrayInputStream(bytes);
    }

}
