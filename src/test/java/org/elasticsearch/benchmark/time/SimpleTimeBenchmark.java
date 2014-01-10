begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.time
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|time
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SimpleTimeBenchmark
specifier|public
class|class
name|SimpleTimeBenchmark
block|{
DECL|field|USE_NANO_TIME
specifier|private
specifier|static
name|boolean
name|USE_NANO_TIME
init|=
literal|false
decl_stmt|;
DECL|field|NUMBER_OF_ITERATIONS
specifier|private
specifier|static
name|long
name|NUMBER_OF_ITERATIONS
init|=
literal|1000000
decl_stmt|;
DECL|field|NUMBER_OF_THREADS
specifier|private
specifier|static
name|int
name|NUMBER_OF_THREADS
init|=
literal|100
decl_stmt|;
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
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Running "
operator|+
name|NUMBER_OF_ITERATIONS
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBER_OF_ITERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Took "
operator|+
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|totalTime
argument_list|()
operator|+
literal|" TP Millis "
operator|+
operator|(
name|NUMBER_OF_ITERATIONS
operator|/
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Running using "
operator|+
name|NUMBER_OF_THREADS
operator|+
literal|" threads with "
operator|+
name|NUMBER_OF_ITERATIONS
operator|+
literal|" iterations"
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|NUMBER_OF_THREADS
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|NUMBER_OF_THREADS
index|]
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|USE_NANO_TIME
condition|)
block|{
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBER_OF_ITERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBER_OF_ITERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
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
literal|"Took "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|" TP Millis "
operator|+
operator|(
operator|(
name|NUMBER_OF_ITERATIONS
operator|*
name|NUMBER_OF_THREADS
operator|)
operator|/
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

