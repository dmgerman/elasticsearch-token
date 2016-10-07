begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.noop.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|noop
operator|.
name|action
operator|.
name|bulk
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
name|DocWriteResponse
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
name|DocumentRequest
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
name|BulkRequest
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
name|BulkShardRequest
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
name|support
operator|.
name|ActiveShardCount
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
name|update
operator|.
name|UpdateResponse
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
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|Strings
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
name|inject
operator|.
name|Inject
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
name|rest
operator|.
name|BaseRestHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|BytesRestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|RestBuilderListener
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|POST
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|PUT
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_class
DECL|class|RestNoopBulkAction
specifier|public
class|class
name|RestNoopBulkAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestNoopBulkAction
specifier|public
name|RestNoopBulkAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_noop_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/_noop_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/_noop_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/{index}/_noop_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_noop_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/{index}/{type}/_noop_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
name|BulkRequest
name|bulkRequest
init|=
name|Requests
operator|.
name|bulkRequest
argument_list|()
decl_stmt|;
name|String
name|defaultIndex
init|=
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
name|String
name|defaultType
init|=
name|request
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
name|String
name|defaultRouting
init|=
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
decl_stmt|;
name|String
name|fieldsParam
init|=
name|request
operator|.
name|param
argument_list|(
literal|"fields"
argument_list|)
decl_stmt|;
name|String
name|defaultPipeline
init|=
name|request
operator|.
name|param
argument_list|(
literal|"pipeline"
argument_list|)
decl_stmt|;
name|String
index|[]
name|defaultFields
init|=
name|fieldsParam
operator|!=
literal|null
condition|?
name|Strings
operator|.
name|commaDelimitedListToStringArray
argument_list|(
name|fieldsParam
argument_list|)
else|:
literal|null
decl_stmt|;
name|String
name|waitForActiveShards
init|=
name|request
operator|.
name|param
argument_list|(
literal|"wait_for_active_shards"
argument_list|)
decl_stmt|;
if|if
condition|(
name|waitForActiveShards
operator|!=
literal|null
condition|)
block|{
name|bulkRequest
operator|.
name|waitForActiveShards
argument_list|(
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
name|waitForActiveShards
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|bulkRequest
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|BulkShardRequest
operator|.
name|DEFAULT_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|bulkRequest
operator|.
name|setRefreshPolicy
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"refresh"
argument_list|)
argument_list|)
expr_stmt|;
name|bulkRequest
operator|.
name|add
argument_list|(
name|request
operator|.
name|content
argument_list|()
argument_list|,
name|defaultIndex
argument_list|,
name|defaultType
argument_list|,
name|defaultRouting
argument_list|,
name|defaultFields
argument_list|,
literal|null
argument_list|,
name|defaultPipeline
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// short circuit the call to the transport layer
return|return
name|channel
lambda|->
block|{
name|BulkRestBuilderListener
name|listener
init|=
operator|new
name|BulkRestBuilderListener
argument_list|(
name|channel
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|bulkRequest
argument_list|)
expr_stmt|;
block|}
return|;
block|}
DECL|class|BulkRestBuilderListener
specifier|private
specifier|static
class|class
name|BulkRestBuilderListener
extends|extends
name|RestBuilderListener
argument_list|<
name|BulkRequest
argument_list|>
block|{
DECL|field|ITEM_RESPONSE
specifier|private
specifier|final
name|BulkItemResponse
name|ITEM_RESPONSE
init|=
operator|new
name|BulkItemResponse
argument_list|(
literal|1
argument_list|,
name|DocumentRequest
operator|.
name|OpType
operator|.
name|UPDATE
argument_list|,
operator|new
name|UpdateResponse
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"mock"
argument_list|,
literal|""
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|"mock_type"
argument_list|,
literal|"1"
argument_list|,
literal|1L
argument_list|,
name|DocWriteResponse
operator|.
name|Result
operator|.
name|CREATED
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|request
specifier|private
specifier|final
name|RestRequest
name|request
decl_stmt|;
DECL|method|BulkRestBuilderListener
specifier|public
name|BulkRestBuilderListener
parameter_list|(
name|RestChannel
name|channel
parameter_list|,
name|RestRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|channel
argument_list|)
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|buildResponse
specifier|public
name|RestResponse
name|buildResponse
parameter_list|(
name|BulkRequest
name|bulkRequest
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|Exception
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOOK
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ERRORS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|ITEMS
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|bulkRequest
operator|.
name|numberOfActions
argument_list|()
condition|;
name|idx
operator|++
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ITEM_RESPONSE
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|builder
argument_list|)
return|;
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|ITEMS
specifier|static
specifier|final
name|String
name|ITEMS
init|=
literal|"items"
decl_stmt|;
DECL|field|ERRORS
specifier|static
specifier|final
name|String
name|ERRORS
init|=
literal|"errors"
decl_stmt|;
DECL|field|TOOK
specifier|static
specifier|final
name|String
name|TOOK
init|=
literal|"took"
decl_stmt|;
block|}
block|}
end_class

end_unit

