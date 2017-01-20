begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.document
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|document
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
name|logging
operator|.
name|DeprecationLogger
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
name|logging
operator|.
name|Loggers
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|FetchSourceContext
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

begin_comment
comment|/**  *<pre>  * { "index" : { "_index" : "test", "_type" : "type1", "_id" : "1" }  * { "type1" : { "field1" : "value1" } }  * { "delete" : { "_index" : "test", "_type" : "type1", "_id" : "2" } }  * { "create" : { "_index" : "test", "_type" : "type1", "_id" : "1" }  * { "type1" : { "field1" : "value1" } }  *</pre>  */
end_comment

begin_class
DECL|class|RestBulkAction
specifier|public
class|class
name|RestBulkAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|DEPRECATION_LOGGER
specifier|private
specifier|static
specifier|final
name|DeprecationLogger
name|DEPRECATION_LOGGER
init|=
operator|new
name|DeprecationLogger
argument_list|(
name|Loggers
operator|.
name|getLogger
argument_list|(
name|RestBulkAction
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|allowExplicitIndex
specifier|private
specifier|final
name|boolean
name|allowExplicitIndex
decl_stmt|;
DECL|method|RestBulkAction
specifier|public
name|RestBulkAction
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
literal|"/_bulk"
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
literal|"/_bulk"
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
literal|"/{index}/_bulk"
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
literal|"/{index}/_bulk"
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
literal|"/{index}/{type}/_bulk"
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
literal|"/{index}/{type}/_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|allowExplicitIndex
operator|=
name|MULTI_ALLOW_EXPLICIT_INDEX
operator|.
name|get
argument_list|(
name|settings
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
name|FetchSourceContext
name|defaultFetchSourceContext
init|=
name|FetchSourceContext
operator|.
name|parseFromRestRequest
argument_list|(
name|request
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
if|if
condition|(
name|fieldsParam
operator|!=
literal|null
condition|)
block|{
name|DEPRECATION_LOGGER
operator|.
name|deprecated
argument_list|(
literal|"Deprecated field [fields] used, expected [_source] instead"
argument_list|)
expr_stmt|;
block|}
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
name|defaultFetchSourceContext
argument_list|,
name|defaultPipeline
argument_list|,
literal|null
argument_list|,
name|allowExplicitIndex
argument_list|)
expr_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|bulk
argument_list|(
name|bulkRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|BulkResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public RestResponse buildResponse(BulkResponse response
operator|,
name|XContentBuilder
name|builder
block|)
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
name|response
operator|.
name|getTookInMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|response
operator|.
name|getIngestTookInMillis
argument_list|()
operator|!=
name|BulkResponse
operator|.
name|NO_INGEST_TOOK
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|INGEST_TOOK
argument_list|,
name|response
operator|.
name|getIngestTookInMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ERRORS
argument_list|,
name|response
operator|.
name|hasFailures
argument_list|()
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
name|BulkItemResponse
name|itemResponse
range|:
name|response
control|)
block|{
name|itemResponse
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
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
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_class
unit|}      static
DECL|class|Fields
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
DECL|field|INGEST_TOOK
specifier|static
specifier|final
name|String
name|INGEST_TOOK
init|=
literal|"ingest_took"
decl_stmt|;
block|}
end_class

unit|}
end_unit

