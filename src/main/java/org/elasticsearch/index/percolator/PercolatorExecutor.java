begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|percolator
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
name|analysis
operator|.
name|TokenStream
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
name|document
operator|.
name|Fieldable
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
name|memory
operator|.
name|CustomMemoryIndex
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
name|Collector
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
name|Scorer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|Preconditions
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
name|Strings
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
name|io
operator|.
name|FastStringReader
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
name|ESLogger
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|Engine
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
name|field
operator|.
name|data
operator|.
name|FieldData
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
name|field
operator|.
name|data
operator|.
name|FieldDataType
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
name|*
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
name|UidFieldMapper
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
name|IndexQueryParserService
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
name|QueryBuilders
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
name|service
operator|.
name|IndexService
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
name|shard
operator|.
name|service
operator|.
name|IndexShard
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
name|IndicesService
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
name|io
operator|.
name|Reader
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|SourceToParse
operator|.
name|source
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PercolatorExecutor
specifier|public
class|class
name|PercolatorExecutor
extends|extends
name|AbstractIndexComponent
block|{
DECL|class|SourceRequest
specifier|public
specifier|static
class|class
name|SourceRequest
block|{
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|BytesReference
name|source
decl_stmt|;
DECL|method|SourceRequest
specifier|public
name|SourceRequest
parameter_list|(
name|String
name|type
parameter_list|,
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
DECL|method|source
specifier|public
name|BytesReference
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
block|}
DECL|class|DocAndSourceQueryRequest
specifier|public
specifier|static
class|class
name|DocAndSourceQueryRequest
block|{
DECL|field|doc
specifier|private
specifier|final
name|ParsedDocument
name|doc
decl_stmt|;
annotation|@
name|Nullable
DECL|field|query
specifier|private
specifier|final
name|String
name|query
decl_stmt|;
DECL|method|DocAndSourceQueryRequest
specifier|public
name|DocAndSourceQueryRequest
parameter_list|(
name|ParsedDocument
name|doc
parameter_list|,
annotation|@
name|Nullable
name|String
name|query
parameter_list|)
block|{
name|this
operator|.
name|doc
operator|=
name|doc
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
block|}
DECL|method|doc
specifier|public
name|ParsedDocument
name|doc
parameter_list|()
block|{
return|return
name|this
operator|.
name|doc
return|;
block|}
annotation|@
name|Nullable
DECL|method|query
name|String
name|query
parameter_list|()
block|{
return|return
name|this
operator|.
name|query
return|;
block|}
block|}
DECL|class|DocAndQueryRequest
specifier|public
specifier|static
class|class
name|DocAndQueryRequest
block|{
DECL|field|doc
specifier|private
specifier|final
name|ParsedDocument
name|doc
decl_stmt|;
annotation|@
name|Nullable
DECL|field|query
specifier|private
specifier|final
name|Query
name|query
decl_stmt|;
DECL|method|DocAndQueryRequest
specifier|public
name|DocAndQueryRequest
parameter_list|(
name|ParsedDocument
name|doc
parameter_list|,
annotation|@
name|Nullable
name|Query
name|query
parameter_list|)
block|{
name|this
operator|.
name|doc
operator|=
name|doc
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
block|}
DECL|method|doc
specifier|public
name|ParsedDocument
name|doc
parameter_list|()
block|{
return|return
name|this
operator|.
name|doc
return|;
block|}
annotation|@
name|Nullable
DECL|method|query
name|Query
name|query
parameter_list|()
block|{
return|return
name|this
operator|.
name|query
return|;
block|}
block|}
DECL|class|Response
specifier|public
specifier|static
specifier|final
class|class
name|Response
block|{
DECL|field|matches
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|matches
decl_stmt|;
DECL|field|mappersAdded
specifier|private
specifier|final
name|boolean
name|mappersAdded
decl_stmt|;
DECL|method|Response
specifier|public
name|Response
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|matches
parameter_list|,
name|boolean
name|mappersAdded
parameter_list|)
block|{
name|this
operator|.
name|matches
operator|=
name|matches
expr_stmt|;
name|this
operator|.
name|mappersAdded
operator|=
name|mappersAdded
expr_stmt|;
block|}
DECL|method|mappersAdded
specifier|public
name|boolean
name|mappersAdded
parameter_list|()
block|{
return|return
name|this
operator|.
name|mappersAdded
return|;
block|}
DECL|method|matches
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|matches
parameter_list|()
block|{
return|return
name|matches
return|;
block|}
block|}
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|queryParserService
specifier|private
specifier|final
name|IndexQueryParserService
name|queryParserService
decl_stmt|;
DECL|field|indexCache
specifier|private
specifier|final
name|IndexCache
name|indexCache
decl_stmt|;
DECL|field|queries
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|queries
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|indicesService
specifier|private
name|IndicesService
name|indicesService
decl_stmt|;
annotation|@
name|Inject
DECL|method|PercolatorExecutor
specifier|public
name|PercolatorExecutor
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|IndexQueryParserService
name|queryParserService
parameter_list|,
name|IndexCache
name|indexCache
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
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|queryParserService
operator|=
name|queryParserService
expr_stmt|;
name|this
operator|.
name|indexCache
operator|=
name|indexCache
expr_stmt|;
block|}
DECL|method|setIndicesService
specifier|public
name|void
name|setIndicesService
parameter_list|(
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|queries
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|addQuery
specifier|public
name|void
name|addQuery
parameter_list|(
name|String
name|name
parameter_list|,
name|QueryBuilder
name|queryBuilder
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|smileBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|queryBuilder
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|addQuery
argument_list|(
name|name
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"Failed to add query ["
operator|+
name|name
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|addQuery
specifier|public
name|void
name|addQuery
parameter_list|(
name|String
name|name
parameter_list|,
name|BytesReference
name|source
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|addQuery
argument_list|(
name|name
argument_list|,
name|parseQuery
argument_list|(
name|name
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|parseQuery
specifier|public
name|Query
name|parseQuery
parameter_list|(
name|String
name|name
parameter_list|,
name|BytesReference
name|source
parameter_list|)
throws|throws
name|ElasticSearchException
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
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|Query
name|query
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
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
comment|// move the START_OBJECT
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
name|ElasticSearchException
argument_list|(
literal|"failed to parse query ["
operator|+
name|name
operator|+
literal|"], not starting with OBJECT"
argument_list|)
throw|;
block|}
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
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|query
operator|=
name|queryParserService
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
break|break;
block|}
else|else
block|{
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
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
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|query
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"failed to parse query ["
operator|+
name|name
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
DECL|method|addQuery
specifier|private
name|void
name|addQuery
parameter_list|(
name|String
name|name
parameter_list|,
name|Query
name|query
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|query
operator|!=
literal|null
argument_list|,
literal|"query must be provided for percolate request"
argument_list|)
expr_stmt|;
name|this
operator|.
name|queries
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
DECL|method|removeQuery
specifier|public
name|void
name|removeQuery
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|queries
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|method|addQueries
specifier|public
name|void
name|addQueries
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|queries
parameter_list|)
block|{
name|this
operator|.
name|queries
operator|.
name|putAll
argument_list|(
name|queries
argument_list|)
expr_stmt|;
block|}
DECL|method|percolate
specifier|public
name|Response
name|percolate
parameter_list|(
specifier|final
name|SourceRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|Query
name|query
init|=
literal|null
decl_stmt|;
name|ParsedDocument
name|doc
init|=
literal|null
decl_stmt|;
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
name|request
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
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
comment|// we need to check the "doc" here, so the next token will be START_OBJECT which is
comment|// the actual document starting
if|if
condition|(
literal|"doc"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|DocumentMapper
name|docMapper
init|=
name|mapperService
operator|.
name|documentMapperWithAutoCreate
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|=
name|docMapper
operator|.
name|parse
argument_list|(
name|source
argument_list|(
name|parser
argument_list|)
operator|.
name|type
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|flyweight
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
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
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|query
operator|=
name|percolatorIndexServiceSafe
argument_list|()
operator|.
name|queryParserService
argument_list|()
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|PercolatorException
argument_list|(
name|index
argument_list|,
literal|"failed to parse request"
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
if|if
condition|(
name|doc
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|PercolatorException
argument_list|(
name|index
argument_list|,
literal|"No doc to percolate in the request"
argument_list|)
throw|;
block|}
return|return
name|percolate
argument_list|(
operator|new
name|DocAndQueryRequest
argument_list|(
name|doc
argument_list|,
name|query
argument_list|)
argument_list|)
return|;
block|}
DECL|method|percolate
specifier|public
name|Response
name|percolate
parameter_list|(
name|DocAndSourceQueryRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|Query
name|query
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|request
operator|.
name|query
argument_list|()
argument_list|)
operator|&&
operator|!
name|request
operator|.
name|query
argument_list|()
operator|.
name|equals
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
name|query
operator|=
name|percolatorIndexServiceSafe
argument_list|()
operator|.
name|queryParserService
argument_list|()
operator|.
name|parse
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
name|request
operator|.
name|query
argument_list|()
argument_list|)
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
block|}
return|return
name|percolate
argument_list|(
operator|new
name|DocAndQueryRequest
argument_list|(
name|request
operator|.
name|doc
argument_list|()
argument_list|,
name|query
argument_list|)
argument_list|)
return|;
block|}
DECL|method|percolate
specifier|private
name|Response
name|percolate
parameter_list|(
name|DocAndQueryRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
comment|// first, parse the source doc into a MemoryIndex
specifier|final
name|CustomMemoryIndex
name|memoryIndex
init|=
operator|new
name|CustomMemoryIndex
argument_list|()
decl_stmt|;
comment|// TODO: This means percolation does not support nested docs...
for|for
control|(
name|Fieldable
name|field
range|:
name|request
operator|.
name|doc
argument_list|()
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|field
operator|.
name|isIndexed
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// no need to index the UID field
if|if
condition|(
name|field
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|TokenStream
name|tokenStream
init|=
name|field
operator|.
name|tokenStreamValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|tokenStream
operator|!=
literal|null
condition|)
block|{
name|memoryIndex
operator|.
name|addField
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|tokenStream
argument_list|,
name|field
operator|.
name|getBoost
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Reader
name|reader
init|=
name|field
operator|.
name|readerValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|memoryIndex
operator|.
name|addField
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|doc
argument_list|()
operator|.
name|analyzer
argument_list|()
operator|.
name|reusableTokenStream
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|reader
argument_list|)
argument_list|,
name|field
operator|.
name|getBoost
argument_list|()
operator|*
name|request
operator|.
name|doc
argument_list|()
operator|.
name|rootDoc
argument_list|()
operator|.
name|getBoost
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Failed to analyze field ["
operator|+
name|field
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|String
name|value
init|=
name|field
operator|.
name|stringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|memoryIndex
operator|.
name|addField
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|doc
argument_list|()
operator|.
name|analyzer
argument_list|()
operator|.
name|reusableTokenStream
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
operator|new
name|FastStringReader
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|,
name|field
operator|.
name|getBoost
argument_list|()
operator|*
name|request
operator|.
name|doc
argument_list|()
operator|.
name|rootDoc
argument_list|()
operator|.
name|getBoost
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Failed to analyze field ["
operator|+
name|field
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
block|}
specifier|final
name|IndexSearcher
name|searcher
init|=
name|memoryIndex
operator|.
name|createSearcher
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|matches
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|request
operator|.
name|query
argument_list|()
operator|==
literal|null
condition|)
block|{
name|Lucene
operator|.
name|ExistsCollector
name|collector
init|=
operator|new
name|Lucene
operator|.
name|ExistsCollector
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|entry
range|:
name|queries
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|collector
operator|.
name|reset
argument_list|()
expr_stmt|;
try|try
block|{
name|searcher
operator|.
name|search
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] failed to execute query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|collector
operator|.
name|exists
argument_list|()
condition|)
block|{
name|matches
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|IndexService
name|percolatorIndex
init|=
name|percolatorIndexServiceSafe
argument_list|()
decl_stmt|;
if|if
condition|(
name|percolatorIndex
operator|.
name|numberOfShards
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|PercolateIndexUnavailable
argument_list|(
operator|new
name|Index
argument_list|(
name|PercolatorService
operator|.
name|INDEX_NAME
argument_list|)
argument_list|)
throw|;
block|}
name|IndexShard
name|percolatorShard
init|=
name|percolatorIndex
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Engine
operator|.
name|Searcher
name|percolatorSearcher
init|=
name|percolatorShard
operator|.
name|searcher
argument_list|()
decl_stmt|;
try|try
block|{
name|percolatorSearcher
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|request
operator|.
name|query
argument_list|()
argument_list|,
operator|new
name|QueryCollector
argument_list|(
name|logger
argument_list|,
name|queries
argument_list|,
name|searcher
argument_list|,
name|percolatorIndex
argument_list|,
name|matches
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|percolatorSearcher
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
comment|// explicitly clear the reader, since we can only register on callback on SegmentReader
name|indexCache
operator|.
name|clear
argument_list|(
name|searcher
operator|.
name|getIndexReader
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Response
argument_list|(
name|matches
argument_list|,
name|request
operator|.
name|doc
argument_list|()
operator|.
name|mappingsModified
argument_list|()
argument_list|)
return|;
block|}
DECL|method|percolatorIndexServiceSafe
specifier|private
name|IndexService
name|percolatorIndexServiceSafe
parameter_list|()
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|PercolatorService
operator|.
name|INDEX_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|PercolateIndexUnavailable
argument_list|(
operator|new
name|Index
argument_list|(
name|PercolatorService
operator|.
name|INDEX_NAME
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|indexService
return|;
block|}
DECL|class|QueryCollector
specifier|static
class|class
name|QueryCollector
extends|extends
name|Collector
block|{
DECL|field|searcher
specifier|private
specifier|final
name|IndexSearcher
name|searcher
decl_stmt|;
DECL|field|percolatorIndex
specifier|private
specifier|final
name|IndexService
name|percolatorIndex
decl_stmt|;
DECL|field|matches
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|matches
decl_stmt|;
DECL|field|queries
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|queries
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|collector
specifier|private
specifier|final
name|Lucene
operator|.
name|ExistsCollector
name|collector
init|=
operator|new
name|Lucene
operator|.
name|ExistsCollector
argument_list|()
decl_stmt|;
DECL|field|fieldData
specifier|private
name|FieldData
name|fieldData
decl_stmt|;
DECL|method|QueryCollector
name|QueryCollector
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|queries
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|IndexService
name|percolatorIndex
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|matches
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|queries
operator|=
name|queries
expr_stmt|;
name|this
operator|.
name|searcher
operator|=
name|searcher
expr_stmt|;
name|this
operator|.
name|percolatorIndex
operator|=
name|percolatorIndex
expr_stmt|;
name|this
operator|.
name|matches
operator|=
name|matches
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{         }
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|uid
init|=
name|fieldData
operator|.
name|stringValue
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|uid
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|String
name|id
init|=
name|Uid
operator|.
name|idFromUid
argument_list|(
name|uid
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|queries
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
comment|// log???
return|return;
block|}
comment|// run the query
try|try
block|{
name|collector
operator|.
name|reset
argument_list|()
expr_stmt|;
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
if|if
condition|(
name|collector
operator|.
name|exists
argument_list|()
condition|)
block|{
name|matches
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"["
operator|+
name|id
operator|+
literal|"] failed to execute query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we use the UID because id might not be indexed
name|fieldData
operator|=
name|percolatorIndex
operator|.
name|cache
argument_list|()
operator|.
name|fieldData
argument_list|()
operator|.
name|cache
argument_list|(
name|FieldDataType
operator|.
name|DefaultTypes
operator|.
name|STRING
argument_list|,
name|reader
argument_list|,
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|acceptsDocsOutOfOrder
specifier|public
name|boolean
name|acceptsDocsOutOfOrder
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

