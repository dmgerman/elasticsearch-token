begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indexing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indexing
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
name|bulk
operator|.
name|BulkResponse
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
name|IndexRequestBuilder
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
name|index
operator|.
name|VersionType
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
name|ElasticsearchIntegrationTest
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|concurrent
operator|.
name|Callable
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
name|ExecutorService
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
name|Executors
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
name|AtomicIntegerArray
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
name|ElasticsearchAssertions
operator|.
name|assertHitCount
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
name|equalTo
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
name|lessThanOrEqualTo
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IndexActionTests
specifier|public
class|class
name|IndexActionTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
comment|/**      * This test tries to simulate load while creating an index and indexing documents      * while the index is being created.      */
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"action.search:TRACE,indices.recovery:TRACE,index.shard.service:TRACE"
argument_list|)
DECL|method|testAutoGenerateIdNoDuplicates
specifier|public
name|void
name|testAutoGenerateIdNoDuplicates
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numberOfIterations
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|50
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
name|numberOfIterations
condition|;
name|i
operator|++
control|)
block|{
name|Throwable
name|firstError
init|=
literal|null
decl_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|int
name|numOfDocs
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"indexing [{}] docs"
argument_list|,
name|numOfDocs
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|builders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numOfDocs
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numOfDocs
condition|;
name|j
operator|++
control|)
block|{
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builders
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"verifying indexed content"
argument_list|)
expr_stmt|;
name|int
name|numOfChecks
init|=
name|randomIntBetween
argument_list|(
literal|8
argument_list|,
literal|12
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numOfChecks
condition|;
name|j
operator|++
control|)
block|{
try|try
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"running search with all types"
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|numOfDocs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"search for all docs types failed"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|firstError
operator|==
literal|null
condition|)
block|{
name|firstError
operator|=
name|t
expr_stmt|;
block|}
block|}
try|try
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"running search with a specific type"
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|numOfDocs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"search for all docs of a specific type failed"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|firstError
operator|==
literal|null
condition|)
block|{
name|firstError
operator|=
name|t
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|firstError
operator|!=
literal|null
condition|)
block|{
name|fail
argument_list|(
name|firstError
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|cluster
argument_list|()
operator|.
name|wipeIndices
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testCreatedFlag
specifier|public
name|void
name|testCreatedFlag
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndexResponse
name|indexResponse
init|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|indexResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|indexResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCreatedFlagWithFlush
specifier|public
name|void
name|testCreatedFlagWithFlush
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndexResponse
name|indexResponse
init|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|flush
argument_list|()
expr_stmt|;
name|indexResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCreatedFlagParallelExecution
specifier|public
name|void
name|testCreatedFlagParallelExecution
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|int
name|threadCount
init|=
literal|20
decl_stmt|;
specifier|final
name|int
name|docCount
init|=
literal|300
decl_stmt|;
name|int
name|taskCount
init|=
name|docCount
operator|*
name|threadCount
decl_stmt|;
specifier|final
name|AtomicIntegerArray
name|createdCounts
init|=
operator|new
name|AtomicIntegerArray
argument_list|(
name|docCount
argument_list|)
decl_stmt|;
name|ExecutorService
name|threadPool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threadCount
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|taskCount
argument_list|)
decl_stmt|;
specifier|final
name|Random
name|random
init|=
name|getRandom
argument_list|()
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
name|taskCount
condition|;
name|i
operator|++
control|)
block|{
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|docId
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|docCount
argument_list|)
decl_stmt|;
name|IndexResponse
name|indexResponse
init|=
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|docId
argument_list|)
argument_list|,
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
condition|)
name|createdCounts
operator|.
name|incrementAndGet
argument_list|(
name|docId
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|threadPool
operator|.
name|invokeAll
argument_list|(
name|tasks
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
name|docCount
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|createdCounts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testCreatedFlagWithExternalVersioning
specifier|public
name|void
name|testCreatedFlagWithExternalVersioning
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndexResponse
name|indexResponse
init|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|123
argument_list|)
operator|.
name|setVersionType
argument_list|(
name|VersionType
operator|.
name|EXTERNAL
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCreateFlagWithBulk
specifier|public
name|void
name|testCreateFlagWithBulk
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|BulkResponse
name|bulkResponse
init|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
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
name|bulkResponse
operator|.
name|hasFailures
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|IndexResponse
name|indexResponse
init|=
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

