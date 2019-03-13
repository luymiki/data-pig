/**
 * (C) 2010-2014 Alibaba Group Holding Limited.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anluy.datapig.plugin.core.exchanger;

import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.element.ClassSize;
import com.anluy.datapig.plugin.core.element.DefaultRecord;
import com.anluy.datapig.plugin.core.element.Record;
import com.anluy.datapig.plugin.core.element.TerminateRecord;
import com.anluy.datapig.plugin.utils.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RecordExchanger implements RecordSender, RecordReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordExchanger.class);
    private LinkedBlockingQueue<Record> queue = new LinkedBlockingQueue<Record>(5000);

    private long readerMemorySize = ClassSize.DefaultRecordHead;
    private long writerMemorySize = ClassSize.DefaultRecordHead;
    private AtomicLong readerSize = new AtomicLong();
    private AtomicLong writerSize = new AtomicLong();
    /**
     * 重试次数
     */
    private static final int ERROR_TRY = 10;

    private volatile boolean shutdown = false;

    public RecordExchanger() {
    }

    @Override
    public Record getFromReader() {
        Record record = null;
        do{
            if (shutdown) {
                break;
            }
            try {
                record = queue.poll(2L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("read database fail : data poll fail !" + e.getMessage(), e);
            }
        }while (record == null);

        if (record == null) {
            return TerminateRecord.get();
        }
        if(!(record instanceof TerminateRecord)){
            writerSize.addAndGet(1);
        }
        writerMemorySize += record.getMemorySize();
        return record;
    }

    @Override
    public Record createRecord() {
        try {
            return new DefaultRecord();
        } catch (Exception e) {
            throw new DataPigException(e.getMessage(), e);
        }
    }

    @Override
    public void sendToWriter(Record record) {
        if (shutdown) {
            throw new DataPigException("schedule is stop");
        }
        if (record == null) {
            return;
        }
        readerMemorySize += record.getMemorySize();
        int rty = ERROR_TRY;
        boolean b = false;
        do{
            rty--;
            if (shutdown || rty < 0) {
                break;
            }
            try {
                b = queue.offer(record, 5L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("read database fail : data offer fail !" + e.getMessage(), e);
            }
        } while (!b);
        readerSize.addAndGet(1);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
        queue.clear();
        queue = null;
    }

    @Override
    public void terminate() {
        this.queue.offer(TerminateRecord.get());
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    public long getReaderMemorySize() {
        return readerMemorySize;
    }

    public long getWriterMemorySize() {
        return writerMemorySize;
    }
    public String getReaderMemorySizeString() {
        return StrUtil.byteStringify(readerMemorySize);
    }

    public String getWriterMemorySizeString() {
        return StrUtil.byteStringify(writerMemorySize);
    }

    public long getReaderSize() {
        return readerSize.get();
    }

    public long getWriterSize() {
        return writerSize.get();
    }
}
