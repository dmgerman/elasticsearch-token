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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|FixedBitSet
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
name|Setting
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
name|IndexSettings
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
name|AbstractIndexShardComponent
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
name|index
operator|.
name|shard
operator|.
name|SnapshotStatus
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_comment
comment|/**  * This class generates sequences numbers and keeps track of the so called local checkpoint - the highest number for which  * all previous seqNo have been processed (including)  */
end_comment

begin_class
DECL|class|LocalCheckpointService
specifier|public
class|class
name|LocalCheckpointService
extends|extends
name|AbstractIndexShardComponent
block|{
comment|/**      * we keep a bit for each seq No that is still pending. to optimize allocation, we do so in multiple arrays      * allocating them on demand and cleaning up while completed. This setting controls the size of the arrays      */
DECL|field|SETTINGS_BIT_ARRAYS_SIZE
specifier|public
specifier|static
name|Setting
argument_list|<
name|Integer
argument_list|>
name|SETTINGS_BIT_ARRAYS_SIZE
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"index.seq_no.checkpoint.bit_arrays_size"
argument_list|,
literal|1024
argument_list|,
literal|4
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|IndexScope
argument_list|)
decl_stmt|;
comment|/**      * an ordered list of bit arrays representing pending seq nos. The list is "anchored" in {@link #firstProcessedSeqNo}      * which marks the seqNo the fist bit in the first array corresponds to.      */
DECL|field|processedSeqNo
specifier|final
name|LinkedList
argument_list|<
name|FixedBitSet
argument_list|>
name|processedSeqNo
decl_stmt|;
DECL|field|bitArraysSize
specifier|private
specifier|final
name|int
name|bitArraysSize
decl_stmt|;
DECL|field|firstProcessedSeqNo
name|long
name|firstProcessedSeqNo
decl_stmt|;
comment|/** the current local checkpoint, i.e., all seqNo lower (&lt;=) than this number have been completed */
DECL|field|checkpoint
specifier|volatile
name|long
name|checkpoint
decl_stmt|;
comment|/** the next available seqNo - used for seqNo generation */
DECL|field|nextSeqNo
specifier|private
specifier|volatile
name|long
name|nextSeqNo
decl_stmt|;
comment|/**      * Initialize the local checkpoint service. The {@code maxSeqNo} should be      * set to the last sequence number assigned by this shard, or      * {@link SequenceNumbersService#NO_OPS_PERFORMED} and      * {@code localCheckpoint} should be set to the last known local checkpoint      * for this shard, or {@link SequenceNumbersService#NO_OPS_PERFORMED}.      *      * @param shardId         the shard this service is providing tracking      *                        local checkpoints for      * @param indexSettings   the index settings      * @param maxSeqNo        the last sequence number assigned by this shard, or      *                        {@link SequenceNumbersService#NO_OPS_PERFORMED}      * @param localCheckpoint the last known local checkpoint for this shard, or      *                        {@link SequenceNumbersService#NO_OPS_PERFORMED}      */
DECL|method|LocalCheckpointService
name|LocalCheckpointService
parameter_list|(
specifier|final
name|ShardId
name|shardId
parameter_list|,
specifier|final
name|IndexSettings
name|indexSettings
parameter_list|,
specifier|final
name|long
name|maxSeqNo
parameter_list|,
specifier|final
name|long
name|localCheckpoint
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
if|if
condition|(
name|localCheckpoint
operator|<
literal|0
operator|&&
name|localCheckpoint
operator|!=
name|SequenceNumbersService
operator|.
name|NO_OPS_PERFORMED
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"local checkpoint must be non-negative or ["
operator|+
name|SequenceNumbersService
operator|.
name|NO_OPS_PERFORMED
operator|+
literal|"] "
operator|+
literal|"but was ["
operator|+
name|localCheckpoint
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|maxSeqNo
operator|<
literal|0
operator|&&
name|maxSeqNo
operator|!=
name|SequenceNumbersService
operator|.
name|NO_OPS_PERFORMED
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"max seq. no. must be non-negative or ["
operator|+
name|SequenceNumbersService
operator|.
name|NO_OPS_PERFORMED
operator|+
literal|"] but was ["
operator|+
name|maxSeqNo
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|bitArraysSize
operator|=
name|SETTINGS_BIT_ARRAYS_SIZE
operator|.
name|get
argument_list|(
name|indexSettings
operator|.
name|getSettings
argument_list|()
argument_list|)
expr_stmt|;
name|processedSeqNo
operator|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
expr_stmt|;
name|firstProcessedSeqNo
operator|=
name|localCheckpoint
operator|==
name|SequenceNumbersService
operator|.
name|NO_OPS_PERFORMED
condition|?
literal|0
else|:
name|localCheckpoint
operator|+
literal|1
expr_stmt|;
name|this
operator|.
name|nextSeqNo
operator|=
name|maxSeqNo
operator|==
name|SequenceNumbersService
operator|.
name|NO_OPS_PERFORMED
condition|?
literal|0
else|:
name|maxSeqNo
operator|+
literal|1
expr_stmt|;
name|this
operator|.
name|checkpoint
operator|=
name|localCheckpoint
expr_stmt|;
block|}
comment|/**      * issue the next sequence number      **/
DECL|method|generateSeqNo
specifier|synchronized
name|long
name|generateSeqNo
parameter_list|()
block|{
return|return
name|nextSeqNo
operator|++
return|;
block|}
comment|/**      * marks the processing of the given seqNo have been completed      **/
DECL|method|markSeqNoAsCompleted
specifier|synchronized
name|void
name|markSeqNoAsCompleted
parameter_list|(
name|long
name|seqNo
parameter_list|)
block|{
comment|// make sure we track highest seen seqNo
if|if
condition|(
name|seqNo
operator|>=
name|nextSeqNo
condition|)
block|{
name|nextSeqNo
operator|=
name|seqNo
operator|+
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|seqNo
operator|<=
name|checkpoint
condition|)
block|{
comment|// this is possible during recovery where we might replay an op that was also replicated
return|return;
block|}
name|FixedBitSet
name|bitSet
init|=
name|getBitSetForSeqNo
argument_list|(
name|seqNo
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
name|seqNoToBitSetOffset
argument_list|(
name|seqNo
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
name|offset
argument_list|)
expr_stmt|;
if|if
condition|(
name|seqNo
operator|==
name|checkpoint
operator|+
literal|1
condition|)
block|{
name|updateCheckpoint
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** gets the current check point */
DECL|method|getCheckpoint
specifier|public
name|long
name|getCheckpoint
parameter_list|()
block|{
return|return
name|checkpoint
return|;
block|}
comment|/** gets the maximum seqno seen so far */
DECL|method|getMaxSeqNo
name|long
name|getMaxSeqNo
parameter_list|()
block|{
return|return
name|nextSeqNo
operator|-
literal|1
return|;
block|}
comment|/**      * moves the checkpoint to the last consecutively processed seqNo      * Note: this method assumes that the seqNo following the current checkpoint is processed.      */
DECL|method|updateCheckpoint
specifier|private
name|void
name|updateCheckpoint
parameter_list|()
block|{
assert|assert
name|Thread
operator|.
name|holdsLock
argument_list|(
name|this
argument_list|)
assert|;
assert|assert
name|checkpoint
operator|<
name|firstProcessedSeqNo
operator|+
name|bitArraysSize
operator|-
literal|1
operator|:
literal|"checkpoint should be below the end of the first bit set (o.w. current bit set is completed and shouldn't be there)"
assert|;
assert|assert
name|getBitSetForSeqNo
argument_list|(
name|checkpoint
operator|+
literal|1
argument_list|)
operator|==
name|processedSeqNo
operator|.
name|getFirst
argument_list|()
operator|:
literal|"checkpoint + 1 doesn't point to the first bit set (o.w. current bit set is completed and shouldn't be there)"
assert|;
assert|assert
name|getBitSetForSeqNo
argument_list|(
name|checkpoint
operator|+
literal|1
argument_list|)
operator|.
name|get
argument_list|(
name|seqNoToBitSetOffset
argument_list|(
name|checkpoint
operator|+
literal|1
argument_list|)
argument_list|)
operator|:
literal|"updateCheckpoint is called but the bit following the checkpoint is not set"
assert|;
comment|// keep it simple for now, get the checkpoint one by one. in the future we can optimize and read words
name|FixedBitSet
name|current
init|=
name|processedSeqNo
operator|.
name|getFirst
argument_list|()
decl_stmt|;
do|do
block|{
name|checkpoint
operator|++
expr_stmt|;
comment|// the checkpoint always falls in the first bit set or just before. If it falls
comment|// on the last bit of the current bit set, we can clean it.
if|if
condition|(
name|checkpoint
operator|==
name|firstProcessedSeqNo
operator|+
name|bitArraysSize
operator|-
literal|1
condition|)
block|{
name|processedSeqNo
operator|.
name|removeFirst
argument_list|()
expr_stmt|;
name|firstProcessedSeqNo
operator|+=
name|bitArraysSize
expr_stmt|;
assert|assert
name|checkpoint
operator|-
name|firstProcessedSeqNo
operator|<
name|bitArraysSize
assert|;
name|current
operator|=
name|processedSeqNo
operator|.
name|peekFirst
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|current
operator|!=
literal|null
operator|&&
name|current
operator|.
name|get
argument_list|(
name|seqNoToBitSetOffset
argument_list|(
name|checkpoint
operator|+
literal|1
argument_list|)
argument_list|)
condition|)
do|;
block|}
comment|/**      * gets the bit array for the given seqNo, allocating new ones if needed.      */
DECL|method|getBitSetForSeqNo
specifier|private
name|FixedBitSet
name|getBitSetForSeqNo
parameter_list|(
name|long
name|seqNo
parameter_list|)
block|{
assert|assert
name|Thread
operator|.
name|holdsLock
argument_list|(
name|this
argument_list|)
assert|;
assert|assert
name|seqNo
operator|>=
name|firstProcessedSeqNo
operator|:
literal|"seqNo: "
operator|+
name|seqNo
operator|+
literal|" firstProcessedSeqNo: "
operator|+
name|firstProcessedSeqNo
assert|;
name|int
name|bitSetOffset
init|=
operator|(
call|(
name|int
call|)
argument_list|(
name|seqNo
operator|-
name|firstProcessedSeqNo
argument_list|)
operator|)
operator|/
name|bitArraysSize
decl_stmt|;
while|while
condition|(
name|bitSetOffset
operator|>=
name|processedSeqNo
operator|.
name|size
argument_list|()
condition|)
block|{
name|processedSeqNo
operator|.
name|add
argument_list|(
operator|new
name|FixedBitSet
argument_list|(
name|bitArraysSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|processedSeqNo
operator|.
name|get
argument_list|(
name|bitSetOffset
argument_list|)
return|;
block|}
comment|/** maps the given seqNo to a position in the bit set returned by {@link #getBitSetForSeqNo} */
DECL|method|seqNoToBitSetOffset
specifier|private
name|int
name|seqNoToBitSetOffset
parameter_list|(
name|long
name|seqNo
parameter_list|)
block|{
assert|assert
name|Thread
operator|.
name|holdsLock
argument_list|(
name|this
argument_list|)
assert|;
assert|assert
name|seqNo
operator|>=
name|firstProcessedSeqNo
assert|;
return|return
operator|(
call|(
name|int
call|)
argument_list|(
name|seqNo
operator|-
name|firstProcessedSeqNo
argument_list|)
operator|)
operator|%
name|bitArraysSize
return|;
block|}
block|}
end_class

end_unit

