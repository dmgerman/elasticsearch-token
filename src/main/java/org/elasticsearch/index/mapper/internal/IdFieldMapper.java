begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|internal
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
name|Iterables
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
name|BinaryDocValuesField
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
name|Field
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
name|FieldType
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
name|XStringField
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
name|FieldInfo
operator|.
name|IndexOptions
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
name|Term
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
name|queries
operator|.
name|TermsFilter
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
name|*
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
name|BytesRef
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
name|lucene
operator|.
name|BytesRefs
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
name|RegexpFilter
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
name|XBooleanFilter
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
name|ImmutableSettings
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
name|codec
operator|.
name|docvaluesformat
operator|.
name|DocValuesFormatProvider
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
name|codec
operator|.
name|docvaluesformat
operator|.
name|DocValuesFormatService
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatProvider
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatService
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
name|core
operator|.
name|AbstractFieldMapper
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
name|Collection
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
name|MapperBuilders
operator|.
name|id
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
name|core
operator|.
name|TypeParsers
operator|.
name|parseField
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IdFieldMapper
specifier|public
class|class
name|IdFieldMapper
extends|extends
name|AbstractFieldMapper
argument_list|<
name|String
argument_list|>
implements|implements
name|InternalMapper
implements|,
name|RootMapper
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"_id"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_id"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|AbstractFieldMapper
operator|.
name|Defaults
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|IdFieldMapper
operator|.
name|NAME
decl_stmt|;
DECL|field|INDEX_NAME
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
name|IdFieldMapper
operator|.
name|NAME
decl_stmt|;
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|FIELD_TYPE
init|=
operator|new
name|FieldType
argument_list|(
name|AbstractFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|setIndexed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS_ONLY
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
DECL|field|PATH
specifier|public
specifier|static
specifier|final
name|String
name|PATH
init|=
literal|null
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|AbstractFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|IdFieldMapper
argument_list|>
block|{
DECL|field|path
specifier|private
name|String
name|path
init|=
name|Defaults
operator|.
name|PATH
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{
name|super
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
operator|new
name|FieldType
argument_list|(
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|indexName
operator|=
name|Defaults
operator|.
name|INDEX_NAME
expr_stmt|;
block|}
DECL|method|path
specifier|public
name|Builder
name|path
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|IdFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|IdFieldMapper
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|boost
argument_list|,
name|fieldType
argument_list|,
name|docValues
argument_list|,
name|path
argument_list|,
name|postingsProvider
argument_list|,
name|docValuesProvider
argument_list|,
name|fieldDataSettings
argument_list|,
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|class|TypeParser
specifier|public
specifier|static
class|class
name|TypeParser
implements|implements
name|Mapper
operator|.
name|TypeParser
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Mapper
operator|.
name|Builder
name|parse
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
parameter_list|,
name|ParserContext
name|parserContext
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|IdFieldMapper
operator|.
name|Builder
name|builder
init|=
name|id
argument_list|()
decl_stmt|;
name|parseField
argument_list|(
name|builder
argument_list|,
name|builder
operator|.
name|name
argument_list|,
name|node
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|node
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fieldName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"path"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|path
argument_list|(
name|fieldNode
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|method|IdFieldMapper
specifier|public
name|IdFieldMapper
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|FieldType
argument_list|(
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|IdFieldMapper
specifier|public
name|IdFieldMapper
parameter_list|(
name|FieldType
name|fieldType
parameter_list|)
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|INDEX_NAME
argument_list|,
name|fieldType
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|IdFieldMapper
specifier|protected
name|IdFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|,
name|FieldType
name|fieldType
parameter_list|,
name|Boolean
name|docValues
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|Defaults
operator|.
name|BOOST
argument_list|,
name|fieldType
argument_list|,
name|docValues
argument_list|,
name|Defaults
operator|.
name|PATH
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|IdFieldMapper
specifier|protected
name|IdFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|,
name|float
name|boost
parameter_list|,
name|FieldType
name|fieldType
parameter_list|,
name|Boolean
name|docValues
parameter_list|,
name|String
name|path
parameter_list|,
name|PostingsFormatProvider
name|postingsProvider
parameter_list|,
name|DocValuesFormatProvider
name|docValuesProvider
parameter_list|,
annotation|@
name|Nullable
name|Settings
name|fieldDataSettings
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|Names
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|indexName
argument_list|,
name|name
argument_list|)
argument_list|,
name|boost
argument_list|,
name|fieldType
argument_list|,
name|docValues
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|postingsProvider
argument_list|,
name|docValuesProvider
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|fieldDataSettings
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
DECL|method|path
specifier|public
name|String
name|path
parameter_list|()
block|{
return|return
name|this
operator|.
name|path
return|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldType
specifier|public
name|FieldType
name|defaultFieldType
parameter_list|()
block|{
return|return
name|Defaults
operator|.
name|FIELD_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldDataType
specifier|public
name|FieldDataType
name|defaultFieldDataType
parameter_list|()
block|{
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"string"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|String
name|value
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|value
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|useTermQueryWithQueryString
specifier|public
name|boolean
name|useTermQueryWithQueryString
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|termQuery
specifier|public
name|Query
name|termQuery
parameter_list|(
name|Object
name|value
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|termQuery
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
return|;
block|}
comment|// no need for constant score filter, since we don't cache the filter, and it always takes deletes into account
return|return
operator|new
name|ConstantScoreQuery
argument_list|(
name|termFilter
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|termFilter
specifier|public
name|Filter
name|termFilter
parameter_list|(
name|Object
name|value
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|termFilter
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
return|;
block|}
return|return
operator|new
name|TermsFilter
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createTypeUids
argument_list|(
name|context
operator|.
name|queryTypes
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|termsFilter
specifier|public
name|Filter
name|termsFilter
parameter_list|(
name|List
name|values
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|termsFilter
argument_list|(
name|values
argument_list|,
name|context
argument_list|)
return|;
block|}
return|return
operator|new
name|TermsFilter
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createTypeUids
argument_list|(
name|context
operator|.
name|queryTypes
argument_list|()
argument_list|,
name|values
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|prefixQuery
specifier|public
name|Query
name|prefixQuery
parameter_list|(
name|Object
name|value
parameter_list|,
annotation|@
name|Nullable
name|MultiTermQuery
operator|.
name|RewriteMethod
name|method
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|prefixQuery
argument_list|(
name|value
argument_list|,
name|method
argument_list|,
name|context
argument_list|)
return|;
block|}
name|Collection
argument_list|<
name|String
argument_list|>
name|queryTypes
init|=
name|context
operator|.
name|queryTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryTypes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|PrefixQuery
name|prefixQuery
init|=
operator|new
name|PrefixQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|Iterables
operator|.
name|getFirst
argument_list|(
name|queryTypes
argument_list|,
literal|null
argument_list|)
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|method
operator|!=
literal|null
condition|)
block|{
name|prefixQuery
operator|.
name|setRewriteMethod
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
return|return
name|prefixQuery
return|;
block|}
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|queryType
range|:
name|queryTypes
control|)
block|{
name|PrefixQuery
name|prefixQuery
init|=
operator|new
name|PrefixQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|queryType
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|method
operator|!=
literal|null
condition|)
block|{
name|prefixQuery
operator|.
name|setRewriteMethod
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|add
argument_list|(
name|prefixQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|prefixFilter
specifier|public
name|Filter
name|prefixFilter
parameter_list|(
name|Object
name|value
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|prefixFilter
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
return|;
block|}
name|Collection
argument_list|<
name|String
argument_list|>
name|queryTypes
init|=
name|context
operator|.
name|queryTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryTypes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
operator|new
name|PrefixFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|Iterables
operator|.
name|getFirst
argument_list|(
name|queryTypes
argument_list|,
literal|null
argument_list|)
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
name|XBooleanFilter
name|filter
init|=
operator|new
name|XBooleanFilter
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|queryType
range|:
name|queryTypes
control|)
block|{
name|filter
operator|.
name|add
argument_list|(
operator|new
name|PrefixFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|queryType
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
annotation|@
name|Override
DECL|method|regexpQuery
specifier|public
name|Query
name|regexpQuery
parameter_list|(
name|Object
name|value
parameter_list|,
name|int
name|flags
parameter_list|,
annotation|@
name|Nullable
name|MultiTermQuery
operator|.
name|RewriteMethod
name|method
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|regexpQuery
argument_list|(
name|value
argument_list|,
name|flags
argument_list|,
name|method
argument_list|,
name|context
argument_list|)
return|;
block|}
name|Collection
argument_list|<
name|String
argument_list|>
name|queryTypes
init|=
name|context
operator|.
name|queryTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryTypes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|RegexpQuery
name|regexpQuery
init|=
operator|new
name|RegexpQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|Iterables
operator|.
name|getFirst
argument_list|(
name|queryTypes
argument_list|,
literal|null
argument_list|)
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|flags
argument_list|)
decl_stmt|;
if|if
condition|(
name|method
operator|!=
literal|null
condition|)
block|{
name|regexpQuery
operator|.
name|setRewriteMethod
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
return|return
name|regexpQuery
return|;
block|}
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|queryType
range|:
name|queryTypes
control|)
block|{
name|RegexpQuery
name|regexpQuery
init|=
operator|new
name|RegexpQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|queryType
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|flags
argument_list|)
decl_stmt|;
if|if
condition|(
name|method
operator|!=
literal|null
condition|)
block|{
name|regexpQuery
operator|.
name|setRewriteMethod
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|add
argument_list|(
name|regexpQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
DECL|method|regexpFilter
specifier|public
name|Filter
name|regexpFilter
parameter_list|(
name|Object
name|value
parameter_list|,
name|int
name|flags
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|regexpFilter
argument_list|(
name|value
argument_list|,
name|flags
argument_list|,
name|context
argument_list|)
return|;
block|}
name|Collection
argument_list|<
name|String
argument_list|>
name|queryTypes
init|=
name|context
operator|.
name|queryTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryTypes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
operator|new
name|RegexpFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|Iterables
operator|.
name|getFirst
argument_list|(
name|queryTypes
argument_list|,
literal|null
argument_list|)
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|flags
argument_list|)
return|;
block|}
name|XBooleanFilter
name|filter
init|=
operator|new
name|XBooleanFilter
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|queryType
range|:
name|queryTypes
control|)
block|{
name|filter
operator|.
name|add
argument_list|(
operator|new
name|RegexpFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|queryType
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|flags
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
annotation|@
name|Override
DECL|method|preParse
specifier|public
name|void
name|preParse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|id
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|id
argument_list|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|postParse
specifier|public
name|void
name|postParse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|id
argument_list|()
operator|==
literal|null
operator|&&
operator|!
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|flyweight
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"No id found while parsing the content source"
argument_list|)
throw|;
block|}
comment|// it either get built in the preParse phase, or get parsed...
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|void
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|void
name|validate
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|MapperParsingException
block|{     }
annotation|@
name|Override
DECL|method|includeInObject
specifier|public
name|boolean
name|includeInObject
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|void
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentName
argument_list|()
operator|!=
literal|null
operator|&&
name|parser
operator|.
name|currentName
argument_list|()
operator|.
name|equals
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|)
operator|&&
name|parser
operator|.
name|currentToken
argument_list|()
operator|.
name|isValue
argument_list|()
condition|)
block|{
comment|// we are in the parse Phase
name|String
name|id
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|id
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|context
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|id
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Provided id ["
operator|+
name|context
operator|.
name|id
argument_list|()
operator|+
literal|"] does not match the content one ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|context
operator|.
name|id
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|// else we are in the pre/post parse phase
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|fieldType
operator|.
name|stored
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|XStringField
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|context
operator|.
name|id
argument_list|()
argument_list|,
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasDocValues
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|BinaryDocValuesField
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|context
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|boolean
name|includeDefaults
init|=
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"include_defaults"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// if all are defaults, no sense to write it at all
if|if
condition|(
operator|!
name|includeDefaults
operator|&&
name|fieldType
operator|.
name|stored
argument_list|()
operator|==
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|stored
argument_list|()
operator|&&
name|fieldType
operator|.
name|indexed
argument_list|()
operator|==
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|indexed
argument_list|()
operator|&&
name|path
operator|==
name|Defaults
operator|.
name|PATH
operator|&&
name|customFieldDataSettings
operator|==
literal|null
operator|&&
operator|(
name|postingsFormat
operator|==
literal|null
operator|||
name|postingsFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultPostingFormat
argument_list|()
argument_list|)
operator|)
operator|&&
operator|(
name|docValuesFormat
operator|==
literal|null
operator|||
name|docValuesFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultDocValuesFormat
argument_list|()
argument_list|)
operator|)
condition|)
block|{
return|return
name|builder
return|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeDefaults
operator|||
name|fieldType
operator|.
name|stored
argument_list|()
operator|!=
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|stored
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
name|fieldType
operator|.
name|stored
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeDefaults
operator|||
name|fieldType
operator|.
name|indexed
argument_list|()
operator|!=
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|indexed
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|indexTokenizeOptionToString
argument_list|(
name|fieldType
operator|.
name|indexed
argument_list|()
argument_list|,
name|fieldType
operator|.
name|tokenized
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeDefaults
operator|||
name|path
operator|!=
name|Defaults
operator|.
name|PATH
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"path"
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|postingsFormat
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|includeDefaults
operator|||
operator|!
name|postingsFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultPostingFormat
argument_list|()
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"postings_format"
argument_list|,
name|postingsFormat
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|String
name|format
init|=
name|defaultPostingFormat
argument_list|()
decl_stmt|;
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
name|format
operator|=
name|PostingsFormatService
operator|.
name|DEFAULT_FORMAT
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"postings_format"
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|docValuesFormat
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|includeDefaults
operator|||
operator|!
name|docValuesFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultDocValuesFormat
argument_list|()
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|DOC_VALUES_FORMAT
argument_list|,
name|docValuesFormat
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|String
name|format
init|=
name|defaultDocValuesFormat
argument_list|()
decl_stmt|;
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
name|format
operator|=
name|DocValuesFormatService
operator|.
name|DEFAULT_FORMAT
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|DOC_VALUES_FORMAT
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|customFieldDataSettings
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fielddata"
argument_list|,
operator|(
name|Map
operator|)
name|customFieldDataSettings
operator|.
name|getAsMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fielddata"
argument_list|,
operator|(
name|Map
operator|)
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|merge
specifier|public
name|void
name|merge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{
comment|// do nothing here, no merging, but also no exception
block|}
block|}
end_class

end_unit

