begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ElasticSearchException
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
name|common
operator|.
name|component
operator|.
name|AbstractLifecycleComponent
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|DiscoveryService
specifier|public
class|class
name|DiscoveryService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|DiscoveryService
argument_list|>
block|{
DECL|field|initialStateTimeout
specifier|private
specifier|final
name|TimeValue
name|initialStateTimeout
decl_stmt|;
DECL|field|discovery
specifier|private
specifier|final
name|Discovery
name|discovery
decl_stmt|;
DECL|field|initialStateReceived
specifier|private
specifier|volatile
name|boolean
name|initialStateReceived
decl_stmt|;
DECL|method|DiscoveryService
annotation|@
name|Inject
specifier|public
name|DiscoveryService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Discovery
name|discovery
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|discovery
operator|=
name|discovery
expr_stmt|;
name|this
operator|.
name|initialStateTimeout
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"initial_state_timeout"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|InitialStateDiscoveryListener
name|listener
init|=
operator|new
name|InitialStateDiscoveryListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|initialStateProcessed
parameter_list|()
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|discovery
operator|.
name|addListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
try|try
block|{
name|discovery
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"waiting for {} for the initial state to be set by the discovery"
argument_list|,
name|initialStateTimeout
argument_list|)
expr_stmt|;
if|if
condition|(
name|latch
operator|.
name|await
argument_list|(
name|initialStateTimeout
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"initial state set from discovery"
argument_list|)
expr_stmt|;
name|initialStateReceived
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|initialStateReceived
operator|=
literal|false
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"waited for {} and no initial state was set by the discovery"
argument_list|,
name|initialStateTimeout
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
finally|finally
block|{
name|discovery
operator|.
name|removeListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
name|discovery
operator|.
name|nodeDescription
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|discovery
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|discovery
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|localNode
specifier|public
name|DiscoveryNode
name|localNode
parameter_list|()
block|{
return|return
name|discovery
operator|.
name|localNode
argument_list|()
return|;
block|}
comment|/**      * Returns<tt>true</tt> if the initial state was received within the timeout waiting for it      * on {@link #doStart()}.      */
DECL|method|initialStateReceived
specifier|public
name|boolean
name|initialStateReceived
parameter_list|()
block|{
return|return
name|initialStateReceived
return|;
block|}
DECL|method|nodeDescription
specifier|public
name|String
name|nodeDescription
parameter_list|()
block|{
return|return
name|discovery
operator|.
name|nodeDescription
argument_list|()
return|;
block|}
comment|/**      * Publish all the changes to the cluster from the master (can be called just by the master). The publish      * process should not publish this state to the master as well! (the master is sending it...).      */
DECL|method|publish
specifier|public
name|void
name|publish
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
return|return;
block|}
name|discovery
operator|.
name|publish
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

