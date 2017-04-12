/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.support;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import org.modeshape.jcr.api.JcrTools;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Extension of the ModeShape JcrTools that allows printing to any PrintStream, and not just
 * System.out.
 */
public class JcrTool extends JcrTools {

    private final PrintWriter out;

    public JcrTool() {
        this(false);
    }

    public JcrTool(boolean debug) {
        this(debug, System.out);
    }

    public JcrTool(boolean debug, PrintStream out) {
        this(debug, new PrintWriter(out));
    }

    public JcrTool(boolean debug, PrintWriter out) {
        super(debug);
        this.out = out;
    }

    @Override
    public void print(Object msg) {
        if (isDebug() && msg != null) {
            out.println(msg.toString());
        }
    }

    public void printNode(Session session, String absPath) {
        try {
            Node node = session.getNode(absPath);
            super.printNode(node);
            out.flush();
        } catch (RepositoryException e) {
            e.printStackTrace(this.out);
        }
    }

    public void printSubgraph(Session session, String absPath) {
        try {
            Node node = session.getNode(absPath);
            super.printSubgraph(node);
            out.flush();
        } catch (RepositoryException e) {
            e.printStackTrace(this.out);
        }
    }
}
