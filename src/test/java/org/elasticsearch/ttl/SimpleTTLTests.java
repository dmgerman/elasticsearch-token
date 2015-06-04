begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
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
name|indices
operator|.
name|mapping
operator|.
name|get
operator|.
name|GetMappingsResponse
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
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingResponse
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
name|action
operator|.
name|index
operator|.
name|IndexResponse
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
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|Locale
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
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
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
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|SimpleTTLTests
specifier|public
class|class
name|SimpleTTLTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|PURGE_INTERVAL
specifier|static
specifier|private
specifier|final
name|long
name|PURGE_INTERVAL
init|=
literal|200
decl_stmt|;
annotation|@
name|Override
DECL|method|numberOfShards
specifier|protected
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
literal|2
return|;
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"indices.ttl.interval"
argument_list|,
name|PURGE_INTERVAL
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
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
return|;
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
name|assertAcked
argument_list|(
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
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
specifier|final
name|NumShards
name|test
init|=
name|getNumShards
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
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
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|IndexResponse
name|indexResponse
init|=
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
name|setTimestamp
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|now
argument_list|)
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
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexResponse
operator|=
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
name|setTimestamp
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|now
argument_list|)
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
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexResponse
operator|=
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
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexResponse
operator|=
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
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|indexResponse
operator|.
name|isCreated
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
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
name|get
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
name|lessThanOrEqualTo
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
name|assertThat
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|0l
argument_list|)
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
name|get
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
name|lessThanOrEqualTo
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
name|assertThat
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|0l
argument_list|)
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
name|get
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
name|lessThanOrEqualTo
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
name|assertThat
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|0l
argument_list|)
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
name|get
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
name|lessThanOrEqualTo
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
name|assertThat
argument_list|(
name|providedTTLValue
operator|-
operator|(
name|currentTime
operator|-
name|now
operator|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|0l
argument_list|)
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
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
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
argument_list|,
name|equalTo
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
name|PURGE_INTERVAL
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
name|assertThat
argument_list|(
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
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
name|get
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
name|get
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
name|get
argument_list|()
decl_stmt|;
comment|// TTL deletes two docs, but it is indexed in the primary shard and replica shard.
return|return
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
operator|==
literal|2L
operator|*
name|test
operator|.
name|dataCopies
return|;
block|}
block|}
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
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
annotation|@
name|Test
comment|// issue 5053
DECL|method|testThatUpdatingMappingShouldNotRemoveTTLConfiguration
specifier|public
name|void
name|testThatUpdatingMappingShouldNotRemoveTTLConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|index
init|=
literal|"foo"
decl_stmt|;
name|String
name|type
init|=
literal|"mytype"
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
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
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
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
name|index
argument_list|)
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
comment|// check mapping again
name|assertTTLMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|)
expr_stmt|;
comment|// update some field in the mapping
name|XContentBuilder
name|updateMappingBuilder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"otherField"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|PutMappingResponse
name|putMappingResponse
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
name|preparePutMapping
argument_list|(
name|index
argument_list|)
operator|.
name|setType
argument_list|(
name|type
argument_list|)
operator|.
name|setSource
argument_list|(
name|updateMappingBuilder
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|putMappingResponse
argument_list|)
expr_stmt|;
comment|// make sure timestamp field is still in mapping
name|assertTTLMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
DECL|method|assertTTLMappingEnabled
specifier|private
name|void
name|assertTTLMappingEnabled
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|errMsg
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"Expected ttl field mapping to be enabled for %s/%s"
argument_list|,
name|index
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
name|index
argument_list|)
operator|.
name|addTypes
argument_list|(
name|type
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingSource
init|=
name|getMappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|(
name|type
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|errMsg
argument_list|,
name|mappingSource
argument_list|,
name|hasKey
argument_list|(
literal|"_ttl"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|ttlAsString
init|=
name|mappingSource
operator|.
name|get
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ttlAsString
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|errMsg
argument_list|,
name|ttlAsString
argument_list|,
name|is
argument_list|(
literal|"{enabled=true}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

