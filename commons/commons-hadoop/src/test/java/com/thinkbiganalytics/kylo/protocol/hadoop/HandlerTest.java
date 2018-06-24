package com.thinkbiganalytics.kylo.protocol.hadoop;

/*-
 * #%L
 * Kylo Catalog Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class HandlerTest {

    @Before
    public void setUp() {
        System.clearProperty(Handler.PROTOCOL_PATH_PROP);
        Handler.register();
        Handler.FACTORY.remove();
    }

    /**
     * Verify registering the handler.
     */
    @Test
    public void register() {
        Assert.assertEquals(Handler.class.getName(), System.getProperty(Handler.PROTOCOL_PATH_PROP) + ".hadoop.Handler");
    }

    /**
     * Verify opening connections.
     */
    @Test
    public void openConnection() throws IOException {
        final Handler handler = new Handler();
        Assert.assertNull("Expected null connection from missing configuration", handler.openConnection(new URL("hadoop:file:/path/file.ext")));

        Handler.setConfiguration(new Configuration(false));
        Assert.assertNull("Expected null connection from null url", handler.openConnection(null));
        Assert.assertNull("Expected null connection from file url", handler.openConnection(new URL("file:/path/file.ext")));
        Assert.assertNotNull("Expected non-null connection from hadoop url", handler.openConnection(new URL("hadoop:file:/path/file.ext")));
    }
}
