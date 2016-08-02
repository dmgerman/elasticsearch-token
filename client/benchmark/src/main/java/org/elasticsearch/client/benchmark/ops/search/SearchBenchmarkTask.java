begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.benchmark.ops.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|benchmark
operator|.
name|ops
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|benchmark
operator|.
name|BenchmarkTask
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|benchmark
operator|.
name|metrics
operator|.
name|Sample
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|benchmark
operator|.
name|metrics
operator|.
name|SampleRecorder
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
name|TimeUnit
import|;
end_import

begin_class
DECL|class|SearchBenchmarkTask
specifier|public
class|class
name|SearchBenchmarkTask
implements|implements
name|BenchmarkTask
block|{
DECL|field|MICROS_PER_SEC
specifier|private
specifier|static
specifier|final
name|long
name|MICROS_PER_SEC
init|=
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toMicros
argument_list|(
literal|1L
argument_list|)
decl_stmt|;
DECL|field|NANOS_PER_MICRO
specifier|private
specifier|static
specifier|final
name|long
name|NANOS_PER_MICRO
init|=
name|TimeUnit
operator|.
name|MICROSECONDS
operator|.
name|toNanos
argument_list|(
literal|1L
argument_list|)
decl_stmt|;
DECL|field|searchRequestExecutor
specifier|private
specifier|final
name|SearchRequestExecutor
name|searchRequestExecutor
decl_stmt|;
DECL|field|searchRequestBody
specifier|private
specifier|final
name|String
name|searchRequestBody
decl_stmt|;
DECL|field|iterations
specifier|private
specifier|final
name|int
name|iterations
decl_stmt|;
DECL|field|targetThroughput
specifier|private
specifier|final
name|int
name|targetThroughput
decl_stmt|;
DECL|field|sampleRecorder
specifier|private
name|SampleRecorder
name|sampleRecorder
decl_stmt|;
DECL|method|SearchBenchmarkTask
specifier|public
name|SearchBenchmarkTask
parameter_list|(
name|SearchRequestExecutor
name|searchRequestExecutor
parameter_list|,
name|String
name|body
parameter_list|,
name|int
name|iterations
parameter_list|,
name|int
name|targetThroughput
parameter_list|)
block|{
name|this
operator|.
name|searchRequestExecutor
operator|=
name|searchRequestExecutor
expr_stmt|;
name|this
operator|.
name|searchRequestBody
operator|=
name|body
expr_stmt|;
name|this
operator|.
name|iterations
operator|=
name|iterations
expr_stmt|;
name|this
operator|.
name|targetThroughput
operator|=
name|targetThroughput
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|(
name|SampleRecorder
name|sampleRecorder
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|sampleRecorder
operator|=
name|sampleRecorder
expr_stmt|;
block|}
annotation|@
name|Override
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
name|iteration
init|=
literal|0
init|;
name|iteration
operator|<
name|this
operator|.
name|iterations
condition|;
name|iteration
operator|++
control|)
block|{
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|boolean
name|success
init|=
name|searchRequestExecutor
operator|.
name|search
argument_list|(
name|searchRequestBody
argument_list|)
decl_stmt|;
specifier|final
name|long
name|stop
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|sampleRecorder
operator|.
name|addSample
argument_list|(
operator|new
name|Sample
argument_list|(
literal|"search"
argument_list|,
name|start
argument_list|,
name|stop
argument_list|,
name|success
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|waitTime
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|MICROS_PER_SEC
operator|/
name|targetThroughput
operator|-
operator|(
name|stop
operator|-
name|start
operator|)
operator|/
name|NANOS_PER_MICRO
argument_list|)
decl_stmt|;
if|if
condition|(
name|waitTime
operator|>
literal|0
condition|)
block|{
name|waitMicros
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|waitMicros
specifier|private
name|void
name|waitMicros
parameter_list|(
name|int
name|waitTime
parameter_list|)
throws|throws
name|InterruptedException
block|{
comment|// Thread.sleep() time is not very accurate (it's most of the time around 1 - 2 ms off)
comment|// we busy spin all the time to avoid introducing additional measurement artifacts (noticed 100% skew on 99.9th percentile)
comment|// this approach is not suitable for low throughput rates (in the second range) though
if|if
condition|(
name|waitTime
operator|>
literal|0
condition|)
block|{
name|long
name|end
init|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|+
literal|1000L
operator|*
name|waitTime
decl_stmt|;
while|while
condition|(
name|end
operator|>
name|System
operator|.
name|nanoTime
argument_list|()
condition|)
block|{
comment|// busy spin
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// no op
block|}
block|}
end_class

end_unit

