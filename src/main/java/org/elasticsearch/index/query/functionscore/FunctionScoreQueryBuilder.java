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
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|BaseQueryBuilder
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
name|BoostableQueryBuilder
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
name|FilterBuilder
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
name|ArrayList
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
name|BaseQueryBuilder
implements|implements
name|BoostableQueryBuilder
argument_list|<
name|FunctionScoreQueryBuilder
argument_list|>
block|{
DECL|field|queryBuilder
specifier|private
specifier|final
name|QueryBuilder
name|queryBuilder
decl_stmt|;
DECL|field|filterBuilder
specifier|private
specifier|final
name|FilterBuilder
name|filterBuilder
decl_stmt|;
DECL|field|boost
specifier|private
name|Float
name|boost
decl_stmt|;
DECL|field|maxBoost
specifier|private
name|Float
name|maxBoost
decl_stmt|;
DECL|field|scoreMode
specifier|private
name|String
name|scoreMode
decl_stmt|;
DECL|field|boostMode
specifier|private
name|String
name|boostMode
decl_stmt|;
DECL|field|filters
specifier|private
name|ArrayList
argument_list|<
name|FilterBuilder
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|scoreFunctions
specifier|private
name|ArrayList
argument_list|<
name|ScoreFunctionBuilder
argument_list|>
name|scoreFunctions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|minScore
specifier|private
name|Float
name|minScore
init|=
literal|null
decl_stmt|;
comment|/**      * Creates a function_score query that executes on documents that match query a query.      * Query and filter will be wrapped into a filtered_query.      *      * @param queryBuilder the query that defines which documents the function_score query will be executed on.      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|this
operator|.
name|queryBuilder
operator|=
name|queryBuilder
expr_stmt|;
name|this
operator|.
name|filterBuilder
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Creates a function_score query that executes on documents that match query a query.      * Query and filter will be wrapped into a filtered_query.      *      * @param filterBuilder the filter that defines which documents the function_score query will be executed on.      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|this
operator|.
name|filterBuilder
operator|=
name|filterBuilder
expr_stmt|;
name|this
operator|.
name|queryBuilder
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Creates a function_score query that executes on documents that match query and filter.      * Query and filter will be wrapped into a filtered_query.      *      * @param queryBuilder a query that will; be wrapped in a filtered query.      * @param filterBuilder the filter for the filtered query.      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|,
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|this
operator|.
name|filterBuilder
operator|=
name|filterBuilder
expr_stmt|;
name|this
operator|.
name|queryBuilder
operator|=
name|queryBuilder
expr_stmt|;
block|}
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|()
block|{
name|this
operator|.
name|filterBuilder
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|queryBuilder
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Creates a function_score query that will execute the function scoreFunctionBuilder on all documents.      *      * @param scoreFunctionBuilder score function that is executed      */
DECL|method|FunctionScoreQueryBuilder
specifier|public
name|FunctionScoreQueryBuilder
parameter_list|(
name|ScoreFunctionBuilder
name|scoreFunctionBuilder
parameter_list|)
block|{
if|if
condition|(
name|scoreFunctionBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"function_score: function must not be null"
argument_list|)
throw|;
block|}
name|queryBuilder
operator|=
literal|null
expr_stmt|;
name|filterBuilder
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|filters
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|scoreFunctions
operator|.
name|add
argument_list|(
name|scoreFunctionBuilder
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a score function that will will execute the function scoreFunctionBuilder on all documents matching the filter.      *      * @param filter the filter that defines which documents the function_score query will be executed on.      * @param scoreFunctionBuilder score function that is executed      */
DECL|method|add
specifier|public
name|FunctionScoreQueryBuilder
name|add
parameter_list|(
name|FilterBuilder
name|filter
parameter_list|,
name|ScoreFunctionBuilder
name|scoreFunctionBuilder
parameter_list|)
block|{
if|if
condition|(
name|scoreFunctionBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"function_score: function must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|filters
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|this
operator|.
name|scoreFunctions
operator|.
name|add
argument_list|(
name|scoreFunctionBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a score function that will will execute the function scoreFunctionBuilder on all documents.      *      * @param scoreFunctionBuilder score function that is executed      */
DECL|method|add
specifier|public
name|FunctionScoreQueryBuilder
name|add
parameter_list|(
name|ScoreFunctionBuilder
name|scoreFunctionBuilder
parameter_list|)
block|{
if|if
condition|(
name|scoreFunctionBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"function_score: function must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|filters
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|scoreFunctions
operator|.
name|add
argument_list|(
name|scoreFunctionBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Score mode defines how results of individual score functions will be aggregated.      * Can be first, avg, max, sum, min, multiply      */
DECL|method|scoreMode
specifier|public
name|FunctionScoreQueryBuilder
name|scoreMode
parameter_list|(
name|String
name|scoreMode
parameter_list|)
block|{
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
comment|/**      * Score mode defines how the combined result of score functions will influence the final score together with the sub query score.      * Can be replace, avg, max, sum, min, multiply      */
DECL|method|boostMode
specifier|public
name|FunctionScoreQueryBuilder
name|boostMode
parameter_list|(
name|String
name|boostMode
parameter_list|)
block|{
name|this
operator|.
name|boostMode
operator|=
name|boostMode
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Score mode defines how the combined result of score functions will influence the final score together with the sub query score.      */
DECL|method|boostMode
specifier|public
name|FunctionScoreQueryBuilder
name|boostMode
parameter_list|(
name|CombineFunction
name|combineFunction
parameter_list|)
block|{
name|this
operator|.
name|boostMode
operator|=
name|combineFunction
operator|.
name|getName
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Tha maximum boost that will be applied by function score.      */
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
comment|/**      * Sets the boost for this query. Documents matching this query will (in      * addition to the normal weightings) have their score multiplied by the      * boost provided.      */
annotation|@
name|Override
DECL|method|boost
specifier|public
name|FunctionScoreQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
return|return
name|this
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
name|FunctionScoreQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryBuilder
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"query"
argument_list|)
expr_stmt|;
name|queryBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filterBuilder
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"filter"
argument_list|)
expr_stmt|;
name|filterBuilder
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
literal|"functions"
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
name|filters
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"filter"
argument_list|)
expr_stmt|;
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|scoreFunctions
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
if|if
condition|(
name|scoreMode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"score_mode"
argument_list|,
name|scoreMode
argument_list|)
expr_stmt|;
block|}
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
literal|"boost_mode"
argument_list|,
name|boostMode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxBoost
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_boost"
argument_list|,
name|maxBoost
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boost
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
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
literal|"min_score"
argument_list|,
name|minScore
argument_list|)
expr_stmt|;
block|}
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
block|}
end_class

end_unit

