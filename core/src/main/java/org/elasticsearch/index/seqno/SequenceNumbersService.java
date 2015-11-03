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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_comment
comment|/** a very light weight implementation. will be replaced with proper machinery later */
end_comment

begin_class
DECL|class|SequenceNumbersService
specifier|public
class|class
name|SequenceNumbersService
extends|extends
name|AbstractIndexShardComponent
block|{
DECL|field|UNASSIGNED_SEQ_NO
specifier|public
specifier|final
specifier|static
name|long
name|UNASSIGNED_SEQ_NO
init|=
operator|-
literal|1L
decl_stmt|;
DECL|field|seqNoGenerator
name|AtomicLong
name|seqNoGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|method|SequenceNumbersService
specifier|public
name|SequenceNumbersService
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|IndexSettings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
comment|/**      * generates a new sequence number.      * Note: you must call {@link #markSeqNoAsCompleted(long)} after the operation for which this seq# was generated      * was completed (whether successfully or with a failure      */
DECL|method|generateSeqNo
specifier|public
name|long
name|generateSeqNo
parameter_list|()
block|{
return|return
name|seqNoGenerator
operator|.
name|getAndIncrement
argument_list|()
return|;
block|}
DECL|method|markSeqNoAsCompleted
specifier|public
name|void
name|markSeqNoAsCompleted
parameter_list|(
name|long
name|seqNo
parameter_list|)
block|{
comment|// this is temporary to make things semi sane on primary promotion and recovery. will be replaced with better machinery
name|boolean
name|success
decl_stmt|;
do|do
block|{
name|long
name|maxSeqNo
init|=
name|seqNoGenerator
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|seqNo
operator|>
name|maxSeqNo
condition|)
block|{
name|success
operator|=
name|seqNoGenerator
operator|.
name|compareAndSet
argument_list|(
name|maxSeqNo
argument_list|,
name|seqNo
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|success
operator|=
literal|true
expr_stmt|;
block|}
block|}
do|while
condition|(
name|success
operator|==
literal|false
condition|)
do|;
block|}
block|}
end_class

end_unit

