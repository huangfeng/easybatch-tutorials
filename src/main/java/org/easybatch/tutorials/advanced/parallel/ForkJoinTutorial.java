/*
 * The MIT License
 *
 *  Copyright (c) 2016, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.easybatch.tutorials.advanced.parallel;

import org.easybatch.core.dispatcher.PoisonRecordBroadcaster;
import org.easybatch.core.dispatcher.RoundRobinRecordDispatcher;
import org.easybatch.core.filter.HeaderRecordFilter;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.BlockingQueueRecordWriter;
import org.easybatch.core.writer.StandardOutputRecordWriter;
import org.easybatch.flatfile.DelimitedRecordMapper;
import org.easybatch.flatfile.FlatFileRecordReader;
import org.easybatch.tutorials.common.Tweet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.*;

import static java.util.Arrays.asList;
import static org.easybatch.core.job.JobBuilder.aNewJob;

/**
 * Main class to run the fork/join tutorial.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class ForkJoinTutorial {

    private static final int THREAD_POOL_SIZE = 4;
    private static final int NB_WORKERS = 2;

    public static void main(String[] args) throws Exception {

        // Create data source
        File tweets = new File("src/main/resources/data/tweets.csv");

        // Create queues
        BlockingQueue<Record> workQueue1 = new LinkedBlockingQueue<>();
        BlockingQueue<Record> workQueue2 = new LinkedBlockingQueue<>();
        BlockingQueue<Record> joinQueue = new LinkedBlockingQueue<>();

        // Build jobs
        Job forkJob = buildForkJob("fork-job", tweets, asList(workQueue1, workQueue2));
        Job workerJob1 = buildWorkerJob("worker-job1", workQueue1, joinQueue);
        Job workerJob2 = buildWorkerJob("worker-job2", workQueue2, joinQueue);
        Job joinJob = buildJoinJob("join-job", joinQueue);

        // Create a thread pool to call jobs in parallel
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Submit jobs to executor service
        executorService.invokeAll(asList(forkJob, workerJob1, workerJob2, joinJob));

        // Shutdown executor service
        executorService.shutdown();
    }

    public static Job buildForkJob(String jobName, File dataSource, List<BlockingQueue<Record>> workQueues) throws FileNotFoundException {
        return aNewJob()
                .named(jobName)
                .reader(new FlatFileRecordReader(dataSource))
                .filter(new HeaderRecordFilter())
                .mapper(new DelimitedRecordMapper(Tweet.class, "id", "user", "message"))
                .dispatcher(new RoundRobinRecordDispatcher<>(workQueues))
                .jobListener(new PoisonRecordBroadcaster<>(workQueues))
                .build();
    }

    public static Job buildWorkerJob(String jobName, BlockingQueue<Record> workQueue, BlockingQueue<Record> joinQueue) {
        return aNewJob()
                .named(jobName)
                .reader(new BlockingQueueRecordReader<>(workQueue))
                .processor(new TweetProcessor(jobName))
                .writer(new BlockingQueueRecordWriter<>(joinQueue))
                .build();
    }

    public static Job buildJoinJob(String jobName, BlockingQueue<Record> joinQueue) {
        return aNewJob()
                .named(jobName)
                .reader(new BlockingQueueRecordReader<>(joinQueue, NB_WORKERS))
                .filter(new PoisonRecordFilter())
                .writer(new StandardOutputRecordWriter())
                .build();
    }

    static class TweetProcessor implements RecordProcessor<Record, Record> {

        private String workerName;

        public TweetProcessor(String workerName) {
            this.workerName = workerName;
        }

        @Override
        public Record processRecord(Record record) {
            System.out.println(workerName + ": processing tweet " + record.getPayload());
            return record;
        }

    }

}
