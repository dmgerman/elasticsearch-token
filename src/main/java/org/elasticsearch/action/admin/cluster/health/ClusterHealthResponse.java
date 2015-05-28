begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.health
package|package
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
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|ActionResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|IndexRoutingTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|RoutingTableValidation
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|ToXContent
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
name|rest
operator|.
name|RestStatus
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
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
import|;
end_import

begin_import
import|import static
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
name|ClusterIndexHealth
operator|.
name|readClusterIndexHealth
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ClusterHealthResponse
specifier|public
class|class
name|ClusterHealthResponse
extends|extends
name|ActionResponse
implements|implements
name|Iterable
argument_list|<
name|ClusterIndexHealth
argument_list|>
implements|,
name|ToXContent
block|{
DECL|field|clusterName
specifier|private
name|String
name|clusterName
decl_stmt|;
DECL|field|numberOfNodes
name|int
name|numberOfNodes
init|=
literal|0
decl_stmt|;
DECL|field|numberOfDataNodes
name|int
name|numberOfDataNodes
init|=
literal|0
decl_stmt|;
DECL|field|activeShards
name|int
name|activeShards
init|=
literal|0
decl_stmt|;
DECL|field|relocatingShards
name|int
name|relocatingShards
init|=
literal|0
decl_stmt|;
DECL|field|activePrimaryShards
name|int
name|activePrimaryShards
init|=
literal|0
decl_stmt|;
DECL|field|initializingShards
name|int
name|initializingShards
init|=
literal|0
decl_stmt|;
DECL|field|unassignedShards
name|int
name|unassignedShards
init|=
literal|0
decl_stmt|;
DECL|field|numberOfPendingTasks
name|int
name|numberOfPendingTasks
init|=
literal|0
decl_stmt|;
DECL|field|numberOfInFlightFetch
name|int
name|numberOfInFlightFetch
init|=
literal|0
decl_stmt|;
DECL|field|timedOut
name|boolean
name|timedOut
init|=
literal|false
decl_stmt|;
DECL|field|status
name|ClusterHealthStatus
name|status
init|=
name|ClusterHealthStatus
operator|.
name|RED
decl_stmt|;
DECL|field|validationFailures
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|validationFailures
decl_stmt|;
DECL|field|indices
name|Map
argument_list|<
name|String
argument_list|,
name|ClusterIndexHealth
argument_list|>
name|indices
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|method|ClusterHealthResponse
name|ClusterHealthResponse
parameter_list|()
block|{     }
comment|/** needed for plugins BWC */
DECL|method|ClusterHealthResponse
specifier|public
name|ClusterHealthResponse
parameter_list|(
name|String
name|clusterName
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|this
argument_list|(
name|clusterName
argument_list|,
name|concreteIndices
argument_list|,
name|clusterState
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|ClusterHealthResponse
specifier|public
name|ClusterHealthResponse
parameter_list|(
name|String
name|clusterName
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|,
name|int
name|numberOfPendingTasks
parameter_list|,
name|int
name|numberOfInFlightFetch
parameter_list|)
block|{
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|numberOfPendingTasks
operator|=
name|numberOfPendingTasks
expr_stmt|;
name|this
operator|.
name|numberOfInFlightFetch
operator|=
name|numberOfInFlightFetch
expr_stmt|;
name|RoutingTableValidation
name|validation
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|validate
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
name|validationFailures
operator|=
name|validation
operator|.
name|failures
argument_list|()
expr_stmt|;
name|numberOfNodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|numberOfDataNodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|dataNodes
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|concreteIndices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|ClusterIndexHealth
name|indexHealth
init|=
operator|new
name|ClusterIndexHealth
argument_list|(
name|indexMetaData
argument_list|,
name|indexRoutingTable
argument_list|)
decl_stmt|;
name|indices
operator|.
name|put
argument_list|(
name|indexHealth
operator|.
name|getIndex
argument_list|()
argument_list|,
name|indexHealth
argument_list|)
expr_stmt|;
block|}
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|GREEN
expr_stmt|;
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|indices
operator|.
name|values
argument_list|()
control|)
block|{
name|activePrimaryShards
operator|+=
name|indexHealth
operator|.
name|activePrimaryShards
expr_stmt|;
name|activeShards
operator|+=
name|indexHealth
operator|.
name|activeShards
expr_stmt|;
name|relocatingShards
operator|+=
name|indexHealth
operator|.
name|relocatingShards
expr_stmt|;
name|initializingShards
operator|+=
name|indexHealth
operator|.
name|initializingShards
expr_stmt|;
name|unassignedShards
operator|+=
name|indexHealth
operator|.
name|unassignedShards
expr_stmt|;
if|if
condition|(
name|indexHealth
operator|.
name|getStatus
argument_list|()
operator|==
name|ClusterHealthStatus
operator|.
name|RED
condition|)
block|{
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexHealth
operator|.
name|getStatus
argument_list|()
operator|==
name|ClusterHealthStatus
operator|.
name|YELLOW
operator|&&
name|status
operator|!=
name|ClusterHealthStatus
operator|.
name|RED
condition|)
block|{
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|YELLOW
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|validationFailures
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|clusterState
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
condition|)
block|{
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
block|}
block|}
DECL|method|getClusterName
specifier|public
name|String
name|getClusterName
parameter_list|()
block|{
return|return
name|clusterName
return|;
block|}
comment|/**      * The validation failures on the cluster level (without index validation failures).      */
DECL|method|getValidationFailures
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValidationFailures
parameter_list|()
block|{
return|return
name|this
operator|.
name|validationFailures
return|;
block|}
comment|/**      * All the validation failures, including index level validation failures.      */
DECL|method|getAllValidationFailures
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllValidationFailures
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|allFailures
init|=
name|newArrayList
argument_list|(
name|getValidationFailures
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|indices
operator|.
name|values
argument_list|()
control|)
block|{
name|allFailures
operator|.
name|addAll
argument_list|(
name|indexHealth
operator|.
name|getValidationFailures
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allFailures
return|;
block|}
DECL|method|getActiveShards
specifier|public
name|int
name|getActiveShards
parameter_list|()
block|{
return|return
name|activeShards
return|;
block|}
DECL|method|getRelocatingShards
specifier|public
name|int
name|getRelocatingShards
parameter_list|()
block|{
return|return
name|relocatingShards
return|;
block|}
DECL|method|getActivePrimaryShards
specifier|public
name|int
name|getActivePrimaryShards
parameter_list|()
block|{
return|return
name|activePrimaryShards
return|;
block|}
DECL|method|getInitializingShards
specifier|public
name|int
name|getInitializingShards
parameter_list|()
block|{
return|return
name|initializingShards
return|;
block|}
DECL|method|getUnassignedShards
specifier|public
name|int
name|getUnassignedShards
parameter_list|()
block|{
return|return
name|unassignedShards
return|;
block|}
DECL|method|getNumberOfNodes
specifier|public
name|int
name|getNumberOfNodes
parameter_list|()
block|{
return|return
name|this
operator|.
name|numberOfNodes
return|;
block|}
DECL|method|getNumberOfDataNodes
specifier|public
name|int
name|getNumberOfDataNodes
parameter_list|()
block|{
return|return
name|this
operator|.
name|numberOfDataNodes
return|;
block|}
DECL|method|getNumberOfPendingTasks
specifier|public
name|int
name|getNumberOfPendingTasks
parameter_list|()
block|{
return|return
name|this
operator|.
name|numberOfPendingTasks
return|;
block|}
DECL|method|getNumberOfInFlightFetch
specifier|public
name|int
name|getNumberOfInFlightFetch
parameter_list|()
block|{
return|return
name|this
operator|.
name|numberOfInFlightFetch
return|;
block|}
comment|/**      *<tt>true</tt> if the waitForXXX has timeout out and did not match.      */
DECL|method|isTimedOut
specifier|public
name|boolean
name|isTimedOut
parameter_list|()
block|{
return|return
name|this
operator|.
name|timedOut
return|;
block|}
DECL|method|getStatus
specifier|public
name|ClusterHealthStatus
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
DECL|method|getIndices
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ClusterIndexHealth
argument_list|>
name|getIndices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|ClusterIndexHealth
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|indices
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|readResponseFrom
specifier|public
specifier|static
name|ClusterHealthResponse
name|readResponseFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ClusterHealthResponse
name|response
init|=
operator|new
name|ClusterHealthResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|clusterName
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|activePrimaryShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|activeShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|relocatingShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|initializingShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|unassignedShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|numberOfNodes
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|numberOfDataNodes
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|numberOfPendingTasks
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|ClusterIndexHealth
name|indexHealth
init|=
name|readClusterIndexHealth
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|indices
operator|.
name|put
argument_list|(
name|indexHealth
operator|.
name|getIndex
argument_list|()
argument_list|,
name|indexHealth
argument_list|)
expr_stmt|;
block|}
name|timedOut
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|validationFailures
operator|=
name|ImmutableList
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|validationFailures
operator|.
name|add
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|numberOfInFlightFetch
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|clusterName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|activePrimaryShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|activeShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|relocatingShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|initializingShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|unassignedShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|numberOfNodes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|numberOfDataNodes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numberOfPendingTasks
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|status
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indices
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|this
control|)
block|{
name|indexHealth
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|timedOut
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|validationFailures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|failure
range|:
name|validationFailures
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|numberOfInFlightFetch
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|"{ \"error\" : \""
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|"\"}"
return|;
block|}
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
DECL|field|NUMBER_OF_PENDING_TASKS
specifier|static
specifier|final
name|XContentBuilderString
name|NUMBER_OF_PENDING_TASKS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"number_of_pending_tasks"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_OF_IN_FLIGHT_FETCH
specifier|static
specifier|final
name|XContentBuilderString
name|NUMBER_OF_IN_FLIGHT_FETCH
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"number_of_in_flight_fetch"
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
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|CLUSTER_NAME
argument_list|,
name|getClusterName
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
name|getStatus
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
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
name|isTimedOut
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
name|getNumberOfNodes
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
name|getNumberOfDataNodes
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
name|getActivePrimaryShards
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
name|getActiveShards
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
name|getRelocatingShards
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
name|getInitializingShards
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
name|getUnassignedShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUMBER_OF_PENDING_TASKS
argument_list|,
name|getNumberOfPendingTasks
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUMBER_OF_IN_FLIGHT_FETCH
argument_list|,
name|getNumberOfInFlightFetch
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|level
init|=
name|params
operator|.
name|param
argument_list|(
literal|"level"
argument_list|,
literal|"cluster"
argument_list|)
decl_stmt|;
name|boolean
name|outputIndices
init|=
literal|"indices"
operator|.
name|equals
argument_list|(
name|level
argument_list|)
operator|||
literal|"shards"
operator|.
name|equals
argument_list|(
name|level
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|getValidationFailures
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
name|getValidationFailures
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
operator|!
name|outputIndices
condition|)
block|{
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|indices
operator|.
name|values
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexHealth
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|indexHealth
operator|.
name|getValidationFailures
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
name|getValidationFailures
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
name|outputIndices
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
name|indices
operator|.
name|values
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexHealth
operator|.
name|getIndex
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|indexHealth
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
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
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

