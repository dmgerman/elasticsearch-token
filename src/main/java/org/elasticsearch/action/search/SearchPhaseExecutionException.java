begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|index
operator|.
name|Index
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
name|IndexException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SearchPhaseExecutionException
specifier|public
class|class
name|SearchPhaseExecutionException
extends|extends
name|ElasticsearchException
block|{
DECL|field|phaseName
specifier|private
specifier|final
name|String
name|phaseName
decl_stmt|;
DECL|field|shardFailures
specifier|private
name|ShardSearchFailure
index|[]
name|shardFailures
decl_stmt|;
DECL|method|SearchPhaseExecutionException
specifier|public
name|SearchPhaseExecutionException
parameter_list|(
name|String
name|phaseName
parameter_list|,
name|String
name|msg
parameter_list|,
name|ShardSearchFailure
index|[]
name|shardFailures
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|this
operator|.
name|phaseName
operator|=
name|phaseName
expr_stmt|;
name|this
operator|.
name|shardFailures
operator|=
name|shardFailures
expr_stmt|;
block|}
DECL|method|SearchPhaseExecutionException
specifier|public
name|SearchPhaseExecutionException
parameter_list|(
name|String
name|phaseName
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|,
name|ShardSearchFailure
index|[]
name|shardFailures
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|this
operator|.
name|phaseName
operator|=
name|phaseName
expr_stmt|;
name|this
operator|.
name|shardFailures
operator|=
name|shardFailures
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
if|if
condition|(
name|shardFailures
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// if no successful shards, it means no active shards, so just return SERVICE_UNAVAILABLE
return|return
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
return|;
block|}
name|RestStatus
name|status
init|=
name|shardFailures
index|[
literal|0
index|]
operator|.
name|status
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardFailures
operator|.
name|length
operator|>
literal|1
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
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
if|if
condition|(
name|shardFailures
index|[
name|i
index|]
operator|.
name|status
argument_list|()
operator|.
name|getStatus
argument_list|()
operator|>=
literal|500
condition|)
block|{
name|status
operator|=
name|shardFailures
index|[
name|i
index|]
operator|.
name|status
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|status
return|;
block|}
DECL|method|shardFailures
specifier|public
name|ShardSearchFailure
index|[]
name|shardFailures
parameter_list|()
block|{
return|return
name|shardFailures
return|;
block|}
DECL|method|buildMessage
specifier|private
specifier|static
name|String
name|buildMessage
parameter_list|(
name|String
name|phaseName
parameter_list|,
name|String
name|msg
parameter_list|,
name|ShardSearchFailure
index|[]
name|shardFailures
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Failed to execute phase ["
argument_list|)
operator|.
name|append
argument_list|(
name|phaseName
argument_list|)
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
operator|.
name|append
argument_list|(
name|msg
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardFailures
operator|!=
literal|null
operator|&&
name|shardFailures
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"; shardFailures "
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardSearchFailure
name|shardFailure
range|:
name|shardFailures
control|)
block|{
if|if
condition|(
name|shardFailure
operator|.
name|shard
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
name|shardFailure
operator|.
name|shard
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|": "
argument_list|)
operator|.
name|append
argument_list|(
name|shardFailure
operator|.
name|reason
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
name|shardFailure
operator|.
name|reason
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|protected
name|void
name|innerToXContent
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
literal|"phase"
argument_list|,
name|phaseName
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|group
init|=
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"group_shard_failures"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// we group by default
name|builder
operator|.
name|field
argument_list|(
literal|"grouped"
argument_list|,
name|group
argument_list|)
expr_stmt|;
comment|// notify that it's grouped
name|builder
operator|.
name|field
argument_list|(
literal|"failed_shards"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|()
expr_stmt|;
name|ShardSearchFailure
index|[]
name|failures
init|=
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"group_shard_failures"
argument_list|,
literal|true
argument_list|)
condition|?
name|groupBy
argument_list|(
name|shardFailures
argument_list|)
else|:
name|shardFailures
decl_stmt|;
for|for
control|(
name|ShardSearchFailure
name|failure
range|:
name|failures
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|failure
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
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|super
operator|.
name|innerToXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
DECL|method|groupBy
specifier|private
name|ShardSearchFailure
index|[]
name|groupBy
parameter_list|(
name|ShardSearchFailure
index|[]
name|failures
parameter_list|)
block|{
name|List
argument_list|<
name|ShardSearchFailure
argument_list|>
name|uniqueFailures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|GroupBy
argument_list|>
name|reasons
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardSearchFailure
name|failure
range|:
name|failures
control|)
block|{
name|GroupBy
name|reason
init|=
operator|new
name|GroupBy
argument_list|(
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|reasons
operator|.
name|contains
argument_list|(
name|reason
argument_list|)
operator|==
literal|false
condition|)
block|{
name|reasons
operator|.
name|add
argument_list|(
name|reason
argument_list|)
expr_stmt|;
name|uniqueFailures
operator|.
name|add
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|uniqueFailures
operator|.
name|toArray
argument_list|(
operator|new
name|ShardSearchFailure
index|[
literal|0
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|guessRootCauses
specifier|public
name|ElasticsearchException
index|[]
name|guessRootCauses
parameter_list|()
block|{
name|ShardSearchFailure
index|[]
name|failures
init|=
name|groupBy
argument_list|(
name|shardFailures
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ElasticsearchException
argument_list|>
name|rootCauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|failures
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardSearchFailure
name|failure
range|:
name|failures
control|)
block|{
name|ElasticsearchException
index|[]
name|guessRootCauses
init|=
name|ElasticsearchException
operator|.
name|guessRootCauses
argument_list|(
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
decl_stmt|;
name|rootCauses
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|guessRootCauses
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rootCauses
operator|.
name|toArray
argument_list|(
operator|new
name|ElasticsearchException
index|[
literal|0
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|buildMessage
argument_list|(
name|phaseName
argument_list|,
name|getMessage
argument_list|()
argument_list|,
name|shardFailures
argument_list|)
return|;
block|}
DECL|class|GroupBy
specifier|static
class|class
name|GroupBy
block|{
DECL|field|reason
specifier|final
name|String
name|reason
decl_stmt|;
DECL|field|index
specifier|final
name|Index
name|index
decl_stmt|;
DECL|field|causeType
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|causeType
decl_stmt|;
DECL|method|GroupBy
specifier|public
name|GroupBy
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|IndexException
condition|)
block|{
name|index
operator|=
operator|(
operator|(
name|IndexException
operator|)
name|t
operator|)
operator|.
name|index
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|index
operator|=
literal|null
expr_stmt|;
block|}
name|reason
operator|=
name|t
operator|.
name|getMessage
argument_list|()
expr_stmt|;
name|causeType
operator|=
name|t
operator|.
name|getClass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|GroupBy
name|groupBy
init|=
operator|(
name|GroupBy
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|causeType
operator|.
name|equals
argument_list|(
name|groupBy
operator|.
name|causeType
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|index
operator|!=
literal|null
condition|?
operator|!
name|index
operator|.
name|equals
argument_list|(
name|groupBy
operator|.
name|index
argument_list|)
else|:
name|groupBy
operator|.
name|index
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|?
operator|!
name|reason
operator|.
name|equals
argument_list|(
name|groupBy
operator|.
name|reason
argument_list|)
else|:
name|groupBy
operator|.
name|reason
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|reason
operator|!=
literal|null
condition|?
name|reason
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|index
operator|!=
literal|null
condition|?
name|index
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|causeType
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

