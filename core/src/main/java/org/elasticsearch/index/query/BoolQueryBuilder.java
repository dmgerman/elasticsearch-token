begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|BooleanClause
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
name|BooleanClause
operator|.
name|Occur
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
name|BooleanQuery
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
name|Version
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
name|lucene
operator|.
name|search
operator|.
name|Queries
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
name|XContentParser
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
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import static
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
name|Queries
operator|.
name|fixNegativeQueryIfNeeded
import|;
end_import

begin_comment
comment|/**  * A Query that matches documents matching boolean combinations of other queries.  */
end_comment

begin_class
DECL|class|BoolQueryBuilder
specifier|public
class|class
name|BoolQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|BoolQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"bool"
decl_stmt|;
DECL|field|ADJUST_PURE_NEGATIVE_DEFAULT
specifier|public
specifier|static
specifier|final
name|boolean
name|ADJUST_PURE_NEGATIVE_DEFAULT
init|=
literal|true
decl_stmt|;
DECL|field|MUSTNOT
specifier|private
specifier|static
specifier|final
name|String
name|MUSTNOT
init|=
literal|"mustNot"
decl_stmt|;
DECL|field|MUST_NOT
specifier|private
specifier|static
specifier|final
name|String
name|MUST_NOT
init|=
literal|"must_not"
decl_stmt|;
DECL|field|FILTER
specifier|private
specifier|static
specifier|final
name|String
name|FILTER
init|=
literal|"filter"
decl_stmt|;
DECL|field|SHOULD
specifier|private
specifier|static
specifier|final
name|String
name|SHOULD
init|=
literal|"should"
decl_stmt|;
DECL|field|MUST
specifier|private
specifier|static
specifier|final
name|String
name|MUST
init|=
literal|"must"
decl_stmt|;
DECL|field|DISABLE_COORD_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|DISABLE_COORD_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"disable_coord"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"disable_coord has been removed"
argument_list|)
decl_stmt|;
DECL|field|MINIMUM_SHOULD_MATCH
specifier|private
specifier|static
specifier|final
name|ParseField
name|MINIMUM_SHOULD_MATCH
init|=
operator|new
name|ParseField
argument_list|(
literal|"minimum_should_match"
argument_list|)
decl_stmt|;
DECL|field|ADJUST_PURE_NEGATIVE
specifier|private
specifier|static
specifier|final
name|ParseField
name|ADJUST_PURE_NEGATIVE
init|=
operator|new
name|ParseField
argument_list|(
literal|"adjust_pure_negative"
argument_list|)
decl_stmt|;
DECL|field|mustClauses
specifier|private
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|mustClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|mustNotClauses
specifier|private
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|mustNotClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|filterClauses
specifier|private
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|filterClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|shouldClauses
specifier|private
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|shouldClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|adjustPureNegative
specifier|private
name|boolean
name|adjustPureNegative
init|=
name|ADJUST_PURE_NEGATIVE_DEFAULT
decl_stmt|;
DECL|field|minimumShouldMatch
specifier|private
name|String
name|minimumShouldMatch
decl_stmt|;
comment|/**      * Build an empty bool query.      */
DECL|method|BoolQueryBuilder
specifier|public
name|BoolQueryBuilder
parameter_list|()
block|{     }
comment|/**      * Read from a stream.      */
DECL|method|BoolQueryBuilder
specifier|public
name|BoolQueryBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|mustClauses
operator|.
name|addAll
argument_list|(
name|readQueries
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|mustNotClauses
operator|.
name|addAll
argument_list|(
name|readQueries
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|shouldClauses
operator|.
name|addAll
argument_list|(
name|readQueries
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|filterClauses
operator|.
name|addAll
argument_list|(
name|readQueries
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|adjustPureNegative
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_6_0_0_alpha1_UNRELEASED
argument_list|)
condition|)
block|{
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
comment|// disable_coord
block|}
name|minimumShouldMatch
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
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
name|writeQueries
argument_list|(
name|out
argument_list|,
name|mustClauses
argument_list|)
expr_stmt|;
name|writeQueries
argument_list|(
name|out
argument_list|,
name|mustNotClauses
argument_list|)
expr_stmt|;
name|writeQueries
argument_list|(
name|out
argument_list|,
name|shouldClauses
argument_list|)
expr_stmt|;
name|writeQueries
argument_list|(
name|out
argument_list|,
name|filterClauses
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|adjustPureNegative
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_6_0_0_alpha1_UNRELEASED
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// disable_coord
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|minimumShouldMatch
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a query that<b>must</b> appear in the matching documents and will      * contribute to scoring. No<tt>null</tt> value allowed.      */
DECL|method|must
specifier|public
name|BoolQueryBuilder
name|must
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
if|if
condition|(
name|queryBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner bool query clause cannot be null"
argument_list|)
throw|;
block|}
name|mustClauses
operator|.
name|add
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the queries that<b>must</b> appear in the matching documents.      */
DECL|method|must
specifier|public
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|must
parameter_list|()
block|{
return|return
name|this
operator|.
name|mustClauses
return|;
block|}
comment|/**      * Adds a query that<b>must</b> appear in the matching documents but will      * not contribute to scoring. No<tt>null</tt> value allowed.      */
DECL|method|filter
specifier|public
name|BoolQueryBuilder
name|filter
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
if|if
condition|(
name|queryBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner bool query clause cannot be null"
argument_list|)
throw|;
block|}
name|filterClauses
operator|.
name|add
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the queries that<b>must</b> appear in the matching documents but don't contribute to scoring      */
DECL|method|filter
specifier|public
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|filter
parameter_list|()
block|{
return|return
name|this
operator|.
name|filterClauses
return|;
block|}
comment|/**      * Adds a query that<b>must not</b> appear in the matching documents.      * No<tt>null</tt> value allowed.      */
DECL|method|mustNot
specifier|public
name|BoolQueryBuilder
name|mustNot
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
if|if
condition|(
name|queryBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner bool query clause cannot be null"
argument_list|)
throw|;
block|}
name|mustNotClauses
operator|.
name|add
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the queries that<b>must not</b> appear in the matching documents.      */
DECL|method|mustNot
specifier|public
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|mustNot
parameter_list|()
block|{
return|return
name|this
operator|.
name|mustNotClauses
return|;
block|}
comment|/**      * Adds a clause that<i>should</i> be matched by the returned documents. For a boolean query with no      *<tt>MUST</tt> clauses one or more<code>SHOULD</code> clauses must match a document      * for the BooleanQuery to match. No<tt>null</tt> value allowed.      *      * @see #minimumShouldMatch(int)      */
DECL|method|should
specifier|public
name|BoolQueryBuilder
name|should
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
if|if
condition|(
name|queryBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner bool query clause cannot be null"
argument_list|)
throw|;
block|}
name|shouldClauses
operator|.
name|add
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the list of clauses that<b>should</b> be matched by the returned documents.      *      * @see #should(QueryBuilder)      *  @see #minimumShouldMatch(int)      */
DECL|method|should
specifier|public
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|should
parameter_list|()
block|{
return|return
name|this
operator|.
name|shouldClauses
return|;
block|}
comment|/**      * @return the string representation of the minimumShouldMatch settings for this query      */
DECL|method|minimumShouldMatch
specifier|public
name|String
name|minimumShouldMatch
parameter_list|()
block|{
return|return
name|this
operator|.
name|minimumShouldMatch
return|;
block|}
comment|/**      * Sets the minimum should match parameter using the special syntax (for example, supporting percentage).      * @see BoolQueryBuilder#minimumShouldMatch(int)      */
DECL|method|minimumShouldMatch
specifier|public
name|BoolQueryBuilder
name|minimumShouldMatch
parameter_list|(
name|String
name|minimumShouldMatch
parameter_list|)
block|{
name|this
operator|.
name|minimumShouldMatch
operator|=
name|minimumShouldMatch
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specifies a minimum number of the optional (should) boolean clauses which must be satisfied.      *<p>      * By default no optional clauses are necessary for a match      * (unless there are no required clauses).  If this method is used,      * then the specified number of clauses is required.      *<p>      * Use of this method is totally independent of specifying that      * any specific clauses are required (or prohibited).  This number will      * only be compared against the number of matching optional clauses.      *      * @param minimumShouldMatch the number of optional clauses that must match      */
DECL|method|minimumShouldMatch
specifier|public
name|BoolQueryBuilder
name|minimumShouldMatch
parameter_list|(
name|int
name|minimumShouldMatch
parameter_list|)
block|{
name|this
operator|.
name|minimumShouldMatch
operator|=
name|Integer
operator|.
name|toString
argument_list|(
name|minimumShouldMatch
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns<code>true</code> iff this query builder has at least one should, must, must not or filter clause.      * Otherwise<code>false</code>.      */
DECL|method|hasClauses
specifier|public
name|boolean
name|hasClauses
parameter_list|()
block|{
return|return
operator|!
operator|(
name|mustClauses
operator|.
name|isEmpty
argument_list|()
operator|&&
name|shouldClauses
operator|.
name|isEmpty
argument_list|()
operator|&&
name|mustNotClauses
operator|.
name|isEmpty
argument_list|()
operator|&&
name|filterClauses
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
comment|/**      * If a boolean query contains only negative ("must not") clauses should the      * BooleanQuery be enhanced with a {@link MatchAllDocsQuery} in order to act      * as a pure exclude. The default is<code>true</code>.      */
DECL|method|adjustPureNegative
specifier|public
name|BoolQueryBuilder
name|adjustPureNegative
parameter_list|(
name|boolean
name|adjustPureNegative
parameter_list|)
block|{
name|this
operator|.
name|adjustPureNegative
operator|=
name|adjustPureNegative
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the setting for the adjust_pure_negative setting in this query      */
DECL|method|adjustPureNegative
specifier|public
name|boolean
name|adjustPureNegative
parameter_list|()
block|{
return|return
name|this
operator|.
name|adjustPureNegative
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
name|doXArrayContent
argument_list|(
name|MUST
argument_list|,
name|mustClauses
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|doXArrayContent
argument_list|(
name|FILTER
argument_list|,
name|filterClauses
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|doXArrayContent
argument_list|(
name|MUST_NOT
argument_list|,
name|mustNotClauses
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|doXArrayContent
argument_list|(
name|SHOULD
argument_list|,
name|shouldClauses
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|ADJUST_PURE_NEGATIVE
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|adjustPureNegative
argument_list|)
expr_stmt|;
if|if
condition|(
name|minimumShouldMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|MINIMUM_SHOULD_MATCH
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|minimumShouldMatch
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
DECL|method|doXArrayContent
specifier|private
specifier|static
name|void
name|doXArrayContent
parameter_list|(
name|String
name|field
parameter_list|,
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|clauses
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|clauses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|builder
operator|.
name|startArray
argument_list|(
name|field
argument_list|)
expr_stmt|;
for|for
control|(
name|QueryBuilder
name|clause
range|:
name|clauses
control|)
block|{
name|clause
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
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|BoolQueryBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|ParsingException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|boolean
name|adjustPureNegative
init|=
name|BoolQueryBuilder
operator|.
name|ADJUST_PURE_NEGATIVE_DEFAULT
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|String
name|minimumShouldMatch
init|=
literal|null
decl_stmt|;
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|mustClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|mustNotClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|shouldClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|filterClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|isDeprecatedSetting
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// skip
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
switch|switch
condition|(
name|currentFieldName
condition|)
block|{
case|case
name|MUST
case|:
name|mustClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHOULD
case|:
name|shouldClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|FILTER
case|:
name|filterClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MUST_NOT
case|:
case|case
name|MUSTNOT
case|:
name|mustNotClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"[bool] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
switch|switch
condition|(
name|currentFieldName
condition|)
block|{
case|case
name|MUST
case|:
name|mustClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHOULD
case|:
name|shouldClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|FILTER
case|:
name|filterClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MUST_NOT
case|:
case|case
name|MUSTNOT
case|:
name|mustNotClauses
operator|.
name|add
argument_list|(
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"bool query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|DISABLE_COORD_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// ignore
block|}
elseif|else
if|if
condition|(
name|MINIMUM_SHOULD_MATCH
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|minimumShouldMatch
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|AbstractQueryBuilder
operator|.
name|BOOST_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ADJUST_PURE_NEGATIVE
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|adjustPureNegative
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|AbstractQueryBuilder
operator|.
name|NAME_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|queryName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"[bool] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|BoolQueryBuilder
name|boolQuery
init|=
operator|new
name|BoolQueryBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|QueryBuilder
name|queryBuilder
range|:
name|mustClauses
control|)
block|{
name|boolQuery
operator|.
name|must
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|QueryBuilder
name|queryBuilder
range|:
name|mustNotClauses
control|)
block|{
name|boolQuery
operator|.
name|mustNot
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|QueryBuilder
name|queryBuilder
range|:
name|shouldClauses
control|)
block|{
name|boolQuery
operator|.
name|should
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|QueryBuilder
name|queryBuilder
range|:
name|filterClauses
control|)
block|{
name|boolQuery
operator|.
name|filter
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
block|}
name|boolQuery
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|adjustPureNegative
argument_list|(
name|adjustPureNegative
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|minimumShouldMatch
argument_list|(
name|minimumShouldMatch
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
return|return
name|boolQuery
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
name|NAME
return|;
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
name|BooleanQuery
operator|.
name|Builder
name|booleanQueryBuilder
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|addBooleanClauses
argument_list|(
name|context
argument_list|,
name|booleanQueryBuilder
argument_list|,
name|mustClauses
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|addBooleanClauses
argument_list|(
name|context
argument_list|,
name|booleanQueryBuilder
argument_list|,
name|mustNotClauses
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
name|addBooleanClauses
argument_list|(
name|context
argument_list|,
name|booleanQueryBuilder
argument_list|,
name|shouldClauses
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|addBooleanClauses
argument_list|(
name|context
argument_list|,
name|booleanQueryBuilder
argument_list|,
name|filterClauses
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|FILTER
argument_list|)
expr_stmt|;
name|BooleanQuery
name|booleanQuery
init|=
name|booleanQueryBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|booleanQuery
operator|.
name|clauses
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
operator|new
name|MatchAllDocsQuery
argument_list|()
return|;
block|}
specifier|final
name|String
name|minimumShouldMatch
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|isFilter
argument_list|()
operator|&&
name|this
operator|.
name|minimumShouldMatch
operator|==
literal|null
operator|&&
name|shouldClauses
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|minimumShouldMatch
operator|=
literal|"1"
expr_stmt|;
block|}
else|else
block|{
name|minimumShouldMatch
operator|=
name|this
operator|.
name|minimumShouldMatch
expr_stmt|;
block|}
name|Query
name|query
init|=
name|Queries
operator|.
name|applyMinimumShouldMatch
argument_list|(
name|booleanQuery
argument_list|,
name|minimumShouldMatch
argument_list|)
decl_stmt|;
return|return
name|adjustPureNegative
condition|?
name|fixNegativeQueryIfNeeded
argument_list|(
name|query
argument_list|)
else|:
name|query
return|;
block|}
DECL|method|addBooleanClauses
specifier|private
specifier|static
name|void
name|addBooleanClauses
parameter_list|(
name|QueryShardContext
name|context
parameter_list|,
name|BooleanQuery
operator|.
name|Builder
name|booleanQueryBuilder
parameter_list|,
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|clauses
parameter_list|,
name|Occur
name|occurs
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|QueryBuilder
name|query
range|:
name|clauses
control|)
block|{
name|Query
name|luceneQuery
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|occurs
condition|)
block|{
case|case
name|MUST
case|:
case|case
name|SHOULD
case|:
name|luceneQuery
operator|=
name|query
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|FILTER
case|:
case|case
name|MUST_NOT
case|:
name|luceneQuery
operator|=
name|query
operator|.
name|toFilter
argument_list|(
name|context
argument_list|)
expr_stmt|;
break|break;
block|}
name|booleanQueryBuilder
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
name|luceneQuery
argument_list|,
name|occurs
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|adjustPureNegative
argument_list|,
name|minimumShouldMatch
argument_list|,
name|mustClauses
argument_list|,
name|shouldClauses
argument_list|,
name|mustNotClauses
argument_list|,
name|filterClauses
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|BoolQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|adjustPureNegative
argument_list|,
name|other
operator|.
name|adjustPureNegative
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|minimumShouldMatch
argument_list|,
name|other
operator|.
name|minimumShouldMatch
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|mustClauses
argument_list|,
name|other
operator|.
name|mustClauses
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|shouldClauses
argument_list|,
name|other
operator|.
name|shouldClauses
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|mustNotClauses
argument_list|,
name|other
operator|.
name|mustNotClauses
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|filterClauses
argument_list|,
name|other
operator|.
name|filterClauses
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doRewrite
specifier|protected
name|QueryBuilder
name|doRewrite
parameter_list|(
name|QueryRewriteContext
name|queryRewriteContext
parameter_list|)
throws|throws
name|IOException
block|{
name|BoolQueryBuilder
name|newBuilder
init|=
operator|new
name|BoolQueryBuilder
argument_list|()
decl_stmt|;
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
specifier|final
name|int
name|clauses
init|=
name|mustClauses
operator|.
name|size
argument_list|()
operator|+
name|mustNotClauses
operator|.
name|size
argument_list|()
operator|+
name|filterClauses
operator|.
name|size
argument_list|()
operator|+
name|shouldClauses
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|clauses
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|MatchAllQueryBuilder
argument_list|()
operator|.
name|boost
argument_list|(
name|boost
argument_list|()
argument_list|)
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|()
argument_list|)
return|;
block|}
name|changed
operator||=
name|rewriteClauses
argument_list|(
name|queryRewriteContext
argument_list|,
name|mustClauses
argument_list|,
name|newBuilder
operator|::
name|must
argument_list|)
expr_stmt|;
name|changed
operator||=
name|rewriteClauses
argument_list|(
name|queryRewriteContext
argument_list|,
name|mustNotClauses
argument_list|,
name|newBuilder
operator|::
name|mustNot
argument_list|)
expr_stmt|;
name|changed
operator||=
name|rewriteClauses
argument_list|(
name|queryRewriteContext
argument_list|,
name|filterClauses
argument_list|,
name|newBuilder
operator|::
name|filter
argument_list|)
expr_stmt|;
name|changed
operator||=
name|rewriteClauses
argument_list|(
name|queryRewriteContext
argument_list|,
name|shouldClauses
argument_list|,
name|newBuilder
operator|::
name|should
argument_list|)
expr_stmt|;
if|if
condition|(
name|changed
condition|)
block|{
name|newBuilder
operator|.
name|adjustPureNegative
operator|=
name|adjustPureNegative
expr_stmt|;
name|newBuilder
operator|.
name|minimumShouldMatch
operator|=
name|minimumShouldMatch
expr_stmt|;
name|newBuilder
operator|.
name|boost
argument_list|(
name|boost
argument_list|()
argument_list|)
expr_stmt|;
name|newBuilder
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|newBuilder
return|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|extractInnerHitBuilders
specifier|protected
name|void
name|extractInnerHitBuilders
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|innerHits
parameter_list|)
block|{
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|filter
argument_list|()
argument_list|)
decl_stmt|;
name|clauses
operator|.
name|addAll
argument_list|(
name|must
argument_list|()
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|addAll
argument_list|(
name|should
argument_list|()
argument_list|)
expr_stmt|;
comment|// no need to include must_not (since there will be no hits for it)
for|for
control|(
name|QueryBuilder
name|clause
range|:
name|clauses
control|)
block|{
name|InnerHitContextBuilder
operator|.
name|extractInnerHits
argument_list|(
name|clause
argument_list|,
name|innerHits
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|rewriteClauses
specifier|private
specifier|static
name|boolean
name|rewriteClauses
parameter_list|(
name|QueryRewriteContext
name|queryRewriteContext
parameter_list|,
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|builders
parameter_list|,
name|Consumer
argument_list|<
name|QueryBuilder
argument_list|>
name|consumer
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
for|for
control|(
name|QueryBuilder
name|builder
range|:
name|builders
control|)
block|{
name|QueryBuilder
name|result
init|=
name|builder
operator|.
name|rewrite
argument_list|(
name|queryRewriteContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
name|builder
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
block|}
name|consumer
operator|.
name|accept
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|changed
return|;
block|}
block|}
end_class

end_unit

