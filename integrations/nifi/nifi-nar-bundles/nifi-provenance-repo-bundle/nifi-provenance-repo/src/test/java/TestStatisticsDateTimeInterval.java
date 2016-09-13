import com.thinkbiganalytics.nifi.provenance.TestMaps;
import com.thinkbiganalytics.nifi.provenance.v2.cache.stats.DateTimeInterval;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/23/16.
 */
public class TestStatisticsDateTimeInterval {


    //@Test
    public void testTimeInterval() {
        DateTime now = DateTime.now();

        DateTime next = now.plus(10000);

        DateTime start = DateTime.now().withHourOfDay(10).withMinuteOfHour(58).withSecondOfMinute(40).withMillisOfSecond(0);
        DateTime end = start.plusMinutes(1);
        DateTimeInterval dateTimeInterval = new DateTimeInterval(start, end);
        DateTime adjustedEnd = dateTimeInterval.getAdjustedEndTime();
        DateTime nextStart = dateTimeInterval.getNextStartTime();
        int i = 0;


    }


    // @Test
    public void testName() throws Exception {

        final Map<String, String> m1 = new ConcurrentHashMap<String, String>();

        TestMaps testMaps = new TestMaps();
        AtomicLong id = new AtomicLong(0);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    testMaps.prepareAndAdd(id.getAndIncrement());
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    testMaps.prepareAndAdd(id.getAndIncrement());
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }


        });
        //  t1.start();
        //  t2.start();
        while (true) {

        }

    }

}
