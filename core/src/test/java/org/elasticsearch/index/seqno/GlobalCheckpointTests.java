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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|IndexSettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|seqno
operator|.
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThan
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
name|not
import|;
end_import

begin_class
DECL|class|GlobalCheckpointTests
specifier|public
class|class
name|GlobalCheckpointTests
extends|extends
name|ESTestCase
block|{
DECL|field|checkpointService
name|GlobalCheckpointService
name|checkpointService
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|checkpointService
operator|=
operator|new
name|GlobalCheckpointService
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|UNASSIGNED_SEQ_NO
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmptyShards
specifier|public
name|void
name|testEmptyShards
parameter_list|()
block|{
name|assertFalse
argument_list|(
literal|"checkpoint shouldn't be updated when the are no active shards"
argument_list|,
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|field|aIdGenerator
specifier|private
specifier|final
name|AtomicInteger
name|aIdGenerator
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|method|randomAllocationsWithLocalCheckpoints
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|randomAllocationsWithLocalCheckpoints
parameter_list|(
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|allocations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|randomIntBetween
argument_list|(
name|min
argument_list|,
name|max
argument_list|)
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|allocations
operator|.
name|put
argument_list|(
literal|"id_"
operator|+
name|aIdGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
operator|(
name|long
operator|)
name|randomInt
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|allocations
return|;
block|}
DECL|method|testGlobalCheckpointUpdate
specifier|public
name|void
name|testGlobalCheckpointUpdate
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|allocations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|activeWithCheckpoints
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|active
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|activeWithCheckpoints
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|allocations
operator|.
name|putAll
argument_list|(
name|activeWithCheckpoints
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|initializingWithCheckpoints
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|initializing
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|initializingWithCheckpoints
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|allocations
operator|.
name|putAll
argument_list|(
name|initializingWithCheckpoints
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|allocations
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|active
operator|.
name|size
argument_list|()
operator|+
name|initializing
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// note: allocations can never be empty in practice as we always have at least one primary shard active/in sync
comment|// it is however nice not to assume this on this level and check we do the right thing.
specifier|final
name|long
name|maxLocalCheckpoint
init|=
name|allocations
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|min
argument_list|(
name|Long
operator|::
name|compare
argument_list|)
operator|.
name|orElse
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> using allocations"
argument_list|)
expr_stmt|;
name|allocations
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|aId
lambda|->
block|{
specifier|final
name|String
name|type
decl_stmt|;
if|if
condition|(
name|active
operator|.
name|contains
argument_list|(
name|aId
argument_list|)
condition|)
block|{
name|type
operator|=
literal|"active"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|initializing
operator|.
name|contains
argument_list|(
name|aId
argument_list|)
condition|)
block|{
name|type
operator|=
literal|"init"
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|aId
operator|+
literal|" not found in any map"
argument_list|)
throw|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"  - [{}], local checkpoint [{}], [{}]"
argument_list|,
name|aId
argument_list|,
name|allocations
operator|.
name|get
argument_list|(
name|aId
argument_list|)
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|active
argument_list|,
name|initializing
argument_list|)
expr_stmt|;
name|initializing
operator|.
name|forEach
argument_list|(
name|aId
lambda|->
name|checkpointService
operator|.
name|markAllocationIdAsInSync
argument_list|(
name|aId
argument_list|)
argument_list|)
expr_stmt|;
name|allocations
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|aId
lambda|->
name|checkpointService
operator|.
name|updateLocalCheckpoint
argument_list|(
name|aId
argument_list|,
name|allocations
operator|.
name|get
argument_list|(
name|aId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|maxLocalCheckpoint
operator|!=
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|maxLocalCheckpoint
argument_list|)
argument_list|)
expr_stmt|;
comment|// increment checkpoints
name|active
operator|.
name|forEach
argument_list|(
name|aId
lambda|->
name|allocations
operator|.
name|put
argument_list|(
name|aId
argument_list|,
name|allocations
operator|.
name|get
argument_list|(
name|aId
argument_list|)
operator|+
literal|1
operator|+
name|randomInt
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|initializing
operator|.
name|forEach
argument_list|(
name|aId
lambda|->
name|allocations
operator|.
name|put
argument_list|(
name|aId
argument_list|,
name|allocations
operator|.
name|get
argument_list|(
name|aId
argument_list|)
operator|+
literal|1
operator|+
name|randomInt
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|allocations
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|aId
lambda|->
name|checkpointService
operator|.
name|updateLocalCheckpoint
argument_list|(
name|aId
argument_list|,
name|allocations
operator|.
name|get
argument_list|(
name|aId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// now insert an unknown active/insync id , the checkpoint shouldn't change but a refresh should be requested.
specifier|final
name|String
name|extraId
init|=
literal|"extra_"
operator|+
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
comment|// first check that adding it without the master blessing doesn't change anything.
name|checkpointService
operator|.
name|updateLocalCheckpoint
argument_list|(
name|extraId
argument_list|,
name|maxLocalCheckpoint
operator|+
literal|1
operator|+
name|randomInt
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getLocalCheckpointForAllocation
argument_list|(
name|extraId
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|newActive
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|active
argument_list|)
decl_stmt|;
name|newActive
operator|.
name|add
argument_list|(
name|extraId
argument_list|)
expr_stmt|;
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|newActive
argument_list|,
name|initializing
argument_list|)
expr_stmt|;
comment|// we should ask for a refresh , but not update the checkpoint
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|maxLocalCheckpoint
argument_list|)
argument_list|)
expr_stmt|;
comment|// now notify for the new id
name|checkpointService
operator|.
name|updateLocalCheckpoint
argument_list|(
name|extraId
argument_list|,
name|maxLocalCheckpoint
operator|+
literal|1
operator|+
name|randomInt
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
comment|// now it should be incremented
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
name|maxLocalCheckpoint
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingActiveIdsPreventAdvance
specifier|public
name|void
name|testMissingActiveIdsPreventAdvance
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|active
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|initializing
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|assigned
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|assigned
operator|.
name|putAll
argument_list|(
name|active
argument_list|)
expr_stmt|;
name|assigned
operator|.
name|putAll
argument_list|(
name|initializing
argument_list|)
expr_stmt|;
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|randomSubsetOf
argument_list|(
name|randomInt
argument_list|(
name|active
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|,
name|active
operator|.
name|keySet
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|initializing
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|randomSubsetOf
argument_list|(
name|initializing
operator|.
name|keySet
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|markAllocationIdAsInSync
argument_list|)
expr_stmt|;
name|assigned
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|updateLocalCheckpoint
argument_list|)
expr_stmt|;
comment|// now mark all active shards
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|active
operator|.
name|keySet
argument_list|()
argument_list|,
name|initializing
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
comment|// global checkpoint can't be advanced, but we need a sync
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
expr_stmt|;
comment|// update again
name|assigned
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|updateLocalCheckpoint
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingInSyncIdsPreventAdvance
specifier|public
name|void
name|testMissingInSyncIdsPreventAdvance
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|active
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|initializing
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|active
operator|.
name|keySet
argument_list|()
argument_list|,
name|initializing
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|initializing
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|markAllocationIdAsInSync
argument_list|)
expr_stmt|;
name|randomSubsetOf
argument_list|(
name|randomInt
argument_list|(
name|initializing
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|,
name|initializing
operator|.
name|keySet
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
name|aId
lambda|->
name|checkpointService
operator|.
name|updateLocalCheckpoint
argument_list|(
name|aId
argument_list|,
name|initializing
operator|.
name|get
argument_list|(
name|aId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|active
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|updateLocalCheckpoint
argument_list|)
expr_stmt|;
comment|// global checkpoint can't be advanced, but we need a sync
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
expr_stmt|;
comment|// update again
name|initializing
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|updateLocalCheckpoint
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInSyncIdsAreIgnoredIfNotValidatedByMaster
specifier|public
name|void
name|testInSyncIdsAreIgnoredIfNotValidatedByMaster
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|active
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|initializing
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|nonApproved
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|active
operator|.
name|keySet
argument_list|()
argument_list|,
name|initializing
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|initializing
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|markAllocationIdAsInSync
argument_list|)
expr_stmt|;
name|nonApproved
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|markAllocationIdAsInSync
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|allocations
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|active
argument_list|,
name|initializing
argument_list|,
name|nonApproved
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|allocations
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|allocations
operator|.
name|forEach
argument_list|(
name|a
lambda|->
name|a
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|updateLocalCheckpoint
argument_list|)
argument_list|)
expr_stmt|;
comment|// global checkpoint can be advanced, but we need a sync
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInSyncIdsAreRemovedIfNotValidatedByMaster
specifier|public
name|void
name|testInSyncIdsAreRemovedIfNotValidatedByMaster
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|activeToStay
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|initializingToStay
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|activeToBeRemoved
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|initializingToBeRemoved
init|=
name|randomAllocationsWithLocalCheckpoints
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|active
init|=
name|Sets
operator|.
name|union
argument_list|(
name|activeToStay
operator|.
name|keySet
argument_list|()
argument_list|,
name|activeToBeRemoved
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|initializing
init|=
name|Sets
operator|.
name|union
argument_list|(
name|initializingToStay
operator|.
name|keySet
argument_list|()
argument_list|,
name|initializingToBeRemoved
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|allocations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|allocations
operator|.
name|putAll
argument_list|(
name|activeToStay
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|allocations
operator|.
name|putAll
argument_list|(
name|activeToBeRemoved
argument_list|)
expr_stmt|;
block|}
name|allocations
operator|.
name|putAll
argument_list|(
name|initializingToStay
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|allocations
operator|.
name|putAll
argument_list|(
name|initializingToBeRemoved
argument_list|)
expr_stmt|;
block|}
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|active
argument_list|,
name|initializing
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|initializingToStay
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|markAllocationIdAsInSync
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|initializing
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|markAllocationIdAsInSync
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|allocations
operator|.
name|forEach
argument_list|(
name|checkpointService
operator|::
name|updateLocalCheckpoint
argument_list|)
expr_stmt|;
block|}
comment|// global checkpoint may be advanced, but we need a sync in any case
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
comment|// now remove shards
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|activeToStay
operator|.
name|keySet
argument_list|()
argument_list|,
name|initializingToStay
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|allocations
operator|.
name|forEach
argument_list|(
parameter_list|(
name|aid
parameter_list|,
name|ckp
parameter_list|)
lambda|->
name|checkpointService
operator|.
name|updateLocalCheckpoint
argument_list|(
name|aid
argument_list|,
name|ckp
operator|+
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|allocations
operator|.
name|forEach
argument_list|(
parameter_list|(
name|aid
parameter_list|,
name|ckp
parameter_list|)
lambda|->
name|checkpointService
operator|.
name|updateLocalCheckpoint
argument_list|(
name|aid
argument_list|,
name|ckp
operator|+
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|checkpointService
operator|.
name|updateAllocationIdsFromMaster
argument_list|(
name|activeToStay
operator|.
name|keySet
argument_list|()
argument_list|,
name|initializingToStay
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|checkpoint
init|=
name|Stream
operator|.
name|concat
argument_list|(
name|activeToStay
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|,
name|initializingToStay
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|min
argument_list|(
name|Long
operator|::
name|compare
argument_list|)
operator|.
name|get
argument_list|()
operator|+
literal|10
decl_stmt|;
comment|// we added 10 to make sure it's advanced in the second time
comment|// global checkpoint is advanced and we need a sync
name|assertTrue
argument_list|(
name|checkpointService
operator|.
name|updateCheckpointOnPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|checkpointService
operator|.
name|getCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|checkpoint
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

