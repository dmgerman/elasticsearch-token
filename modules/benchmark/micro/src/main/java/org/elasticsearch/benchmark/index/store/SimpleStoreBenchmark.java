begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|index
operator|.
name|store
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|IndexInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|IndexOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|ByteSizeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|ByteSizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|Store
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|fs
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|memory
operator|.
name|ByteBufferStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|memory
operator|.
name|HeapStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|ram
operator|.
name|RamStore
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy  */
end_comment

begin_class
DECL|class|SimpleStoreBenchmark
specifier|public
class|class
name|SimpleStoreBenchmark
block|{
DECL|field|dynamicFilesCounter
specifier|private
specifier|final
name|AtomicLong
name|dynamicFilesCounter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|store
specifier|private
specifier|final
name|Store
name|store
decl_stmt|;
DECL|field|staticFiles
specifier|private
name|String
index|[]
name|staticFiles
init|=
operator|new
name|String
index|[
literal|10
index|]
decl_stmt|;
DECL|field|staticFileSize
specifier|private
name|ByteSizeValue
name|staticFileSize
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
decl_stmt|;
DECL|field|dynamicFileSize
specifier|private
name|ByteSizeValue
name|dynamicFileSize
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|1
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
decl_stmt|;
DECL|field|readerIterations
specifier|private
name|int
name|readerIterations
init|=
literal|10
decl_stmt|;
DECL|field|writerIterations
specifier|private
name|int
name|writerIterations
init|=
literal|10
decl_stmt|;
DECL|field|readerThreads
specifier|private
name|Thread
index|[]
name|readerThreads
init|=
operator|new
name|Thread
index|[
literal|1
index|]
decl_stmt|;
DECL|field|writerThreads
specifier|private
name|Thread
index|[]
name|writerThreads
init|=
operator|new
name|Thread
index|[
literal|1
index|]
decl_stmt|;
DECL|field|latch
specifier|private
name|CountDownLatch
name|latch
decl_stmt|;
DECL|field|barrier1
specifier|private
name|CyclicBarrier
name|barrier1
decl_stmt|;
DECL|field|barrier2
specifier|private
name|CyclicBarrier
name|barrier2
decl_stmt|;
DECL|method|SimpleStoreBenchmark
specifier|public
name|SimpleStoreBenchmark
parameter_list|(
name|Store
name|store
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
block|}
DECL|method|numberStaticFiles
specifier|public
name|SimpleStoreBenchmark
name|numberStaticFiles
parameter_list|(
name|int
name|numberStaticFiles
parameter_list|)
block|{
name|this
operator|.
name|staticFiles
operator|=
operator|new
name|String
index|[
name|numberStaticFiles
index|]
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|staticFileSize
specifier|public
name|SimpleStoreBenchmark
name|staticFileSize
parameter_list|(
name|ByteSizeValue
name|staticFileSize
parameter_list|)
block|{
name|this
operator|.
name|staticFileSize
operator|=
name|staticFileSize
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|dynamicFileSize
specifier|public
name|SimpleStoreBenchmark
name|dynamicFileSize
parameter_list|(
name|ByteSizeValue
name|dynamicFileSize
parameter_list|)
block|{
name|this
operator|.
name|dynamicFileSize
operator|=
name|dynamicFileSize
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|readerThreads
specifier|public
name|SimpleStoreBenchmark
name|readerThreads
parameter_list|(
name|int
name|readerThreads
parameter_list|)
block|{
name|this
operator|.
name|readerThreads
operator|=
operator|new
name|Thread
index|[
name|readerThreads
index|]
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|readerIterations
specifier|public
name|SimpleStoreBenchmark
name|readerIterations
parameter_list|(
name|int
name|readerIterations
parameter_list|)
block|{
name|this
operator|.
name|readerIterations
operator|=
name|readerIterations
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|writerIterations
specifier|public
name|SimpleStoreBenchmark
name|writerIterations
parameter_list|(
name|int
name|writerIterations
parameter_list|)
block|{
name|this
operator|.
name|writerIterations
operator|=
name|writerIterations
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|writerThreads
specifier|public
name|SimpleStoreBenchmark
name|writerThreads
parameter_list|(
name|int
name|writerThreads
parameter_list|)
block|{
name|this
operator|.
name|writerThreads
operator|=
operator|new
name|Thread
index|[
name|writerThreads
index|]
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|SimpleStoreBenchmark
name|build
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Creating ["
operator|+
name|staticFiles
operator|.
name|length
operator|+
literal|"] static files with size ["
operator|+
name|staticFileSize
operator|+
literal|"]"
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
name|staticFiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|staticFiles
index|[
name|i
index|]
operator|=
literal|"static"
operator|+
name|i
expr_stmt|;
name|IndexOutput
name|io
init|=
name|store
operator|.
name|directory
argument_list|()
operator|.
name|createOutput
argument_list|(
name|staticFiles
index|[
name|i
index|]
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|sizeCounter
init|=
literal|0
init|;
name|sizeCounter
operator|<
name|staticFileSize
operator|.
name|bytes
argument_list|()
condition|;
name|sizeCounter
operator|++
control|)
block|{
name|io
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
block|}
name|io
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Using ["
operator|+
name|dynamicFileSize
operator|+
literal|"] size for dynamic files"
argument_list|)
expr_stmt|;
comment|// warmp
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
for|for
control|(
name|String
name|staticFile
range|:
name|staticFiles
control|)
block|{
name|IndexInput
name|ii
init|=
name|store
operator|.
name|directory
argument_list|()
operator|.
name|openInput
argument_list|(
name|staticFile
argument_list|)
decl_stmt|;
comment|// do a full read
for|for
control|(
name|long
name|counter
init|=
literal|0
init|;
name|counter
operator|<
name|ii
operator|.
name|length
argument_list|()
condition|;
name|counter
operator|++
control|)
block|{
name|byte
name|result
init|=
name|ii
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|1
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failure, read wrong value ["
operator|+
name|result
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// do a list of the files
name|store
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
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
literal|"Warmup Took: "
operator|+
name|stopWatch
operator|.
name|shortSummary
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
name|readerThreads
operator|.
name|length
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
literal|"Reader["
operator|+
name|i
operator|+
literal|"]"
argument_list|)
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
name|writerThreads
operator|.
name|length
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
literal|"Writer["
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
name|readerThreads
operator|.
name|length
operator|+
name|writerThreads
operator|.
name|length
argument_list|)
expr_stmt|;
name|barrier1
operator|=
operator|new
name|CyclicBarrier
argument_list|(
name|readerThreads
operator|.
name|length
operator|+
name|writerThreads
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
name|barrier2
operator|=
operator|new
name|CyclicBarrier
argument_list|(
name|readerThreads
operator|.
name|length
operator|+
name|writerThreads
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
return|return
name|this
return|;
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Running:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"   -- Readers ["
operator|+
name|readerThreads
operator|.
name|length
operator|+
literal|"] with ["
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
literal|"   -- Writers ["
operator|+
name|writerThreads
operator|.
name|length
operator|+
literal|"] with ["
operator|+
name|writerIterations
operator|+
literal|"] iterations"
argument_list|)
expr_stmt|;
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Took: "
operator|+
name|stopWatch
operator|.
name|shortSummary
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
name|staticFiles
operator|.
name|length
operator|+
literal|"], each with size ["
operator|+
name|staticFileSize
operator|+
literal|"], is "
operator|+
operator|new
name|ByteSizeValue
argument_list|(
name|bytesTaken
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
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
try|try
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
name|readerIterations
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|String
name|staticFile
range|:
name|staticFiles
control|)
block|{
comment|// do a list of the files
name|store
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
expr_stmt|;
name|IndexInput
name|ii
init|=
name|store
operator|.
name|directory
argument_list|()
operator|.
name|openInput
argument_list|(
name|staticFile
argument_list|)
decl_stmt|;
comment|// do a full read
for|for
control|(
name|long
name|counter
init|=
literal|0
init|;
name|counter
operator|<
name|ii
operator|.
name|length
argument_list|()
condition|;
name|counter
operator|++
control|)
block|{
name|byte
name|result
init|=
name|ii
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|1
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failure, read wrong value ["
operator|+
name|result
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// do a list of the files
name|store
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
expr_stmt|;
comment|// do a seek and read some byes
name|ii
operator|.
name|seek
argument_list|(
name|ii
operator|.
name|length
argument_list|()
operator|/
literal|2
argument_list|)
expr_stmt|;
name|ii
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|ii
operator|.
name|readByte
argument_list|()
expr_stmt|;
comment|// do a list of the files
name|store
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Reader Thread failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
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
try|try
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
name|writerIterations
condition|;
name|i
operator|++
control|)
block|{
name|String
name|dynamicFileName
init|=
literal|"dynamic"
operator|+
name|dynamicFilesCounter
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|IndexOutput
name|io
init|=
name|store
operator|.
name|directory
argument_list|()
operator|.
name|createOutput
argument_list|(
name|dynamicFileName
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|sizeCounter
init|=
literal|0
init|;
name|sizeCounter
operator|<
name|dynamicFileSize
operator|.
name|bytes
argument_list|()
condition|;
name|sizeCounter
operator|++
control|)
block|{
name|io
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
block|}
name|io
operator|.
name|close
argument_list|()
expr_stmt|;
name|store
operator|.
name|directory
argument_list|()
operator|.
name|deleteFile
argument_list|(
name|dynamicFileName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Writer thread failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
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
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|EMPTY_SETTINGS
decl_stmt|;
name|NodeEnvironment
name|nodeEnvironment
init|=
operator|new
name|NodeEnvironment
argument_list|(
name|settings
argument_list|,
name|environment
argument_list|)
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
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
literal|"ram"
decl_stmt|;
name|Store
name|store
decl_stmt|;
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"ram"
argument_list|)
condition|)
block|{
name|store
operator|=
operator|new
name|RamStore
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"simple-fs"
argument_list|)
condition|)
block|{
name|store
operator|=
operator|new
name|SimpleFsStore
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|,
operator|new
name|SimpleFsIndexStore
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|,
name|nodeEnvironment
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"mmap-fs"
argument_list|)
condition|)
block|{
name|store
operator|=
operator|new
name|NioFsStore
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|,
operator|new
name|NioFsIndexStore
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|,
name|nodeEnvironment
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"nio-fs"
argument_list|)
condition|)
block|{
name|store
operator|=
operator|new
name|MmapFsStore
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|,
operator|new
name|MmapFsIndexStore
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|,
name|nodeEnvironment
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"memory-direct"
argument_list|)
condition|)
block|{
name|Settings
name|byteBufferSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.store.bytebuffer.direct"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|store
operator|=
operator|new
name|ByteBufferStore
argument_list|(
name|shardId
argument_list|,
name|byteBufferSettings
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"memory-heap"
argument_list|)
condition|)
block|{
name|Settings
name|memorySettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|store
operator|=
operator|new
name|HeapStore
argument_list|(
name|shardId
argument_list|,
name|memorySettings
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No type store ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Using Store ["
operator|+
name|store
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|store
operator|.
name|deleteContent
argument_list|()
expr_stmt|;
name|SimpleStoreBenchmark
name|simpleStoreBenchmark
init|=
operator|new
name|SimpleStoreBenchmark
argument_list|(
name|store
argument_list|)
operator|.
name|numberStaticFiles
argument_list|(
literal|5
argument_list|)
operator|.
name|staticFileSize
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
operator|.
name|dynamicFileSize
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|1
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
operator|.
name|readerThreads
argument_list|(
literal|5
argument_list|)
operator|.
name|readerIterations
argument_list|(
literal|10
argument_list|)
operator|.
name|writerThreads
argument_list|(
literal|2
argument_list|)
operator|.
name|writerIterations
argument_list|(
literal|10
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|simpleStoreBenchmark
operator|.
name|run
argument_list|()
expr_stmt|;
name|store
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

