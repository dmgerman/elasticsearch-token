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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedTest
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
name|primitives
operator|.
name|Ints
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
name|ImmutableSettings
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
name|test
operator|.
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|InternalTestCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|SettingsSource
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
name|local
operator|.
name|LocalTransport
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
DECL|class|ClusterDiscoveryConfiguration
specifier|public
class|class
name|ClusterDiscoveryConfiguration
extends|extends
name|SettingsSource
block|{
DECL|field|DEFAULT_SETTINGS
specifier|public
specifier|static
name|Settings
name|DEFAULT_SETTINGS
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|field|numOfNodes
specifier|final
name|int
name|numOfNodes
decl_stmt|;
DECL|field|baseSettings
specifier|final
name|Settings
name|baseSettings
decl_stmt|;
DECL|method|ClusterDiscoveryConfiguration
specifier|public
name|ClusterDiscoveryConfiguration
parameter_list|(
name|int
name|numOfNodes
parameter_list|)
block|{
name|this
argument_list|(
name|numOfNodes
argument_list|,
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|ClusterDiscoveryConfiguration
specifier|public
name|ClusterDiscoveryConfiguration
parameter_list|(
name|int
name|numOfNodes
parameter_list|,
name|Settings
name|extraSettings
parameter_list|)
block|{
name|this
operator|.
name|numOfNodes
operator|=
name|numOfNodes
expr_stmt|;
name|this
operator|.
name|baseSettings
operator|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DEFAULT_SETTINGS
argument_list|)
operator|.
name|put
argument_list|(
name|extraSettings
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|node
specifier|public
name|Settings
name|node
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|baseSettings
return|;
block|}
annotation|@
name|Override
DECL|method|transportClient
specifier|public
name|Settings
name|transportClient
parameter_list|()
block|{
return|return
name|baseSettings
return|;
block|}
DECL|class|UnicastZen
specifier|public
specifier|static
class|class
name|UnicastZen
extends|extends
name|ClusterDiscoveryConfiguration
block|{
DECL|field|portRangeCounter
specifier|private
specifier|final
specifier|static
name|AtomicInteger
name|portRangeCounter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|unicastHostOrdinals
specifier|private
specifier|final
name|int
index|[]
name|unicastHostOrdinals
decl_stmt|;
DECL|field|basePort
specifier|private
specifier|final
name|int
name|basePort
decl_stmt|;
DECL|method|UnicastZen
specifier|public
name|UnicastZen
parameter_list|(
name|int
name|numOfNodes
parameter_list|)
block|{
name|this
argument_list|(
name|numOfNodes
argument_list|,
name|numOfNodes
argument_list|)
expr_stmt|;
block|}
DECL|method|UnicastZen
specifier|public
name|UnicastZen
parameter_list|(
name|int
name|numOfNodes
parameter_list|,
name|Settings
name|extraSettings
parameter_list|)
block|{
name|this
argument_list|(
name|numOfNodes
argument_list|,
name|numOfNodes
argument_list|,
name|extraSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|UnicastZen
specifier|public
name|UnicastZen
parameter_list|(
name|int
name|numOfNodes
parameter_list|,
name|int
name|numOfUnicastHosts
parameter_list|)
block|{
name|this
argument_list|(
name|numOfNodes
argument_list|,
name|numOfUnicastHosts
argument_list|,
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|UnicastZen
specifier|public
name|UnicastZen
parameter_list|(
name|int
name|numOfNodes
parameter_list|,
name|int
name|numOfUnicastHosts
parameter_list|,
name|Settings
name|extraSettings
parameter_list|)
block|{
name|super
argument_list|(
name|numOfNodes
argument_list|,
name|extraSettings
argument_list|)
expr_stmt|;
if|if
condition|(
name|numOfUnicastHosts
operator|==
name|numOfNodes
condition|)
block|{
name|unicastHostOrdinals
operator|=
operator|new
name|int
index|[
name|numOfNodes
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numOfNodes
condition|;
name|i
operator|++
control|)
block|{
name|unicastHostOrdinals
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
else|else
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|ordinals
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|numOfUnicastHosts
argument_list|)
decl_stmt|;
while|while
condition|(
name|ordinals
operator|.
name|size
argument_list|()
operator|!=
name|numOfUnicastHosts
condition|)
block|{
name|ordinals
operator|.
name|add
argument_list|(
name|RandomizedTest
operator|.
name|randomInt
argument_list|(
name|numOfNodes
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|unicastHostOrdinals
operator|=
name|Ints
operator|.
name|toArray
argument_list|(
name|ordinals
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|basePort
operator|=
name|calcBasePort
argument_list|()
expr_stmt|;
block|}
DECL|method|UnicastZen
specifier|public
name|UnicastZen
parameter_list|(
name|int
name|numOfNodes
parameter_list|,
name|int
index|[]
name|unicastHostOrdinals
parameter_list|)
block|{
name|this
argument_list|(
name|numOfNodes
argument_list|,
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|,
name|unicastHostOrdinals
argument_list|)
expr_stmt|;
block|}
DECL|method|UnicastZen
specifier|public
name|UnicastZen
parameter_list|(
name|int
name|numOfNodes
parameter_list|,
name|Settings
name|extraSettings
parameter_list|,
name|int
index|[]
name|unicastHostOrdinals
parameter_list|)
block|{
name|super
argument_list|(
name|numOfNodes
argument_list|,
name|extraSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|unicastHostOrdinals
operator|=
name|unicastHostOrdinals
expr_stmt|;
name|this
operator|.
name|basePort
operator|=
name|calcBasePort
argument_list|()
expr_stmt|;
block|}
DECL|method|calcBasePort
specifier|private
specifier|final
specifier|static
name|int
name|calcBasePort
parameter_list|()
block|{
comment|// note that this has properly co-exist with the port logic at InternalTestCluster's constructor
return|return
literal|30000
operator|+
literal|1000
operator|*
operator|(
name|ElasticsearchIntegrationTest
operator|.
name|CHILD_JVM_ID
operator|%
literal|60
operator|)
operator|+
comment|// up to 60 jvms
literal|100
operator|*
name|portRangeCounter
operator|.
name|incrementAndGet
argument_list|()
return|;
comment|// up to 100 nodes
block|}
annotation|@
name|Override
DECL|method|node
specifier|public
name|Settings
name|node
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
name|ImmutableSettings
operator|.
name|Builder
name|builder
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.zen.ping.multicast.enabled"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
index|[]
name|unicastHosts
init|=
operator|new
name|String
index|[
name|unicastHostOrdinals
operator|.
name|length
index|]
decl_stmt|;
name|String
name|mode
init|=
name|baseSettings
operator|.
name|get
argument_list|(
literal|"node.mode"
argument_list|,
name|InternalTestCluster
operator|.
name|NODE_MODE
argument_list|)
decl_stmt|;
if|if
condition|(
name|mode
operator|.
name|equals
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|LocalTransport
operator|.
name|TRANSPORT_LOCAL_ADDRESS
argument_list|,
literal|"node_"
operator|+
name|nodeOrdinal
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|unicastHosts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|unicastHosts
index|[
name|i
index|]
operator|=
literal|"node_"
operator|+
name|unicastHostOrdinals
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// we need to pin the node port& host so we'd know where to point things
name|builder
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|basePort
operator|+
name|nodeOrdinal
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"transport.host"
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|unicastHosts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|unicastHosts
index|[
name|i
index|]
operator|=
literal|"localhost:"
operator|+
operator|(
name|basePort
operator|+
name|unicastHostOrdinals
index|[
name|i
index|]
operator|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|putArray
argument_list|(
literal|"discovery.zen.ping.unicast.hosts"
argument_list|,
name|unicastHosts
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|put
argument_list|(
name|super
operator|.
name|node
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

