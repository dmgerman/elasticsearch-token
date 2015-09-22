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
name|ElasticsearchTimeoutException
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
name|block
operator|.
name|ClusterBlock
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
name|Random
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
comment|/**  *  */
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
DECL|field|SETTING_INITIAL_STATE_TIMEOUT
specifier|public
specifier|static
specifier|final
name|String
name|SETTING_INITIAL_STATE_TIMEOUT
init|=
literal|"discovery.initial_state_timeout"
decl_stmt|;
DECL|class|InitialStateListener
specifier|private
specifier|static
class|class
name|InitialStateListener
implements|implements
name|InitialStateDiscoveryListener
block|{
DECL|field|latch
specifier|private
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
DECL|field|initialStateReceived
specifier|private
specifier|volatile
name|boolean
name|initialStateReceived
decl_stmt|;
annotation|@
name|Override
DECL|method|initialStateProcessed
specifier|public
name|void
name|initialStateProcessed
parameter_list|()
block|{
name|initialStateReceived
operator|=
literal|true
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
DECL|method|waitForInitialState
specifier|public
name|boolean
name|waitForInitialState
parameter_list|(
name|TimeValue
name|timeValue
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|timeValue
operator|.
name|millis
argument_list|()
operator|>
literal|0
condition|)
block|{
name|latch
operator|.
name|await
argument_list|(
name|timeValue
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
return|return
name|initialStateReceived
return|;
block|}
block|}
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
DECL|field|initialStateListener
specifier|private
name|InitialStateListener
name|initialStateListener
decl_stmt|;
DECL|field|discoverySettings
specifier|private
specifier|final
name|DiscoverySettings
name|discoverySettings
decl_stmt|;
annotation|@
name|Inject
DECL|method|DiscoveryService
specifier|public
name|DiscoveryService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|DiscoverySettings
name|discoverySettings
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
name|discoverySettings
operator|=
name|discoverySettings
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
name|settings
operator|.
name|getAsTime
argument_list|(
name|SETTING_INITIAL_STATE_TIMEOUT
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
DECL|method|getNoMasterBlock
specifier|public
name|ClusterBlock
name|getNoMasterBlock
parameter_list|()
block|{
return|return
name|discoverySettings
operator|.
name|getNoMasterBlock
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
name|initialStateListener
operator|=
operator|new
name|InitialStateListener
argument_list|()
expr_stmt|;
name|discovery
operator|.
name|addListener
argument_list|(
name|initialStateListener
argument_list|)
expr_stmt|;
name|discovery
operator|.
name|start
argument_list|()
expr_stmt|;
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
DECL|method|waitForInitialState
specifier|public
name|void
name|waitForInitialState
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|initialStateListener
operator|.
name|waitForInitialState
argument_list|(
name|initialStateTimeout
argument_list|)
condition|)
block|{
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
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ElasticsearchTimeoutException
argument_list|(
literal|"Interrupted while waiting for initial discovery state"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
if|if
condition|(
name|initialStateListener
operator|!=
literal|null
condition|)
block|{
name|discovery
operator|.
name|removeListener
argument_list|(
name|initialStateListener
argument_list|)
expr_stmt|;
block|}
name|discovery
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
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
name|initialStateListener
operator|.
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
comment|/**      * Publish all the changes to the cluster from the master (can be called just by the master). The publish      * process should not publish this state to the master as well! (the master is sending it...).      *<p>      * The {@link org.elasticsearch.discovery.Discovery.AckListener} allows to acknowledge the publish      * event based on the response gotten from all nodes      */
DECL|method|publish
specifier|public
name|void
name|publish
parameter_list|(
name|ClusterChangedEvent
name|clusterChangedEvent
parameter_list|,
name|Discovery
operator|.
name|AckListener
name|ackListener
parameter_list|)
block|{
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|discovery
operator|.
name|publish
argument_list|(
name|clusterChangedEvent
argument_list|,
name|ackListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|generateNodeId
specifier|public
specifier|static
name|String
name|generateNodeId
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|seed
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"discovery.id.seed"
argument_list|)
decl_stmt|;
if|if
condition|(
name|seed
operator|!=
literal|null
condition|)
block|{
return|return
name|Strings
operator|.
name|randomBase64UUID
argument_list|(
operator|new
name|Random
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|seed
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
return|return
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
return|;
block|}
block|}
end_class

end_unit

