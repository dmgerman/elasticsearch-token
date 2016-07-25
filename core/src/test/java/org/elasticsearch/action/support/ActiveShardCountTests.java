begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
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
name|metadata
operator|.
name|IndexMetaData
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
name|metadata
operator|.
name|MetaData
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
name|IndexRoutingTable
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
name|IndexShardRoutingTable
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
name|RoutingTable
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
name|common
operator|.
name|UUIDs
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
name|ByteBufferStreamInput
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
name|nio
operator|.
name|ByteBuffer
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

begin_comment
comment|/**  * Tests for the {@link ActiveShardCount} class  */
end_comment

begin_class
DECL|class|ActiveShardCountTests
specifier|public
class|class
name|ActiveShardCountTests
extends|extends
name|ESTestCase
block|{
DECL|method|testFromIntValue
specifier|public
name|void
name|testFromIntValue
parameter_list|()
block|{
name|assertSame
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
literal|0
argument_list|)
argument_list|,
name|ActiveShardCount
operator|.
name|NONE
argument_list|)
expr_stmt|;
specifier|final
name|int
name|value
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|value
argument_list|)
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|randomIntBetween
argument_list|(
operator|-
literal|10
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolve
specifier|public
name|void
name|testResolve
parameter_list|()
block|{
comment|// one shard
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|ALL
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|DEFAULT
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|NONE
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|value
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|value
argument_list|)
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
comment|// more than one shard
specifier|final
name|int
name|numNewShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|indexMetaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
name|numNewShards
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|ALL
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|numNewShards
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|DEFAULT
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|NONE
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|value
argument_list|)
operator|.
name|resolve
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|doWriteRead
argument_list|(
name|ActiveShardCount
operator|.
name|ALL
argument_list|)
expr_stmt|;
name|doWriteRead
argument_list|(
name|ActiveShardCount
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
name|doWriteRead
argument_list|(
name|ActiveShardCount
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|doWriteRead
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|50
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseString
specifier|public
name|void
name|testParseString
parameter_list|()
block|{
name|assertSame
argument_list|(
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
literal|"all"
argument_list|)
argument_list|,
name|ActiveShardCount
operator|.
name|ALL
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
literal|null
argument_list|)
argument_list|,
name|ActiveShardCount
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
literal|"0"
argument_list|)
argument_list|,
name|ActiveShardCount
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|int
name|value
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
name|value
operator|+
literal|""
argument_list|)
argument_list|,
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|4
argument_list|,
literal|8
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
literal|"-1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// magic numbers not exposed through API
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
literal|"-2"
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
name|randomIntBetween
argument_list|(
operator|-
literal|10
argument_list|,
operator|-
literal|3
argument_list|)
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|doWriteRead
specifier|private
name|void
name|doWriteRead
parameter_list|(
name|ActiveShardCount
name|activeShardCount
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|activeShardCount
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
specifier|final
name|ByteBufferStreamInput
name|in
init|=
operator|new
name|ByteBufferStreamInput
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|toBytesRef
argument_list|()
operator|.
name|bytes
argument_list|)
argument_list|)
decl_stmt|;
name|ActiveShardCount
name|readActiveShardCount
init|=
name|ActiveShardCount
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
name|activeShardCount
operator|==
name|ActiveShardCount
operator|.
name|DEFAULT
operator|||
name|activeShardCount
operator|==
name|ActiveShardCount
operator|.
name|ALL
operator|||
name|activeShardCount
operator|==
name|ActiveShardCount
operator|.
name|NONE
condition|)
block|{
name|assertSame
argument_list|(
name|activeShardCount
argument_list|,
name|readActiveShardCount
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|activeShardCount
argument_list|,
name|readActiveShardCount
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEnoughShardsActiveZero
specifier|public
name|void
name|testEnoughShardsActiveZero
parameter_list|()
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
specifier|final
name|int
name|numberOfShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numberOfReplicas
init|=
name|randomIntBetween
argument_list|(
literal|4
argument_list|,
literal|7
argument_list|)
decl_stmt|;
specifier|final
name|ActiveShardCount
name|waitForActiveShards
init|=
name|ActiveShardCount
operator|.
name|from
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|initializeWithNewIndex
argument_list|(
name|indexName
argument_list|,
name|numberOfShards
argument_list|,
name|numberOfReplicas
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startPrimaries
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startLessThanWaitOnShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|,
name|waitForActiveShards
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startWaitOnShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|,
name|waitForActiveShards
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startAllShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnoughShardsActiveLevelOne
specifier|public
name|void
name|testEnoughShardsActiveLevelOne
parameter_list|()
block|{
name|runTestForOneActiveShard
argument_list|(
name|ActiveShardCount
operator|.
name|ONE
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnoughShardsActiveLevelDefault
specifier|public
name|void
name|testEnoughShardsActiveLevelDefault
parameter_list|()
block|{
comment|// default is 1
name|runTestForOneActiveShard
argument_list|(
name|ActiveShardCount
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnoughShardsActiveRandom
specifier|public
name|void
name|testEnoughShardsActiveRandom
parameter_list|()
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
specifier|final
name|int
name|numberOfShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numberOfReplicas
init|=
name|randomIntBetween
argument_list|(
literal|4
argument_list|,
literal|7
argument_list|)
decl_stmt|;
specifier|final
name|ActiveShardCount
name|waitForActiveShards
init|=
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
name|numberOfReplicas
argument_list|)
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|initializeWithNewIndex
argument_list|(
name|indexName
argument_list|,
name|numberOfShards
argument_list|,
name|numberOfReplicas
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startPrimaries
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startLessThanWaitOnShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|,
name|waitForActiveShards
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startWaitOnShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|,
name|waitForActiveShards
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startAllShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnoughShardsActiveLevelAll
specifier|public
name|void
name|testEnoughShardsActiveLevelAll
parameter_list|()
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
specifier|final
name|int
name|numberOfShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numberOfReplicas
init|=
name|randomIntBetween
argument_list|(
literal|4
argument_list|,
literal|7
argument_list|)
decl_stmt|;
comment|// both values should represent "all"
specifier|final
name|ActiveShardCount
name|waitForActiveShards
init|=
name|randomBoolean
argument_list|()
condition|?
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|numberOfReplicas
operator|+
literal|1
argument_list|)
else|:
name|ActiveShardCount
operator|.
name|ALL
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|initializeWithNewIndex
argument_list|(
name|indexName
argument_list|,
name|numberOfShards
argument_list|,
name|numberOfReplicas
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startPrimaries
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startLessThanWaitOnShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|,
name|waitForActiveShards
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startAllShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|runTestForOneActiveShard
specifier|private
name|void
name|runTestForOneActiveShard
parameter_list|(
specifier|final
name|ActiveShardCount
name|activeShardCount
parameter_list|)
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
specifier|final
name|int
name|numberOfShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numberOfReplicas
init|=
name|randomIntBetween
argument_list|(
literal|4
argument_list|,
literal|7
argument_list|)
decl_stmt|;
assert|assert
name|activeShardCount
operator|==
name|ActiveShardCount
operator|.
name|ONE
operator|||
name|activeShardCount
operator|==
name|ActiveShardCount
operator|.
name|DEFAULT
assert|;
specifier|final
name|ActiveShardCount
name|waitForActiveShards
init|=
name|activeShardCount
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|initializeWithNewIndex
argument_list|(
name|indexName
argument_list|,
name|numberOfShards
argument_list|,
name|numberOfReplicas
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startPrimaries
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startLessThanWaitOnShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|,
name|waitForActiveShards
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startWaitOnShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|,
name|waitForActiveShards
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|startAllShards
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|waitForActiveShards
operator|.
name|enoughShardsActive
argument_list|(
name|clusterState
argument_list|,
name|indexName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|initializeWithNewIndex
specifier|private
name|ClusterState
name|initializeWithNewIndex
parameter_list|(
specifier|final
name|String
name|indexName
parameter_list|,
specifier|final
name|int
name|numShards
parameter_list|,
specifier|final
name|int
name|numReplicas
parameter_list|)
block|{
comment|// initial index creation and new routing table info
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexName
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
name|numShards
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
name|numReplicas
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RoutingTable
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|addAsNew
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test_cluster"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|startPrimaries
specifier|private
name|ClusterState
name|startPrimaries
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|String
name|indexName
parameter_list|)
block|{
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|routingTable
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
name|IndexRoutingTable
operator|.
name|Builder
name|newIndexRoutingTable
init|=
name|IndexRoutingTable
operator|.
name|builder
argument_list|(
name|indexRoutingTable
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ObjectCursor
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|shardEntry
range|:
name|indexRoutingTable
operator|.
name|getShards
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
specifier|final
name|IndexShardRoutingTable
name|shardRoutingTable
init|=
name|shardEntry
operator|.
name|value
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|shardRoutingTable
operator|.
name|getShards
argument_list|()
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
name|shardRouting
operator|=
name|shardRouting
operator|.
name|initialize
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|8
argument_list|)
argument_list|,
literal|null
argument_list|,
name|shardRouting
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|)
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
block|}
name|newIndexRoutingTable
operator|.
name|addShard
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
name|routingTable
operator|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|routingTable
argument_list|)
operator|.
name|add
argument_list|(
name|newIndexRoutingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|startLessThanWaitOnShards
specifier|private
name|ClusterState
name|startLessThanWaitOnShards
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|String
name|indexName
parameter_list|,
specifier|final
name|ActiveShardCount
name|waitForActiveShards
parameter_list|)
block|{
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|routingTable
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
name|IndexRoutingTable
operator|.
name|Builder
name|newIndexRoutingTable
init|=
name|IndexRoutingTable
operator|.
name|builder
argument_list|(
name|indexRoutingTable
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ObjectCursor
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|shardEntry
range|:
name|indexRoutingTable
operator|.
name|getShards
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
specifier|final
name|IndexShardRoutingTable
name|shardRoutingTable
init|=
name|shardEntry
operator|.
name|value
decl_stmt|;
assert|assert
name|shardRoutingTable
operator|.
name|getSize
argument_list|()
operator|>
literal|2
assert|;
comment|// want less than half, and primary is already started
name|int
name|numToStart
init|=
name|waitForActiveShards
operator|.
name|resolve
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
argument_list|)
operator|-
literal|2
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|shardRoutingTable
operator|.
name|getShards
argument_list|()
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|shardRouting
operator|.
name|active
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|numToStart
operator|>
literal|0
condition|)
block|{
name|shardRouting
operator|=
name|shardRouting
operator|.
name|initialize
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|8
argument_list|)
argument_list|,
literal|null
argument_list|,
name|shardRouting
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|)
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
name|numToStart
operator|--
expr_stmt|;
block|}
block|}
name|newIndexRoutingTable
operator|.
name|addShard
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
name|routingTable
operator|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|routingTable
argument_list|)
operator|.
name|add
argument_list|(
name|newIndexRoutingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|startWaitOnShards
specifier|private
name|ClusterState
name|startWaitOnShards
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|String
name|indexName
parameter_list|,
specifier|final
name|ActiveShardCount
name|waitForActiveShards
parameter_list|)
block|{
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|routingTable
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
name|IndexRoutingTable
operator|.
name|Builder
name|newIndexRoutingTable
init|=
name|IndexRoutingTable
operator|.
name|builder
argument_list|(
name|indexRoutingTable
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ObjectCursor
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|shardEntry
range|:
name|indexRoutingTable
operator|.
name|getShards
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
specifier|final
name|IndexShardRoutingTable
name|shardRoutingTable
init|=
name|shardEntry
operator|.
name|value
decl_stmt|;
assert|assert
name|shardRoutingTable
operator|.
name|getSize
argument_list|()
operator|>
literal|2
assert|;
name|int
name|numToStart
init|=
name|waitForActiveShards
operator|.
name|resolve
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
argument_list|)
operator|-
literal|1
decl_stmt|;
comment|// primary is already started
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|shardRoutingTable
operator|.
name|getShards
argument_list|()
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|shardRouting
operator|.
name|active
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|shardRouting
operator|.
name|active
argument_list|()
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|numToStart
operator|>
literal|0
condition|)
block|{
name|shardRouting
operator|=
name|shardRouting
operator|.
name|initialize
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|8
argument_list|)
argument_list|,
literal|null
argument_list|,
name|shardRouting
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|)
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
name|numToStart
operator|--
expr_stmt|;
block|}
block|}
else|else
block|{
name|numToStart
operator|--
expr_stmt|;
block|}
block|}
name|newIndexRoutingTable
operator|.
name|addShard
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
name|routingTable
operator|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|routingTable
argument_list|)
operator|.
name|add
argument_list|(
name|newIndexRoutingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|startAllShards
specifier|private
name|ClusterState
name|startAllShards
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|String
name|indexName
parameter_list|)
block|{
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|routingTable
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
name|IndexRoutingTable
operator|.
name|Builder
name|newIndexRoutingTable
init|=
name|IndexRoutingTable
operator|.
name|builder
argument_list|(
name|indexRoutingTable
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ObjectCursor
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|shardEntry
range|:
name|indexRoutingTable
operator|.
name|getShards
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
specifier|final
name|IndexShardRoutingTable
name|shardRoutingTable
init|=
name|shardEntry
operator|.
name|value
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|shardRoutingTable
operator|.
name|getShards
argument_list|()
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|shardRouting
operator|.
name|active
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|shardRouting
operator|.
name|active
argument_list|()
operator|==
literal|false
condition|)
block|{
name|shardRouting
operator|=
name|shardRouting
operator|.
name|initialize
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|8
argument_list|)
argument_list|,
literal|null
argument_list|,
name|shardRouting
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|)
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
block|}
block|}
name|newIndexRoutingTable
operator|.
name|addShard
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
name|routingTable
operator|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|routingTable
argument_list|)
operator|.
name|add
argument_list|(
name|newIndexRoutingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

