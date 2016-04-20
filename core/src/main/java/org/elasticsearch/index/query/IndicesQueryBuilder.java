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
name|logging
operator|.
name|DeprecationLogger
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
name|logging
operator|.
name|Loggers
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_comment
comment|/**  * A query that will execute the wrapped query only for the specified indices,  * and "match_all" when it does not match those indices (by default).  *  * @deprecated instead search on the `_index` field  */
end_comment

begin_class
annotation|@
name|Deprecated
comment|// TODO remove this class in 6.0
DECL|class|IndicesQueryBuilder
specifier|public
class|class
name|IndicesQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|IndicesQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"indices"
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
DECL|field|NO_MATCH_QUERY
specifier|private
specifier|static
specifier|final
name|ParseField
name|NO_MATCH_QUERY
init|=
operator|new
name|ParseField
argument_list|(
literal|"no_match_query"
argument_list|)
decl_stmt|;
DECL|field|INDEX_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|INDEX_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
DECL|field|INDICES_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|INDICES_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"indices"
argument_list|)
decl_stmt|;
DECL|field|DEPRECATION_LOGGER
specifier|private
specifier|static
specifier|final
name|DeprecationLogger
name|DEPRECATION_LOGGER
init|=
operator|new
name|DeprecationLogger
argument_list|(
name|Loggers
operator|.
name|getLogger
argument_list|(
name|IndicesQueryBuilder
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|innerQuery
specifier|private
specifier|final
name|QueryBuilder
name|innerQuery
decl_stmt|;
DECL|field|indices
specifier|private
specifier|final
name|String
index|[]
name|indices
decl_stmt|;
DECL|field|noMatchQuery
specifier|private
name|QueryBuilder
name|noMatchQuery
init|=
name|defaultNoMatchQuery
argument_list|()
decl_stmt|;
comment|/**      * @deprecated instead search on the `_index` field      */
annotation|@
name|Deprecated
DECL|method|IndicesQueryBuilder
specifier|public
name|IndicesQueryBuilder
parameter_list|(
name|QueryBuilder
name|innerQuery
parameter_list|,
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|DEPRECATION_LOGGER
operator|.
name|deprecated
argument_list|(
literal|"{} query is deprecated. Instead search on the '_index' field"
argument_list|,
name|NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|innerQuery
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner query cannot be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|indices
operator|==
literal|null
operator|||
name|indices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"list of indices cannot be null or empty"
argument_list|)
throw|;
block|}
name|this
operator|.
name|innerQuery
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|innerQuery
argument_list|)
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|IndicesQueryBuilder
specifier|public
name|IndicesQueryBuilder
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
name|innerQuery
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
name|indices
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|noMatchQuery
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
name|writeNamedWriteable
argument_list|(
name|innerQuery
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|noMatchQuery
argument_list|)
expr_stmt|;
block|}
DECL|method|innerQuery
specifier|public
name|QueryBuilder
name|innerQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|innerQuery
return|;
block|}
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
name|this
operator|.
name|indices
return|;
block|}
comment|/**      * Sets the query to use when it executes on an index that does not match the indices provided.      */
DECL|method|noMatchQuery
specifier|public
name|IndicesQueryBuilder
name|noMatchQuery
parameter_list|(
name|QueryBuilder
name|noMatchQuery
parameter_list|)
block|{
if|if
condition|(
name|noMatchQuery
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"noMatch query cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|noMatchQuery
operator|=
name|noMatchQuery
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the no match query, can either be<tt>all</tt> or<tt>none</tt>.      */
DECL|method|noMatchQuery
specifier|public
name|IndicesQueryBuilder
name|noMatchQuery
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|noMatchQuery
operator|=
name|parseNoMatchQuery
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|noMatchQuery
specifier|public
name|QueryBuilder
name|noMatchQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|noMatchQuery
return|;
block|}
DECL|method|defaultNoMatchQuery
specifier|private
specifier|static
name|QueryBuilder
name|defaultNoMatchQuery
parameter_list|()
block|{
return|return
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
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
name|INDICES_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|indices
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
name|innerQuery
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
name|NO_MATCH_QUERY
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|noMatchQuery
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
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
DECL|method|fromXContent
specifier|public
specifier|static
name|Optional
argument_list|<
name|IndicesQueryBuilder
argument_list|>
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
name|QueryBuilder
name|innerQuery
init|=
literal|null
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|indices
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|QueryBuilder
name|noMatchQuery
init|=
name|defaultNoMatchQuery
argument_list|()
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
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
comment|// the 2.0 behaviour when encountering "query" : {} is to return no docs for matching indices
name|innerQuery
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
operator|.
name|orElse
argument_list|(
operator|new
name|MatchNoneQueryBuilder
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
name|NO_MATCH_QUERY
argument_list|)
condition|)
block|{
name|noMatchQuery
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
operator|.
name|orElse
argument_list|(
name|defaultNoMatchQuery
argument_list|()
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
literal|"[indices] query does not support ["
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
name|INDICES_FIELD
argument_list|)
condition|)
block|{
if|if
condition|(
name|indices
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
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
literal|"[indices] indices or index already specified"
argument_list|)
throw|;
block|}
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|String
name|value
init|=
name|parser
operator|.
name|textOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
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
literal|"[indices] no value specified for 'indices' entry"
argument_list|)
throw|;
block|}
name|indices
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
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
literal|"[indices] query does not support ["
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
name|INDEX_FIELD
argument_list|)
condition|)
block|{
if|if
condition|(
name|indices
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
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
literal|"[indices] indices or index already specified"
argument_list|)
throw|;
block|}
name|indices
operator|.
name|add
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
name|NO_MATCH_QUERY
argument_list|)
condition|)
block|{
name|noMatchQuery
operator|=
name|parseNoMatchQuery
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
literal|"[indices] query does not support ["
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
name|innerQuery
operator|==
literal|null
condition|)
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
literal|"[indices] requires 'query' element"
argument_list|)
throw|;
block|}
if|if
condition|(
name|indices
operator|.
name|isEmpty
argument_list|()
condition|)
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
literal|"[indices] requires 'indices' or 'index' element"
argument_list|)
throw|;
block|}
return|return
name|Optional
operator|.
name|of
argument_list|(
operator|new
name|IndicesQueryBuilder
argument_list|(
name|innerQuery
argument_list|,
name|indices
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indices
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
operator|.
name|noMatchQuery
argument_list|(
name|noMatchQuery
argument_list|)
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
argument_list|)
return|;
block|}
DECL|method|parseNoMatchQuery
specifier|static
name|QueryBuilder
name|parseNoMatchQuery
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
literal|"all"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
literal|"none"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
operator|new
name|MatchNoneQueryBuilder
argument_list|()
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"query type can only be [all] or [none] but not "
operator|+
literal|"["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
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
if|if
condition|(
name|context
operator|.
name|matchesIndices
argument_list|(
name|indices
argument_list|)
condition|)
block|{
return|return
name|innerQuery
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
return|;
block|}
return|return
name|noMatchQuery
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|public
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|innerQuery
argument_list|,
name|noMatchQuery
argument_list|,
name|Arrays
operator|.
name|hashCode
argument_list|(
name|indices
argument_list|)
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
name|IndicesQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|innerQuery
argument_list|,
name|other
operator|.
name|innerQuery
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|indices
argument_list|,
name|other
operator|.
name|indices
argument_list|)
operator|&&
comment|// otherwise we are comparing pointers
name|Objects
operator|.
name|equals
argument_list|(
name|noMatchQuery
argument_list|,
name|other
operator|.
name|noMatchQuery
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
name|newInnnerQuery
init|=
name|innerQuery
operator|.
name|rewrite
argument_list|(
name|queryShardContext
argument_list|)
decl_stmt|;
name|QueryBuilder
name|newNoMatchQuery
init|=
name|noMatchQuery
operator|.
name|rewrite
argument_list|(
name|queryShardContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|newInnnerQuery
operator|!=
name|innerQuery
operator|||
name|newNoMatchQuery
operator|!=
name|noMatchQuery
condition|)
block|{
return|return
operator|new
name|IndicesQueryBuilder
argument_list|(
name|innerQuery
argument_list|,
name|indices
argument_list|)
operator|.
name|noMatchQuery
argument_list|(
name|noMatchQuery
argument_list|)
return|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

