begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
package|;
end_package

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
name|BytesStreamOutput
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
name|util
operator|.
name|set
operator|.
name|Sets
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
name|common
operator|.
name|xcontent
operator|.
name|json
operator|.
name|JsonXContent
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
name|test
operator|.
name|ESTestCase
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
name|Set
import|;
end_import

begin_class
DECL|class|IndexMetaDataTests
specifier|public
class|class
name|IndexMetaDataTests
extends|extends
name|ESTestCase
block|{
DECL|method|testIndexMetaDataSerialization
specifier|public
name|void
name|testIndexMetaDataSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|Integer
name|numShard
init|=
name|randomFrom
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|4
argument_list|,
literal|8
argument_list|,
literal|16
argument_list|)
decl_stmt|;
name|int
name|numberOfReplicas
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.version.created"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
name|numShard
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|numberOfReplicas
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|creationDate
argument_list|(
name|randomLong
argument_list|()
argument_list|)
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
operator|.
name|setRoutingNumShards
argument_list|(
literal|32
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|metaData
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|XContentParser
name|parser
init|=
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndexMetaData
name|fromXContentMeta
init|=
name|IndexMetaData
operator|.
name|PROTO
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|metaData
argument_list|,
name|fromXContentMeta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|hashCode
argument_list|()
argument_list|,
name|fromXContentMeta
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getNumberOfReplicas
argument_list|()
argument_list|,
name|fromXContentMeta
operator|.
name|getNumberOfReplicas
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getNumberOfShards
argument_list|()
argument_list|,
name|fromXContentMeta
operator|.
name|getNumberOfShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getCreationVersion
argument_list|()
argument_list|,
name|fromXContentMeta
operator|.
name|getCreationVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getRoutingNumShards
argument_list|()
argument_list|,
name|fromXContentMeta
operator|.
name|getRoutingNumShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getCreationDate
argument_list|()
argument_list|,
name|fromXContentMeta
operator|.
name|getCreationDate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getRoutingFactor
argument_list|()
argument_list|,
name|fromXContentMeta
operator|.
name|getRoutingFactor
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|)
argument_list|,
name|fromXContentMeta
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|metaData
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|IndexMetaData
name|deserialized
init|=
name|IndexMetaData
operator|.
name|PROTO
operator|.
name|readFrom
argument_list|(
name|StreamInput
operator|.
name|wrap
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|metaData
argument_list|,
name|deserialized
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|hashCode
argument_list|()
argument_list|,
name|deserialized
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getNumberOfReplicas
argument_list|()
argument_list|,
name|deserialized
operator|.
name|getNumberOfReplicas
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getNumberOfShards
argument_list|()
argument_list|,
name|deserialized
operator|.
name|getNumberOfShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getCreationVersion
argument_list|()
argument_list|,
name|deserialized
operator|.
name|getCreationVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getRoutingNumShards
argument_list|()
argument_list|,
name|deserialized
operator|.
name|getRoutingNumShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getCreationDate
argument_list|()
argument_list|,
name|deserialized
operator|.
name|getCreationDate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|getRoutingFactor
argument_list|()
argument_list|,
name|deserialized
operator|.
name|getRoutingFactor
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|)
argument_list|,
name|deserialized
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetRoutingFactor
specifier|public
name|void
name|testGetRoutingFactor
parameter_list|()
block|{
name|int
name|numberOfReplicas
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.version.created"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|32
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|numberOfReplicas
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|creationDate
argument_list|(
name|randomLong
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Integer
name|numShard
init|=
name|randomFrom
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|4
argument_list|,
literal|8
argument_list|,
literal|16
argument_list|)
decl_stmt|;
name|int
name|routingFactor
init|=
name|IndexMetaData
operator|.
name|getRoutingFactor
argument_list|(
name|metaData
argument_list|,
name|numShard
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|routingFactor
operator|*
name|numShard
argument_list|,
name|metaData
operator|.
name|getNumberOfShards
argument_list|()
argument_list|)
expr_stmt|;
name|Integer
name|brokenNumShards
init|=
name|randomFrom
argument_list|(
literal|3
argument_list|,
literal|5
argument_list|,
literal|9
argument_list|,
literal|12
argument_list|,
literal|29
argument_list|,
literal|42
argument_list|,
literal|64
argument_list|)
decl_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|IndexMetaData
operator|.
name|getRoutingFactor
argument_list|(
name|metaData
argument_list|,
name|brokenNumShards
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSelectShrinkShards
specifier|public
name|void
name|testSelectShrinkShards
parameter_list|()
block|{
name|int
name|numberOfReplicas
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.version.created"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|32
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|numberOfReplicas
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|creationDate
argument_list|(
name|randomLong
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|ShardId
argument_list|>
name|shardIds
init|=
name|IndexMetaData
operator|.
name|selectShrinkShards
argument_list|(
literal|0
argument_list|,
name|metaData
argument_list|,
literal|8
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|shardIds
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|1
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|2
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|shardIds
operator|=
name|IndexMetaData
operator|.
name|selectShrinkShards
argument_list|(
literal|1
argument_list|,
name|metaData
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardIds
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|4
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|5
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|6
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|7
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|shardIds
operator|=
name|IndexMetaData
operator|.
name|selectShrinkShards
argument_list|(
literal|7
argument_list|,
name|metaData
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardIds
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|28
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|29
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|30
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
name|metaData
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|31
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"the number of target shards (8) must be greater than the shard id: 8"
argument_list|,
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|IndexMetaData
operator|.
name|selectShrinkShards
argument_list|(
literal|8
argument_list|,
name|metaData
argument_list|,
literal|8
argument_list|)
argument_list|)
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

