/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.support;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.modeshape.jcr.api.JcrTools;

/**
 * Extension of the ModeShape JcrTools that allows printing to any PrintStream, and not just
 * System.out.
 * @author Sean Felten
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
}
