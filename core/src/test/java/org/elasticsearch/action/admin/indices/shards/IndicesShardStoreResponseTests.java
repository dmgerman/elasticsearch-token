begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.shards
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|shards
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|CollectionUtil
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
name|bytes
operator|.
name|BytesReference
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
name|collect
operator|.
name|ImmutableOpenIntMap
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|DummyTransportAddress
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentParser
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
name|xcontent
operator|.
name|XContentType
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
name|ShardStateMetaData
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
name|transport
operator|.
name|NodeDisconnectedException
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

begin_class
DECL|class|IndicesShardStoreResponseTests
specifier|public
class|class
name|IndicesShardStoreResponseTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBasicSerialization
specifier|public
name|void
name|testBasicSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|indexStoreStatuses
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|Failure
argument_list|>
name|failures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ImmutableOpenIntMap
operator|.
name|Builder
argument_list|<
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
argument_list|>
name|storeStatuses
init|=
name|ImmutableOpenIntMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|node1
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node2
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node2"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|storeStatusList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|storeStatusList
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
literal|3
argument_list|,
literal|null
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|PRIMARY
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|storeStatusList
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node2
argument_list|,
name|ShardStateMetaData
operator|.
name|NO_VERSION
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|REPLICA
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|storeStatusList
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
name|ShardStateMetaData
operator|.
name|NO_VERSION
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|UNUSED
argument_list|,
operator|new
name|IOException
argument_list|(
literal|"corrupted"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|storeStatuses
operator|.
name|put
argument_list|(
literal|0
argument_list|,
name|storeStatusList
argument_list|)
expr_stmt|;
name|storeStatuses
operator|.
name|put
argument_list|(
literal|1
argument_list|,
name|storeStatusList
argument_list|)
expr_stmt|;
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
argument_list|>
name|storesMap
init|=
name|storeStatuses
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexStoreStatuses
operator|.
name|put
argument_list|(
literal|"test"
argument_list|,
name|storesMap
argument_list|)
expr_stmt|;
name|indexStoreStatuses
operator|.
name|put
argument_list|(
literal|"test2"
argument_list|,
name|storesMap
argument_list|)
expr_stmt|;
name|failures
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|Failure
argument_list|(
literal|"node1"
argument_list|,
literal|"test"
argument_list|,
literal|3
argument_list|,
operator|new
name|NodeDisconnectedException
argument_list|(
name|node1
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IndicesShardStoresResponse
name|storesResponse
init|=
operator|new
name|IndicesShardStoresResponse
argument_list|(
name|indexStoreStatuses
operator|.
name|build
argument_list|()
argument_list|,
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|failures
argument_list|)
argument_list|)
decl_stmt|;
name|XContentBuilder
name|contentBuilder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|contentBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|storesResponse
operator|.
name|toXContent
argument_list|(
name|contentBuilder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|contentBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|BytesReference
name|bytes
init|=
name|contentBuilder
operator|.
name|bytes
argument_list|()
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
init|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
name|List
name|failureList
init|=
operator|(
name|List
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"failures"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|failureList
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|HashMap
name|failureMap
init|=
operator|(
name|HashMap
operator|)
name|failureList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|failureMap
operator|.
name|containsKey
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|String
operator|)
name|failureMap
operator|.
name|get
argument_list|(
literal|"index"
argument_list|)
operator|)
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|failureMap
operator|.
name|containsKey
argument_list|(
literal|"shard"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|int
operator|)
name|failureMap
operator|.
name|get
argument_list|(
literal|"shard"
argument_list|)
operator|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|failureMap
operator|.
name|containsKey
argument_list|(
literal|"node"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|String
operator|)
name|failureMap
operator|.
name|get
argument_list|(
literal|"node"
argument_list|)
operator|)
argument_list|,
name|equalTo
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|indices
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"indices"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
operator|new
name|String
index|[]
block|{
literal|"test"
block|,
literal|"test2"
block|}
control|)
block|{
name|assertThat
argument_list|(
name|indices
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|shards
init|=
operator|(
call|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
call|)
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|indices
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|"shards"
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|shardId
range|:
name|shards
operator|.
name|keySet
argument_list|()
control|)
block|{
name|HashMap
name|shardStoresStatus
init|=
operator|(
name|HashMap
operator|)
name|shards
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shardStoresStatus
operator|.
name|containsKey
argument_list|(
literal|"stores"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|List
name|stores
init|=
operator|(
name|ArrayList
operator|)
name|shardStoresStatus
operator|.
name|get
argument_list|(
literal|"stores"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|stores
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|storeStatusList
operator|.
name|size
argument_list|()
argument_list|)
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
name|stores
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|HashMap
name|storeInfo
init|=
operator|(
operator|(
name|HashMap
operator|)
name|stores
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
decl_stmt|;
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|storeStatus
init|=
name|storeStatusList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|boolean
name|eitherLegacyVersionOrAllocationIdSet
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|storeInfo
operator|.
name|containsKey
argument_list|(
literal|"legacy_version"
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|int
operator|)
name|storeInfo
operator|.
name|get
argument_list|(
literal|"legacy_version"
argument_list|)
operator|)
argument_list|,
name|equalTo
argument_list|(
operator|(
operator|(
name|int
operator|)
name|storeStatus
operator|.
name|getLegacyVersion
argument_list|()
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|eitherLegacyVersionOrAllocationIdSet
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|storeInfo
operator|.
name|containsKey
argument_list|(
literal|"allocation_id"
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|String
operator|)
name|storeInfo
operator|.
name|get
argument_list|(
literal|"allocation_id"
argument_list|)
operator|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|storeStatus
operator|.
name|getAllocationId
argument_list|()
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|eitherLegacyVersionOrAllocationIdSet
operator|=
literal|true
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|eitherLegacyVersionOrAllocationIdSet
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|storeInfo
operator|.
name|containsKey
argument_list|(
literal|"allocation"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|String
operator|)
name|storeInfo
operator|.
name|get
argument_list|(
literal|"allocation"
argument_list|)
operator|)
argument_list|,
name|equalTo
argument_list|(
name|storeStatus
operator|.
name|getAllocationStatus
argument_list|()
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|storeInfo
operator|.
name|containsKey
argument_list|(
name|storeStatus
operator|.
name|getNode
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeStatus
operator|.
name|getStoreException
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|storeInfo
operator|.
name|containsKey
argument_list|(
literal|"store_exception"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
DECL|method|testStoreStatusOrdering
specifier|public
name|void
name|testStoreStatusOrdering
parameter_list|()
throws|throws
name|Exception
block|{
name|DiscoveryNode
name|node1
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|orderedStoreStatuses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
name|ShardStateMetaData
operator|.
name|NO_VERSION
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|PRIMARY
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
name|ShardStateMetaData
operator|.
name|NO_VERSION
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|REPLICA
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
name|ShardStateMetaData
operator|.
name|NO_VERSION
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|UNUSED
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
literal|2
argument_list|,
literal|null
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|PRIMARY
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|PRIMARY
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|REPLICA
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|UNUSED
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
name|ShardStateMetaData
operator|.
name|NO_VERSION
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|REPLICA
argument_list|,
operator|new
name|IOException
argument_list|(
literal|"corrupted"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|orderedStoreStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node1
argument_list|,
literal|3
argument_list|,
literal|null
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|REPLICA
argument_list|,
operator|new
name|IOException
argument_list|(
literal|"corrupted"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|storeStatuses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|orderedStoreStatuses
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|storeStatuses
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|storeStatuses
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|storeStatuses
argument_list|,
name|equalTo
argument_list|(
name|orderedStoreStatuses
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

