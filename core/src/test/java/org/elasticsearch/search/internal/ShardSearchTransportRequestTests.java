begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
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
name|search
operator|.
name|SearchRequest
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
name|cluster
operator|.
name|routing
operator|.
name|UnassignedInfo
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
name|NamedWriteableAwareStreamInput
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
name|AbstractSearchTestCase
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

begin_class
DECL|class|ShardSearchTransportRequestTests
specifier|public
class|class
name|ShardSearchTransportRequestTests
extends|extends
name|AbstractSearchTestCase
block|{
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|ShardSearchTransportRequest
name|shardSearchTransportRequest
init|=
name|createShardSearchTransportRequest
argument_list|()
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|shardSearchTransportRequest
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
operator|new
name|NamedWriteableAwareStreamInput
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|)
init|)
block|{
name|ShardSearchTransportRequest
name|deserializedRequest
init|=
operator|new
name|ShardSearchTransportRequest
argument_list|()
decl_stmt|;
name|deserializedRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|scroll
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|scroll
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|deserializedRequest
operator|.
name|filteringAliases
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|filteringAliases
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|deserializedRequest
operator|.
name|indices
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|deserializedRequest
operator|.
name|types
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|isProfile
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|isProfile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|nowInMillis
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|nowInMillis
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|source
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|searchType
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|searchType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|numberOfShards
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|numberOfShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|cacheKey
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|cacheKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|deserializedRequest
argument_list|,
name|shardSearchTransportRequest
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|createShardSearchTransportRequest
specifier|private
name|ShardSearchTransportRequest
name|createShardSearchTransportRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|SearchRequest
name|searchRequest
init|=
name|createSearchRequest
argument_list|()
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomInt
argument_list|()
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
literal|null
argument_list|,
literal|null
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|randomFrom
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
literal|"reason"
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|filteringAliases
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|filteringAliases
operator|=
name|generateRandomStringArray
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|filteringAliases
operator|=
name|Strings
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
return|return
operator|new
name|ShardSearchTransportRequest
argument_list|(
name|searchRequest
argument_list|,
name|shardRouting
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|,
name|filteringAliases
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|randomLong
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

