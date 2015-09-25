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
name|IndexReader
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
name|MultiDocValues
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
name|IndexSearcher
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
name|JoinUtil
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
name|IndexParentChildFieldData
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
name|internal
operator|.
name|ParentFieldMapper
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
name|support
operator|.
name|QueryInnerHits
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
name|innerhits
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
name|fetch
operator|.
name|innerhits
operator|.
name|InnerHitsSubSearchContext
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

begin_comment
comment|/**  * A query builder for<tt>has_child</tt> queries.  */
end_comment

begin_class
DECL|class|HasChildQueryBuilder
specifier|public
class|class
name|HasChildQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|HasChildQueryBuilder
argument_list|>
block|{
comment|/**      * The queries name      */
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"has_child"
decl_stmt|;
comment|/**      * The default maximum number of children that are required to match for the parent to be considered a match.      */
DECL|field|DEFAULT_MAX_CHILDREN
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_CHILDREN
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**      * The default minimum number of children that are required to match for the parent to be considered a match.      */
DECL|field|DEFAULT_MIN_CHILDREN
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MIN_CHILDREN
init|=
literal|0
decl_stmt|;
comment|/*      * The default score mode that is used to combine score coming from multiple parent documents.      */
DECL|field|DEFAULT_SCORE_MODE
specifier|public
specifier|static
specifier|final
name|ScoreMode
name|DEFAULT_SCORE_MODE
init|=
name|ScoreMode
operator|.
name|None
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
DECL|field|scoreMode
specifier|private
name|ScoreMode
name|scoreMode
init|=
name|DEFAULT_SCORE_MODE
decl_stmt|;
DECL|field|minChildren
specifier|private
name|int
name|minChildren
init|=
name|DEFAULT_MIN_CHILDREN
decl_stmt|;
DECL|field|maxChildren
specifier|private
name|int
name|maxChildren
init|=
name|DEFAULT_MAX_CHILDREN
decl_stmt|;
DECL|field|queryInnerHits
specifier|private
name|QueryInnerHits
name|queryInnerHits
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|HasChildQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|HasChildQueryBuilder
argument_list|(
literal|""
argument_list|,
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
argument_list|)
decl_stmt|;
DECL|method|HasChildQueryBuilder
specifier|public
name|HasChildQueryBuilder
parameter_list|(
name|String
name|type
parameter_list|,
name|QueryBuilder
name|query
parameter_list|,
name|int
name|maxChildren
parameter_list|,
name|int
name|minChildren
parameter_list|,
name|ScoreMode
name|scoreMode
parameter_list|,
name|QueryInnerHits
name|queryInnerHits
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
name|query
argument_list|)
expr_stmt|;
name|scoreMode
argument_list|(
name|scoreMode
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxChildren
operator|=
name|maxChildren
expr_stmt|;
name|this
operator|.
name|minChildren
operator|=
name|minChildren
expr_stmt|;
name|this
operator|.
name|queryInnerHits
operator|=
name|queryInnerHits
expr_stmt|;
block|}
DECL|method|HasChildQueryBuilder
specifier|public
name|HasChildQueryBuilder
parameter_list|(
name|String
name|type
parameter_list|,
name|QueryBuilder
name|query
parameter_list|)
block|{
if|if
condition|(
name|type
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
literal|"] requires 'type' field"
argument_list|)
throw|;
block|}
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
literal|"["
operator|+
name|NAME
operator|+
literal|"] requires 'query' field"
argument_list|)
throw|;
block|}
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
block|}
comment|/**      * Defines how the scores from the matching child documents are mapped into the parent document.      */
DECL|method|scoreMode
specifier|public
name|HasChildQueryBuilder
name|scoreMode
parameter_list|(
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
comment|/**      * Defines the minimum number of children that are required to match for the parent to be considered a match.      */
DECL|method|minChildren
specifier|public
name|HasChildQueryBuilder
name|minChildren
parameter_list|(
name|int
name|minChildren
parameter_list|)
block|{
if|if
condition|(
name|minChildren
operator|<
literal|0
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
literal|"]  requires non-negative 'min_children' field"
argument_list|)
throw|;
block|}
name|this
operator|.
name|minChildren
operator|=
name|minChildren
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Defines the maximum number of children that are required to match for the parent to be considered a match.      */
DECL|method|maxChildren
specifier|public
name|HasChildQueryBuilder
name|maxChildren
parameter_list|(
name|int
name|maxChildren
parameter_list|)
block|{
if|if
condition|(
name|maxChildren
operator|<
literal|0
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
literal|"]  requires non-negative 'max_children' field"
argument_list|)
throw|;
block|}
name|this
operator|.
name|maxChildren
operator|=
name|maxChildren
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the query name for the filter that can be used when searching for matched_filters per hit.      */
DECL|method|innerHit
specifier|public
name|HasChildQueryBuilder
name|innerHit
parameter_list|(
name|QueryInnerHits
name|queryInnerHits
parameter_list|)
block|{
name|this
operator|.
name|queryInnerHits
operator|=
name|queryInnerHits
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns inner hit definition in the scope of this query and reusing the defined type and query.      */
DECL|method|innerHit
specifier|public
name|QueryInnerHits
name|innerHit
parameter_list|()
block|{
return|return
name|queryInnerHits
return|;
block|}
comment|/**      * Returns the children query to execute.      */
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
comment|/**      * Returns the child type      */
DECL|method|childType
specifier|public
name|String
name|childType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Returns how the scores from the matching child documents are mapped into the parent document.      */
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
comment|/**      * Returns the minimum number of children that are required to match for the parent to be considered a match.      * The default is {@value #DEFAULT_MAX_CHILDREN}      */
DECL|method|minChildren
specifier|public
name|int
name|minChildren
parameter_list|()
block|{
return|return
name|minChildren
return|;
block|}
comment|/**      * Returns the maximum number of children that are required to match for the parent to be considered a match.      * The default is {@value #DEFAULT_MIN_CHILDREN}      */
DECL|method|maxChildren
specifier|public
name|int
name|maxChildren
parameter_list|()
block|{
return|return
name|maxChildren
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
literal|"query"
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
literal|"child_type"
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"score_mode"
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
name|field
argument_list|(
literal|"min_children"
argument_list|,
name|minChildren
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"max_children"
argument_list|,
name|maxChildren
argument_list|)
expr_stmt|;
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryInnerHits
operator|!=
literal|null
condition|)
block|{
name|queryInnerHits
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
name|Query
name|innerQuery
init|=
name|query
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerQuery
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|innerQuery
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|DocumentMapper
name|childDocMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|childDocMapper
operator|==
literal|null
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
literal|"] no mapping found for type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|childDocMapper
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
operator|==
literal|false
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
literal|"] _parent field has no parent type configured"
argument_list|)
throw|;
block|}
if|if
condition|(
name|queryInnerHits
operator|!=
literal|null
condition|)
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|queryInnerHits
operator|.
name|getXcontentParser
argument_list|()
init|)
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"start object expected but was: ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|InnerHitsSubSearchContext
name|innerHits
init|=
name|context
operator|.
name|indexQueryParserService
argument_list|()
operator|.
name|getInnerHitsQueryParserHelper
argument_list|()
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerHits
operator|!=
literal|null
condition|)
block|{
name|ParsedQuery
name|parsedQuery
init|=
operator|new
name|ParsedQuery
argument_list|(
name|innerQuery
argument_list|,
name|context
operator|.
name|copyNamedQueries
argument_list|()
argument_list|)
decl_stmt|;
name|InnerHitsContext
operator|.
name|ParentChildInnerHits
name|parentChildInnerHits
init|=
operator|new
name|InnerHitsContext
operator|.
name|ParentChildInnerHits
argument_list|(
name|innerHits
operator|.
name|getSubSearchContext
argument_list|()
argument_list|,
name|parsedQuery
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|mapperService
argument_list|()
argument_list|,
name|childDocMapper
argument_list|)
decl_stmt|;
name|String
name|name
init|=
name|innerHits
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|?
name|innerHits
operator|.
name|getName
argument_list|()
else|:
name|type
decl_stmt|;
name|context
operator|.
name|addInnerHits
argument_list|(
name|name
argument_list|,
name|parentChildInnerHits
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|String
name|parentType
init|=
name|parentFieldMapper
operator|.
name|type
argument_list|()
decl_stmt|;
name|DocumentMapper
name|parentDocMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDocMapper
operator|==
literal|null
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
literal|"] Type ["
operator|+
name|type
operator|+
literal|"] points to a non existent parent type ["
operator|+
name|parentType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|maxChildren
operator|>
literal|0
operator|&&
name|maxChildren
operator|<
name|minChildren
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
literal|"] 'max_children' is less than 'min_children'"
argument_list|)
throw|;
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
name|childDocMapper
operator|.
name|typeFilter
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
init|=
name|context
operator|.
name|getForField
argument_list|(
name|parentFieldMapper
operator|.
name|fieldType
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|maxChildren
init|=
name|maxChildren
argument_list|()
decl_stmt|;
comment|// 0 in pre 2.x p/c impl means unbounded
if|if
condition|(
name|maxChildren
operator|==
literal|0
condition|)
block|{
name|maxChildren
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
return|return
operator|new
name|LateParsingQuery
argument_list|(
name|parentDocMapper
operator|.
name|typeFilter
argument_list|()
argument_list|,
name|innerQuery
argument_list|,
name|minChildren
argument_list|()
argument_list|,
name|maxChildren
argument_list|,
name|parentType
argument_list|,
name|scoreMode
argument_list|,
name|parentChildIndexFieldData
argument_list|)
return|;
block|}
DECL|class|LateParsingQuery
specifier|final
specifier|static
class|class
name|LateParsingQuery
extends|extends
name|Query
block|{
DECL|field|toQuery
specifier|private
specifier|final
name|Query
name|toQuery
decl_stmt|;
DECL|field|innerQuery
specifier|private
specifier|final
name|Query
name|innerQuery
decl_stmt|;
DECL|field|minChildren
specifier|private
specifier|final
name|int
name|minChildren
decl_stmt|;
DECL|field|maxChildren
specifier|private
specifier|final
name|int
name|maxChildren
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|scoreMode
specifier|private
specifier|final
name|ScoreMode
name|scoreMode
decl_stmt|;
DECL|field|parentChildIndexFieldData
specifier|private
specifier|final
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
decl_stmt|;
DECL|method|LateParsingQuery
name|LateParsingQuery
parameter_list|(
name|Query
name|toQuery
parameter_list|,
name|Query
name|innerQuery
parameter_list|,
name|int
name|minChildren
parameter_list|,
name|int
name|maxChildren
parameter_list|,
name|String
name|parentType
parameter_list|,
name|ScoreMode
name|scoreMode
parameter_list|,
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
parameter_list|)
block|{
name|this
operator|.
name|toQuery
operator|=
name|toQuery
expr_stmt|;
name|this
operator|.
name|innerQuery
operator|=
name|innerQuery
expr_stmt|;
name|this
operator|.
name|minChildren
operator|=
name|minChildren
expr_stmt|;
name|this
operator|.
name|maxChildren
operator|=
name|maxChildren
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
name|this
operator|.
name|parentChildIndexFieldData
operator|=
name|parentChildIndexFieldData
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|getBoost
argument_list|()
operator|!=
literal|1.0F
condition|)
block|{
return|return
name|super
operator|.
name|rewrite
argument_list|(
name|reader
argument_list|)
return|;
block|}
name|String
name|joinField
init|=
name|ParentFieldMapper
operator|.
name|joinField
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|indexSearcher
operator|.
name|setQueryCache
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|IndexParentChildFieldData
name|indexParentChildFieldData
init|=
name|parentChildIndexFieldData
operator|.
name|loadGlobal
argument_list|(
name|indexSearcher
operator|.
name|getIndexReader
argument_list|()
argument_list|)
decl_stmt|;
name|MultiDocValues
operator|.
name|OrdinalMap
name|ordinalMap
init|=
name|ParentChildIndexFieldData
operator|.
name|getOrdinalMap
argument_list|(
name|indexParentChildFieldData
argument_list|,
name|parentType
argument_list|)
decl_stmt|;
return|return
name|JoinUtil
operator|.
name|createJoinQuery
argument_list|(
name|joinField
argument_list|,
name|innerQuery
argument_list|,
name|toQuery
argument_list|,
name|indexSearcher
argument_list|,
name|scoreMode
argument_list|,
name|ordinalMap
argument_list|,
name|minChildren
argument_list|,
name|maxChildren
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
if|if
condition|(
operator|!
name|super
operator|.
name|equals
argument_list|(
name|o
argument_list|)
condition|)
return|return
literal|false
return|;
name|LateParsingQuery
name|that
init|=
operator|(
name|LateParsingQuery
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|minChildren
operator|!=
name|that
operator|.
name|minChildren
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|maxChildren
operator|!=
name|that
operator|.
name|maxChildren
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|toQuery
operator|.
name|equals
argument_list|(
name|that
operator|.
name|toQuery
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|innerQuery
operator|.
name|equals
argument_list|(
name|that
operator|.
name|innerQuery
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|parentType
operator|.
name|equals
argument_list|(
name|that
operator|.
name|parentType
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
name|scoreMode
operator|==
name|that
operator|.
name|scoreMode
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
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|toQuery
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|innerQuery
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|minChildren
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|maxChildren
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|parentType
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|scoreMode
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
literal|"LateParsingQuery {parentType="
operator|+
name|parentType
operator|+
literal|"}"
return|;
block|}
DECL|method|getMinChildren
specifier|public
name|int
name|getMinChildren
parameter_list|()
block|{
return|return
name|minChildren
return|;
block|}
DECL|method|getMaxChildren
specifier|public
name|int
name|getMaxChildren
parameter_list|()
block|{
return|return
name|maxChildren
return|;
block|}
DECL|method|getScoreMode
specifier|public
name|ScoreMode
name|getScoreMode
parameter_list|()
block|{
return|return
name|scoreMode
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|HasChildQueryBuilder
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
name|minChildren
argument_list|,
name|that
operator|.
name|minChildren
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|maxChildren
argument_list|,
name|that
operator|.
name|maxChildren
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|queryInnerHits
argument_list|,
name|that
operator|.
name|queryInnerHits
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
name|scoreMode
argument_list|,
name|minChildren
argument_list|,
name|maxChildren
argument_list|,
name|queryInnerHits
argument_list|)
return|;
block|}
DECL|method|HasChildQueryBuilder
specifier|protected
name|HasChildQueryBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|type
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|minChildren
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|maxChildren
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
specifier|final
name|int
name|ordinal
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|scoreMode
operator|=
name|ScoreMode
operator|.
name|values
argument_list|()
index|[
name|ordinal
index|]
expr_stmt|;
name|query
operator|=
name|in
operator|.
name|readQuery
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|queryInnerHits
operator|=
operator|new
name|QueryInnerHits
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|HasChildQueryBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HasChildQueryBuilder
argument_list|(
name|in
argument_list|)
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
name|writeString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|minChildren
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|maxChildren
argument_list|()
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
name|writeQuery
argument_list|(
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryInnerHits
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|queryInnerHits
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

