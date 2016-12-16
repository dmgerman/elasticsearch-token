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
name|ScoreMode
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
name|fielddata
operator|.
name|plain
operator|.
name|ParentChildIndexFieldData
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
name|DocumentMapper
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
name|ParentFieldMapper
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
name|HashSet
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Builder for the 'has_parent' query.  */
end_comment

begin_class
DECL|class|HasParentQueryBuilder
specifier|public
class|class
name|HasParentQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|HasParentQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"has_parent"
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
argument_list|,
literal|"filter"
argument_list|)
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
operator|.
name|withAllDeprecated
argument_list|(
literal|"score"
argument_list|)
decl_stmt|;
DECL|field|TYPE_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|TYPE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"parent_type"
argument_list|,
literal|"type"
argument_list|)
decl_stmt|;
DECL|field|SCORE_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|SCORE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"score"
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
DECL|field|query
specifier|private
specifier|final
name|QueryBuilder
name|query
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|score
specifier|private
specifier|final
name|boolean
name|score
decl_stmt|;
DECL|field|innerHit
specifier|private
name|InnerHitBuilder
name|innerHit
decl_stmt|;
DECL|field|ignoreUnmapped
specifier|private
name|boolean
name|ignoreUnmapped
init|=
literal|false
decl_stmt|;
DECL|method|HasParentQueryBuilder
specifier|public
name|HasParentQueryBuilder
parameter_list|(
name|String
name|type
parameter_list|,
name|QueryBuilder
name|query
parameter_list|,
name|boolean
name|score
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
name|query
argument_list|,
name|score
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|HasParentQueryBuilder
specifier|private
name|HasParentQueryBuilder
parameter_list|(
name|String
name|type
parameter_list|,
name|QueryBuilder
name|query
parameter_list|,
name|boolean
name|score
parameter_list|,
name|InnerHitBuilder
name|innerHit
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|requireValue
argument_list|(
name|type
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] requires 'type' field"
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
name|score
operator|=
name|score
expr_stmt|;
name|this
operator|.
name|innerHit
operator|=
name|innerHit
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|HasParentQueryBuilder
specifier|public
name|HasParentQueryBuilder
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
name|type
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|score
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
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
name|innerHit
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
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|score
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
name|innerHit
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
comment|/**      * Returns the query to execute.      */
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
comment|/**      * Returns<code>true</code> if the parent score is mapped into the child documents      */
DECL|method|score
specifier|public
name|boolean
name|score
parameter_list|()
block|{
return|return
name|score
return|;
block|}
comment|/**      * Returns the parents type name      */
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      *  Returns inner hit definition in the scope of this query and reusing the defined type and query.      */
DECL|method|innerHit
specifier|public
name|InnerHitBuilder
name|innerHit
parameter_list|()
block|{
return|return
name|innerHit
return|;
block|}
DECL|method|innerHit
specifier|public
name|HasParentQueryBuilder
name|innerHit
parameter_list|(
name|InnerHitBuilder
name|innerHit
parameter_list|)
block|{
name|this
operator|.
name|innerHit
operator|=
operator|new
name|InnerHitBuilder
argument_list|(
name|innerHit
argument_list|,
name|query
argument_list|,
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets whether the query builder should ignore unmapped types (and run a      * {@link MatchNoDocsQuery} in place of this query) or throw an exception if      * the type is unmapped.      */
DECL|method|ignoreUnmapped
specifier|public
name|HasParentQueryBuilder
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
comment|/**      * Gets whether the query builder will ignore unmapped types (and run a      * {@link MatchNoDocsQuery} in place of this query) or throw an exception if      * the type is unmapped.      */
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
name|Query
name|innerQuery
decl_stmt|;
name|String
index|[]
name|previousTypes
init|=
name|context
operator|.
name|getTypes
argument_list|()
decl_stmt|;
name|context
operator|.
name|setTypes
argument_list|(
name|type
argument_list|)
expr_stmt|;
try|try
block|{
name|innerQuery
operator|=
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
name|setTypes
argument_list|(
name|previousTypes
argument_list|)
expr_stmt|;
block|}
name|DocumentMapper
name|parentDocMapper
init|=
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDocMapper
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
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] query configured 'parent_type' ["
operator|+
name|type
operator|+
literal|"] is not a valid type"
argument_list|)
throw|;
block|}
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|childTypes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
init|=
literal|null
decl_stmt|;
for|for
control|(
name|DocumentMapper
name|documentMapper
range|:
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|docMappers
argument_list|(
literal|false
argument_list|)
control|)
block|{
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|documentMapper
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentFieldMapper
operator|.
name|active
argument_list|()
operator|&&
name|type
operator|.
name|equals
argument_list|(
name|parentFieldMapper
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|childTypes
operator|.
name|add
argument_list|(
name|documentMapper
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|parentChildIndexFieldData
operator|=
name|context
operator|.
name|getForField
argument_list|(
name|parentFieldMapper
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|childTypes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] no child types found for type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Query
name|childrenQuery
decl_stmt|;
if|if
condition|(
name|childTypes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|DocumentMapper
name|documentMapper
init|=
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|childTypes
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
name|childrenQuery
operator|=
name|documentMapper
operator|.
name|typeFilter
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|BooleanQuery
operator|.
name|Builder
name|childrenFilter
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|childrenTypeStr
range|:
name|childTypes
control|)
block|{
name|DocumentMapper
name|documentMapper
init|=
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|childrenTypeStr
argument_list|)
decl_stmt|;
name|childrenFilter
operator|.
name|add
argument_list|(
name|documentMapper
operator|.
name|typeFilter
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
name|childrenQuery
operator|=
name|childrenFilter
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
comment|// wrap the query with type query
name|innerQuery
operator|=
name|Queries
operator|.
name|filtered
argument_list|(
name|innerQuery
argument_list|,
name|parentDocMapper
operator|.
name|typeFilter
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|HasChildQueryBuilder
operator|.
name|LateParsingQuery
argument_list|(
name|childrenQuery
argument_list|,
name|innerQuery
argument_list|,
name|HasChildQueryBuilder
operator|.
name|DEFAULT_MIN_CHILDREN
argument_list|,
name|HasChildQueryBuilder
operator|.
name|DEFAULT_MAX_CHILDREN
argument_list|,
name|type
argument_list|,
name|score
condition|?
name|ScoreMode
operator|.
name|Max
else|:
name|ScoreMode
operator|.
name|None
argument_list|,
name|parentChildIndexFieldData
argument_list|,
name|context
operator|.
name|getSearchSimilarity
argument_list|()
argument_list|)
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
name|TYPE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|SCORE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|score
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
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
name|innerHit
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
name|innerHit
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
name|HasParentQueryBuilder
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
name|String
name|parentType
init|=
literal|null
decl_stmt|;
name|boolean
name|score
init|=
literal|false
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|InnerHitBuilder
name|innerHits
init|=
literal|null
decl_stmt|;
name|boolean
name|ignoreUnmapped
init|=
name|DEFAULT_IGNORE_UNMAPPED
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
name|QueryBuilder
name|iqb
init|=
literal|null
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
name|iqb
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
name|innerHits
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
literal|"[has_parent] query does not support ["
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
name|TYPE_FIELD
argument_list|)
condition|)
block|{
name|parentType
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
name|SCORE_MODE_FIELD
argument_list|)
condition|)
block|{
name|String
name|scoreModeValue
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"score"
operator|.
name|equals
argument_list|(
name|scoreModeValue
argument_list|)
condition|)
block|{
name|score
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"none"
operator|.
name|equals
argument_list|(
name|scoreModeValue
argument_list|)
condition|)
block|{
name|score
operator|=
literal|false
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
literal|"[has_parent] query does not support ["
operator|+
name|scoreModeValue
operator|+
literal|"] as an option for score_mode"
argument_list|)
throw|;
block|}
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
name|SCORE_FIELD
argument_list|)
condition|)
block|{
name|score
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
literal|"[has_parent] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|HasParentQueryBuilder
name|queryBuilder
init|=
operator|new
name|HasParentQueryBuilder
argument_list|(
name|parentType
argument_list|,
name|iqb
argument_list|,
name|score
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
name|innerHits
operator|!=
literal|null
condition|)
block|{
name|queryBuilder
operator|.
name|innerHit
argument_list|(
name|innerHits
argument_list|)
expr_stmt|;
block|}
return|return
name|queryBuilder
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
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|HasParentQueryBuilder
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
name|type
argument_list|,
name|that
operator|.
name|type
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|score
argument_list|,
name|that
operator|.
name|score
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|innerHit
argument_list|,
name|that
operator|.
name|innerHit
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
name|type
argument_list|,
name|score
argument_list|,
name|innerHit
argument_list|,
name|ignoreUnmapped
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
name|queryShardContext
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
name|queryShardContext
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
name|innerHit
argument_list|,
name|rewrittenQuery
argument_list|)
decl_stmt|;
return|return
operator|new
name|HasParentQueryBuilder
argument_list|(
name|type
argument_list|,
name|rewrittenQuery
argument_list|,
name|score
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
name|innerHit
operator|!=
literal|null
condition|)
block|{
name|innerHit
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

