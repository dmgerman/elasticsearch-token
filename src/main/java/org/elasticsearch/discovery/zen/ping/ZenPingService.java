begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Version
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EsRejectedExecutionException
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
name|elect
operator|.
name|ElectMasterService
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
name|UnicastHostsProvider
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
name|java
operator|.
name|util
operator|.
name|Set
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
comment|/**  *  */
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
annotation|@
name|Inject
DECL|method|ZenPingService
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
parameter_list|,
name|Version
name|version
parameter_list|,
name|ElectMasterService
name|electMasterService
parameter_list|,
annotation|@
name|Nullable
name|Set
argument_list|<
name|UnicastHostsProvider
argument_list|>
name|unicastHostsProviders
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
name|this
operator|.
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"discovery.zen.ping.multicast.enabled"
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
argument_list|,
name|version
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// always add the unicast hosts, so it will be able to receive unicast requests even when working in multicast
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
argument_list|,
name|version
argument_list|,
name|electMasterService
argument_list|,
name|unicastHostsProviders
argument_list|)
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
DECL|method|setPingContextProvider
specifier|public
name|void
name|setPingContextProvider
parameter_list|(
name|PingContextProvider
name|contextProvider
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
name|IllegalStateException
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
name|setPingContextProvider
argument_list|(
name|contextProvider
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
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
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
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
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
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
argument_list|<>
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
name|logger
operator|.
name|trace
argument_list|(
literal|"pingAndWait interrupted"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|ping
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
try|try
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
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Ping execution rejected"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|compoundPingListener
operator|.
name|onPing
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
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
DECL|field|counter
specifier|private
specifier|final
name|AtomicInteger
name|counter
decl_stmt|;
DECL|field|responses
specifier|private
name|PingCollection
name|responses
init|=
operator|new
name|PingCollection
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
annotation|@
name|Override
DECL|method|onPing
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
name|responses
operator|.
name|addPings
argument_list|(
name|pings
argument_list|)
expr_stmt|;
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
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

