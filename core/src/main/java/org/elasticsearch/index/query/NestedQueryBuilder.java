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
name|index
operator|.
name|LeafReaderContext
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
name|index
operator|.
name|ReaderUtil
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
name|TopDocs
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
name|TopDocsCollector
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
name|TopFieldCollector
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
name|TopScoreDocCollector
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
name|TotalHitCountCollector
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
name|Weight
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
name|ParentChildrenBlockJoinQuery
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
name|Lucene
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
name|ObjectMapper
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
name|search
operator|.
name|ESToParentBlockJoinQuery
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
name|search
operator|.
name|NestedHelper
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
name|SearchHit
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
name|fetch
operator|.
name|subphase
operator|.
name|InnerHitsContext
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
name|HashMap
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|InnerHitsContext
operator|.
name|intersect
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
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"nested"
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
name|V_6_0_0_alpha2_UNRELEASED
argument_list|)
condition|)
block|{
specifier|final
name|boolean
name|hasInnerHit
init|=
name|innerHitBuilder
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasInnerHit
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasInnerHit
condition|)
block|{
name|innerHitBuilder
operator|.
name|writeToNestedBWC
argument_list|(
name|out
argument_list|,
name|query
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|innerHitBuilder
argument_list|)
expr_stmt|;
block|}
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
name|innerHitBuilder
parameter_list|)
block|{
name|this
operator|.
name|innerHitBuilder
operator|=
name|innerHitBuilder
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
name|NestedQueryBuilder
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
name|QueryBuilder
name|query
init|=
literal|null
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
name|QUERY_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
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
name|INNER_HITS_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
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
name|PATH_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
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
name|IGNORE_UNMAPPED_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
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
name|SCORE_MODE_FIELD
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|scoreMode
operator|=
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
name|NestedQueryBuilder
name|queryBuilder
init|=
operator|new
name|NestedQueryBuilder
argument_list|(
name|path
argument_list|,
name|query
argument_list|,
name|scoreMode
argument_list|,
name|innerHitBuilder
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
return|return
name|queryBuilder
return|;
block|}
DECL|method|parseScoreMode
specifier|public
specifier|static
name|ScoreMode
name|parseScoreMode
parameter_list|(
name|String
name|scoreModeString
parameter_list|)
block|{
if|if
condition|(
literal|"none"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|None
return|;
block|}
elseif|else
if|if
condition|(
literal|"min"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Min
return|;
block|}
elseif|else
if|if
condition|(
literal|"max"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Max
return|;
block|}
elseif|else
if|if
condition|(
literal|"avg"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Avg
return|;
block|}
elseif|else
if|if
condition|(
literal|"sum"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Total
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No score mode for child query ["
operator|+
name|scoreModeString
operator|+
literal|"] found"
argument_list|)
throw|;
block|}
DECL|method|scoreModeAsString
specifier|public
specifier|static
name|String
name|scoreModeAsString
parameter_list|(
name|ScoreMode
name|scoreMode
parameter_list|)
block|{
if|if
condition|(
name|scoreMode
operator|==
name|ScoreMode
operator|.
name|Total
condition|)
block|{
comment|// Lucene uses 'total' but 'sum' is more consistent with other elasticsearch APIs
return|return
literal|"sum"
return|;
block|}
else|else
block|{
return|return
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
return|;
block|}
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
comment|// ToParentBlockJoinQuery requires that the inner query only matches documents
comment|// in its child space
if|if
condition|(
operator|new
name|NestedHelper
argument_list|(
name|context
operator|.
name|getMapperService
argument_list|()
argument_list|)
operator|.
name|mightMatchNonNestedDocs
argument_list|(
name|innerQuery
argument_list|,
name|path
argument_list|)
condition|)
block|{
name|innerQuery
operator|=
name|Queries
operator|.
name|filtered
argument_list|(
name|innerQuery
argument_list|,
name|nestedObjectMapper
operator|.
name|nestedTypeFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ESToParentBlockJoinQuery
argument_list|(
name|innerQuery
argument_list|,
name|parentFilter
argument_list|,
name|scoreMode
argument_list|,
name|objectMapper
operator|==
literal|null
condition|?
literal|null
else|:
name|objectMapper
operator|.
name|fullPath
argument_list|()
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
name|NestedQueryBuilder
name|nestedQuery
init|=
operator|new
name|NestedQueryBuilder
argument_list|(
name|path
argument_list|,
name|rewrittenQuery
argument_list|,
name|scoreMode
argument_list|,
name|innerHitBuilder
argument_list|)
decl_stmt|;
name|nestedQuery
operator|.
name|ignoreUnmapped
argument_list|(
name|ignoreUnmapped
argument_list|)
expr_stmt|;
return|return
name|nestedQuery
return|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|extractInnerHitBuilders
specifier|public
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
if|if
condition|(
name|innerHitBuilder
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|children
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|InnerHitContextBuilder
operator|.
name|extractInnerHits
argument_list|(
name|query
argument_list|,
name|children
argument_list|)
expr_stmt|;
name|InnerHitContextBuilder
name|innerHitContextBuilder
init|=
operator|new
name|NestedInnerHitContextBuilder
argument_list|(
name|path
argument_list|,
name|query
argument_list|,
name|innerHitBuilder
argument_list|,
name|children
argument_list|)
decl_stmt|;
name|String
name|name
init|=
name|innerHitBuilder
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|?
name|innerHitBuilder
operator|.
name|getName
argument_list|()
else|:
name|path
decl_stmt|;
name|innerHits
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|innerHitContextBuilder
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NestedInnerHitContextBuilder
specifier|static
class|class
name|NestedInnerHitContextBuilder
extends|extends
name|InnerHitContextBuilder
block|{
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|method|NestedInnerHitContextBuilder
name|NestedInnerHitContextBuilder
parameter_list|(
name|String
name|path
parameter_list|,
name|QueryBuilder
name|query
parameter_list|,
name|InnerHitBuilder
name|innerHitBuilder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|children
parameter_list|)
block|{
name|super
argument_list|(
name|query
argument_list|,
name|innerHitBuilder
argument_list|,
name|children
argument_list|)
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|void
name|build
parameter_list|(
name|SearchContext
name|parentSearchContext
parameter_list|,
name|InnerHitsContext
name|innerHitsContext
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryShardContext
name|queryShardContext
init|=
name|parentSearchContext
operator|.
name|getQueryShardContext
argument_list|()
decl_stmt|;
name|ObjectMapper
name|nestedObjectMapper
init|=
name|queryShardContext
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
name|innerHitBuilder
operator|.
name|isIgnoreUnmapped
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"["
operator|+
name|query
operator|.
name|getName
argument_list|()
operator|+
literal|"] no mapping found for type ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
return|return;
block|}
block|}
name|String
name|name
init|=
name|innerHitBuilder
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|?
name|innerHitBuilder
operator|.
name|getName
argument_list|()
else|:
name|nestedObjectMapper
operator|.
name|fullPath
argument_list|()
decl_stmt|;
name|ObjectMapper
name|parentObjectMapper
init|=
name|queryShardContext
operator|.
name|nestedScope
argument_list|()
operator|.
name|nextLevel
argument_list|(
name|nestedObjectMapper
argument_list|)
decl_stmt|;
name|NestedInnerHitSubContext
name|nestedInnerHits
init|=
operator|new
name|NestedInnerHitSubContext
argument_list|(
name|name
argument_list|,
name|parentSearchContext
argument_list|,
name|parentObjectMapper
argument_list|,
name|nestedObjectMapper
argument_list|)
decl_stmt|;
name|setupInnerHitsContext
argument_list|(
name|queryShardContext
argument_list|,
name|nestedInnerHits
argument_list|)
expr_stmt|;
name|queryShardContext
operator|.
name|nestedScope
argument_list|()
operator|.
name|previousLevel
argument_list|()
expr_stmt|;
name|innerHitsContext
operator|.
name|addInnerHitDefinition
argument_list|(
name|nestedInnerHits
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NestedInnerHitSubContext
specifier|static
specifier|final
class|class
name|NestedInnerHitSubContext
extends|extends
name|InnerHitsContext
operator|.
name|InnerHitSubContext
block|{
DECL|field|parentObjectMapper
specifier|private
specifier|final
name|ObjectMapper
name|parentObjectMapper
decl_stmt|;
DECL|field|childObjectMapper
specifier|private
specifier|final
name|ObjectMapper
name|childObjectMapper
decl_stmt|;
DECL|method|NestedInnerHitSubContext
name|NestedInnerHitSubContext
parameter_list|(
name|String
name|name
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|ObjectMapper
name|parentObjectMapper
parameter_list|,
name|ObjectMapper
name|childObjectMapper
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentObjectMapper
operator|=
name|parentObjectMapper
expr_stmt|;
name|this
operator|.
name|childObjectMapper
operator|=
name|childObjectMapper
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|topDocs
specifier|public
name|TopDocs
index|[]
name|topDocs
parameter_list|(
name|SearchHit
index|[]
name|hits
parameter_list|)
throws|throws
name|IOException
block|{
name|Weight
name|innerHitQueryWeight
init|=
name|createInnerHitQueryWeight
argument_list|()
decl_stmt|;
name|TopDocs
index|[]
name|result
init|=
operator|new
name|TopDocs
index|[
name|hits
operator|.
name|length
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
name|hits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|SearchHit
name|hit
init|=
name|hits
index|[
name|i
index|]
decl_stmt|;
name|Query
name|rawParentFilter
decl_stmt|;
if|if
condition|(
name|parentObjectMapper
operator|==
literal|null
condition|)
block|{
name|rawParentFilter
operator|=
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|rawParentFilter
operator|=
name|parentObjectMapper
operator|.
name|nestedTypeFilter
argument_list|()
expr_stmt|;
block|}
name|int
name|parentDocId
init|=
name|hit
operator|.
name|docId
argument_list|()
decl_stmt|;
specifier|final
name|int
name|readerIndex
init|=
name|ReaderUtil
operator|.
name|subIndex
argument_list|(
name|parentDocId
argument_list|,
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
argument_list|)
decl_stmt|;
comment|// With nested inner hits the nested docs are always in the same segement, so need to use the other segments
name|LeafReaderContext
name|ctx
init|=
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
name|readerIndex
argument_list|)
decl_stmt|;
name|Query
name|childFilter
init|=
name|childObjectMapper
operator|.
name|nestedTypeFilter
argument_list|()
decl_stmt|;
name|BitSetProducer
name|parentFilter
init|=
name|context
operator|.
name|bitsetFilterCache
argument_list|()
operator|.
name|getBitSetProducer
argument_list|(
name|rawParentFilter
argument_list|)
decl_stmt|;
name|Query
name|q
init|=
operator|new
name|ParentChildrenBlockJoinQuery
argument_list|(
name|parentFilter
argument_list|,
name|childFilter
argument_list|,
name|parentDocId
argument_list|)
decl_stmt|;
name|Weight
name|weight
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|createNormalizedWeight
argument_list|(
name|q
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|TotalHitCountCollector
name|totalHitCountCollector
init|=
operator|new
name|TotalHitCountCollector
argument_list|()
decl_stmt|;
name|intersect
argument_list|(
name|weight
argument_list|,
name|innerHitQueryWeight
argument_list|,
name|totalHitCountCollector
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|result
index|[
name|i
index|]
operator|=
operator|new
name|TopDocs
argument_list|(
name|totalHitCountCollector
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|Lucene
operator|.
name|EMPTY_SCORE_DOCS
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|topN
init|=
name|Math
operator|.
name|min
argument_list|(
name|from
argument_list|()
operator|+
name|size
argument_list|()
argument_list|,
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
name|TopDocsCollector
argument_list|<
name|?
argument_list|>
name|topDocsCollector
decl_stmt|;
if|if
condition|(
name|sort
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|topDocsCollector
operator|=
name|TopFieldCollector
operator|.
name|create
argument_list|(
name|sort
argument_list|()
operator|.
name|sort
argument_list|,
name|topN
argument_list|,
literal|true
argument_list|,
name|trackScores
argument_list|()
argument_list|,
name|trackScores
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|topDocsCollector
operator|=
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|topN
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|intersect
argument_list|(
name|weight
argument_list|,
name|innerHitQueryWeight
argument_list|,
name|topDocsCollector
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|clearReleasables
argument_list|(
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
block|}
name|result
index|[
name|i
index|]
operator|=
name|topDocsCollector
operator|.
name|topDocs
argument_list|(
name|from
argument_list|()
argument_list|,
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

