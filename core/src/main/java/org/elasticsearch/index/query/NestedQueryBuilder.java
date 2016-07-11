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
name|MatchNoDocsQuery
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|join
operator|.
name|BitSetProducer
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
name|join
operator|.
name|ScoreMode
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
name|join
operator|.
name|ToParentBlockJoinQuery
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|object
operator|.
name|ObjectMapper
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
name|Optional
import|;
end_import

begin_class
DECL|class|NestedQueryBuilder
specifier|public
class|class
name|NestedQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|NestedQueryBuilder
argument_list|>
block|{
comment|/**      * The queries name used while parsing      */
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"nested"
decl_stmt|;
DECL|field|QUERY_NAME_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|QUERY_NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
comment|/**      * The default value for ignore_unmapped.      */
DECL|field|DEFAULT_IGNORE_UNMAPPED
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_IGNORE_UNMAPPED
init|=
literal|false
decl_stmt|;
DECL|field|SCORE_MODE_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|SCORE_MODE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"score_mode"
argument_list|)
decl_stmt|;
DECL|field|PATH_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|PATH_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"path"
argument_list|)
decl_stmt|;
DECL|field|QUERY_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|QUERY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"query"
argument_list|)
decl_stmt|;
DECL|field|INNER_HITS_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|INNER_HITS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"inner_hits"
argument_list|)
decl_stmt|;
DECL|field|IGNORE_UNMAPPED_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|IGNORE_UNMAPPED_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"ignore_unmapped"
argument_list|)
decl_stmt|;
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|field|scoreMode
specifier|private
specifier|final
name|ScoreMode
name|scoreMode
decl_stmt|;
DECL|field|query
specifier|private
specifier|final
name|QueryBuilder
name|query
decl_stmt|;
DECL|field|innerHitBuilder
specifier|private
name|InnerHitBuilder
name|innerHitBuilder
decl_stmt|;
DECL|field|ignoreUnmapped
specifier|private
name|boolean
name|ignoreUnmapped
init|=
name|DEFAULT_IGNORE_UNMAPPED
decl_stmt|;
DECL|method|NestedQueryBuilder
specifier|public
name|NestedQueryBuilder
parameter_list|(
name|String
name|path
parameter_list|,
name|QueryBuilder
name|query
parameter_list|,
name|ScoreMode
name|scoreMode
parameter_list|)
block|{
name|this
argument_list|(
name|path
argument_list|,
name|query
argument_list|,
name|scoreMode
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|NestedQueryBuilder
specifier|private
name|NestedQueryBuilder
parameter_list|(
name|String
name|path
parameter_list|,
name|QueryBuilder
name|query
parameter_list|,
name|ScoreMode
name|scoreMode
parameter_list|,
name|InnerHitBuilder
name|innerHitBuilder
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|requireValue
argument_list|(
name|path
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] requires 'path' field"
argument_list|)
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|requireValue
argument_list|(
name|query
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] requires 'query' field"
argument_list|)
expr_stmt|;
name|this
operator|.
name|scoreMode
operator|=
name|requireValue
argument_list|(
name|scoreMode
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] requires 'score_mode' field"
argument_list|)
expr_stmt|;
name|this
operator|.
name|innerHitBuilder
operator|=
name|innerHitBuilder
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|NestedQueryBuilder
specifier|public
name|NestedQueryBuilder
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
name|path
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|scoreMode
operator|=
name|ScoreMode
operator|.
name|values
argument_list|()
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
name|query
operator|=
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|QueryBuilder
operator|.
name|class
argument_list|)
expr_stmt|;
name|innerHitBuilder
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|InnerHitBuilder
operator|::
operator|new
argument_list|)
expr_stmt|;
name|ignoreUnmapped
operator|=
name|in
operator|.
name|readBoolean
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
name|out
operator|.
name|writeString
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|scoreMode
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|innerHitBuilder
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|ignoreUnmapped
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the nested query to execute.      */
DECL|method|query
specifier|public
name|QueryBuilder
name|query
parameter_list|()
block|{
return|return
name|query
return|;
block|}
comment|/**      * Returns inner hit definition in the scope of this query and reusing the defined type and query.      */
DECL|method|innerHit
specifier|public
name|InnerHitBuilder
name|innerHit
parameter_list|()
block|{
return|return
name|innerHitBuilder
return|;
block|}
DECL|method|innerHit
specifier|public
name|NestedQueryBuilder
name|innerHit
parameter_list|(
name|InnerHitBuilder
name|innerHit
parameter_list|)
block|{
name|this
operator|.
name|innerHitBuilder
operator|=
operator|new
name|InnerHitBuilder
argument_list|(
name|innerHit
argument_list|,
name|path
argument_list|,
name|query
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns how the scores from the matching child documents are mapped into the nested parent document.      */
DECL|method|scoreMode
specifier|public
name|ScoreMode
name|scoreMode
parameter_list|()
block|{
return|return
name|scoreMode
return|;
block|}
comment|/**      * Sets whether the query builder should ignore unmapped paths (and run a      * {@link MatchNoDocsQuery} in place of this query) or throw an exception if      * the path is unmapped.      */
DECL|method|ignoreUnmapped
specifier|public
name|NestedQueryBuilder
name|ignoreUnmapped
parameter_list|(
name|boolean
name|ignoreUnmapped
parameter_list|)
block|{
name|this
operator|.
name|ignoreUnmapped
operator|=
name|ignoreUnmapped
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets whether the query builder will ignore unmapped fields (and run a      * {@link MatchNoDocsQuery} in place of this query) or throw an exception if      * the path is unmapped.      */
DECL|method|ignoreUnmapped
specifier|public
name|boolean
name|ignoreUnmapped
parameter_list|()
block|{
return|return
name|ignoreUnmapped
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
name|builder
operator|.
name|field
argument_list|(
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
name|builder
operator|.
name|field
argument_list|(
name|PATH_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|IGNORE_UNMAPPED_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|ignoreUnmapped
argument_list|)
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
name|SCORE_MODE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|HasChildQueryBuilder
operator|.
name|scoreModeAsString
argument_list|(
name|scoreMode
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
name|innerHitBuilder
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|INNER_HITS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|innerHitBuilder
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
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|Optional
argument_list|<
name|NestedQueryBuilder
argument_list|>
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|ScoreMode
name|scoreMode
init|=
name|ScoreMode
operator|.
name|Avg
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|Optional
argument_list|<
name|QueryBuilder
argument_list|>
name|query
init|=
name|Optional
operator|.
name|empty
argument_list|()
decl_stmt|;
name|String
name|path
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|InnerHitBuilder
name|innerHitBuilder
init|=
literal|null
decl_stmt|;
name|boolean
name|ignoreUnmapped
init|=
name|DEFAULT_IGNORE_UNMAPPED
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
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|QUERY_FIELD
argument_list|)
condition|)
block|{
name|query
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INNER_HITS_FIELD
argument_list|)
condition|)
block|{
name|innerHitBuilder
operator|=
name|InnerHitBuilder
operator|.
name|fromXContent
argument_list|(
name|parseContext
argument_list|)
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
literal|"[nested] query does not support ["
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|PATH_FIELD
argument_list|)
condition|)
block|{
name|path
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|BOOST_FIELD
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
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|IGNORE_UNMAPPED_FIELD
argument_list|)
condition|)
block|{
name|ignoreUnmapped
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
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SCORE_MODE_FIELD
argument_list|)
condition|)
block|{
name|scoreMode
operator|=
name|HasChildQueryBuilder
operator|.
name|parseScoreMode
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|NAME_FIELD
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
literal|"[nested] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|query
operator|.
name|isPresent
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// if inner query is empty, bubble this up to caller so they can decide how to deal with it
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
name|NestedQueryBuilder
name|queryBuilder
init|=
operator|new
name|NestedQueryBuilder
argument_list|(
name|path
argument_list|,
name|query
operator|.
name|get
argument_list|()
argument_list|,
name|scoreMode
argument_list|)
operator|.
name|ignoreUnmapped
argument_list|(
name|ignoreUnmapped
argument_list|)
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerHitBuilder
operator|!=
literal|null
condition|)
block|{
name|queryBuilder
operator|.
name|innerHit
argument_list|(
name|innerHitBuilder
argument_list|)
expr_stmt|;
block|}
return|return
name|Optional
operator|.
name|of
argument_list|(
name|queryBuilder
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
specifier|final
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
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|NestedQueryBuilder
name|that
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|query
argument_list|,
name|that
operator|.
name|query
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|path
argument_list|,
name|that
operator|.
name|path
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|scoreMode
argument_list|,
name|that
operator|.
name|scoreMode
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|innerHitBuilder
argument_list|,
name|that
operator|.
name|innerHitBuilder
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|ignoreUnmapped
argument_list|,
name|that
operator|.
name|ignoreUnmapped
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
name|query
argument_list|,
name|path
argument_list|,
name|scoreMode
argument_list|,
name|innerHitBuilder
argument_list|,
name|ignoreUnmapped
argument_list|)
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
name|ObjectMapper
name|nestedObjectMapper
init|=
name|context
operator|.
name|getObjectMapper
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedObjectMapper
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|ignoreUnmapped
condition|)
block|{
return|return
operator|new
name|MatchNoDocsQuery
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"["
operator|+
name|NAME
operator|+
literal|"] failed to find nested object under path ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|nestedObjectMapper
operator|.
name|nested
argument_list|()
operator|.
name|isNested
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"["
operator|+
name|NAME
operator|+
literal|"] nested object under path ["
operator|+
name|path
operator|+
literal|"] is not of nested type"
argument_list|)
throw|;
block|}
specifier|final
name|BitSetProducer
name|parentFilter
decl_stmt|;
specifier|final
name|Query
name|childFilter
decl_stmt|;
specifier|final
name|Query
name|innerQuery
decl_stmt|;
name|ObjectMapper
name|objectMapper
init|=
name|context
operator|.
name|nestedScope
argument_list|()
operator|.
name|getObjectMapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|objectMapper
operator|==
literal|null
condition|)
block|{
name|parentFilter
operator|=
name|context
operator|.
name|bitsetFilter
argument_list|(
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parentFilter
operator|=
name|context
operator|.
name|bitsetFilter
argument_list|(
name|objectMapper
operator|.
name|nestedTypeFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|childFilter
operator|=
name|nestedObjectMapper
operator|.
name|nestedTypeFilter
argument_list|()
expr_stmt|;
try|try
block|{
name|context
operator|.
name|nestedScope
argument_list|()
operator|.
name|nextLevel
argument_list|(
name|nestedObjectMapper
argument_list|)
expr_stmt|;
name|innerQuery
operator|=
name|this
operator|.
name|query
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|context
operator|.
name|nestedScope
argument_list|()
operator|.
name|previousLevel
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|ToParentBlockJoinQuery
argument_list|(
name|Queries
operator|.
name|filtered
argument_list|(
name|innerQuery
argument_list|,
name|childFilter
argument_list|)
argument_list|,
name|parentFilter
argument_list|,
name|scoreMode
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
name|QueryBuilder
name|rewrittenQuery
init|=
name|query
operator|.
name|rewrite
argument_list|(
name|queryRewriteContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|rewrittenQuery
operator|!=
name|query
condition|)
block|{
name|InnerHitBuilder
name|rewrittenInnerHit
init|=
name|InnerHitBuilder
operator|.
name|rewrite
argument_list|(
name|innerHitBuilder
argument_list|,
name|rewrittenQuery
argument_list|)
decl_stmt|;
return|return
operator|new
name|NestedQueryBuilder
argument_list|(
name|path
argument_list|,
name|rewrittenQuery
argument_list|,
name|scoreMode
argument_list|,
name|rewrittenInnerHit
argument_list|)
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
name|InnerHitBuilder
argument_list|>
name|innerHits
parameter_list|)
block|{
if|if
condition|(
name|innerHitBuilder
operator|!=
literal|null
condition|)
block|{
name|innerHitBuilder
operator|.
name|inlineInnerHits
argument_list|(
name|innerHits
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

