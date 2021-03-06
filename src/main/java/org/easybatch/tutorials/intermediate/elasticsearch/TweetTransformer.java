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

package org.easybatch.tutorials.intermediate.elasticsearch;

import org.easybatch.core.processor.RecordProcessingException;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.StringRecord;
import org.easybatch.tutorials.common.Tweet;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

import static java.lang.String.format;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Processor that transforms tweet to json format using ElasticSearch API.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class TweetTransformer implements RecordProcessor<GenericRecord<Tweet>, StringRecord> {

    @Override
    public StringRecord processRecord(GenericRecord<Tweet> record) throws RecordProcessingException {
        Tweet tweet = record.getPayload();
        XContentBuilder builder;
        try {
            builder = jsonBuilder()
                    .startObject()
                    .field("id", tweet.getId())
                    .field("user", tweet.getUser())
                    .field("message", tweet.getMessage())
                    .endObject();
            return new StringRecord(record.getHeader(), builder.string());
        } catch (IOException e) {
            String message = format("Unable to process tweet %s", tweet);
            throw new RecordProcessingException(message, e);
        }
    }

}
