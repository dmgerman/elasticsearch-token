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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|Filter
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
name|util
operator|.
name|CloseableThreadLocal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|CacheRecycler
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
name|Nullable
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
name|bytes
operator|.
name|BytesReference
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
name|inject
operator|.
name|Inject
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
name|settings
operator|.
name|Settings
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
name|XContentFactory
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
name|XContentHelper
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
name|AbstractIndexComponent
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
name|Index
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
name|analysis
operator|.
name|AnalysisService
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
name|cache
operator|.
name|IndexCache
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
name|engine
operator|.
name|IndexEngine
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
name|IndexFieldDataService
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
name|MapperService
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
name|AllFieldMapper
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
name|settings
operator|.
name|IndexSettings
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
name|similarity
operator|.
name|SimilarityService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|query
operator|.
name|IndicesQueriesRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
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
name|EnumSet
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
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
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
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IndexQueryParserService
specifier|public
class|class
name|IndexQueryParserService
extends|extends
name|AbstractIndexComponent
block|{
DECL|class|Defaults
specifier|public
specifier|static
specifier|final
class|class
name|Defaults
block|{
DECL|field|QUERY_PREFIX
specifier|public
specifier|static
specifier|final
name|String
name|QUERY_PREFIX
init|=
literal|"index.queryparser.query"
decl_stmt|;
DECL|field|FILTER_PREFIX
specifier|public
specifier|static
specifier|final
name|String
name|FILTER_PREFIX
init|=
literal|"index.queryparser.filter"
decl_stmt|;
block|}
DECL|field|cache
specifier|private
name|CloseableThreadLocal
argument_list|<
name|QueryParseContext
argument_list|>
name|cache
init|=
operator|new
name|CloseableThreadLocal
argument_list|<
name|QueryParseContext
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|QueryParseContext
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|QueryParseContext
argument_list|(
name|index
argument_list|,
name|IndexQueryParserService
operator|.
name|this
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|cacheRecycler
specifier|final
name|CacheRecycler
name|cacheRecycler
decl_stmt|;
DECL|field|analysisService
specifier|final
name|AnalysisService
name|analysisService
decl_stmt|;
DECL|field|scriptService
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|mapperService
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|similarityService
specifier|final
name|SimilarityService
name|similarityService
decl_stmt|;
DECL|field|indexCache
specifier|final
name|IndexCache
name|indexCache
decl_stmt|;
DECL|field|fieldDataService
specifier|final
name|IndexFieldDataService
name|fieldDataService
decl_stmt|;
DECL|field|indexEngine
specifier|final
name|IndexEngine
name|indexEngine
decl_stmt|;
DECL|field|queryParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|QueryParser
argument_list|>
name|queryParsers
decl_stmt|;
DECL|field|filterParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|FilterParser
argument_list|>
name|filterParsers
decl_stmt|;
DECL|field|defaultField
specifier|private
name|String
name|defaultField
decl_stmt|;
DECL|field|queryStringLenient
specifier|private
name|boolean
name|queryStringLenient
decl_stmt|;
DECL|field|strict
specifier|private
specifier|final
name|boolean
name|strict
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndexQueryParserService
specifier|public
name|IndexQueryParserService
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
parameter_list|,
name|CacheRecycler
name|cacheRecycler
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|AnalysisService
name|analysisService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|IndexCache
name|indexCache
parameter_list|,
name|IndexFieldDataService
name|fieldDataService
parameter_list|,
name|IndexEngine
name|indexEngine
parameter_list|,
annotation|@
name|Nullable
name|SimilarityService
name|similarityService
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|QueryParserFactory
argument_list|>
name|namedQueryParsers
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|FilterParserFactory
argument_list|>
name|namedFilterParsers
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|cacheRecycler
operator|=
name|cacheRecycler
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|analysisService
operator|=
name|analysisService
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|similarityService
operator|=
name|similarityService
expr_stmt|;
name|this
operator|.
name|indexCache
operator|=
name|indexCache
expr_stmt|;
name|this
operator|.
name|fieldDataService
operator|=
name|fieldDataService
expr_stmt|;
name|this
operator|.
name|indexEngine
operator|=
name|indexEngine
expr_stmt|;
name|this
operator|.
name|defaultField
operator|=
name|indexSettings
operator|.
name|get
argument_list|(
literal|"index.query.default_field"
argument_list|,
name|AllFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryStringLenient
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"index.query_string.lenient"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|strict
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"index.query.parse.strict"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|QueryParser
argument_list|>
name|queryParsers
init|=
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|namedQueryParsers
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|queryParserGroups
init|=
name|indexSettings
operator|.
name|getGroups
argument_list|(
name|IndexQueryParserService
operator|.
name|Defaults
operator|.
name|QUERY_PREFIX
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|QueryParserFactory
argument_list|>
name|entry
range|:
name|namedQueryParsers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|queryParserName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|QueryParserFactory
name|queryParserFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|queryParserSettings
init|=
name|queryParserGroups
operator|.
name|get
argument_list|(
name|queryParserName
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryParserSettings
operator|==
literal|null
condition|)
block|{
name|queryParserSettings
operator|=
name|EMPTY_SETTINGS
expr_stmt|;
block|}
name|queryParsers
operator|.
name|add
argument_list|(
name|queryParserFactory
operator|.
name|create
argument_list|(
name|queryParserName
argument_list|,
name|queryParserSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|QueryParser
argument_list|>
name|queryParsersMap
init|=
name|newHashMap
argument_list|()
decl_stmt|;
name|queryParsersMap
operator|.
name|putAll
argument_list|(
name|indicesQueriesRegistry
operator|.
name|queryParsers
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryParsers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|QueryParser
name|queryParser
range|:
name|queryParsers
control|)
block|{
name|add
argument_list|(
name|queryParsersMap
argument_list|,
name|queryParser
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|queryParsers
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|queryParsersMap
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FilterParser
argument_list|>
name|filterParsers
init|=
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|namedFilterParsers
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|filterParserGroups
init|=
name|indexSettings
operator|.
name|getGroups
argument_list|(
name|IndexQueryParserService
operator|.
name|Defaults
operator|.
name|FILTER_PREFIX
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FilterParserFactory
argument_list|>
name|entry
range|:
name|namedFilterParsers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|filterParserName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|FilterParserFactory
name|filterParserFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|filterParserSettings
init|=
name|filterParserGroups
operator|.
name|get
argument_list|(
name|filterParserName
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterParserSettings
operator|==
literal|null
condition|)
block|{
name|filterParserSettings
operator|=
name|EMPTY_SETTINGS
expr_stmt|;
block|}
name|filterParsers
operator|.
name|add
argument_list|(
name|filterParserFactory
operator|.
name|create
argument_list|(
name|filterParserName
argument_list|,
name|filterParserSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|FilterParser
argument_list|>
name|filterParsersMap
init|=
name|newHashMap
argument_list|()
decl_stmt|;
name|filterParsersMap
operator|.
name|putAll
argument_list|(
name|indicesQueriesRegistry
operator|.
name|filterParsers
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterParsers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FilterParser
name|filterParser
range|:
name|filterParsers
control|)
block|{
name|add
argument_list|(
name|filterParsersMap
argument_list|,
name|filterParser
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|filterParsers
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|filterParsersMap
argument_list|)
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|cache
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|defaultField
specifier|public
name|String
name|defaultField
parameter_list|()
block|{
return|return
name|this
operator|.
name|defaultField
return|;
block|}
DECL|method|queryStringLenient
specifier|public
name|boolean
name|queryStringLenient
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryStringLenient
return|;
block|}
DECL|method|queryParser
specifier|public
name|QueryParser
name|queryParser
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|queryParsers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|filterParser
specifier|public
name|FilterParser
name|filterParser
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|filterParsers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|ParsedQuery
name|parse
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|BytesReference
name|bytes
init|=
name|queryBuilder
operator|.
name|buildAsBytes
argument_list|()
decl_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
name|parse
argument_list|(
name|cache
operator|.
name|get
argument_list|()
argument_list|,
name|parser
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|QueryParsingException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|parse
specifier|public
name|ParsedQuery
name|parse
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
return|return
name|parse
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|length
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|ParsedQuery
name|parse
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|parse
argument_list|(
name|cache
operator|.
name|get
argument_list|()
argument_list|,
name|parser
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|QueryParsingException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|parse
specifier|public
name|ParsedQuery
name|parse
parameter_list|(
name|BytesReference
name|source
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|parse
argument_list|(
name|cache
operator|.
name|get
argument_list|()
argument_list|,
name|parser
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|QueryParsingException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|parse
specifier|public
name|ParsedQuery
name|parse
parameter_list|(
name|String
name|source
parameter_list|)
throws|throws
name|QueryParsingException
block|{
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|parse
argument_list|(
name|cache
operator|.
name|get
argument_list|()
argument_list|,
name|parser
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|QueryParsingException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"Failed to parse ["
operator|+
name|source
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|parse
specifier|public
name|ParsedQuery
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
try|try
block|{
return|return
name|parse
argument_list|(
name|cache
operator|.
name|get
argument_list|()
argument_list|,
name|parser
argument_list|)
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
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Parses an inner filter, returning null if the filter should be ignored.      */
annotation|@
name|Nullable
DECL|method|parseInnerFilter
specifier|public
name|ParsedFilter
name|parseInnerFilter
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryParseContext
name|context
init|=
name|cache
operator|.
name|get
argument_list|()
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
name|Filter
name|filter
init|=
name|context
operator|.
name|parseInnerFilter
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ParsedFilter
argument_list|(
name|filter
argument_list|,
name|context
operator|.
name|copyNamedFilters
argument_list|()
argument_list|)
return|;
block|}
finally|finally
block|{
name|context
operator|.
name|reset
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Nullable
DECL|method|parseInnerQuery
specifier|public
name|Query
name|parseInnerQuery
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryParseContext
name|context
init|=
name|cache
operator|.
name|get
argument_list|()
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|context
operator|.
name|parseInnerQuery
argument_list|()
return|;
block|}
finally|finally
block|{
name|context
operator|.
name|reset
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Selectively parses a query from a top level query or query_binary json field from the specified source.      */
DECL|method|parseQuery
specifier|public
name|ParsedQuery
name|parseQuery
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
try|try
block|{
name|ParsedQuery
name|parsedQuery
init|=
literal|null
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
for|for
control|(
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
init|;
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
control|)
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
name|String
name|fieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"query"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|parsedQuery
operator|=
name|parse
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"query_binary"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"queryBinary"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|byte
index|[]
name|querySource
init|=
name|parser
operator|.
name|binaryValue
argument_list|()
decl_stmt|;
name|XContentParser
name|qSourceParser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|querySource
argument_list|)
operator|.
name|createParser
argument_list|(
name|querySource
argument_list|)
decl_stmt|;
name|parsedQuery
operator|=
name|parse
argument_list|(
name|qSourceParser
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|()
argument_list|,
literal|"request does not support ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|parsedQuery
operator|!=
literal|null
condition|)
block|{
return|return
name|parsedQuery
return|;
block|}
block|}
catch|catch
parameter_list|(
name|QueryParsingException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|()
argument_list|,
literal|"Required query is missing"
argument_list|)
throw|;
block|}
DECL|method|parse
specifier|private
name|ParsedQuery
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|parseContext
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|strict
condition|)
block|{
name|parseContext
operator|.
name|parseFlags
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|ParseField
operator|.
name|Flag
operator|.
name|STRICT
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Query
name|query
init|=
name|parseContext
operator|.
name|parseInnerQuery
argument_list|()
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
name|Queries
operator|.
name|newMatchNoDocsQuery
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|ParsedQuery
argument_list|(
name|query
argument_list|,
name|parseContext
operator|.
name|copyNamedFilters
argument_list|()
argument_list|)
return|;
block|}
finally|finally
block|{
name|parseContext
operator|.
name|reset
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|add
specifier|private
name|void
name|add
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|FilterParser
argument_list|>
name|map
parameter_list|,
name|FilterParser
name|filterParser
parameter_list|)
block|{
for|for
control|(
name|String
name|name
range|:
name|filterParser
operator|.
name|names
argument_list|()
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|name
operator|.
name|intern
argument_list|()
argument_list|,
name|filterParser
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|add
specifier|private
name|void
name|add
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|QueryParser
argument_list|>
name|map
parameter_list|,
name|QueryParser
name|queryParser
parameter_list|)
block|{
for|for
control|(
name|String
name|name
range|:
name|queryParser
operator|.
name|names
argument_list|()
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|name
operator|.
name|intern
argument_list|()
argument_list|,
name|queryParser
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

