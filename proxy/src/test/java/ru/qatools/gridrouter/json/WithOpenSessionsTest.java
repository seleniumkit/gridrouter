package ru.qatools.gridrouter.json;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class WithOpenSessionsTest {

    @Test
    public void testSynchronizationOnStart() throws Exception {
        int sessions = 3;
        GridStats gridStats = new GridStatsWithDelay();
        execInParallel(gridStats::startSession, sessions);
        assertThat(gridStats.getOpenSessions(), is(sessions));
        execInParallel(gridStats::stopSession, sessions);
        assertThat(gridStats.getOpenSessions(), is(0));
    }

    private void execInParallel(Runnable task, int times) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(times);
        while (times --> 0) {
            pool.submit(task);
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, SECONDS));
    }

    private class GridStatsWithDelay extends GridStats {

        @Override
        public int getOpenSessions() {
            int result = super.getOpenSessions();
            sleep();
            return result;
        }

        private void sleep() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
