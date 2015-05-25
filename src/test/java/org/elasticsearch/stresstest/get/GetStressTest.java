begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.stresstest.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|stresstest
operator|.
name|get
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
operator|.
name|GetResponse
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
name|ImmutableSettings
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
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
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
name|ThreadLocalRandom
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
name|AtomicBoolean
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

begin_class
DECL|class|GetStressTest
specifier|public
class|class
name|GetStressTest
block|{
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
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|int
name|NUMBER_OF_NODES
init|=
literal|2
decl_stmt|;
specifier|final
name|int
name|NUMBER_OF_THREADS
init|=
literal|50
decl_stmt|;
specifier|final
name|TimeValue
name|TEST_TIME
init|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
literal|"10m"
argument_list|,
literal|null
argument_list|,
literal|"TEST_TIME"
argument_list|)
decl_stmt|;
name|Node
index|[]
name|nodes
init|=
operator|new
name|Node
index|[
name|NUMBER_OF_NODES
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
name|nodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
block|}
specifier|final
name|Node
name|client
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|client
argument_list|(
literal|true
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|client
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
specifier|final
name|AtomicBoolean
name|done
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|final
name|AtomicLong
name|idGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicLong
name|counter
init|=
operator|new
name|AtomicLong
argument_list|()
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
name|ThreadLocalRandom
name|random
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|done
operator|.
name|get
argument_list|()
condition|)
block|{
name|String
name|id
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|idGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
name|random
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|GetResponse
name|getResponse
init|=
name|client
operator|.
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|id
argument_list|)
comment|//.setFields(Strings.EMPTY_ARRAY)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Failed to find "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
name|long
name|count
init|=
name|counter
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|count
operator|%
literal|10000
operator|)
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Executed "
operator|+
name|count
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
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
name|Thread
operator|.
name|sleep
argument_list|(
name|TEST_TIME
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test done."
argument_list|)
expr_stmt|;
name|done
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

