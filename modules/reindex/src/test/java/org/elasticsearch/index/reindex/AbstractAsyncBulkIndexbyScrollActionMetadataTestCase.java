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
name|index
operator|.
name|IndexRequest
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
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueMillis
import|;
end_import

begin_class
DECL|class|AbstractAsyncBulkIndexbyScrollActionMetadataTestCase
specifier|public
specifier|abstract
class|class
name|AbstractAsyncBulkIndexbyScrollActionMetadataTestCase
parameter_list|<
name|Request
extends|extends
name|AbstractBulkIndexByScrollRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|BulkIndexByScrollResponse
parameter_list|>
extends|extends
name|AbstractAsyncBulkIndexByScrollActionTestCase
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|method|doc
specifier|protected
name|ScrollableHitSource
operator|.
name|BasicHit
name|doc
parameter_list|()
block|{
return|return
operator|new
name|ScrollableHitSource
operator|.
name|BasicHit
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
literal|0
argument_list|)
return|;
block|}
DECL|method|testTimestampIsCopied
specifier|public
name|void
name|testTimestampIsCopied
parameter_list|()
block|{
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|action
argument_list|()
operator|.
name|copyMetadata
argument_list|(
name|AbstractAsyncBulkIndexByScrollAction
operator|.
name|wrap
argument_list|(
name|index
argument_list|)
argument_list|,
name|doc
argument_list|()
operator|.
name|setTimestamp
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"10"
argument_list|,
name|index
operator|.
name|timestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testTTL
specifier|public
name|void
name|testTTL
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|action
argument_list|()
operator|.
name|copyMetadata
argument_list|(
name|AbstractAsyncBulkIndexByScrollAction
operator|.
name|wrap
argument_list|(
name|index
argument_list|)
argument_list|,
name|doc
argument_list|()
operator|.
name|setTTL
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
argument_list|,
name|index
operator|.
name|ttl
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|action
specifier|protected
specifier|abstract
name|AbstractAsyncBulkIndexByScrollAction
argument_list|<
name|Request
argument_list|>
name|action
parameter_list|()
function_decl|;
block|}
end_class

end_unit

