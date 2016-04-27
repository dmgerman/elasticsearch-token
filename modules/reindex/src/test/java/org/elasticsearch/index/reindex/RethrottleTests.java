begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
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
name|ListenableActionFuture
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|tasks
operator|.
name|list
operator|.
name|ListTasksResponse
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
name|hasSize
import|;
end_import

begin_comment
comment|/**  * Tests that you can set requests_per_second over the Java API and that you can rethrottle running requests. There are REST tests for this  * too but this is the only place that tests running against multiple nodes so it is the only integration tests that checks for  * serialization.  */
end_comment

begin_class
DECL|class|RethrottleTests
specifier|public
class|class
name|RethrottleTests
extends|extends
name|ReindexTestCase
block|{
DECL|method|testReindex
specifier|public
name|void
name|testReindex
parameter_list|()
throws|throws
name|Exception
block|{
name|testCase
argument_list|(
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
literal|"test"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|)
argument_list|,
name|ReindexAction
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpdateByQuery
specifier|public
name|void
name|testUpdateByQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|testCase
argument_list|(
name|updateByQuery
argument_list|()
operator|.
name|source
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|UpdateByQueryAction
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
DECL|method|testCase
specifier|private
name|void
name|testCase
parameter_list|(
name|AbstractBulkIndexByScrollRequestBuilder
argument_list|<
name|?
argument_list|,
name|?
extends|extends
name|BulkIndexByScrollResponse
argument_list|,
name|?
argument_list|>
name|request
parameter_list|,
name|String
name|actionName
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Use a single shard so the reindex has to happen in multiple batches
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
name|setSettings
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Start a request that will never finish unless we rethrottle it
name|request
operator|.
name|setRequestsPerSecond
argument_list|(
literal|.000001f
argument_list|)
expr_stmt|;
comment|// Throttle forever
name|request
operator|.
name|source
argument_list|()
operator|.
name|setSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// Make sure we use multiple batches
name|ListenableActionFuture
argument_list|<
name|?
extends|extends
name|BulkIndexByScrollResponse
argument_list|>
name|responseListener
init|=
name|request
operator|.
name|execute
argument_list|()
decl_stmt|;
comment|// Now rethrottle it so it'll finish
name|ListTasksResponse
name|rethrottleResponse
init|=
name|rethrottle
argument_list|()
operator|.
name|setActions
argument_list|(
name|actionName
argument_list|)
operator|.
name|setRequestsPerSecond
argument_list|(
name|Float
operator|.
name|POSITIVE_INFINITY
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|rethrottleResponse
operator|.
name|getTasks
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now the response should come back quickly because we've rethrottled the request
name|BulkIndexByScrollResponse
name|response
init|=
name|responseListener
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Batches didn't match, this may invalidate the test as throttling is done between batches"
argument_list|,
literal|3
argument_list|,
name|response
operator|.
name|getBatches
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

