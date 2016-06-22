begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.seqno
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|seqno
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|ShardStats
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
name|IndexRequestBuilder
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
name|XContentHelper
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
name|ESIntegTestCase
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
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
name|List
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
name|anyOf
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
annotation|@
name|TestLogging
argument_list|(
literal|"index.shard:TRACE,index.seqno:TRACE"
argument_list|)
DECL|class|CheckpointsIT
specifier|public
class|class
name|CheckpointsIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testCheckpointsAdvance
specifier|public
name|void
name|testCheckpointsAdvance
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
literal|"index.seq_no.checkpoint_sync_interval"
argument_list|,
literal|"100ms"
argument_list|,
comment|// update global point frequently
literal|"index.number_of_shards"
argument_list|,
literal|"1"
comment|// simplify things so we know how many ops goes to the shards
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
specifier|final
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|builders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|long
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> will index [{}] docs"
argument_list|,
name|numDocs
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"id_"
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
name|randomBoolean
argument_list|()
argument_list|,
literal|false
argument_list|,
name|builders
argument_list|)
expr_stmt|;
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
name|IndicesStatsResponse
name|stats
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
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|stats
operator|.
name|getShards
argument_list|()
control|)
block|{
if|if
condition|(
name|shardStats
operator|.
name|getSeqNoStats
argument_list|()
operator|==
literal|null
condition|)
block|{
name|assertFalse
argument_list|(
literal|"no seq_no stats for primary "
operator|+
name|shardStats
operator|.
name|getShardRouting
argument_list|()
argument_list|,
name|shardStats
operator|.
name|getShardRouting
argument_list|()
operator|.
name|primary
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"seq_no stats for {}: {}"
argument_list|,
name|shardStats
operator|.
name|getShardRouting
argument_list|()
argument_list|,
name|XContentHelper
operator|.
name|toString
argument_list|(
name|shardStats
operator|.
name|getSeqNoStats
argument_list|()
argument_list|,
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"pretty"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardStats
operator|.
name|getShardRouting
argument_list|()
operator|+
literal|" local checkpoint mismatch"
argument_list|,
name|shardStats
operator|.
name|getSeqNoStats
argument_list|()
operator|.
name|getLocalCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardStats
operator|.
name|getShardRouting
argument_list|()
operator|+
literal|" global checkpoint mismatch"
argument_list|,
name|shardStats
operator|.
name|getSeqNoStats
argument_list|()
operator|.
name|getGlobalCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardStats
operator|.
name|getShardRouting
argument_list|()
operator|+
literal|" max seq no mismatch"
argument_list|,
name|shardStats
operator|.
name|getSeqNoStats
argument_list|()
operator|.
name|getMaxSeqNo
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

