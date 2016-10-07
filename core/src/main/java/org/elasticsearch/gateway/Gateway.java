begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectFloatHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
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
name|FailedNodeException
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
name|ClusterChangedEvent
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
name|ClusterStateListener
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
name|metadata
operator|.
name|MetaData
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
name|service
operator|.
name|ClusterService
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
name|component
operator|.
name|AbstractComponent
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
name|ClusterSettings
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
name|discovery
operator|.
name|Discovery
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
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_class
DECL|class|Gateway
specifier|public
class|class
name|Gateway
extends|extends
name|AbstractComponent
implements|implements
name|ClusterStateListener
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|metaState
specifier|private
specifier|final
name|GatewayMetaState
name|metaState
decl_stmt|;
DECL|field|listGatewayMetaState
specifier|private
specifier|final
name|TransportNodesListGatewayMetaState
name|listGatewayMetaState
decl_stmt|;
DECL|field|minimumMasterNodesProvider
specifier|private
specifier|final
name|Supplier
argument_list|<
name|Integer
argument_list|>
name|minimumMasterNodesProvider
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|method|Gateway
specifier|public
name|Gateway
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|GatewayMetaState
name|metaState
parameter_list|,
name|TransportNodesListGatewayMetaState
name|listGatewayMetaState
parameter_list|,
name|Discovery
name|discovery
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|metaState
operator|=
name|metaState
expr_stmt|;
name|this
operator|.
name|listGatewayMetaState
operator|=
name|listGatewayMetaState
expr_stmt|;
name|this
operator|.
name|minimumMasterNodesProvider
operator|=
name|discovery
operator|::
name|getMinimumMasterNodes
expr_stmt|;
name|clusterService
operator|.
name|addLast
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|performStateRecovery
specifier|public
name|void
name|performStateRecovery
parameter_list|(
specifier|final
name|GatewayStateRecoveredListener
name|listener
parameter_list|)
throws|throws
name|GatewayException
block|{
name|String
index|[]
name|nodesIds
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodes
argument_list|()
operator|.
name|keys
argument_list|()
operator|.
name|toArray
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"performing state recovery from {}"
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|nodesIds
argument_list|)
argument_list|)
expr_stmt|;
name|TransportNodesListGatewayMetaState
operator|.
name|NodesGatewayMetaState
name|nodesState
init|=
name|listGatewayMetaState
operator|.
name|list
argument_list|(
name|nodesIds
argument_list|,
literal|null
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|int
name|requiredAllocation
init|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|minimumMasterNodesProvider
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodesState
operator|.
name|hasFailures
argument_list|()
condition|)
block|{
for|for
control|(
name|FailedNodeException
name|failedNodeException
range|:
name|nodesState
operator|.
name|failures
argument_list|()
control|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to fetch state from node"
argument_list|,
name|failedNodeException
argument_list|)
expr_stmt|;
block|}
block|}
name|ObjectFloatHashMap
argument_list|<
name|Index
argument_list|>
name|indices
init|=
operator|new
name|ObjectFloatHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|MetaData
name|electedGlobalState
init|=
literal|null
decl_stmt|;
name|int
name|found
init|=
literal|0
decl_stmt|;
for|for
control|(
name|TransportNodesListGatewayMetaState
operator|.
name|NodeGatewayMetaState
name|nodeState
range|:
name|nodesState
operator|.
name|getNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeState
operator|.
name|metaData
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|found
operator|++
expr_stmt|;
if|if
condition|(
name|electedGlobalState
operator|==
literal|null
condition|)
block|{
name|electedGlobalState
operator|=
name|nodeState
operator|.
name|metaData
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nodeState
operator|.
name|metaData
argument_list|()
operator|.
name|version
argument_list|()
operator|>
name|electedGlobalState
operator|.
name|version
argument_list|()
condition|)
block|{
name|electedGlobalState
operator|=
name|nodeState
operator|.
name|metaData
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|ObjectCursor
argument_list|<
name|IndexMetaData
argument_list|>
name|cursor
range|:
name|nodeState
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|indices
operator|.
name|addTo
argument_list|(
name|cursor
operator|.
name|value
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|found
operator|<
name|requiredAllocation
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
literal|"found ["
operator|+
name|found
operator|+
literal|"] metadata states, required ["
operator|+
name|requiredAllocation
operator|+
literal|"]"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// update the global state, and clean the indices, we elect them in the next phase
name|MetaData
operator|.
name|Builder
name|metaDataBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|electedGlobalState
argument_list|)
operator|.
name|removeAllIndices
argument_list|()
decl_stmt|;
assert|assert
operator|!
name|indices
operator|.
name|containsKey
argument_list|(
literal|null
argument_list|)
assert|;
specifier|final
name|Object
index|[]
name|keys
init|=
name|indices
operator|.
name|keys
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|Index
name|index
init|=
operator|(
name|Index
operator|)
name|keys
index|[
name|i
index|]
decl_stmt|;
name|IndexMetaData
name|electedIndexMetaData
init|=
literal|null
decl_stmt|;
name|int
name|indexMetaDataCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|TransportNodesListGatewayMetaState
operator|.
name|NodeGatewayMetaState
name|nodeState
range|:
name|nodesState
operator|.
name|getNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeState
operator|.
name|metaData
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|IndexMetaData
name|indexMetaData
init|=
name|nodeState
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
name|indexMetaData
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|electedIndexMetaData
operator|==
literal|null
condition|)
block|{
name|electedIndexMetaData
operator|=
name|indexMetaData
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexMetaData
operator|.
name|getVersion
argument_list|()
operator|>
name|electedIndexMetaData
operator|.
name|getVersion
argument_list|()
condition|)
block|{
name|electedIndexMetaData
operator|=
name|indexMetaData
expr_stmt|;
block|}
name|indexMetaDataCount
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|electedIndexMetaData
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|indexMetaDataCount
operator|<
name|requiredAllocation
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] found [{}], required [{}], not adding"
argument_list|,
name|index
argument_list|,
name|indexMetaDataCount
argument_list|,
name|requiredAllocation
argument_list|)
expr_stmt|;
block|}
comment|// TODO if this logging statement is correct then we are missing an else here
try|try
block|{
if|if
condition|(
name|electedIndexMetaData
operator|.
name|getState
argument_list|()
operator|==
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
condition|)
block|{
comment|// verify that we can actually create this index - if not we recover it as closed with lots of warn logs
name|indicesService
operator|.
name|verifyIndexMetadata
argument_list|(
name|electedIndexMetaData
argument_list|,
name|electedIndexMetaData
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
specifier|final
name|Index
name|electedIndex
init|=
name|electedIndexMetaData
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|logger
operator|.
name|warn
argument_list|(
call|(
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"recovering index {} failed - recovering as closed"
argument_list|,
name|electedIndex
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|electedIndexMetaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|electedIndexMetaData
argument_list|)
operator|.
name|state
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|metaDataBuilder
operator|.
name|put
argument_list|(
name|electedIndexMetaData
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|final
name|ClusterSettings
name|clusterSettings
init|=
name|clusterService
operator|.
name|getClusterSettings
argument_list|()
decl_stmt|;
name|metaDataBuilder
operator|.
name|persistentSettings
argument_list|(
name|clusterSettings
operator|.
name|archiveUnknownOrBrokenSettings
argument_list|(
name|metaDataBuilder
operator|.
name|persistentSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|metaDataBuilder
operator|.
name|transientSettings
argument_list|(
name|clusterSettings
operator|.
name|archiveUnknownOrBrokenSettings
argument_list|(
name|metaDataBuilder
operator|.
name|transientSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterState
operator|.
name|Builder
name|builder
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|getClusterName
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|metaData
argument_list|(
name|metaDataBuilder
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onSuccess
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clusterChanged
specifier|public
name|void
name|clusterChanged
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
comment|// order is important, first metaState, and then shardsState
comment|// so dangling indices will be recorded
name|metaState
operator|.
name|clusterChanged
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
DECL|interface|GatewayStateRecoveredListener
specifier|public
interface|interface
name|GatewayStateRecoveredListener
block|{
DECL|method|onSuccess
name|void
name|onSuccess
parameter_list|(
name|ClusterState
name|build
parameter_list|)
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|String
name|s
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

