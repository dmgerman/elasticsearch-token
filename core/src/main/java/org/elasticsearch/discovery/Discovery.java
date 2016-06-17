begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|node
operator|.
name|DiscoveryNode
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
name|allocation
operator|.
name|AllocationService
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
name|Nullable
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
name|LifecycleComponent
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * A pluggable module allowing to implement discovery of other nodes, publishing of the cluster  * state to all nodes, electing a master of the cluster that raises cluster state change  * events.  */
end_comment

begin_interface
DECL|interface|Discovery
specifier|public
interface|interface
name|Discovery
extends|extends
name|LifecycleComponent
argument_list|<
name|Discovery
argument_list|>
block|{
DECL|method|localNode
name|DiscoveryNode
name|localNode
parameter_list|()
function_decl|;
DECL|method|nodeDescription
name|String
name|nodeDescription
parameter_list|()
function_decl|;
comment|/**      * Another hack to solve dep injection problem..., note, this will be called before      * any start is called.      */
DECL|method|setAllocationService
name|void
name|setAllocationService
parameter_list|(
name|AllocationService
name|allocationService
parameter_list|)
function_decl|;
comment|/**      * Publish all the changes to the cluster from the master (can be called just by the master). The publish      * process should not publish this state to the master as well! (the master is sending it...).      *      * The {@link AckListener} allows to keep track of the ack received from nodes, and verify whether      * they updated their own cluster state or not.      *      * The method is guaranteed to throw a {@link FailedToCommitClusterStateException} if the change is not committed and should be rejected.      * Any other exception signals the something wrong happened but the change is committed.      */
DECL|method|publish
name|void
name|publish
parameter_list|(
name|ClusterChangedEvent
name|clusterChangedEvent
parameter_list|,
name|AckListener
name|ackListener
parameter_list|)
function_decl|;
DECL|interface|AckListener
interface|interface
name|AckListener
block|{
DECL|method|onNodeAck
name|void
name|onNodeAck
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
annotation|@
name|Nullable
name|Throwable
name|t
parameter_list|)
function_decl|;
DECL|method|onTimeout
name|void
name|onTimeout
parameter_list|()
function_decl|;
block|}
DECL|class|FailedToCommitClusterStateException
class|class
name|FailedToCommitClusterStateException
extends|extends
name|ElasticsearchException
block|{
DECL|method|FailedToCommitClusterStateException
specifier|public
name|FailedToCommitClusterStateException
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
DECL|method|FailedToCommitClusterStateException
specifier|public
name|FailedToCommitClusterStateException
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|FailedToCommitClusterStateException
specifier|public
name|FailedToCommitClusterStateException
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|cause
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * @return stats about the discovery      */
DECL|method|stats
name|DiscoveryStats
name|stats
parameter_list|()
function_decl|;
DECL|method|getDiscoverySettings
name|DiscoverySettings
name|getDiscoverySettings
parameter_list|()
function_decl|;
comment|/**      * Triggers the first join cycle      */
DECL|method|startInitialJoin
name|void
name|startInitialJoin
parameter_list|()
function_decl|;
comment|/***      * @return the current value of minimum master nodes, or -1 for not set      */
DECL|method|getMinimumMasterNodes
name|int
name|getMinimumMasterNodes
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

