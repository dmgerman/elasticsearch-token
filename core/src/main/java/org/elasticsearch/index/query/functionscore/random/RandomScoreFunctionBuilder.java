begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.functionscore.random
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|random
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
name|lucene
operator|.
name|search
operator|.
name|function
operator|.
name|RandomScoreFunction
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
name|lucene
operator|.
name|search
operator|.
name|function
operator|.
name|ScoreFunction
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
name|fielddata
operator|.
name|IndexFieldData
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
name|mapper
operator|.
name|MappedFieldType
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
name|query
operator|.
name|QueryShardContext
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
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionBuilder
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
name|internal
operator|.
name|SearchContext
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * A function that computes a random score for the matched documents  */
end_comment

begin_class
DECL|class|RandomScoreFunctionBuilder
specifier|public
class|class
name|RandomScoreFunctionBuilder
extends|extends
name|ScoreFunctionBuilder
argument_list|<
name|RandomScoreFunctionBuilder
argument_list|>
block|{
DECL|field|seed
specifier|private
name|Integer
name|seed
decl_stmt|;
DECL|method|RandomScoreFunctionBuilder
specifier|public
name|RandomScoreFunctionBuilder
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|RandomScoreFunctionParser
operator|.
name|NAMES
index|[
literal|0
index|]
return|;
block|}
comment|/**      * Sets the seed based on which the random number will be generated. Using the same seed is guaranteed to generate the same      * random number for a specific doc.      *      * @param seed The seed.      */
DECL|method|seed
specifier|public
name|RandomScoreFunctionBuilder
name|seed
parameter_list|(
name|int
name|seed
parameter_list|)
block|{
name|this
operator|.
name|seed
operator|=
name|seed
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * seed variant taking a long value.      * @see #seed(int)      */
DECL|method|seed
specifier|public
name|RandomScoreFunctionBuilder
name|seed
parameter_list|(
name|long
name|seed
parameter_list|)
block|{
name|this
operator|.
name|seed
operator|=
name|hash
argument_list|(
name|seed
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * seed variant taking a String value.      * @see #seed(int)      */
DECL|method|seed
specifier|public
name|RandomScoreFunctionBuilder
name|seed
parameter_list|(
name|String
name|seed
parameter_list|)
block|{
if|if
condition|(
name|seed
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"random_score function: seed must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|seed
operator|=
name|seed
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|getSeed
specifier|public
name|Integer
name|getSeed
parameter_list|()
block|{
return|return
name|seed
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|public
name|void
name|doXContent
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
name|startObject
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|seed
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"seed"
argument_list|,
name|seed
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|RandomScoreFunctionBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|RandomScoreFunctionBuilder
name|randomScoreFunctionBuilder
init|=
operator|new
name|RandomScoreFunctionBuilder
argument_list|()
decl_stmt|;
name|randomScoreFunctionBuilder
operator|.
name|seed
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
return|return
name|randomScoreFunctionBuilder
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|RandomScoreFunctionBuilder
name|functionBuilder
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|seed
argument_list|,
name|functionBuilder
operator|.
name|seed
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|this
operator|.
name|seed
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doToFunction
specifier|protected
name|ScoreFunction
name|doToFunction
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
block|{
specifier|final
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|smartNameFieldType
argument_list|(
literal|"_uid"
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
comment|// mapper could be null if we are on a shard with no docs yet, so this won't actually be used
return|return
operator|new
name|RandomScoreFunction
argument_list|()
return|;
block|}
comment|//TODO find a way to not get the shard_id from the current search context? make it available in QueryShardContext?
comment|//this currently causes NPE in FunctionScoreQueryBuilderTests#testToQuery
specifier|final
name|ShardId
name|shardId
init|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|indexShard
argument_list|()
operator|.
name|shardId
argument_list|()
decl_stmt|;
specifier|final
name|int
name|salt
init|=
operator|(
name|context
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|<<
literal|10
operator|)
operator||
name|shardId
operator|.
name|id
argument_list|()
decl_stmt|;
specifier|final
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|uidFieldData
init|=
name|context
operator|.
name|getForField
argument_list|(
name|fieldType
argument_list|)
decl_stmt|;
return|return
operator|new
name|RandomScoreFunction
argument_list|(
name|this
operator|.
name|seed
operator|==
literal|null
condition|?
name|hash
argument_list|(
name|context
operator|.
name|nowInMillis
argument_list|()
argument_list|)
else|:
name|seed
argument_list|,
name|salt
argument_list|,
name|uidFieldData
argument_list|)
return|;
block|}
DECL|method|hash
specifier|private
specifier|static
name|int
name|hash
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
name|Long
operator|.
name|hashCode
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
end_class

end_unit

