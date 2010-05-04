begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen.ping
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ping
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
name|ElasticSearchIllegalStateException
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
name|ClusterName
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
name|discovery
operator|.
name|zen
operator|.
name|DiscoveryNodesProvider
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
name|zen
operator|.
name|ping
operator|.
name|multicast
operator|.
name|MulticastZenPing
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
name|zen
operator|.
name|ping
operator|.
name|unicast
operator|.
name|UnicastZenPing
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|guice
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
name|util
operator|.
name|network
operator|.
name|NetworkService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|ConcurrentHashMap
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
name|ConcurrentMap
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
name|atomic
operator|.
name|AtomicInteger
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
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ZenPingService
specifier|public
class|class
name|ZenPingService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|ZenPing
argument_list|>
implements|implements
name|ZenPing
block|{
DECL|field|zenPings
specifier|private
specifier|volatile
name|ImmutableList
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|zenPings
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|method|ZenPingService
annotation|@
name|Inject
specifier|public
name|ZenPingService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|ZenPing
argument_list|>
name|zenPingsBuilder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
if|if
condition|(
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"multicast.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|zenPingsBuilder
operator|.
name|add
argument_list|(
operator|new
name|MulticastZenPing
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|clusterName
argument_list|,
name|networkService
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|componentSettings
operator|.
name|get
argument_list|(
literal|"unicast.hosts"
argument_list|)
operator|!=
literal|null
operator|||
name|componentSettings
operator|.
name|getAsArray
argument_list|(
literal|"unicast.hosts"
argument_list|)
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|zenPingsBuilder
operator|.
name|add
argument_list|(
operator|new
name|UnicastZenPing
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|clusterName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|zenPings
operator|=
name|zenPingsBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|zenPings
specifier|public
name|ImmutableList
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|zenPings
parameter_list|()
block|{
return|return
name|this
operator|.
name|zenPings
return|;
block|}
DECL|method|zenPings
specifier|public
name|void
name|zenPings
parameter_list|(
name|ImmutableList
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|pings
parameter_list|)
block|{
name|this
operator|.
name|zenPings
operator|=
name|pings
expr_stmt|;
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
name|zenPing
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|lifecycle
operator|.
name|stopped
argument_list|()
condition|)
block|{
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
name|zenPing
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|setNodesProvider
annotation|@
name|Override
specifier|public
name|void
name|setNodesProvider
parameter_list|(
name|DiscoveryNodesProvider
name|nodesProvider
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
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Can't set nodes provider when started"
argument_list|)
throw|;
block|}
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
name|zenPing
operator|.
name|setNodesProvider
argument_list|(
name|nodesProvider
argument_list|)
expr_stmt|;
block|}
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
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
name|zenPing
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
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
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
name|zenPing
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
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
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
name|zenPing
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|pingAndWait
specifier|public
name|PingResponse
index|[]
name|pingAndWait
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
specifier|final
name|AtomicReference
argument_list|<
name|PingResponse
index|[]
argument_list|>
name|response
init|=
operator|new
name|AtomicReference
argument_list|<
name|PingResponse
index|[]
argument_list|>
argument_list|()
decl_stmt|;
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
name|ping
argument_list|(
operator|new
name|PingListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onPing
parameter_list|(
name|PingResponse
index|[]
name|pings
parameter_list|)
block|{
name|response
operator|.
name|set
argument_list|(
name|pings
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
return|return
name|response
operator|.
name|get
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|ping
annotation|@
name|Override
specifier|public
name|void
name|ping
parameter_list|(
name|PingListener
name|listener
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|ImmutableList
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|zenPings
init|=
name|this
operator|.
name|zenPings
decl_stmt|;
name|CompoundPingListener
name|compoundPingListener
init|=
operator|new
name|CompoundPingListener
argument_list|(
name|listener
argument_list|,
name|zenPings
argument_list|)
decl_stmt|;
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
name|zenPing
operator|.
name|ping
argument_list|(
name|compoundPingListener
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|CompoundPingListener
specifier|private
specifier|static
class|class
name|CompoundPingListener
implements|implements
name|PingListener
block|{
DECL|field|listener
specifier|private
specifier|final
name|PingListener
name|listener
decl_stmt|;
DECL|field|zenPings
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|zenPings
decl_stmt|;
DECL|field|counter
specifier|private
specifier|final
name|AtomicInteger
name|counter
decl_stmt|;
DECL|field|responses
specifier|private
name|ConcurrentMap
argument_list|<
name|DiscoveryNode
argument_list|,
name|PingResponse
argument_list|>
name|responses
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|DiscoveryNode
argument_list|,
name|PingResponse
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|CompoundPingListener
specifier|private
name|CompoundPingListener
parameter_list|(
name|PingListener
name|listener
parameter_list|,
name|ImmutableList
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|zenPings
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|zenPings
operator|=
name|zenPings
expr_stmt|;
name|this
operator|.
name|counter
operator|=
operator|new
name|AtomicInteger
argument_list|(
name|zenPings
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|onPing
annotation|@
name|Override
specifier|public
name|void
name|onPing
parameter_list|(
name|PingResponse
index|[]
name|pings
parameter_list|)
block|{
if|if
condition|(
name|pings
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|PingResponse
name|pingResponse
range|:
name|pings
control|)
block|{
name|responses
operator|.
name|put
argument_list|(
name|pingResponse
operator|.
name|target
argument_list|()
argument_list|,
name|pingResponse
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|listener
operator|.
name|onPing
argument_list|(
name|responses
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|PingResponse
index|[
name|responses
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

