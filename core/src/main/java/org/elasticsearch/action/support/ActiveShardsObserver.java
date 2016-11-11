begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
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
name|ClusterStateObserver
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
name|cluster
operator|.
name|service
operator|.
name|ClusterServiceState
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
name|unit
operator|.
name|TimeValue
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
name|NodeClosedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|Consumer
import|;
end_import

begin_comment
comment|/**  * This class provides primitives for waiting for a configured number of shards  * to become active before sending a response on an {@link ActionListener}.  */
end_comment

begin_class
DECL|class|ActiveShardsObserver
specifier|public
class|class
name|ActiveShardsObserver
extends|extends
name|AbstractComponent
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|method|ActiveShardsObserver
specifier|public
name|ActiveShardsObserver
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|,
specifier|final
name|ClusterService
name|clusterService
parameter_list|,
specifier|final
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
block|}
comment|/**      * Waits on the specified number of active shards to be started before executing the      *      * @param indexName the index to wait for active shards on      * @param activeShardCount the number of active shards to wait on before returning      * @param timeout the timeout value      * @param onResult a function that is executed in response to the requisite shards becoming active or a timeout (whichever comes first)      * @param onFailure a function that is executed in response to an error occurring during waiting for the active shards      */
DECL|method|waitForActiveShards
specifier|public
name|void
name|waitForActiveShards
parameter_list|(
specifier|final
name|String
name|indexName
parameter_list|,
specifier|final
name|ActiveShardCount
name|activeShardCount
parameter_list|,
specifier|final
name|TimeValue
name|timeout
parameter_list|,
specifier|final
name|Consumer
argument_list|<
name|Boolean
argument_list|>
name|onResult
parameter_list|,
specifier|final
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|onFailure
parameter_list|)
block|{
comment|// wait for the configured number of active shards to be allocated before executing the result consumer
if|if
condition|(
name|activeShardCount
operator|==
name|ActiveShardCount
operator|.
name|NONE
condition|)
block|{
comment|// not waiting, so just run whatever we were to run when the waiting is
name|onResult
operator|.
name|accept
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|ClusterStateObserver
name|observer
init|=
operator|new
name|ClusterStateObserver
argument_list|(
name|clusterService
argument_list|,
name|logger
argument_list|,
name|threadPool
operator|.
name|getThreadContext
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|activeShardCount
operator|.
name|enoughShardsActive
argument_list|(
name|observer
operator|.
name|observedState
argument_list|()
operator|.
name|getClusterState
argument_list|()
argument_list|,
name|indexName
argument_list|)
condition|)
block|{
name|onResult
operator|.
name|accept
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|ClusterStateObserver
operator|.
name|ChangePredicate
name|shardsAllocatedPredicate
init|=
operator|new
name|ClusterStateObserver
operator|.
name|ValidationPredicate
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|validate
parameter_list|(
specifier|final
name|ClusterServiceState
name|newState
parameter_list|)
block|{
return|return
name|activeShardCount
operator|.
name|enoughShardsActive
argument_list|(
name|newState
operator|.
name|getClusterState
argument_list|()
argument_list|,
name|indexName
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|final
name|ClusterStateObserver
operator|.
name|Listener
name|observerListener
init|=
operator|new
name|ClusterStateObserver
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onNewClusterState
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
name|onResult
operator|.
name|accept
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onClusterServiceClose
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] cluster service closed while waiting for enough shards to be started."
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|onFailure
operator|.
name|accept
argument_list|(
operator|new
name|NodeClosedException
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|onResult
operator|.
name|accept
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|observer
operator|.
name|waitForNextChange
argument_list|(
name|observerListener
argument_list|,
name|shardsAllocatedPredicate
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

