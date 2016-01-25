begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.rescore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
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
name|ParseField
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
name|ParsingException
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
name|xcontent
operator|.
name|ObjectParser
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
name|QueryParseContext
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
name|search
operator|.
name|rescore
operator|.
name|QueryRescorer
operator|.
name|QueryRescoreContext
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
name|Objects
import|;
end_import

begin_class
DECL|class|QueryRescorerBuilder
specifier|public
class|class
name|QueryRescorerBuilder
extends|extends
name|AbstractRescoreBuilder
argument_list|<
name|QueryRescorerBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"query"
decl_stmt|;
DECL|field|PROTOTYPE
specifier|public
specifier|static
specifier|final
name|QueryRescorerBuilder
name|PROTOTYPE
init|=
operator|new
name|QueryRescorerBuilder
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_RESCORE_QUERYWEIGHT
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_RESCORE_QUERYWEIGHT
init|=
literal|1.0f
decl_stmt|;
DECL|field|DEFAULT_QUERYWEIGHT
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_QUERYWEIGHT
init|=
literal|1.0f
decl_stmt|;
DECL|field|DEFAULT_SCORE_MODE
specifier|public
specifier|static
specifier|final
name|QueryRescoreMode
name|DEFAULT_SCORE_MODE
init|=
name|QueryRescoreMode
operator|.
name|Total
decl_stmt|;
DECL|field|queryBuilder
specifier|private
specifier|final
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|queryBuilder
decl_stmt|;
DECL|field|rescoreQueryWeight
specifier|private
name|float
name|rescoreQueryWeight
init|=
name|DEFAULT_RESCORE_QUERYWEIGHT
decl_stmt|;
DECL|field|queryWeight
specifier|private
name|float
name|queryWeight
init|=
name|DEFAULT_QUERYWEIGHT
decl_stmt|;
DECL|field|scoreMode
specifier|private
name|QueryRescoreMode
name|scoreMode
init|=
name|DEFAULT_SCORE_MODE
decl_stmt|;
DECL|field|RESCORE_QUERY_FIELD
specifier|private
specifier|static
name|ParseField
name|RESCORE_QUERY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"rescore_query"
argument_list|)
decl_stmt|;
DECL|field|QUERY_WEIGHT_FIELD
specifier|private
specifier|static
name|ParseField
name|QUERY_WEIGHT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"query_weight"
argument_list|)
decl_stmt|;
DECL|field|RESCORE_QUERY_WEIGHT_FIELD
specifier|private
specifier|static
name|ParseField
name|RESCORE_QUERY_WEIGHT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"rescore_query_weight"
argument_list|)
decl_stmt|;
DECL|field|SCORE_MODE_FIELD
specifier|private
specifier|static
name|ParseField
name|SCORE_MODE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"score_mode"
argument_list|)
decl_stmt|;
DECL|field|QUERY_RESCORE_PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|InnerBuilder
argument_list|,
name|QueryParseContext
argument_list|>
name|QUERY_RESCORE_PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|NAME
argument_list|,
literal|null
argument_list|)
decl_stmt|;
static|static
block|{
name|QUERY_RESCORE_PARSER
operator|.
name|declareObject
argument_list|(
name|InnerBuilder
operator|::
name|setQueryBuilder
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
block|{
try|try
block|{
return|return
name|c
operator|.
name|parseInnerQueryBuilder
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|p
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Could not parse inner query"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|,
name|RESCORE_QUERY_FIELD
argument_list|)
expr_stmt|;
name|QUERY_RESCORE_PARSER
operator|.
name|declareFloat
argument_list|(
name|InnerBuilder
operator|::
name|setQueryWeight
argument_list|,
name|QUERY_WEIGHT_FIELD
argument_list|)
expr_stmt|;
name|QUERY_RESCORE_PARSER
operator|.
name|declareFloat
argument_list|(
name|InnerBuilder
operator|::
name|setRescoreQueryWeight
argument_list|,
name|RESCORE_QUERY_WEIGHT_FIELD
argument_list|)
expr_stmt|;
name|QUERY_RESCORE_PARSER
operator|.
name|declareString
argument_list|(
parameter_list|(
name|struct
parameter_list|,
name|value
parameter_list|)
lambda|->
name|struct
operator|.
name|setScoreMode
argument_list|(
name|QueryRescoreMode
operator|.
name|fromString
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|,
name|SCORE_MODE_FIELD
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new {@link QueryRescorerBuilder} instance      * @param builder the query builder to build the rescore query from      */
DECL|method|QueryRescorerBuilder
specifier|public
name|QueryRescorerBuilder
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|builder
parameter_list|)
block|{
name|this
operator|.
name|queryBuilder
operator|=
name|builder
expr_stmt|;
block|}
comment|/**      * @return the query used for this rescore query      */
DECL|method|getRescoreQuery
specifier|public
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|getRescoreQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryBuilder
return|;
block|}
comment|/**      * Sets the original query weight for rescoring. The default is<tt>1.0</tt>      */
DECL|method|setQueryWeight
specifier|public
name|QueryRescorerBuilder
name|setQueryWeight
parameter_list|(
name|float
name|queryWeight
parameter_list|)
block|{
name|this
operator|.
name|queryWeight
operator|=
name|queryWeight
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the original query weight for rescoring. The default is<tt>1.0</tt>      */
DECL|method|getQueryWeight
specifier|public
name|float
name|getQueryWeight
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryWeight
return|;
block|}
comment|/**      * Sets the original query weight for rescoring. The default is<tt>1.0</tt>      */
DECL|method|setRescoreQueryWeight
specifier|public
name|QueryRescorerBuilder
name|setRescoreQueryWeight
parameter_list|(
name|float
name|rescoreQueryWeight
parameter_list|)
block|{
name|this
operator|.
name|rescoreQueryWeight
operator|=
name|rescoreQueryWeight
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the original query weight for rescoring. The default is<tt>1.0</tt>      */
DECL|method|getRescoreQueryWeight
specifier|public
name|float
name|getRescoreQueryWeight
parameter_list|()
block|{
return|return
name|this
operator|.
name|rescoreQueryWeight
return|;
block|}
comment|/**      * Sets the original query score mode. The default is {@link QueryRescoreMode#Total}.      */
DECL|method|setScoreMode
specifier|public
name|QueryRescorerBuilder
name|setScoreMode
parameter_list|(
name|QueryRescoreMode
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
comment|/**      * Gets the original query score mode. The default is<tt>total</tt>      */
DECL|method|getScoreMode
specifier|public
name|QueryRescoreMode
name|getScoreMode
parameter_list|()
block|{
return|return
name|this
operator|.
name|scoreMode
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
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|RESCORE_QUERY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|queryBuilder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|QUERY_WEIGHT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|queryWeight
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|RESCORE_QUERY_WEIGHT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|rescoreQueryWeight
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|QueryRescorerBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|InnerBuilder
name|innerBuilder
init|=
name|QUERY_RESCORE_PARSER
operator|.
name|parse
argument_list|(
name|parseContext
operator|.
name|parser
argument_list|()
argument_list|,
operator|new
name|InnerBuilder
argument_list|()
argument_list|,
name|parseContext
argument_list|)
decl_stmt|;
return|return
name|innerBuilder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|QueryRescoreContext
name|build
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
operator|.
name|QueryRescorer
name|rescorer
init|=
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
operator|.
name|QueryRescorer
argument_list|()
decl_stmt|;
name|QueryRescoreContext
name|queryRescoreContext
init|=
operator|new
name|QueryRescoreContext
argument_list|(
name|rescorer
argument_list|)
decl_stmt|;
name|queryRescoreContext
operator|.
name|setQuery
argument_list|(
name|this
operator|.
name|queryBuilder
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
name|queryRescoreContext
operator|.
name|setQueryWeight
argument_list|(
name|this
operator|.
name|queryWeight
argument_list|)
expr_stmt|;
name|queryRescoreContext
operator|.
name|setRescoreQueryWeight
argument_list|(
name|this
operator|.
name|rescoreQueryWeight
argument_list|)
expr_stmt|;
name|queryRescoreContext
operator|.
name|setScoreMode
argument_list|(
name|this
operator|.
name|scoreMode
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|windowSize
operator|!=
literal|null
condition|)
block|{
name|queryRescoreContext
operator|.
name|setWindowSize
argument_list|(
name|this
operator|.
name|windowSize
argument_list|)
expr_stmt|;
block|}
return|return
name|queryRescoreContext
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
specifier|final
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
return|return
literal|31
operator|*
name|result
operator|+
name|Objects
operator|.
name|hash
argument_list|(
name|scoreMode
argument_list|,
name|queryWeight
argument_list|,
name|rescoreQueryWeight
argument_list|,
name|queryBuilder
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
specifier|final
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
name|QueryRescorerBuilder
name|other
init|=
operator|(
name|QueryRescorerBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
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
name|queryWeight
argument_list|,
name|other
operator|.
name|queryWeight
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|rescoreQueryWeight
argument_list|,
name|other
operator|.
name|rescoreQueryWeight
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|queryBuilder
argument_list|,
name|other
operator|.
name|queryBuilder
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|public
name|QueryRescorerBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryRescorerBuilder
name|rescorer
init|=
operator|new
name|QueryRescorerBuilder
argument_list|(
name|in
operator|.
name|readQuery
argument_list|()
argument_list|)
decl_stmt|;
name|rescorer
operator|.
name|setScoreMode
argument_list|(
name|QueryRescoreMode
operator|.
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|rescorer
operator|.
name|setRescoreQueryWeight
argument_list|(
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
expr_stmt|;
name|rescorer
operator|.
name|setQueryWeight
argument_list|(
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|rescorer
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|public
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
name|queryBuilder
argument_list|)
expr_stmt|;
name|scoreMode
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|rescoreQueryWeight
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|queryWeight
argument_list|)
expr_stmt|;
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
name|NAME
return|;
block|}
comment|/**      * Helper to be able to use {@link ObjectParser}, since we need the inner query builder      * for the constructor of {@link QueryRescorerBuilder}, but {@link ObjectParser} only      * allows filling properties of an already constructed value.      */
DECL|class|InnerBuilder
specifier|private
class|class
name|InnerBuilder
block|{
DECL|field|queryBuilder
specifier|private
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|queryBuilder
decl_stmt|;
DECL|field|rescoreQueryWeight
specifier|private
name|float
name|rescoreQueryWeight
init|=
name|DEFAULT_RESCORE_QUERYWEIGHT
decl_stmt|;
DECL|field|queryWeight
specifier|private
name|float
name|queryWeight
init|=
name|DEFAULT_QUERYWEIGHT
decl_stmt|;
DECL|field|scoreMode
specifier|private
name|QueryRescoreMode
name|scoreMode
init|=
name|DEFAULT_SCORE_MODE
decl_stmt|;
DECL|method|setQueryBuilder
name|void
name|setQueryBuilder
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|builder
parameter_list|)
block|{
name|this
operator|.
name|queryBuilder
operator|=
name|builder
expr_stmt|;
block|}
DECL|method|build
name|QueryRescorerBuilder
name|build
parameter_list|()
block|{
name|QueryRescorerBuilder
name|queryRescoreBuilder
init|=
operator|new
name|QueryRescorerBuilder
argument_list|(
name|queryBuilder
argument_list|)
decl_stmt|;
name|queryRescoreBuilder
operator|.
name|setQueryWeight
argument_list|(
name|queryWeight
argument_list|)
expr_stmt|;
name|queryRescoreBuilder
operator|.
name|setRescoreQueryWeight
argument_list|(
name|rescoreQueryWeight
argument_list|)
expr_stmt|;
name|queryRescoreBuilder
operator|.
name|setScoreMode
argument_list|(
name|scoreMode
argument_list|)
expr_stmt|;
return|return
name|queryRescoreBuilder
return|;
block|}
DECL|method|setQueryWeight
name|void
name|setQueryWeight
parameter_list|(
name|float
name|queryWeight
parameter_list|)
block|{
name|this
operator|.
name|queryWeight
operator|=
name|queryWeight
expr_stmt|;
block|}
DECL|method|setRescoreQueryWeight
name|void
name|setRescoreQueryWeight
parameter_list|(
name|float
name|rescoreQueryWeight
parameter_list|)
block|{
name|this
operator|.
name|rescoreQueryWeight
operator|=
name|rescoreQueryWeight
expr_stmt|;
block|}
DECL|method|setScoreMode
name|void
name|setScoreMode
parameter_list|(
name|QueryRescoreMode
name|scoreMode
parameter_list|)
block|{
name|this
operator|.
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

