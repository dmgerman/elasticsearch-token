begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

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
name|collect
operator|.
name|Tuple
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
name|compress
operator|.
name|CompressedXContent
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
name|NamedXContentRegistry
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
name|common
operator|.
name|xcontent
operator|.
name|XContentType
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
name|analysis
operator|.
name|IndexAnalyzers
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
name|mapper
operator|.
name|MapperRegistry
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
name|Iterator
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
name|function
operator|.
name|Supplier
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_class
DECL|class|DocumentMapperParser
specifier|public
class|class
name|DocumentMapperParser
block|{
DECL|field|mapperService
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|indexAnalyzers
specifier|final
name|IndexAnalyzers
name|indexAnalyzers
decl_stmt|;
DECL|field|xContentRegistry
specifier|private
specifier|final
name|NamedXContentRegistry
name|xContentRegistry
decl_stmt|;
DECL|field|similarityService
specifier|private
specifier|final
name|SimilarityService
name|similarityService
decl_stmt|;
DECL|field|queryShardContextSupplier
specifier|private
specifier|final
name|Supplier
argument_list|<
name|QueryShardContext
argument_list|>
name|queryShardContextSupplier
decl_stmt|;
DECL|field|rootObjectTypeParser
specifier|private
specifier|final
name|RootObjectMapper
operator|.
name|TypeParser
name|rootObjectTypeParser
init|=
operator|new
name|RootObjectMapper
operator|.
name|TypeParser
argument_list|()
decl_stmt|;
DECL|field|indexVersionCreated
specifier|private
specifier|final
name|Version
name|indexVersionCreated
decl_stmt|;
DECL|field|typeParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|typeParsers
decl_stmt|;
DECL|field|rootTypeParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|MetadataFieldMapper
operator|.
name|TypeParser
argument_list|>
name|rootTypeParsers
decl_stmt|;
DECL|method|DocumentMapperParser
specifier|public
name|DocumentMapperParser
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|IndexAnalyzers
name|indexAnalyzers
parameter_list|,
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|SimilarityService
name|similarityService
parameter_list|,
name|MapperRegistry
name|mapperRegistry
parameter_list|,
name|Supplier
argument_list|<
name|QueryShardContext
argument_list|>
name|queryShardContextSupplier
parameter_list|)
block|{
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|indexAnalyzers
operator|=
name|indexAnalyzers
expr_stmt|;
name|this
operator|.
name|xContentRegistry
operator|=
name|xContentRegistry
expr_stmt|;
name|this
operator|.
name|similarityService
operator|=
name|similarityService
expr_stmt|;
name|this
operator|.
name|queryShardContextSupplier
operator|=
name|queryShardContextSupplier
expr_stmt|;
name|this
operator|.
name|typeParsers
operator|=
name|mapperRegistry
operator|.
name|getMapperParsers
argument_list|()
expr_stmt|;
name|this
operator|.
name|rootTypeParsers
operator|=
name|mapperRegistry
operator|.
name|getMetadataMapperParsers
argument_list|()
expr_stmt|;
name|indexVersionCreated
operator|=
name|indexSettings
operator|.
name|getIndexVersionCreated
argument_list|()
expr_stmt|;
block|}
DECL|method|parserContext
specifier|public
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|(
name|String
name|type
parameter_list|)
block|{
return|return
operator|new
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
argument_list|(
name|type
argument_list|,
name|indexAnalyzers
argument_list|,
name|similarityService
operator|::
name|getSimilarity
argument_list|,
name|mapperService
argument_list|,
name|typeParsers
operator|::
name|get
argument_list|,
name|indexVersionCreated
argument_list|,
name|queryShardContextSupplier
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|DocumentMapper
name|parse
parameter_list|(
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|CompressedXContent
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
return|return
name|parse
argument_list|(
name|type
argument_list|,
name|source
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|DocumentMapper
name|parse
parameter_list|(
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|CompressedXContent
name|source
parameter_list|,
name|String
name|defaultSource
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|root
init|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|source
operator|.
name|compressedReference
argument_list|()
argument_list|,
literal|true
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|v2
argument_list|()
decl_stmt|;
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|t
init|=
name|extractMapping
argument_list|(
name|type
argument_list|,
name|root
argument_list|)
decl_stmt|;
name|type
operator|=
name|t
operator|.
name|v1
argument_list|()
expr_stmt|;
name|mapping
operator|=
name|t
operator|.
name|v2
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|mapping
operator|==
literal|null
condition|)
block|{
name|mapping
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
return|return
name|parse
argument_list|(
name|type
argument_list|,
name|mapping
argument_list|,
name|defaultSource
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|parse
specifier|private
name|DocumentMapper
name|parse
parameter_list|(
name|String
name|type
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
parameter_list|,
name|String
name|defaultSource
parameter_list|)
throws|throws
name|MapperParsingException
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
name|MapperParsingException
argument_list|(
literal|"Failed to derive type"
argument_list|)
throw|;
block|}
if|if
condition|(
name|defaultSource
operator|!=
literal|null
condition|)
block|{
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|t
init|=
name|extractMapping
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
name|defaultSource
argument_list|)
decl_stmt|;
if|if
condition|(
name|t
operator|.
name|v2
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|XContentHelper
operator|.
name|mergeDefaults
argument_list|(
name|mapping
argument_list|,
name|t
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
name|parserContext
init|=
name|parserContext
argument_list|(
name|type
argument_list|)
decl_stmt|;
comment|// parse RootObjectMapper
name|DocumentMapper
operator|.
name|Builder
name|docBuilder
init|=
operator|new
name|DocumentMapper
operator|.
name|Builder
argument_list|(
operator|(
name|RootObjectMapper
operator|.
name|Builder
operator|)
name|rootObjectTypeParser
operator|.
name|parse
argument_list|(
name|type
argument_list|,
name|mapping
argument_list|,
name|parserContext
argument_list|)
argument_list|,
name|mapperService
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iterator
init|=
name|mapping
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|// parse DocumentMapper
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|MetadataFieldMapper
operator|.
name|TypeParser
name|typeParser
init|=
name|rootTypeParsers
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeParser
operator|!=
literal|null
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
if|if
condition|(
literal|false
operator|==
name|fieldNode
operator|instanceof
name|Map
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[_parent] must be an object containing [type]"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|fieldNodeMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|fieldNode
decl_stmt|;
name|docBuilder
operator|.
name|put
argument_list|(
name|typeParser
operator|.
name|parse
argument_list|(
name|fieldName
argument_list|,
name|fieldNodeMap
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
name|fieldNodeMap
operator|.
name|remove
argument_list|(
literal|"type"
argument_list|)
expr_stmt|;
name|checkNoRemainingFields
argument_list|(
name|fieldName
argument_list|,
name|fieldNodeMap
argument_list|,
name|parserContext
operator|.
name|indexVersionCreated
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|meta
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|mapping
operator|.
name|remove
argument_list|(
literal|"_meta"
argument_list|)
decl_stmt|;
if|if
condition|(
name|meta
operator|!=
literal|null
condition|)
block|{
comment|// It may not be required to copy meta here to maintain immutability
comment|// but the cost is pretty low here.
name|docBuilder
operator|.
name|meta
argument_list|(
name|unmodifiableMap
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|meta
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|checkNoRemainingFields
argument_list|(
name|mapping
argument_list|,
name|parserContext
operator|.
name|indexVersionCreated
argument_list|()
argument_list|,
literal|"Root mapping definition has unsupported parameters: "
argument_list|)
expr_stmt|;
return|return
name|docBuilder
operator|.
name|build
argument_list|(
name|mapperService
argument_list|)
return|;
block|}
DECL|method|checkNoRemainingFields
specifier|public
specifier|static
name|void
name|checkNoRemainingFields
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|fieldNodeMap
parameter_list|,
name|Version
name|indexVersionCreated
parameter_list|)
block|{
name|checkNoRemainingFields
argument_list|(
name|fieldNodeMap
argument_list|,
name|indexVersionCreated
argument_list|,
literal|"Mapping definition for ["
operator|+
name|fieldName
operator|+
literal|"] has unsupported parameters: "
argument_list|)
expr_stmt|;
block|}
DECL|method|checkNoRemainingFields
specifier|public
specifier|static
name|void
name|checkNoRemainingFields
parameter_list|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|fieldNodeMap
parameter_list|,
name|Version
name|indexVersionCreated
parameter_list|,
name|String
name|message
parameter_list|)
block|{
if|if
condition|(
operator|!
name|fieldNodeMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
name|message
operator|+
name|getRemainingFields
argument_list|(
name|fieldNodeMap
argument_list|)
argument_list|)
throw|;
block|}
block|}
DECL|method|getRemainingFields
specifier|private
specifier|static
name|String
name|getRemainingFields
parameter_list|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
parameter_list|)
block|{
name|StringBuilder
name|remainingFields
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|key
range|:
name|map
operator|.
name|keySet
argument_list|()
control|)
block|{
name|remainingFields
operator|.
name|append
argument_list|(
literal|" ["
argument_list|)
operator|.
name|append
argument_list|(
name|key
argument_list|)
operator|.
name|append
argument_list|(
literal|" : "
argument_list|)
operator|.
name|append
argument_list|(
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
return|return
name|remainingFields
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|extractMapping
specifier|private
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|extractMapping
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|root
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
operator|.
name|createParser
argument_list|(
name|xContentRegistry
argument_list|,
name|source
argument_list|)
init|)
block|{
name|root
operator|=
name|parser
operator|.
name|mapOrdered
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"failed to parse mapping definition"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|extractMapping
argument_list|(
name|type
argument_list|,
name|root
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|extractMapping
specifier|private
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|extractMapping
parameter_list|(
name|String
name|type
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|root
parameter_list|)
throws|throws
name|MapperParsingException
block|{
if|if
condition|(
name|root
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// if we don't have any keys throw an exception
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"malformed mapping no root object found"
argument_list|)
throw|;
block|}
name|String
name|rootName
init|=
name|root
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|mapping
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
operator|||
name|type
operator|.
name|equals
argument_list|(
name|rootName
argument_list|)
condition|)
block|{
name|mapping
operator|=
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|rootName
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|root
operator|.
name|get
argument_list|(
name|rootName
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mapping
operator|=
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|type
argument_list|,
name|root
argument_list|)
expr_stmt|;
block|}
return|return
name|mapping
return|;
block|}
DECL|method|getXContentRegistry
name|NamedXContentRegistry
name|getXContentRegistry
parameter_list|()
block|{
return|return
name|xContentRegistry
return|;
block|}
block|}
end_class

end_unit

