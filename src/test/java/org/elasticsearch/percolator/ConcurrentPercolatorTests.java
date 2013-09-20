begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
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
name|delete
operator|.
name|DeleteResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
operator|.
name|PercolateResponse
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
name|bytes
operator|.
name|BytesReference
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|AbstractIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|Semaphore
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
name|AtomicInteger
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
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|boolQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|termQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
operator|.
name|PercolatorTests
operator|.
name|convertFromTextArray
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|AbstractIntegrationTest
operator|.
name|ClusterScope
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticSearchAssertions
operator|.
name|assertNoFailures
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|numNodes
operator|=
literal|2
argument_list|)
DECL|class|ConcurrentPercolatorTests
specifier|public
class|class
name|ConcurrentPercolatorTests
extends|extends
name|AbstractIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testSimpleConcurrentPercolator
specifier|public
name|void
name|testSimpleConcurrentPercolator
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"index"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|BytesReference
name|onlyField1
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|1
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
specifier|final
name|BytesReference
name|onlyField2
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
specifier|final
name|BytesReference
name|bothFields
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|1
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
comment|// We need to index a document / define mapping, otherwise field1 doesn't get reconized as number field.
comment|// If we don't do this, then 'test2' percolate query gets parsed as a TermQuery and not a RangeQuery.
comment|// The percolate api doesn't parse the doc if no queries have registered, so it can't lazily create a mapping
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|1
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"_percolator"
argument_list|,
literal|"test1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"_percolator"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
specifier|final
name|CountDownLatch
name|start
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|counts
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|assertionFailure
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
literal|5
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
name|Runnable
name|r
init|=
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
try|try
block|{
name|start
operator|.
name|await
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|int
name|count
init|=
name|counts
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|count
operator|>
literal|10000
operator|)
condition|)
block|{
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|PercolateResponse
name|percolate
decl_stmt|;
if|if
condition|(
name|count
operator|%
literal|3
operator|==
literal|0
condition|)
block|{
name|percolate
operator|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|bothFields
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|percolate
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|convertFromTextArray
argument_list|(
name|percolate
operator|.
name|getMatches
argument_list|()
argument_list|,
literal|"index"
argument_list|)
argument_list|,
name|arrayContainingInAnyOrder
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|count
operator|%
literal|3
operator|==
literal|1
condition|)
block|{
name|percolate
operator|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|onlyField2
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|percolate
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|convertFromTextArray
argument_list|(
name|percolate
operator|.
name|getMatches
argument_list|()
argument_list|,
literal|"index"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"test1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|percolate
operator|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|onlyField1
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|percolate
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|convertFromTextArray
argument_list|(
name|percolate
operator|.
name|getMatches
argument_list|()
argument_list|,
literal|"index"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"test2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
name|assertionFailure
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|start
operator|.
name|countDown
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
name|join
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|assertionFailure
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testConcurrentAddingAndPercolating
specifier|public
name|void
name|testConcurrentAddingAndPercolating
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"index"
argument_list|)
operator|.
name|setSettings
argument_list|(
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
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|int
name|numIndexThreads
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|numPercolateThreads
init|=
literal|6
decl_stmt|;
specifier|final
name|int
name|numPercolatorOperationsPerThread
init|=
literal|1000
decl_stmt|;
specifier|final
name|AtomicBoolean
name|assertionFailure
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|start
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|runningPercolateThreads
init|=
operator|new
name|AtomicInteger
argument_list|(
name|numPercolateThreads
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|type1
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|type2
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|type3
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|idGen
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|Thread
index|[]
name|indexThreads
init|=
operator|new
name|Thread
index|[
name|numIndexThreads
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
name|numIndexThreads
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|getRandom
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|Runnable
name|r
init|=
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
try|try
block|{
name|XContentBuilder
name|onlyField1
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentBuilder
name|onlyField2
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentBuilder
name|field1And2
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|boolQuery
argument_list|()
operator|.
name|must
argument_list|(
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|must
argument_list|(
name|termQuery
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|start
operator|.
name|await
argument_list|()
expr_stmt|;
while|while
condition|(
name|runningPercolateThreads
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|int
name|x
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|idGen
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
decl_stmt|;
name|IndexResponse
name|response
decl_stmt|;
switch|switch
condition|(
name|x
condition|)
block|{
case|case
literal|0
case|:
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"_percolator"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
name|onlyField1
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|type1
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"_percolator"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
name|onlyField2
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|type2
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"_percolator"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
name|field1And2
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|type3
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Illegal x="
operator|+
name|x
argument_list|)
throw|;
block|}
name|assertThat
argument_list|(
name|response
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getVersion
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|assertionFailure
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|logger
operator|.
name|error
argument_list|(
literal|"Error in indexing thread..."
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|indexThreads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|indexThreads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|Thread
index|[]
name|percolateThreads
init|=
operator|new
name|Thread
index|[
name|numPercolateThreads
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
name|numPercolateThreads
condition|;
name|i
operator|++
control|)
block|{
name|Runnable
name|r
init|=
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
try|try
block|{
name|XContentBuilder
name|onlyField1Doc
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentBuilder
name|onlyField2Doc
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentBuilder
name|field1AndField2Doc
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|Random
name|random
init|=
name|getRandom
argument_list|()
decl_stmt|;
name|start
operator|.
name|await
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|counter
init|=
literal|0
init|;
name|counter
operator|<
name|numPercolatorOperationsPerThread
condition|;
name|counter
operator|++
control|)
block|{
name|int
name|x
init|=
name|random
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|int
name|atLeastExpected
decl_stmt|;
name|PercolateResponse
name|response
decl_stmt|;
switch|switch
condition|(
name|x
condition|)
block|{
case|case
literal|0
case|:
name|atLeastExpected
operator|=
name|type1
operator|.
name|get
argument_list|()
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|onlyField1Doc
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|getTotalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|atLeastExpected
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|atLeastExpected
operator|=
name|type2
operator|.
name|get
argument_list|()
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|onlyField2Doc
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|getTotalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|atLeastExpected
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|atLeastExpected
operator|=
name|type3
operator|.
name|get
argument_list|()
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|field1AndField2Doc
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|getTotalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|atLeastExpected
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|assertionFailure
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|logger
operator|.
name|error
argument_list|(
literal|"Error in percolate thread..."
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|runningPercolateThreads
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|percolateThreads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|percolateThreads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|start
operator|.
name|countDown
argument_list|()
expr_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|indexThreads
control|)
block|{
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|thread
range|:
name|percolateThreads
control|)
block|{
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|assertionFailure
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testConcurrentAddingAndRemovingWhilePercolating
specifier|public
name|void
name|testConcurrentAddingAndRemovingWhilePercolating
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"index"
argument_list|)
operator|.
name|setSettings
argument_list|(
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
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|int
name|numIndexThreads
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|numberPercolateOperation
init|=
literal|100
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|exceptionHolder
init|=
operator|new
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|idGen
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|liveIds
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentSet
argument_list|()
decl_stmt|;
specifier|final
name|AtomicBoolean
name|run
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|indexThreads
init|=
operator|new
name|Thread
index|[
name|numIndexThreads
index|]
decl_stmt|;
specifier|final
name|Semaphore
name|semaphore
init|=
operator|new
name|Semaphore
argument_list|(
name|numIndexThreads
argument_list|,
literal|true
argument_list|)
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
name|indexThreads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Runnable
name|r
init|=
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
try|try
block|{
name|XContentBuilder
name|doc
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|outer
label|:
while|while
condition|(
name|run
operator|.
name|get
argument_list|()
condition|)
block|{
name|semaphore
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|liveIds
operator|.
name|isEmpty
argument_list|()
operator|&&
name|getRandom
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|<
literal|19
condition|)
block|{
name|String
name|id
decl_stmt|;
do|do
block|{
if|if
condition|(
name|liveIds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue
name|outer
continue|;
block|}
name|id
operator|=
name|Integer
operator|.
name|toString
argument_list|(
name|randomInt
argument_list|(
name|idGen
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|liveIds
operator|.
name|remove
argument_list|(
name|id
argument_list|)
condition|)
do|;
name|DeleteResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"index"
argument_list|,
literal|"_percolator"
argument_list|,
name|id
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"doc["
operator|+
name|id
operator|+
literal|"] should have been deleted, but isn't"
argument_list|,
name|response
operator|.
name|isNotFound
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|idGen
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
decl_stmt|;
name|IndexResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"_percolator"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
name|doc
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|liveIds
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isCreated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// We only add new docs
name|assertThat
argument_list|(
name|response
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|semaphore
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|iex
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"indexing thread was interrupted..."
argument_list|,
name|iex
argument_list|)
expr_stmt|;
name|run
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|run
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|exceptionHolder
operator|.
name|set
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|logger
operator|.
name|error
argument_list|(
literal|"Error in indexing thread..."
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|indexThreads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|indexThreads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|XContentBuilder
name|percolateDoc
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|counter
init|=
literal|0
init|;
name|counter
operator|<
name|numberPercolateOperation
condition|;
name|counter
operator|++
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|semaphore
operator|.
name|acquire
argument_list|(
name|numIndexThreads
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|run
operator|.
name|get
argument_list|()
condition|)
block|{
break|break;
block|}
name|int
name|atLeastExpected
init|=
name|liveIds
operator|.
name|size
argument_list|()
decl_stmt|;
name|PercolateResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|percolateDoc
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getShardFailures
argument_list|()
argument_list|,
name|emptyArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|getTotalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|atLeastExpected
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|semaphore
operator|.
name|release
argument_list|(
name|numIndexThreads
argument_list|)
expr_stmt|;
block|}
block|}
name|run
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|indexThreads
control|)
block|{
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
literal|"exceptionHolder should have been empty, but holds: "
operator|+
name|exceptionHolder
operator|.
name|toString
argument_list|()
argument_list|,
name|exceptionHolder
operator|.
name|get
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

