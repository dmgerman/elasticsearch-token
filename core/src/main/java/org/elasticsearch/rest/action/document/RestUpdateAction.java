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
name|index
operator|.
name|IndexRequest
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
name|UpdateRequest
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
name|action
operator|.
name|RestActions
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
name|RestStatusToXContentListener
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

begin_class
DECL|class|RestUpdateAction
specifier|public
class|class
name|RestUpdateAction
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
name|RestUpdateAction
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|RestUpdateAction
specifier|public
name|RestUpdateAction
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
literal|"/{index}/{type}/{id}/_update"
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
name|UpdateRequest
name|updateRequest
init|=
operator|new
name|UpdateRequest
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
name|updateRequest
operator|.
name|routing
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|parent
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|updateRequest
operator|.
name|timeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
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
name|updateRequest
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
name|updateRequest
operator|.
name|docAsUpsert
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"doc_as_upsert"
argument_list|,
name|updateRequest
operator|.
name|docAsUpsert
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|FetchSourceContext
name|fetchSourceContext
init|=
name|FetchSourceContext
operator|.
name|parseFromRestRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|String
name|sField
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
name|sField
operator|!=
literal|null
operator|&&
name|fetchSourceContext
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[fields] and [_source] cannot be used in the same request"
argument_list|)
throw|;
block|}
if|if
condition|(
name|sField
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
name|String
index|[]
name|sFields
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|sField
argument_list|)
decl_stmt|;
name|updateRequest
operator|.
name|fields
argument_list|(
name|sFields
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fetchSourceContext
operator|!=
literal|null
condition|)
block|{
name|updateRequest
operator|.
name|fetchSource
argument_list|(
name|fetchSourceContext
argument_list|)
expr_stmt|;
block|}
name|updateRequest
operator|.
name|retryOnConflict
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"retry_on_conflict"
argument_list|,
name|updateRequest
operator|.
name|retryOnConflict
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|version
argument_list|(
name|RestActions
operator|.
name|parseVersion
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"version_type"
argument_list|)
argument_list|,
name|updateRequest
operator|.
name|versionType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|applyContentParser
argument_list|(
name|parser
lambda|->
block|{
name|updateRequest
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|IndexRequest
name|upsertRequest
init|=
name|updateRequest
operator|.
name|upsertRequest
argument_list|()
decl_stmt|;
if|if
condition|(
name|upsertRequest
operator|!=
literal|null
condition|)
block|{
name|upsertRequest
operator|.
name|routing
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|upsertRequest
operator|.
name|parent
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
expr_stmt|;
name|upsertRequest
operator|.
name|version
argument_list|(
name|RestActions
operator|.
name|parseVersion
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
name|upsertRequest
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"version_type"
argument_list|)
argument_list|,
name|upsertRequest
operator|.
name|versionType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|IndexRequest
name|doc
init|=
name|updateRequest
operator|.
name|doc
argument_list|()
decl_stmt|;
if|if
condition|(
name|doc
operator|!=
literal|null
condition|)
block|{
name|doc
operator|.
name|routing
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|parent
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
expr_stmt|;
comment|// order is important, set it after routing, so it will set the routing
name|doc
operator|.
name|version
argument_list|(
name|RestActions
operator|.
name|parseVersion
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"version_type"
argument_list|)
argument_list|,
name|doc
operator|.
name|versionType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|update
argument_list|(
name|updateRequest
argument_list|,
operator|new
name|RestStatusToXContentListener
argument_list|<>
argument_list|(
name|channel
argument_list|,
name|r
lambda|->
name|r
operator|.
name|getLocation
argument_list|(
name|updateRequest
operator|.
name|routing
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

