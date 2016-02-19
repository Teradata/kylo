package com.thinkbiganalytics.controller.precond;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.controller.precond.metric.DatasetUpdatedSinceMetric;
import com.thinkbiganalytics.controller.precond.metric.FeedExecutedSinceMetric;
import com.thinkbiganalytics.controller.precond.metric.WithinSchedule;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;

public class InMemoryFeedPreconditionServiceTest {
    
    private InMemoryFeedPreconditionService service = new InMemoryFeedPreconditionService();

    @Before
    public void setUp() throws Exception {
        this.service.setPrivider(new InMemorySLAProvider());
    }

    @Test
    public void testCreatePrecondition() throws ParseException {
        FeedExecutedSinceMetric metric1 = new FeedExecutedSinceMetric("x", "0 0 6 * * ? *");
        DatasetUpdatedSinceMetric metric2 = new DatasetUpdatedSinceMetric("datasetX", "0 0 6 * * ? *");
        WithinSchedule metric3 = new WithinSchedule("0 0 6 * * ? *");
        
        FeedPrecondition pre = this.service.createPrecondition("test", metric1, metric2, metric3);
        
        assertThat(pre).isNotNull();
        
        assertThat(this.service.getPrecondition(pre.getId())).isNotNull();
        assertThat(this.service.getPrecondition(pre.getName())).isNotNull();
    }

}
