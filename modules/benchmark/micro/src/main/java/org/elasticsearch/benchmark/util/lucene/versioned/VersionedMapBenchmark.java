begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.util.lucene.versioned
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|util
operator|.
name|lucene
operator|.
name|versioned
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|StopWatch
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|versioned
operator|.
name|ConcurrentVersionedMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|versioned
operator|.
name|ConcurrentVersionedMapLong
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|versioned
operator|.
name|NativeVersionedMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|versioned
operator|.
name|VersionedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CyclicBarrier
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|VersionedMapBenchmark
specifier|public
class|class
name|VersionedMapBenchmark
block|{
DECL|field|versionedMap
specifier|private
specifier|final
name|VersionedMap
name|versionedMap
decl_stmt|;
DECL|field|readerIterations
specifier|private
specifier|final
name|int
name|readerIterations
decl_stmt|;
DECL|field|writerIterations
specifier|private
specifier|final
name|int
name|writerIterations
decl_stmt|;
DECL|field|latch
specifier|private
specifier|final
name|CountDownLatch
name|latch
decl_stmt|;
DECL|field|readerThreads
specifier|private
specifier|final
name|Thread
index|[]
name|readerThreads
decl_stmt|;
DECL|field|writerThreads
specifier|private
specifier|final
name|Thread
index|[]
name|writerThreads
decl_stmt|;
DECL|field|barrier1
specifier|private
specifier|final
name|CyclicBarrier
name|barrier1
decl_stmt|;
DECL|field|barrier2
specifier|private
specifier|final
name|CyclicBarrier
name|barrier2
decl_stmt|;
DECL|method|VersionedMapBenchmark
specifier|public
name|VersionedMapBenchmark
parameter_list|(
name|VersionedMap
name|versionedMap
parameter_list|,
name|int
name|numberOfReaders
parameter_list|,
name|int
name|readerIterations
parameter_list|,
name|int
name|numberOfWriters
parameter_list|,
name|int
name|writerIterations
parameter_list|)
block|{
name|this
operator|.
name|versionedMap
operator|=
name|versionedMap
expr_stmt|;
name|this
operator|.
name|readerIterations
operator|=
name|readerIterations
expr_stmt|;
name|this
operator|.
name|writerIterations
operator|=
name|writerIterations
expr_stmt|;
name|readerThreads
operator|=
operator|new
name|Thread
index|[
name|numberOfReaders
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numberOfReaders
condition|;
name|i
operator|++
control|)
block|{
name|readerThreads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|ReaderThread
argument_list|()
argument_list|,
literal|"reader["
operator|+
name|i
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|writerThreads
operator|=
operator|new
name|Thread
index|[
name|numberOfWriters
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numberOfWriters
condition|;
name|i
operator|++
control|)
block|{
name|writerThreads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|WriterThread
argument_list|()
argument_list|,
literal|"writer["
operator|+
name|i
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|latch
operator|=
operator|new
name|CountDownLatch
argument_list|(
name|numberOfReaders
operator|+
name|numberOfWriters
argument_list|)
expr_stmt|;
name|barrier1
operator|=
operator|new
name|CyclicBarrier
argument_list|(
name|numberOfReaders
operator|+
name|numberOfWriters
operator|+
literal|1
argument_list|)
expr_stmt|;
name|barrier2
operator|=
operator|new
name|CyclicBarrier
argument_list|(
name|numberOfReaders
operator|+
name|numberOfWriters
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// now, warm up a bit
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|(
literal|"warmup"
argument_list|)
decl_stmt|;
name|stopWatch
operator|.
name|start
argument_list|()
expr_stmt|;
name|int
name|warmupSize
init|=
literal|1000000
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|warmupSize
condition|;
name|i
operator|++
control|)
block|{
name|versionedMap
operator|.
name|putVersion
argument_list|(
name|i
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|versionedMap
operator|.
name|beforeVersion
argument_list|(
name|i
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Warmup up of ["
operator|+
name|warmupSize
operator|+
literal|"]: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
argument_list|)
expr_stmt|;
name|versionedMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
name|MILLISECONDS
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|long
name|emptyUsed
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
decl_stmt|;
for|for
control|(
name|Thread
name|t
range|:
name|readerThreads
control|)
block|{
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|t
range|:
name|writerThreads
control|)
block|{
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|barrier1
operator|.
name|await
argument_list|()
expr_stmt|;
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
decl_stmt|;
name|stopWatch
operator|.
name|start
argument_list|()
expr_stmt|;
name|barrier2
operator|.
name|await
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// verify that the writers wrote...
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|writerIterations
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|versionedMap
operator|.
name|beforeVersion
argument_list|(
name|i
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Wrong value for ["
operator|+
name|i
operator|+
literal|']'
argument_list|)
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Total:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"   - ["
operator|+
name|readerThreads
operator|.
name|length
operator|+
literal|"] readers with ["
operator|+
name|readerIterations
operator|+
literal|"] iterations"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"   - ["
operator|+
name|writerThreads
operator|.
name|length
operator|+
literal|"] writers with ["
operator|+
name|writerIterations
operator|+
literal|"] iterations"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"   - Took: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
name|MILLISECONDS
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|long
name|bytesTaken
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
operator|-
name|emptyUsed
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Size of ["
operator|+
name|writerIterations
operator|+
literal|"] entries is "
operator|+
operator|new
name|SizeValue
argument_list|(
name|bytesTaken
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|ReaderThread
specifier|private
class|class
name|ReaderThread
implements|implements
name|Runnable
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|barrier1
operator|.
name|await
argument_list|()
expr_stmt|;
name|barrier2
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|readerIterations
condition|;
name|i
operator|++
control|)
block|{
name|versionedMap
operator|.
name|beforeVersion
argument_list|(
name|i
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|WriterThread
specifier|private
class|class
name|WriterThread
implements|implements
name|Runnable
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|barrier1
operator|.
name|await
argument_list|()
expr_stmt|;
name|barrier2
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|writerIterations
condition|;
name|i
operator|++
control|)
block|{
name|versionedMap
operator|.
name|putVersionIfAbsent
argument_list|(
name|i
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Some results: Two cores machine, general average across 5 runs
comment|//    VersionedMapBenchmark benchmark = new VersionedMapBenchmark(
comment|//            versionedMap, 30, 2000000, 10, 2000000
comment|//    );
comment|//        Running [native] type
comment|//        Took StopWatch '': running time  = 11.9s
comment|//        -----------------------------------------
comment|//        ms     %     Task name
comment|//        -----------------------------------------
comment|//        11909  100%
comment|//
comment|//        Size of [2000000] entries is 17.9mb
comment|//        Running [nb] type
comment|//        Took StopWatch '': running time  = 6.1s
comment|//        -----------------------------------------
comment|//        ms     %     Task name
comment|//        -----------------------------------------
comment|//        06134  100%
comment|//
comment|//        Size of [2000000] entries is 77.6mb
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|type
init|=
name|args
operator|.
name|length
operator|>
literal|0
condition|?
name|args
index|[
literal|0
index|]
else|:
literal|"nb"
decl_stmt|;
name|VersionedMap
name|versionedMap
decl_stmt|;
if|if
condition|(
literal|"nb"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|versionedMap
operator|=
operator|new
name|ConcurrentVersionedMapLong
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"native"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|versionedMap
operator|=
operator|new
name|NativeVersionedMap
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"concurrent"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|versionedMap
operator|=
operator|new
name|ConcurrentVersionedMap
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Type ["
operator|+
name|type
operator|+
literal|"] unknown"
argument_list|)
throw|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Running ["
operator|+
name|type
operator|+
literal|"] type"
argument_list|)
expr_stmt|;
name|VersionedMapBenchmark
name|benchmark
init|=
operator|new
name|VersionedMapBenchmark
argument_list|(
name|versionedMap
argument_list|,
literal|30
argument_list|,
literal|2000000
argument_list|,
literal|10
argument_list|,
literal|2000000
argument_list|)
decl_stmt|;
name|benchmark
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

