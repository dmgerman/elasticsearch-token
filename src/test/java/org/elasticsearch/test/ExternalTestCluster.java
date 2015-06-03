begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|Lists
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
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
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
name|node
operator|.
name|stats
operator|.
name|NodeStats
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
name|node
operator|.
name|stats
operator|.
name|NodesStatsResponse
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
name|client
operator|.
name|transport
operator|.
name|TransportClient
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
name|breaker
operator|.
name|CircuitBreaker
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|transport
operator|.
name|InetSocketTransportAddress
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
name|transport
operator|.
name|TransportAddress
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
name|internal
operator|.
name|InternalSettingsPreparer
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
name|net
operator|.
name|InetSocketAddress
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
import|;
end_import

begin_comment
comment|/**  * External cluster to run the tests against.  * It is a pure immutable test cluster that allows to send requests to a pre-existing cluster  * and supports by nature all the needed test operations like wipeIndices etc.  */
end_comment

begin_class
DECL|class|ExternalTestCluster
specifier|public
specifier|final
class|class
name|ExternalTestCluster
extends|extends
name|TestCluster
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ExternalTestCluster
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|counter
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|EXTERNAL_CLUSTER_PREFIX
specifier|public
specifier|static
specifier|final
name|String
name|EXTERNAL_CLUSTER_PREFIX
init|=
literal|"external_"
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|httpAddresses
specifier|private
specifier|final
name|InetSocketAddress
index|[]
name|httpAddresses
decl_stmt|;
DECL|field|clusterName
specifier|private
specifier|final
name|String
name|clusterName
decl_stmt|;
DECL|field|numDataNodes
specifier|private
specifier|final
name|int
name|numDataNodes
decl_stmt|;
DECL|field|numMasterAndDataNodes
specifier|private
specifier|final
name|int
name|numMasterAndDataNodes
decl_stmt|;
DECL|method|ExternalTestCluster
specifier|public
name|ExternalTestCluster
parameter_list|(
name|TransportAddress
modifier|...
name|transportAddresses
parameter_list|)
block|{
name|super
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Settings
name|clientSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|InternalTestCluster
operator|.
name|TRANSPORT_CLIENT_PREFIX
operator|+
name|EXTERNAL_CLUSTER_PREFIX
operator|+
name|counter
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|InternalSettingsPreparer
operator|.
name|IGNORE_SYSTEM_PROPERTIES_SETTING
argument_list|,
literal|true
argument_list|)
comment|// prevents any settings to be replaced by system properties.
operator|.
name|put
argument_list|(
literal|"client.transport.ignore_cluster_name"
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.mode"
argument_list|,
literal|"network"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// we require network here!
name|this
operator|.
name|client
operator|=
name|TransportClient
operator|.
name|builder
argument_list|()
operator|.
name|settings
argument_list|(
name|clientSettings
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|addTransportAddresses
argument_list|(
name|transportAddresses
argument_list|)
expr_stmt|;
name|NodesInfoResponse
name|nodeInfos
init|=
name|this
operator|.
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setSettings
argument_list|(
literal|true
argument_list|)
operator|.
name|setHttp
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|httpAddresses
operator|=
operator|new
name|InetSocketAddress
index|[
name|nodeInfos
operator|.
name|getNodes
argument_list|()
operator|.
name|length
index|]
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|nodeInfos
operator|.
name|getClusterName
argument_list|()
operator|.
name|value
argument_list|()
expr_stmt|;
name|int
name|dataNodes
init|=
literal|0
decl_stmt|;
name|int
name|masterAndDataNodes
init|=
literal|0
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
name|nodeInfos
operator|.
name|getNodes
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|NodeInfo
name|nodeInfo
init|=
name|nodeInfos
operator|.
name|getNodes
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|httpAddresses
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|nodeInfo
operator|.
name|getHttp
argument_list|()
operator|.
name|address
argument_list|()
operator|.
name|publishAddress
argument_list|()
operator|)
operator|.
name|address
argument_list|()
expr_stmt|;
if|if
condition|(
name|DiscoveryNode
operator|.
name|dataNode
argument_list|(
name|nodeInfo
operator|.
name|getSettings
argument_list|()
argument_list|)
condition|)
block|{
name|dataNodes
operator|++
expr_stmt|;
name|masterAndDataNodes
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|DiscoveryNode
operator|.
name|masterNode
argument_list|(
name|nodeInfo
operator|.
name|getSettings
argument_list|()
argument_list|)
condition|)
block|{
name|masterAndDataNodes
operator|++
expr_stmt|;
block|}
block|}
name|this
operator|.
name|numDataNodes
operator|=
name|dataNodes
expr_stmt|;
name|this
operator|.
name|numMasterAndDataNodes
operator|=
name|masterAndDataNodes
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Setup ExternalTestCluster [{}] made of [{}] nodes"
argument_list|,
name|nodeInfos
operator|.
name|getClusterName
argument_list|()
operator|.
name|value
argument_list|()
argument_list|,
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|afterTest
specifier|public
name|void
name|afterTest
parameter_list|()
block|{      }
annotation|@
name|Override
DECL|method|client
specifier|public
name|Client
name|client
parameter_list|()
block|{
return|return
name|client
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|httpAddresses
operator|.
name|length
return|;
block|}
annotation|@
name|Override
DECL|method|numDataNodes
specifier|public
name|int
name|numDataNodes
parameter_list|()
block|{
return|return
name|numDataNodes
return|;
block|}
annotation|@
name|Override
DECL|method|numDataAndMasterNodes
specifier|public
name|int
name|numDataAndMasterNodes
parameter_list|()
block|{
return|return
name|numMasterAndDataNodes
return|;
block|}
annotation|@
name|Override
DECL|method|httpAddresses
specifier|public
name|InetSocketAddress
index|[]
name|httpAddresses
parameter_list|()
block|{
return|return
name|httpAddresses
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ensureEstimatedStats
specifier|public
name|void
name|ensureEstimatedStats
parameter_list|()
block|{
if|if
condition|(
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|NodesStatsResponse
name|nodeStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesStats
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setBreaker
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeStats
name|stats
range|:
name|nodeStats
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
literal|"Fielddata breaker not reset to 0 on node: "
operator|+
name|stats
operator|.
name|getNode
argument_list|()
argument_list|,
name|stats
operator|.
name|getBreaker
argument_list|()
operator|.
name|getStats
argument_list|(
name|CircuitBreaker
operator|.
name|FIELDDATA
argument_list|)
operator|.
name|getEstimated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
comment|// ExternalTestCluster does not check the request breaker,
comment|// because checking it requires a network request, which in
comment|// turn increments the breaker, making it non-0
name|assertThat
argument_list|(
literal|"Fielddata size must be 0 on node: "
operator|+
name|stats
operator|.
name|getNode
argument_list|()
argument_list|,
name|stats
operator|.
name|getIndices
argument_list|()
operator|.
name|getFieldData
argument_list|()
operator|.
name|getMemorySizeInBytes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Filter cache size must be 0 on node: "
operator|+
name|stats
operator|.
name|getNode
argument_list|()
argument_list|,
name|stats
operator|.
name|getIndices
argument_list|()
operator|.
name|getFilterCache
argument_list|()
operator|.
name|getMemorySizeInBytes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"FixedBitSet cache size must be 0 on node: "
operator|+
name|stats
operator|.
name|getNode
argument_list|()
argument_list|,
name|stats
operator|.
name|getIndices
argument_list|()
operator|.
name|getSegments
argument_list|()
operator|.
name|getBitsetMemoryInBytes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Client
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|client
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
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
block|}
end_class

end_unit

