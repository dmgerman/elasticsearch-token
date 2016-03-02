begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
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
name|Priority
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
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
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThanOrEqualTo
import|;
end_import

begin_class
DECL|class|PrioritizedRunnableTests
specifier|public
class|class
name|PrioritizedRunnableTests
extends|extends
name|ESTestCase
block|{
comment|// test unit conversion with a controlled clock
DECL|method|testGetAgeInMillis
specifier|public
name|void
name|testGetAgeInMillis
parameter_list|()
throws|throws
name|Exception
block|{
name|AtomicLong
name|time
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|PrioritizedRunnable
name|runnable
init|=
operator|new
name|PrioritizedRunnable
argument_list|(
name|Priority
operator|.
name|NORMAL
argument_list|,
name|time
operator|::
name|get
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{             }
block|}
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|runnable
operator|.
name|getAgeInMillis
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|milliseconds
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|256
argument_list|)
decl_stmt|;
name|time
operator|.
name|addAndGet
argument_list|(
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|convert
argument_list|(
name|milliseconds
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|milliseconds
argument_list|,
name|runnable
operator|.
name|getAgeInMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// test age advances with System#nanoTime
DECL|method|testGetAgeInMillisWithRealClock
specifier|public
name|void
name|testGetAgeInMillisWithRealClock
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|PrioritizedRunnable
name|runnable
init|=
operator|new
name|PrioritizedRunnable
argument_list|(
name|Priority
operator|.
name|NORMAL
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{             }
block|}
decl_stmt|;
name|long
name|elapsed
init|=
name|spinForAtLeastOneMillisecond
argument_list|()
decl_stmt|;
comment|// creation happened before start, so age will be at least as
comment|// large as elapsed
name|assertThat
argument_list|(
name|runnable
operator|.
name|getAgeInMillis
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|convert
argument_list|(
name|elapsed
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

