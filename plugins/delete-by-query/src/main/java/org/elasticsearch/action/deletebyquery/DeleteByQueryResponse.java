begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.deletebyquery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|deletebyquery
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
name|ActionResponse
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
name|ShardOperationFailedException
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
name|search
operator|.
name|ShardSearchFailure
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|unit
operator|.
name|TimeValue
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
name|XContentBuilderString
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|ShardSearchFailure
operator|.
name|readShardSearchFailure
import|;
end_import

begin_comment
comment|/**  * Delete by query response  * @see DeleteByQueryRequest  */
end_comment

begin_class
DECL|class|DeleteByQueryResponse
specifier|public
class|class
name|DeleteByQueryResponse
extends|extends
name|ActionResponse
implements|implements
name|ToXContent
block|{
DECL|field|tookInMillis
specifier|private
name|long
name|tookInMillis
decl_stmt|;
DECL|field|timedOut
specifier|private
name|boolean
name|timedOut
init|=
literal|false
decl_stmt|;
DECL|field|found
specifier|private
name|long
name|found
decl_stmt|;
DECL|field|deleted
specifier|private
name|long
name|deleted
decl_stmt|;
DECL|field|missing
specifier|private
name|long
name|missing
decl_stmt|;
DECL|field|failed
specifier|private
name|long
name|failed
decl_stmt|;
DECL|field|indices
specifier|private
name|IndexDeleteByQueryResponse
index|[]
name|indices
init|=
name|IndexDeleteByQueryResponse
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|shardFailures
specifier|private
name|ShardOperationFailedException
index|[]
name|shardFailures
init|=
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|method|DeleteByQueryResponse
name|DeleteByQueryResponse
parameter_list|()
block|{     }
DECL|method|DeleteByQueryResponse
name|DeleteByQueryResponse
parameter_list|(
name|long
name|tookInMillis
parameter_list|,
name|boolean
name|timedOut
parameter_list|,
name|long
name|found
parameter_list|,
name|long
name|deleted
parameter_list|,
name|long
name|missing
parameter_list|,
name|long
name|failed
parameter_list|,
name|IndexDeleteByQueryResponse
index|[]
name|indices
parameter_list|,
name|ShardOperationFailedException
index|[]
name|shardFailures
parameter_list|)
block|{
name|this
operator|.
name|tookInMillis
operator|=
name|tookInMillis
expr_stmt|;
name|this
operator|.
name|timedOut
operator|=
name|timedOut
expr_stmt|;
name|this
operator|.
name|found
operator|=
name|found
expr_stmt|;
name|this
operator|.
name|deleted
operator|=
name|deleted
expr_stmt|;
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
name|this
operator|.
name|failed
operator|=
name|failed
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
name|this
operator|.
name|shardFailures
operator|=
name|shardFailures
expr_stmt|;
block|}
comment|/**      * The responses from all the different indices.      */
DECL|method|getIndices
specifier|public
name|IndexDeleteByQueryResponse
index|[]
name|getIndices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
comment|/**      * The response of a specific index.      */
DECL|method|getIndex
specifier|public
name|IndexDeleteByQueryResponse
name|getIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
if|if
condition|(
name|index
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|IndexDeleteByQueryResponse
name|i
range|:
name|indices
control|)
block|{
if|if
condition|(
name|index
operator|.
name|equals
argument_list|(
name|i
operator|.
name|getIndex
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
DECL|method|getTook
specifier|public
name|TimeValue
name|getTook
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|tookInMillis
argument_list|)
return|;
block|}
DECL|method|getTookInMillis
specifier|public
name|long
name|getTookInMillis
parameter_list|()
block|{
return|return
name|tookInMillis
return|;
block|}
DECL|method|isTimedOut
specifier|public
name|boolean
name|isTimedOut
parameter_list|()
block|{
return|return
name|this
operator|.
name|timedOut
return|;
block|}
DECL|method|getTotalFound
specifier|public
name|long
name|getTotalFound
parameter_list|()
block|{
return|return
name|found
return|;
block|}
DECL|method|getTotalDeleted
specifier|public
name|long
name|getTotalDeleted
parameter_list|()
block|{
return|return
name|deleted
return|;
block|}
DECL|method|getTotalMissing
specifier|public
name|long
name|getTotalMissing
parameter_list|()
block|{
return|return
name|missing
return|;
block|}
DECL|method|getTotalFailed
specifier|public
name|long
name|getTotalFailed
parameter_list|()
block|{
return|return
name|failed
return|;
block|}
DECL|method|getShardFailures
specifier|public
name|ShardOperationFailedException
index|[]
name|getShardFailures
parameter_list|()
block|{
return|return
name|shardFailures
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|tookInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|timedOut
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|found
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|deleted
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|missing
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|failed
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|indices
operator|=
operator|new
name|IndexDeleteByQueryResponse
index|[
name|size
index|]
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|IndexDeleteByQueryResponse
name|index
init|=
operator|new
name|IndexDeleteByQueryResponse
argument_list|()
decl_stmt|;
name|index
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indices
index|[
name|i
index|]
operator|=
name|index
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|shardFailures
operator|=
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|shardFailures
operator|=
operator|new
name|ShardSearchFailure
index|[
name|size
index|]
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
name|shardFailures
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|shardFailures
index|[
name|i
index|]
operator|=
name|readShardSearchFailure
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|tookInMillis
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|timedOut
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|found
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|deleted
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|missing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|failed
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indices
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexDeleteByQueryResponse
name|indexResponse
range|:
name|indices
control|)
block|{
name|indexResponse
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|shardFailures
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardOperationFailedException
name|shardSearchFailure
range|:
name|shardFailures
control|)
block|{
name|shardSearchFailure
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|TOOK
specifier|static
specifier|final
name|XContentBuilderString
name|TOOK
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"took"
argument_list|)
decl_stmt|;
DECL|field|TIMED_OUT
specifier|static
specifier|final
name|XContentBuilderString
name|TIMED_OUT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"timed_out"
argument_list|)
decl_stmt|;
DECL|field|INDICES
specifier|static
specifier|final
name|XContentBuilderString
name|INDICES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_indices"
argument_list|)
decl_stmt|;
DECL|field|FAILURES
specifier|static
specifier|final
name|XContentBuilderString
name|FAILURES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"failures"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOOK
argument_list|,
name|tookInMillis
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TIMED_OUT
argument_list|,
name|timedOut
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
name|IndexDeleteByQueryResponse
name|all
init|=
operator|new
name|IndexDeleteByQueryResponse
argument_list|(
literal|"_all"
argument_list|,
name|found
argument_list|,
name|deleted
argument_list|,
name|missing
argument_list|,
name|failed
argument_list|)
decl_stmt|;
name|all
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexDeleteByQueryResponse
name|indexResponse
range|:
name|indices
control|)
block|{
name|indexResponse
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|FAILURES
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardFailures
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ShardOperationFailedException
name|shardFailure
range|:
name|shardFailures
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|shardFailure
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

