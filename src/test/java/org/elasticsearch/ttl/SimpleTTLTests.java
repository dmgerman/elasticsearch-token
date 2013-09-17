begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ttl
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ttl
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndicesStatsResponse
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
name|get
operator|.
name|GetResponse
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
name|Priority
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
name|test
operator|.
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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
name|*
import|;
end_import

begin_class
DECL|class|SimpleTTLTests
specifier|public
class|class
name|SimpleTTLTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|purgeInterval
specifier|static
specifier|private
specifier|final
name|long
name|purgeInterval
init|=
literal|200
decl_stmt|;
annotation|@
name|Override
DECL|method|beforeClass
specifier|protected
name|void
name|beforeClass
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"indices.ttl.interval"
argument_list|,
name|purgeInterval
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
comment|// 2 shards to test TTL purge with routing properly
operator|.
name|put
argument_list|(
literal|"cluster.routing.operation.use_type"
argument_list|,
literal|false
argument_list|)
comment|// make sure we control the shard computation
operator|.
name|put
argument_list|(
literal|"cluster.routing.operation.hash.type"
argument_list|,
literal|"djb"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSimpleTTL
specifier|public
name|void
name|testSimpleTTL
parameter_list|()
throws|throws
name|Exception
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type2"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type2"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
argument_list|)
operator|.
name|field
argument_list|(
literal|"default"
argument_list|,
literal|"1d"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|long
name|providedTTLValue
init|=
literal|3000
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> checking ttl"
argument_list|)
expr_stmt|;
comment|// Index one doc without routing, one doc with routing, one doc with not TTL and no default and one doc with default TTL
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setTTL
argument_list|(
name|providedTTLValue
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"with_routing"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setTTL
argument_list|(
name|providedTTLValue
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"routing"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"no_ttl"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type2"
argument_list|,
literal|"default_ttl"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// realtime get check
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|GetResponse
name|getResponse
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
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
name|long
name|ttl0
decl_stmt|;
if|if
condition|(
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|ttl0
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|greaterThan
argument_list|(
operator|-
name|purgeInterval
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|lessThan
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
operator|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
operator|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// verify the ttl is still decreasing when going to the replica
name|currentTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|ttl0
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|greaterThan
argument_list|(
operator|-
name|purgeInterval
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|lessThan
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
operator|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
operator|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// non realtime get (stored)
name|currentTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|ttl0
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|greaterThan
argument_list|(
operator|-
name|purgeInterval
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|lessThan
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
operator|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
operator|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// non realtime get going the replica
name|currentTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|ttl0
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|greaterThan
argument_list|(
operator|-
name|purgeInterval
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|lessThan
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
operator|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
operator|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// no TTL provided so no TTL fetched
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"no_ttl"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_ttl"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// no TTL provided make sure it has default TTL
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type2"
argument_list|,
literal|"default_ttl"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ttl0
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|ttl0
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure the purger has done its job for all indexed docs that are expired
name|long
name|shouldBeExpiredDate
init|=
name|now
operator|+
name|providedTTLValue
operator|+
name|purgeInterval
operator|+
literal|2000
decl_stmt|;
name|currentTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
if|if
condition|(
name|shouldBeExpiredDate
operator|-
name|currentTime
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|shouldBeExpiredDate
operator|-
name|currentTime
argument_list|)
expr_stmt|;
block|}
comment|// We can't assume that after waiting for ttl + purgeInterval (waitTime) that the document have actually been deleted.
comment|// The ttl purging happens in the background in a different thread, and might not have been completed after waiting for waitTime.
comment|// But we can use index statistics' delete count to be sure that deletes have been executed, that must be incremented before
comment|// ttl purging has finished.
name|logger
operator|.
name|info
argument_list|(
literal|"--> checking purger"
argument_list|)
expr_stmt|;
name|long
name|currentDeleteCount
decl_stmt|;
do|do
block|{
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setFull
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareOptimize
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setMaxNumSegments
argument_list|(
literal|1
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|IndicesStatsResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|clear
argument_list|()
operator|.
name|setIndexing
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
name|currentDeleteCount
operator|=
name|response
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getTotal
argument_list|()
operator|.
name|getIndexing
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getDeleteCount
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|currentDeleteCount
operator|<
literal|4
condition|)
do|;
comment|// TTL deletes two docs, but it is indexed in the primary shard and replica shard.
name|assertThat
argument_list|(
name|currentDeleteCount
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
comment|// realtime get check
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"with_routing"
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"routing"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// replica realtime get check
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"with_routing"
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"routing"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// Need to run a refresh, in order for the non realtime get to work.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// non realtime get (stored) check
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"with_routing"
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"routing"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// non realtime get going the replica check
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"with_routing"
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"routing"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

