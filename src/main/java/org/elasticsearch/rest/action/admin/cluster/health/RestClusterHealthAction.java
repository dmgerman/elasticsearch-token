begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.cluster.health
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|health
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
name|ActionListener
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
name|health
operator|.
name|*
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilderString
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
name|*
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
name|support
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
name|support
operator|.
name|RestXContentBuilder
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
name|client
operator|.
name|Requests
operator|.
name|clusterHealthRequest
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
name|PRECONDITION_FAILED
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestClusterHealthAction
specifier|public
class|class
name|RestClusterHealthAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestClusterHealthAction
specifier|public
name|RestClusterHealthAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|GET
argument_list|,
literal|"/_cluster/health"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|GET
argument_list|,
literal|"/_cluster/health/{index}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|)
block|{
name|ClusterHealthRequest
name|clusterHealthRequest
init|=
name|clusterHealthRequest
argument_list|(
name|RestActions
operator|.
name|splitIndices
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|level
init|=
literal|0
decl_stmt|;
try|try
block|{
name|clusterHealthRequest
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"master_timeout"
argument_list|,
name|clusterHealthRequest
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterHealthRequest
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|clusterHealthRequest
operator|.
name|timeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|waitForStatus
init|=
name|request
operator|.
name|param
argument_list|(
literal|"wait_for_status"
argument_list|)
decl_stmt|;
if|if
condition|(
name|waitForStatus
operator|!=
literal|null
condition|)
block|{
name|clusterHealthRequest
operator|.
name|waitForStatus
argument_list|(
name|ClusterHealthStatus
operator|.
name|valueOf
argument_list|(
name|waitForStatus
operator|.
name|toUpperCase
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|clusterHealthRequest
operator|.
name|waitForRelocatingShards
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"wait_for_relocating_shards"
argument_list|,
name|clusterHealthRequest
operator|.
name|waitForRelocatingShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterHealthRequest
operator|.
name|waitForActiveShards
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"wait_for_active_shards"
argument_list|,
name|clusterHealthRequest
operator|.
name|waitForActiveShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterHealthRequest
operator|.
name|waitForNodes
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"wait_for_nodes"
argument_list|,
name|clusterHealthRequest
operator|.
name|waitForNodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|sLevel
init|=
name|request
operator|.
name|param
argument_list|(
literal|"level"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sLevel
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
literal|"cluster"
operator|.
name|equals
argument_list|(
literal|"sLevel"
argument_list|)
condition|)
block|{
name|level
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"indices"
operator|.
name|equals
argument_list|(
name|sLevel
argument_list|)
condition|)
block|{
name|level
operator|=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"shards"
operator|.
name|equals
argument_list|(
name|sLevel
argument_list|)
condition|)
block|{
name|level
operator|=
literal|2
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|RestXContentBuilder
operator|.
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|PRECONDITION_FAILED
argument_list|,
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"error"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
specifier|final
name|int
name|fLevel
init|=
name|level
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealthRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ClusterHealthResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClusterHealthResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|RestXContentBuilder
operator|.
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
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
name|CLUSTER_NAME
argument_list|,
name|response
operator|.
name|clusterName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STATUS
argument_list|,
name|response
operator|.
name|status
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TIMED_OUT
argument_list|,
name|response
operator|.
name|timedOut
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUMBER_OF_NODES
argument_list|,
name|response
operator|.
name|numberOfNodes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUMBER_OF_DATA_NODES
argument_list|,
name|response
operator|.
name|numberOfDataNodes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ACTIVE_PRIMARY_SHARDS
argument_list|,
name|response
operator|.
name|activePrimaryShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ACTIVE_SHARDS
argument_list|,
name|response
operator|.
name|activeShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|RELOCATING_SHARDS
argument_list|,
name|response
operator|.
name|relocatingShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|INITIALIZING_SHARDS
argument_list|,
name|response
operator|.
name|initializingShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|UNASSIGNED_SHARDS
argument_list|,
name|response
operator|.
name|unassignedShards
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|response
operator|.
name|validationFailures
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|VALIDATION_FAILURES
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|validationFailure
range|:
name|response
operator|.
name|validationFailures
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|validationFailure
argument_list|)
expr_stmt|;
block|}
comment|// if we don't print index level information, still print the index validation failures
comment|// so we know why the status is red
if|if
condition|(
name|fLevel
operator|==
literal|0
condition|)
block|{
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|response
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexHealth
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|indexHealth
operator|.
name|validationFailures
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|VALIDATION_FAILURES
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|validationFailure
range|:
name|indexHealth
operator|.
name|validationFailures
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|validationFailure
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|fLevel
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|response
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexHealth
operator|.
name|index
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STATUS
argument_list|,
name|indexHealth
operator|.
name|status
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUMBER_OF_SHARDS
argument_list|,
name|indexHealth
operator|.
name|numberOfShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUMBER_OF_REPLICAS
argument_list|,
name|indexHealth
operator|.
name|numberOfReplicas
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ACTIVE_PRIMARY_SHARDS
argument_list|,
name|indexHealth
operator|.
name|activePrimaryShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ACTIVE_SHARDS
argument_list|,
name|indexHealth
operator|.
name|activeShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|RELOCATING_SHARDS
argument_list|,
name|indexHealth
operator|.
name|relocatingShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|INITIALIZING_SHARDS
argument_list|,
name|indexHealth
operator|.
name|initializingShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|UNASSIGNED_SHARDS
argument_list|,
name|indexHealth
operator|.
name|unassignedShards
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|indexHealth
operator|.
name|validationFailures
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|VALIDATION_FAILURES
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|validationFailure
range|:
name|indexHealth
operator|.
name|validationFailures
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|validationFailure
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|fLevel
operator|>
literal|1
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|SHARDS
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterShardHealth
name|shardHealth
range|:
name|indexHealth
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|shardHealth
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STATUS
argument_list|,
name|shardHealth
operator|.
name|status
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|PRIMARY_ACTIVE
argument_list|,
name|shardHealth
operator|.
name|primaryActive
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ACTIVE_SHARDS
argument_list|,
name|shardHealth
operator|.
name|activeShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|RELOCATING_SHARDS
argument_list|,
name|shardHealth
operator|.
name|relocatingShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|INITIALIZING_SHARDS
argument_list|,
name|shardHealth
operator|.
name|initializingShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|UNASSIGNED_SHARDS
argument_list|,
name|shardHealth
operator|.
name|unassignedShards
argument_list|()
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
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|RestStatus
operator|.
name|OK
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentThrowableRestResponse
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|CLUSTER_NAME
specifier|static
specifier|final
name|XContentBuilderString
name|CLUSTER_NAME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"cluster_name"
argument_list|)
decl_stmt|;
DECL|field|STATUS
specifier|static
specifier|final
name|XContentBuilderString
name|STATUS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"status"
argument_list|)
decl_stmt|;
DECL|field|TIMED_OUT
specifier|static
specifier|final
name|XContentBuilderString
name|TIMED_OUT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"timed_out"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_OF_SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|NUMBER_OF_SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"number_of_shards"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_OF_REPLICAS
specifier|static
specifier|final
name|XContentBuilderString
name|NUMBER_OF_REPLICAS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"number_of_replicas"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_OF_NODES
specifier|static
specifier|final
name|XContentBuilderString
name|NUMBER_OF_NODES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"number_of_nodes"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_OF_DATA_NODES
specifier|static
specifier|final
name|XContentBuilderString
name|NUMBER_OF_DATA_NODES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"number_of_data_nodes"
argument_list|)
decl_stmt|;
DECL|field|ACTIVE_PRIMARY_SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|ACTIVE_PRIMARY_SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"active_primary_shards"
argument_list|)
decl_stmt|;
DECL|field|ACTIVE_SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|ACTIVE_SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"active_shards"
argument_list|)
decl_stmt|;
DECL|field|RELOCATING_SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|RELOCATING_SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"relocating_shards"
argument_list|)
decl_stmt|;
DECL|field|INITIALIZING_SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|INITIALIZING_SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"initializing_shards"
argument_list|)
decl_stmt|;
DECL|field|UNASSIGNED_SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|UNASSIGNED_SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"unassigned_shards"
argument_list|)
decl_stmt|;
DECL|field|VALIDATION_FAILURES
specifier|static
specifier|final
name|XContentBuilderString
name|VALIDATION_FAILURES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"validation_failures"
argument_list|)
decl_stmt|;
DECL|field|INDICES
specifier|static
specifier|final
name|XContentBuilderString
name|INDICES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"indices"
argument_list|)
decl_stmt|;
DECL|field|SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"shards"
argument_list|)
decl_stmt|;
DECL|field|PRIMARY_ACTIVE
specifier|static
specifier|final
name|XContentBuilderString
name|PRIMARY_ACTIVE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"primary_active"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

