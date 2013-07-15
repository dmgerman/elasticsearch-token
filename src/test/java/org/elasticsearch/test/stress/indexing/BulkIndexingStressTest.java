begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.stress.indexing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|stress
operator|.
name|indexing
package|;
end_package

begin_import
import|import
name|jsr166y
operator|.
name|ThreadLocalRandom
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
name|bulk
operator|.
name|BulkItemResponse
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
name|bulk
operator|.
name|BulkRequestBuilder
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
name|client
operator|.
name|Client
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
name|Requests
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|BulkIndexingStressTest
specifier|public
class|class
name|BulkIndexingStressTest
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
block|{
specifier|final
name|int
name|NUMBER_OF_NODES
init|=
literal|4
decl_stmt|;
specifier|final
name|int
name|NUMBER_OF_INDICES
init|=
literal|600
decl_stmt|;
specifier|final
name|int
name|BATCH
init|=
literal|300
decl_stmt|;
specifier|final
name|Settings
name|nodeSettings
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
name|build
argument_list|()
decl_stmt|;
comment|//            ESLogger logger = Loggers.getLogger("org.elasticsearch");
comment|//            logger.setLevel("DEBUG");
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
name|nodeSettings
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
block|}
name|Client
name|client
init|=
name|nodes
operator|.
name|length
operator|==
literal|1
condition|?
name|nodes
index|[
literal|0
index|]
operator|.
name|client
argument_list|()
else|:
name|nodes
index|[
literal|1
index|]
operator|.
name|client
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|BulkRequestBuilder
name|bulkRequest
init|=
name|client
operator|.
name|prepareBulk
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
name|BATCH
condition|;
name|i
operator|++
control|)
block|{
name|bulkRequest
operator|.
name|add
argument_list|(
name|Requests
operator|.
name|indexRequest
argument_list|(
literal|"test"
operator|+
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
name|NUMBER_OF_INDICES
argument_list|)
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|source
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BulkResponse
name|bulkResponse
init|=
name|bulkRequest
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|bulkResponse
operator|.
name|hasFailures
argument_list|()
condition|)
block|{
for|for
control|(
name|BulkItemResponse
name|item
range|:
name|bulkResponse
control|)
block|{
if|if
condition|(
name|item
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"failed response:"
operator|+
name|item
operator|.
name|getFailureMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed responses"
argument_list|)
throw|;
block|}
empty_stmt|;
block|}
block|}
block|}
end_class

end_unit

