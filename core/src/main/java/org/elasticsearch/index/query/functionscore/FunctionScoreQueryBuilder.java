begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.functionscore
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
name|search
operator|.
name|MatchAllDocsQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|CombineFunction
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
name|FiltersFunctionScoreQuery
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
name|FunctionScoreQuery
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
name|index
operator|.
name|query
operator|.
name|AbstractQueryBuilder
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
name|EmptyQueryBuilder
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
name|MatchAllQueryBuilder
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
name|QueryBuilder
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
name|QueryRewriteContext
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
name|random
operator|.
name|RandomScoreFunctionBuilder
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
name|Arrays
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * A query that uses a filters with a script associated with them to compute the  * score.  */
end_comment

begin_class
DECL|class|FunctionScoreQueryBuilder
specifier|public
class|class
name|FunctionScoreQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|FunctionScoreQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"function_score"
decl_stmt|;
DECL|field|DEFAULT_BOOST_MODE
specifier|public
specifier|static
specifier|final
name|CombineFunction
name|DEFAULT_BOOST_MODE
init|=
name|CombineFunction
operator|.
name|MULTIPLY
decl_stmt|;
DECL|field|DEFAULT_SCORE_MODE
specifier|public
specifier|static
specifier|final
name|FiltersFunctionScoreQuery
operator|.
name|ScoreMode
name|DEFAULT_SCORE_MODE
init|=
name|FiltersFunctionScoreQuery
operator|.
name|ScoreMode
operator|.
name|MULTIPLY
decl_stmt|;
DECL|field|query
specifier|private
specifier|final
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
decl_stmt|;
DECL|field|maxBoost
specifier|private
name|float
name|maxBoost
init|=
name|FunctionScoreQuery
operator|.
name|DEFAULT_MAX_BOOST
decl_stmt|;
DECL|field|scoreMode
specifier|private
name|FiltersFunctionScoreQuery
operator|.
name|ScoreMode
name|scoreMode
init|=
name|DEFAULT_SCORE_MODE
decl_stmt|;
DECL|field|boostMode
specifier|private
name|CombineFunction
name|boostMode
decl_stmt|;
DECL|field|minScore
specifier|private
name|Float
name|minScore
init|=
literal|null
decl_stmt|;
DECL|field|filterFunctionBuilders
specifier|private
specifier|final
name|FilterFunctionBuilder
index|[]
name|filterFunctionBuilders
decl_stmt|;
comment|/**      * Creates a function_score query without functions      *      * @param query the query that needs to be custom scored      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
parameter_list|)
block|{
name|this
argument_list|(
name|query
argument_list|,
operator|new
name|FilterFunctionBuilder
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a function_score query that executes the provided filters and functions on all documents      *      * @param filterFunctionBuilders the filters and functions      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|FilterFunctionBuilder
index|[]
name|filterFunctionBuilders
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
name|filterFunctionBuilders
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a function_score query that will execute the function provided on all documents      *      * @param scoreFunctionBuilder score function that is executed      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|ScoreFunctionBuilder
name|scoreFunctionBuilder
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
operator|new
name|FilterFunctionBuilder
index|[]
block|{
operator|new
name|FilterFunctionBuilder
argument_list|(
name|scoreFunctionBuilder
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a function_score query that will execute the function provided in the context of the provided query      *      * @param query the query to custom score      * @param scoreFunctionBuilder score function that is executed      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
parameter_list|,
name|ScoreFunctionBuilder
name|scoreFunctionBuilder
parameter_list|)
block|{
name|this
argument_list|(
name|query
argument_list|,
operator|new
name|FilterFunctionBuilder
index|[]
block|{
operator|new
name|FilterFunctionBuilder
argument_list|(
name|scoreFunctionBuilder
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a function_score query that executes the provided filters and functions on documents that match a query.      *      * @param query the query that defines which documents the function_score query will be executed on.      * @param filterFunctionBuilders the filters and functions      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
parameter_list|,
name|FilterFunctionBuilder
index|[]
name|filterFunctionBuilders
parameter_list|)
block|{
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"function_score: query must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|filterFunctionBuilders
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"function_score: filters and functions array must not be null"
argument_list|)
throw|;
block|}
for|for
control|(
name|FilterFunctionBuilder
name|filterFunctionBuilder
range|:
name|filterFunctionBuilders
control|)
block|{
if|if
condition|(
name|filterFunctionBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"function_score: each filter and function must not be null"
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
name|this
operator|.
name|filterFunctionBuilders
operator|=
name|filterFunctionBuilders
expr_stmt|;
block|}
comment|/**      * Returns the query that defines which documents the function_score query will be executed on.      */
DECL|method|query
specifier|public
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
parameter_list|()
block|{
return|return
name|this
operator|.
name|query
return|;
block|}
comment|/**      * Returns the filters and functions      */
DECL|method|filterFunctionBuilders
specifier|public
name|FilterFunctionBuilder
index|[]
name|filterFunctionBuilders
parameter_list|()
block|{
return|return
name|this
operator|.
name|filterFunctionBuilders
return|;
block|}
comment|/**      * Score mode defines how results of individual score functions will be aggregated.      * @see org.elasticsearch.common.lucene.search.function.FiltersFunctionScoreQuery.ScoreMode      */
DECL|method|scoreMode
specifier|public
name|FunctionScoreQueryBuilder
name|scoreMode
parameter_list|(
name|FiltersFunctionScoreQuery
operator|.
name|ScoreMode
name|scoreMode
parameter_list|)
block|{
if|if
condition|(
name|scoreMode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"["
operator|+
name|NAME
operator|+
literal|"]  requires 'score_mode' field"
argument_list|)
throw|;
block|}
name|this
operator|.
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the score mode, meaning how results of individual score functions will be aggregated.      * @see org.elasticsearch.common.lucene.search.function.FiltersFunctionScoreQuery.ScoreMode      */
DECL|method|scoreMode
specifier|public
name|FiltersFunctionScoreQuery
operator|.
name|ScoreMode
name|scoreMode
parameter_list|()
block|{
return|return
name|this
operator|.
name|scoreMode
return|;
block|}
comment|/**      * Boost mode defines how the combined result of score functions will influence the final score together with the sub query score.      * @see CombineFunction      */
DECL|method|boostMode
specifier|public
name|FunctionScoreQueryBuilder
name|boostMode
parameter_list|(
name|CombineFunction
name|combineFunction
parameter_list|)
block|{
if|if
condition|(
name|combineFunction
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"["
operator|+
name|NAME
operator|+
literal|"]  requires 'boost_mode' field"
argument_list|)
throw|;
block|}
name|this
operator|.
name|boostMode
operator|=
name|combineFunction
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the boost mode, meaning how the combined result of score functions will influence the final score together with the sub query score.      * @see CombineFunction      */
DECL|method|boostMode
specifier|public
name|CombineFunction
name|boostMode
parameter_list|()
block|{
return|return
name|this
operator|.
name|boostMode
return|;
block|}
comment|/**      * Sets the maximum boost that will be applied by function score.      */
DECL|method|maxBoost
specifier|public
name|FunctionScoreQueryBuilder
name|maxBoost
parameter_list|(
name|float
name|maxBoost
parameter_list|)
block|{
name|this
operator|.
name|maxBoost
operator|=
name|maxBoost
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the maximum boost that will be applied by function score.      */
DECL|method|maxBoost
specifier|public
name|float
name|maxBoost
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxBoost
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
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
name|NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|QUERY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|query
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
name|startArray
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|FUNCTIONS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|FilterFunctionBuilder
name|filterFunctionBuilder
range|:
name|filterFunctionBuilders
control|)
block|{
name|filterFunctionBuilder
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
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|SCORE_MODE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|scoreMode
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|boostMode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|BOOST_MODE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|boostMode
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|MAX_BOOST_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|maxBoost
argument_list|)
expr_stmt|;
if|if
condition|(
name|minScore
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|MIN_SCORE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|minScore
argument_list|)
expr_stmt|;
block|}
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|setMinScore
specifier|public
name|FunctionScoreQueryBuilder
name|setMinScore
parameter_list|(
name|float
name|minScore
parameter_list|)
block|{
name|this
operator|.
name|minScore
operator|=
name|minScore
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|getMinScore
specifier|public
name|Float
name|getMinScore
parameter_list|()
block|{
return|return
name|this
operator|.
name|minScore
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|FunctionScoreQueryBuilder
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|FunctionScoreQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|query
argument_list|,
name|other
operator|.
name|query
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|filterFunctionBuilders
argument_list|,
name|other
operator|.
name|filterFunctionBuilders
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|boostMode
argument_list|,
name|other
operator|.
name|boostMode
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|scoreMode
argument_list|,
name|other
operator|.
name|scoreMode
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|minScore
argument_list|,
name|other
operator|.
name|minScore
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|maxBoost
argument_list|,
name|other
operator|.
name|maxBoost
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
name|query
argument_list|,
name|Arrays
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|filterFunctionBuilders
argument_list|)
argument_list|,
name|this
operator|.
name|boostMode
argument_list|,
name|this
operator|.
name|scoreMode
argument_list|,
name|this
operator|.
name|minScore
argument_list|,
name|this
operator|.
name|maxBoost
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|FunctionScoreQueryBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
init|=
name|in
operator|.
name|readQuery
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|FilterFunctionBuilder
index|[]
name|filterFunctionBuilders
init|=
operator|new
name|FilterFunctionBuilder
index|[
name|size
index|]
decl_stmt|;
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
name|filterFunctionBuilders
index|[
name|i
index|]
operator|=
name|FilterFunctionBuilder
operator|.
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|FunctionScoreQueryBuilder
name|functionScoreQueryBuilder
init|=
operator|new
name|FunctionScoreQueryBuilder
argument_list|(
name|query
argument_list|,
name|filterFunctionBuilders
argument_list|)
decl_stmt|;
name|functionScoreQueryBuilder
operator|.
name|maxBoost
argument_list|(
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|functionScoreQueryBuilder
operator|.
name|setMinScore
argument_list|(
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|functionScoreQueryBuilder
operator|.
name|boostMode
argument_list|(
name|CombineFunction
operator|.
name|readCombineFunctionFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|functionScoreQueryBuilder
operator|.
name|scoreMode
argument_list|(
name|FiltersFunctionScoreQuery
operator|.
name|ScoreMode
operator|.
name|readScoreModeFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|functionScoreQueryBuilder
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
name|writeQuery
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|filterFunctionBuilders
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|FilterFunctionBuilder
name|filterFunctionBuilder
range|:
name|filterFunctionBuilders
control|)
block|{
name|filterFunctionBuilder
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeFloat
argument_list|(
name|maxBoost
argument_list|)
expr_stmt|;
if|if
condition|(
name|minScore
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|minScore
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boostMode
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|boostMode
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|scoreMode
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doToQuery
specifier|protected
name|Query
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|FiltersFunctionScoreQuery
operator|.
name|FilterFunction
index|[]
name|filterFunctions
init|=
operator|new
name|FiltersFunctionScoreQuery
operator|.
name|FilterFunction
index|[
name|filterFunctionBuilders
operator|.
name|length
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FilterFunctionBuilder
name|filterFunctionBuilder
range|:
name|filterFunctionBuilders
control|)
block|{
name|Query
name|filter
init|=
name|filterFunctionBuilder
operator|.
name|getFilter
argument_list|()
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|ScoreFunction
name|scoreFunction
init|=
name|filterFunctionBuilder
operator|.
name|getScoreFunction
argument_list|()
operator|.
name|toFunction
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|filterFunctions
index|[
name|i
operator|++
index|]
operator|=
operator|new
name|FiltersFunctionScoreQuery
operator|.
name|FilterFunction
argument_list|(
name|filter
argument_list|,
name|scoreFunction
argument_list|)
expr_stmt|;
block|}
name|Query
name|query
init|=
name|this
operator|.
name|query
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
name|query
operator|=
operator|new
name|MatchAllDocsQuery
argument_list|()
expr_stmt|;
block|}
comment|// handle cases where only one score function and no filter was provided. In this case we create a FunctionScoreQuery.
if|if
condition|(
name|filterFunctions
operator|.
name|length
operator|==
literal|0
operator|||
name|filterFunctions
operator|.
name|length
operator|==
literal|1
operator|&&
operator|(
name|this
operator|.
name|filterFunctionBuilders
index|[
literal|0
index|]
operator|.
name|getFilter
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|MatchAllQueryBuilder
operator|.
name|NAME
argument_list|)
operator|)
condition|)
block|{
name|ScoreFunction
name|function
init|=
name|filterFunctions
operator|.
name|length
operator|==
literal|0
condition|?
literal|null
else|:
name|filterFunctions
index|[
literal|0
index|]
operator|.
name|function
decl_stmt|;
name|CombineFunction
name|combineFunction
init|=
name|this
operator|.
name|boostMode
decl_stmt|;
if|if
condition|(
name|combineFunction
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|function
operator|!=
literal|null
condition|)
block|{
name|combineFunction
operator|=
name|function
operator|.
name|getDefaultScoreCombiner
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|combineFunction
operator|=
name|DEFAULT_BOOST_MODE
expr_stmt|;
block|}
block|}
return|return
operator|new
name|FunctionScoreQuery
argument_list|(
name|query
argument_list|,
name|function
argument_list|,
name|minScore
argument_list|,
name|combineFunction
argument_list|,
name|maxBoost
argument_list|)
return|;
block|}
comment|// in all other cases we create a FiltersFunctionScoreQuery
return|return
operator|new
name|FiltersFunctionScoreQuery
argument_list|(
name|query
argument_list|,
name|scoreMode
argument_list|,
name|filterFunctions
argument_list|,
name|maxBoost
argument_list|,
name|minScore
argument_list|,
name|boostMode
operator|==
literal|null
condition|?
name|DEFAULT_BOOST_MODE
else|:
name|boostMode
argument_list|)
return|;
block|}
comment|/**      * Function to be associated with an optional filter, meaning it will be executed only for the documents      * that match the given filter.      */
DECL|class|FilterFunctionBuilder
specifier|public
specifier|static
class|class
name|FilterFunctionBuilder
implements|implements
name|ToXContent
implements|,
name|Writeable
argument_list|<
name|FilterFunctionBuilder
argument_list|>
block|{
DECL|field|PROTOTYPE
specifier|private
specifier|static
specifier|final
name|FilterFunctionBuilder
name|PROTOTYPE
init|=
operator|new
name|FilterFunctionBuilder
argument_list|(
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
argument_list|,
operator|new
name|RandomScoreFunctionBuilder
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
decl_stmt|;
DECL|field|scoreFunction
specifier|private
specifier|final
name|ScoreFunctionBuilder
name|scoreFunction
decl_stmt|;
DECL|method|FilterFunctionBuilder
specifier|public
name|FilterFunctionBuilder
parameter_list|(
name|ScoreFunctionBuilder
name|scoreFunctionBuilder
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
name|scoreFunctionBuilder
argument_list|)
expr_stmt|;
block|}
DECL|method|FilterFunctionBuilder
specifier|public
name|FilterFunctionBuilder
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
parameter_list|,
name|ScoreFunctionBuilder
name|scoreFunction
parameter_list|)
block|{
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"function_score: filter must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|scoreFunction
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"function_score: function must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|scoreFunction
operator|=
name|scoreFunction
expr_stmt|;
block|}
DECL|method|getFilter
specifier|public
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
DECL|method|getScoreFunction
specifier|public
name|ScoreFunctionBuilder
argument_list|<
name|?
argument_list|>
name|getScoreFunction
parameter_list|()
block|{
return|return
name|scoreFunction
return|;
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
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|FILTER_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|scoreFunction
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
return|return
name|builder
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
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|filter
argument_list|,
name|scoreFunction
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|FilterFunctionBuilder
name|that
init|=
operator|(
name|FilterFunctionBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|filter
argument_list|,
name|that
operator|.
name|filter
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|scoreFunction
argument_list|,
name|that
operator|.
name|scoreFunction
argument_list|)
return|;
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
name|out
operator|.
name|writeQuery
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeScoreFunction
argument_list|(
name|scoreFunction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|FilterFunctionBuilder
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FilterFunctionBuilder
argument_list|(
name|in
operator|.
name|readQuery
argument_list|()
argument_list|,
name|in
operator|.
name|readScoreFunction
argument_list|()
argument_list|)
return|;
block|}
DECL|method|rewrite
specifier|public
name|FilterFunctionBuilder
name|rewrite
parameter_list|(
name|QueryRewriteContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|rewrite
init|=
name|filter
operator|.
name|rewrite
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|rewrite
operator|!=
name|filter
condition|)
block|{
return|return
operator|new
name|FilterFunctionBuilder
argument_list|(
name|rewrite
argument_list|,
name|scoreFunction
argument_list|)
return|;
block|}
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|doRewrite
specifier|protected
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|doRewrite
parameter_list|(
name|QueryRewriteContext
name|queryRewriteContext
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|queryBuilder
init|=
name|this
operator|.
name|query
operator|.
name|rewrite
argument_list|(
name|queryRewriteContext
argument_list|)
decl_stmt|;
name|FilterFunctionBuilder
index|[]
name|rewrittenBuilders
init|=
operator|new
name|FilterFunctionBuilder
index|[
name|this
operator|.
name|filterFunctionBuilders
operator|.
name|length
index|]
decl_stmt|;
name|boolean
name|rewritten
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rewrittenBuilders
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|FilterFunctionBuilder
name|rewrite
init|=
name|filterFunctionBuilders
index|[
name|i
index|]
operator|.
name|rewrite
argument_list|(
name|queryRewriteContext
argument_list|)
decl_stmt|;
name|rewritten
operator||=
name|rewrite
operator|!=
name|filterFunctionBuilders
index|[
name|i
index|]
expr_stmt|;
name|rewrittenBuilders
index|[
name|i
index|]
operator|=
name|rewrite
expr_stmt|;
block|}
if|if
condition|(
name|queryBuilder
operator|!=
name|query
operator|||
name|rewritten
condition|)
block|{
name|FunctionScoreQueryBuilder
name|newQueryBuilder
init|=
operator|new
name|FunctionScoreQueryBuilder
argument_list|(
name|queryBuilder
argument_list|,
name|rewrittenBuilders
argument_list|)
decl_stmt|;
name|newQueryBuilder
operator|.
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
name|newQueryBuilder
operator|.
name|minScore
operator|=
name|minScore
expr_stmt|;
name|newQueryBuilder
operator|.
name|maxBoost
operator|=
name|maxBoost
expr_stmt|;
return|return
name|newQueryBuilder
return|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

