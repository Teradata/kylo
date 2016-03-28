package com.thinkbiganalytics.spark.metadata;

/**
 * Created by gh186012 on 3/22/16.
 */
public class TransformRequest
{
    private Parent parent;

    private String script;

    public TransformRequest ()
    {}

    public Parent getParent ()
    {
        return parent;
    }

    public void setParent (Parent parent)
    {
        this.parent = parent;
    }

    public String getScript ()
    {
        return script;
    }

    public void setScript (String script)
    {
        this.script = script;
    }

    public static class Parent
    {
        private String script;

        private String table;

        public String getScript ()
        {
            return script;
        }

        public void setScript (String script)
        {
            this.script = script;
        }

        public String getTable ()
        {
            return table;
        }

        public void setTable (String table)
        {
            this.table = table;
        }
    }
}
