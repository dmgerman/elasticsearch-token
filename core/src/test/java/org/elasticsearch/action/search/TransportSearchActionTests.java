begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
package|;
end_package

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
name|action
operator|.
name|OriginalIndices
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
name|shards
operator|.
name|ClusterSearchShardsGroup
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
name|shards
operator|.
name|ClusterSearchShardsResponse
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
name|support
operator|.
name|IndicesOptions
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
name|GroupShardsIterator
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
name|PlainShardIterator
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
name|ShardIterator
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
name|ShardRouting
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
name|ShardRoutingState
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
name|TestShardRouting
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
name|index
operator|.
name|query
operator|.
name|MatchAllQueryBuilder
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
name|query
operator|.
name|TermsQueryBuilder
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
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|AliasFilter
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
name|ESTestCase
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
name|transport
operator|.
name|MockTransportService
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
name|TestThreadPool
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
name|RemoteClusterService
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|STARTED
import|;
end_import

begin_class
DECL|class|TransportSearchActionTests
specifier|public
class|class
name|TransportSearchActionTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
init|=
operator|new
name|TestThreadPool
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|threadPool
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
DECL|method|testMergeShardsIterators
specifier|public
name|void
name|testMergeShardsIterators
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ShardIterator
argument_list|>
name|localShardIterators
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
block|{
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"local_index"
argument_list|,
literal|"local_index_uuid"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|shardId
argument_list|,
literal|"local_node"
argument_list|,
literal|true
argument_list|,
name|STARTED
argument_list|)
decl_stmt|;
name|ShardIterator
name|shardIterator
init|=
operator|new
name|PlainShardIterator
argument_list|(
name|shardId
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|shardRouting
argument_list|)
argument_list|)
decl_stmt|;
name|localShardIterators
operator|.
name|add
argument_list|(
name|shardIterator
argument_list|)
expr_stmt|;
block|}
block|{
name|ShardId
name|shardId2
init|=
operator|new
name|ShardId
argument_list|(
literal|"local_index_2"
argument_list|,
literal|"local_index_2_uuid"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ShardRouting
name|shardRouting2
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|shardId2
argument_list|,
literal|"local_node"
argument_list|,
literal|true
argument_list|,
name|STARTED
argument_list|)
decl_stmt|;
name|ShardIterator
name|shardIterator2
init|=
operator|new
name|PlainShardIterator
argument_list|(
name|shardId2
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|shardRouting2
argument_list|)
argument_list|)
decl_stmt|;
name|localShardIterators
operator|.
name|add
argument_list|(
name|shardIterator2
argument_list|)
expr_stmt|;
block|}
name|GroupShardsIterator
argument_list|<
name|ShardIterator
argument_list|>
name|localShardsIterator
init|=
operator|new
name|GroupShardsIterator
argument_list|<>
argument_list|(
name|localShardIterators
argument_list|)
decl_stmt|;
name|OriginalIndices
name|localIndices
init|=
operator|new
name|OriginalIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"local_alias"
block|,
literal|"local_index_2"
block|}
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
decl_stmt|;
name|OriginalIndices
name|remoteIndices
init|=
operator|new
name|OriginalIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"remote_alias"
block|,
literal|"remote_index_2"
block|}
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpen
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SearchShardIterator
argument_list|>
name|remoteShardIterators
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
block|{
name|ShardId
name|remoteShardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"remote_index"
argument_list|,
literal|"remote_index_uuid"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|ShardRouting
name|remoteShardRouting
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|remoteShardId
argument_list|,
literal|"remote_node"
argument_list|,
literal|true
argument_list|,
name|STARTED
argument_list|)
decl_stmt|;
name|SearchShardIterator
name|remoteShardIterator
init|=
operator|new
name|SearchShardIterator
argument_list|(
literal|"remote"
argument_list|,
name|remoteShardId
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|remoteShardRouting
argument_list|)
argument_list|,
name|remoteIndices
argument_list|)
decl_stmt|;
name|remoteShardIterators
operator|.
name|add
argument_list|(
name|remoteShardIterator
argument_list|)
expr_stmt|;
block|}
block|{
name|ShardId
name|remoteShardId2
init|=
operator|new
name|ShardId
argument_list|(
literal|"remote_index_2"
argument_list|,
literal|"remote_index_2_uuid"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|ShardRouting
name|remoteShardRouting2
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|remoteShardId2
argument_list|,
literal|"remote_node"
argument_list|,
literal|true
argument_list|,
name|STARTED
argument_list|)
decl_stmt|;
name|SearchShardIterator
name|remoteShardIterator2
init|=
operator|new
name|SearchShardIterator
argument_list|(
literal|"remote"
argument_list|,
name|remoteShardId2
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|remoteShardRouting2
argument_list|)
argument_list|,
name|remoteIndices
argument_list|)
decl_stmt|;
name|remoteShardIterators
operator|.
name|add
argument_list|(
name|remoteShardIterator2
argument_list|)
expr_stmt|;
block|}
name|OriginalIndices
name|remoteIndices2
init|=
operator|new
name|OriginalIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"remote_index_3"
block|}
argument_list|,
name|IndicesOptions
operator|.
name|strictExpand
argument_list|()
argument_list|)
decl_stmt|;
block|{
name|ShardId
name|remoteShardId3
init|=
operator|new
name|ShardId
argument_list|(
literal|"remote_index_3"
argument_list|,
literal|"remote_index_3_uuid"
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|ShardRouting
name|remoteShardRouting3
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|remoteShardId3
argument_list|,
literal|"remote_node"
argument_list|,
literal|true
argument_list|,
name|STARTED
argument_list|)
decl_stmt|;
name|SearchShardIterator
name|remoteShardIterator3
init|=
operator|new
name|SearchShardIterator
argument_list|(
literal|"remote"
argument_list|,
name|remoteShardId3
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|remoteShardRouting3
argument_list|)
argument_list|,
name|remoteIndices2
argument_list|)
decl_stmt|;
name|remoteShardIterators
operator|.
name|add
argument_list|(
name|remoteShardIterator3
argument_list|)
expr_stmt|;
block|}
name|GroupShardsIterator
argument_list|<
name|SearchShardIterator
argument_list|>
name|searchShardIterators
init|=
name|TransportSearchAction
operator|.
name|mergeShardsIterators
argument_list|(
name|localShardsIterator
argument_list|,
name|localIndices
argument_list|,
name|remoteShardIterators
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|searchShardIterators
operator|.
name|size
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|SearchShardIterator
name|searchShardIterator
range|:
name|searchShardIterators
control|)
block|{
switch|switch
condition|(
name|i
operator|++
condition|)
block|{
case|case
literal|0
case|:
name|assertEquals
argument_list|(
literal|"local_index"
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|localIndices
argument_list|,
name|searchShardIterator
operator|.
name|getOriginalIndices
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|assertEquals
argument_list|(
literal|"local_index_2"
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|localIndices
argument_list|,
name|searchShardIterator
operator|.
name|getOriginalIndices
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|assertEquals
argument_list|(
literal|"remote_index"
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|remoteIndices
argument_list|,
name|searchShardIterator
operator|.
name|getOriginalIndices
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|assertEquals
argument_list|(
literal|"remote_index_2"
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|remoteIndices
argument_list|,
name|searchShardIterator
operator|.
name|getOriginalIndices
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|assertEquals
argument_list|(
literal|"remote_index_3"
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|searchShardIterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|remoteIndices2
argument_list|,
name|searchShardIterator
operator|.
name|getOriginalIndices
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
DECL|method|testProcessRemoteShards
specifier|public
name|void
name|testProcessRemoteShards
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|TransportService
name|transportService
init|=
name|MockTransportService
operator|.
name|createNewService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|threadPool
argument_list|,
literal|null
argument_list|)
init|)
block|{
name|RemoteClusterService
name|service
init|=
name|transportService
operator|.
name|getRemoteClusterService
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|service
operator|.
name|isCrossClusterSearchEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|SearchShardIterator
argument_list|>
name|iteratorList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ClusterSearchShardsResponse
argument_list|>
name|searchShardsResponseMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|DiscoveryNode
index|[]
name|nodes
init|=
operator|new
name|DiscoveryNode
index|[]
block|{
operator|new
name|DiscoveryNode
argument_list|(
literal|"node1"
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
block|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"node2"
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
block|}
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|indicesAndAliases
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|indicesAndAliases
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|AliasFilter
argument_list|(
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
literal|"some_alias_for_foo"
argument_list|,
literal|"some_other_foo_alias"
argument_list|)
argument_list|)
expr_stmt|;
name|indicesAndAliases
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|AliasFilter
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterSearchShardsGroup
index|[]
name|groups
init|=
operator|new
name|ClusterSearchShardsGroup
index|[]
block|{
operator|new
name|ClusterSearchShardsGroup
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|"foo_id"
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ShardRouting
index|[]
block|{
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|,
literal|"node1"
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
block|,
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|,
literal|"node2"
argument_list|,
literal|false
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
block|}
argument_list|)
block|,
operator|new
name|ClusterSearchShardsGroup
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|"foo_id"
argument_list|,
literal|1
argument_list|)
argument_list|,
operator|new
name|ShardRouting
index|[]
block|{
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|,
literal|"node1"
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
block|,
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|,
literal|"node2"
argument_list|,
literal|false
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
block|}
argument_list|)
block|,
operator|new
name|ClusterSearchShardsGroup
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"bar"
argument_list|,
literal|"bar_id"
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ShardRouting
index|[]
block|{
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
literal|"bar"
argument_list|,
literal|0
argument_list|,
literal|"node2"
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
block|,
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
literal|"bar"
argument_list|,
literal|0
argument_list|,
literal|"node1"
argument_list|,
literal|false
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
block|}
argument_list|)
block|}
decl_stmt|;
name|searchShardsResponseMap
operator|.
name|put
argument_list|(
literal|"test_cluster_1"
argument_list|,
operator|new
name|ClusterSearchShardsResponse
argument_list|(
name|groups
argument_list|,
name|nodes
argument_list|,
name|indicesAndAliases
argument_list|)
argument_list|)
expr_stmt|;
name|DiscoveryNode
index|[]
name|nodes2
init|=
operator|new
name|DiscoveryNode
index|[]
block|{
operator|new
name|DiscoveryNode
argument_list|(
literal|"node3"
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
block|}
decl_stmt|;
name|ClusterSearchShardsGroup
index|[]
name|groups2
init|=
operator|new
name|ClusterSearchShardsGroup
index|[]
block|{
operator|new
name|ClusterSearchShardsGroup
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"xyz"
argument_list|,
literal|"xyz_id"
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ShardRouting
index|[]
block|{
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
literal|"xyz"
argument_list|,
literal|0
argument_list|,
literal|"node3"
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
block|}
argument_list|)
block|}
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|filter
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|filter
operator|.
name|put
argument_list|(
literal|"xyz"
argument_list|,
operator|new
name|AliasFilter
argument_list|(
literal|null
argument_list|,
literal|"some_alias_for_xyz"
argument_list|)
argument_list|)
expr_stmt|;
name|searchShardsResponseMap
operator|.
name|put
argument_list|(
literal|"test_cluster_2"
argument_list|,
operator|new
name|ClusterSearchShardsResponse
argument_list|(
name|groups2
argument_list|,
name|nodes2
argument_list|,
name|filter
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|OriginalIndices
argument_list|>
name|remoteIndicesByCluster
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|remoteIndicesByCluster
operator|.
name|put
argument_list|(
literal|"test_cluster_1"
argument_list|,
operator|new
name|OriginalIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"fo*"
block|,
literal|"ba*"
block|}
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|remoteIndicesByCluster
operator|.
name|put
argument_list|(
literal|"test_cluster_2"
argument_list|,
operator|new
name|OriginalIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"x*"
block|}
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|remoteAliases
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|TransportSearchAction
operator|.
name|processRemoteShards
argument_list|(
name|searchShardsResponseMap
argument_list|,
name|remoteIndicesByCluster
argument_list|,
name|iteratorList
argument_list|,
name|remoteAliases
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|iteratorList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchShardIterator
name|iterator
range|:
name|iteratorList
control|)
block|{
if|if
condition|(
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"foo"
argument_list|)
condition|)
block|{
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"some_alias_for_foo"
block|,
literal|"some_other_foo_alias"
block|}
argument_list|,
name|iterator
operator|.
name|getOriginalIndices
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
operator|==
literal|0
operator|||
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test_cluster_1:foo"
argument_list|,
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|iterator
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardRouting
operator|.
name|getIndexName
argument_list|()
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|shardRouting
operator|=
name|iterator
operator|.
name|nextOrNull
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardRouting
operator|.
name|getIndexName
argument_list|()
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|iterator
operator|.
name|nextOrNull
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"bar"
argument_list|)
condition|)
block|{
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"bar"
block|}
argument_list|,
name|iterator
operator|.
name|getOriginalIndices
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test_cluster_1:bar"
argument_list|,
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|iterator
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardRouting
operator|.
name|getIndexName
argument_list|()
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|shardRouting
operator|=
name|iterator
operator|.
name|nextOrNull
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardRouting
operator|.
name|getIndexName
argument_list|()
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|iterator
operator|.
name|nextOrNull
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"xyz"
argument_list|)
condition|)
block|{
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"some_alias_for_xyz"
block|}
argument_list|,
name|iterator
operator|.
name|getOriginalIndices
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test_cluster_2:xyz"
argument_list|,
name|iterator
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|iterator
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardRouting
operator|.
name|getIndexName
argument_list|()
argument_list|,
literal|"xyz"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|iterator
operator|.
name|nextOrNull
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|remoteAliases
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|remoteAliases
operator|.
name|toString
argument_list|()
argument_list|,
name|remoteAliases
operator|.
name|containsKey
argument_list|(
literal|"foo_id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|remoteAliases
operator|.
name|toString
argument_list|()
argument_list|,
name|remoteAliases
operator|.
name|containsKey
argument_list|(
literal|"bar_id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|remoteAliases
operator|.
name|toString
argument_list|()
argument_list|,
name|remoteAliases
operator|.
name|containsKey
argument_list|(
literal|"xyz_id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
name|remoteAliases
operator|.
name|get
argument_list|(
literal|"foo_id"
argument_list|)
operator|.
name|getQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
name|remoteAliases
operator|.
name|get
argument_list|(
literal|"bar_id"
argument_list|)
operator|.
name|getQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|remoteAliases
operator|.
name|get
argument_list|(
literal|"xyz_id"
argument_list|)
operator|.
name|getQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

