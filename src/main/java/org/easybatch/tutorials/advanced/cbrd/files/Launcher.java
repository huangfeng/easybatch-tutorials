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

package org.easybatch.tutorials.advanced.cbrd.files;

import org.easybatch.core.dispatcher.ContentBasedRecordDispatcher;
import org.easybatch.core.dispatcher.ContentBasedRecordDispatcherBuilder;
import org.easybatch.core.dispatcher.PoisonRecordBroadcaster;
import org.easybatch.core.filter.FileExtensionFilter;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.job.Job;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.reader.FileRecordReader;
import org.easybatch.core.record.FileRecord;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Arrays.asList;
import static org.easybatch.core.job.JobBuilder.aNewJob;

/**
* Main class to run the content based record dispatching tutorial.
 *
* @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
*/
public class Launcher {

    private static final int THREAD_POOL_SIZE = 3;

    public static void main(String[] args) throws Exception {
        
        String path = args.length == 0 ? "." : args[0];
        File directory = new File(path);

        // Create queues
        BlockingQueue<FileRecord> csvQueue = new LinkedBlockingQueue<>();
        BlockingQueue<FileRecord> xmlQueue = new LinkedBlockingQueue<>();

        // Create a content based record dispatcher to dispatch records based on their content
        ContentBasedRecordDispatcher<FileRecord> recordDispatcher = new ContentBasedRecordDispatcherBuilder<FileRecord>()
                .when(new CsvFilePredicate()).dispatchTo(csvQueue)
                .when(new XmlFilePredicate()).dispatchTo(xmlQueue)
                .build();

        // Build a master job that will read files from the directory and dispatch them to worker jobs
        Job masterJob = aNewJob()
                .named("master-job")
                .reader(new FileRecordReader(directory))
                .filter(new FileExtensionFilter(".log", ".tmp"))
                .dispatcher(recordDispatcher)
                .jobListener(new PoisonRecordBroadcaster(Arrays.<BlockingQueue>asList(csvQueue, xmlQueue)))
                .build();

        // Build jobs
        Job workerJob1 = buildWorkerJob(csvQueue, "csv-worker-job");
        Job workerJob2 = buildWorkerJob(xmlQueue, "xml-worker-job");

        // Create a threads pool to call jobs in parallel
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Submit master and worker jobs to executor service
        executorService.invokeAll(asList(masterJob, workerJob1, workerJob2));

        // Shutdown executor service
        executorService.shutdown();

    }

    public static Job buildWorkerJob(BlockingQueue<FileRecord> queue, String jobName) {
        return aNewJob()
                .named(jobName)
                .reader(new BlockingQueueRecordReader<>(queue))
                .filter(new PoisonRecordFilter())
                .processor(new DummyFileProcessor())
                .build();
    }

}
