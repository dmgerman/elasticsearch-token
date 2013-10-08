begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.multifield
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|multifield
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
name|collect
operator|.
name|MapBuilder
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
name|mapper
operator|.
name|internal
operator|.
name|AllFieldMapper
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
name|*
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|MapBuilder
operator|.
name|newMapBuilder
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
name|multiField
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
name|parsePathType
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MultiFieldMapper
specifier|public
class|class
name|MultiFieldMapper
implements|implements
name|Mapper
implements|,
name|AllFieldMapper
operator|.
name|IncludeInAll
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"multi_field"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|PATH_TYPE
specifier|public
specifier|static
specifier|final
name|ContentPath
operator|.
name|Type
name|PATH_TYPE
init|=
name|ContentPath
operator|.
name|Type
operator|.
name|FULL
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|Mapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|MultiFieldMapper
argument_list|>
block|{
DECL|field|pathType
specifier|private
name|ContentPath
operator|.
name|Type
name|pathType
init|=
name|Defaults
operator|.
name|PATH_TYPE
decl_stmt|;
DECL|field|mappersBuilders
specifier|private
specifier|final
name|List
argument_list|<
name|Mapper
operator|.
name|Builder
argument_list|>
name|mappersBuilders
init|=
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|defaultMapperBuilder
specifier|private
name|Mapper
operator|.
name|Builder
name|defaultMapperBuilder
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|builder
operator|=
name|this
expr_stmt|;
block|}
DECL|method|pathType
specifier|public
name|Builder
name|pathType
parameter_list|(
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|)
block|{
name|this
operator|.
name|pathType
operator|=
name|pathType
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|Builder
name|add
parameter_list|(
name|Mapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
if|if
condition|(
name|builder
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|defaultMapperBuilder
operator|=
name|builder
expr_stmt|;
block|}
else|else
block|{
name|mappersBuilders
operator|.
name|add
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|MultiFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
name|ContentPath
operator|.
name|Type
name|origPathType
init|=
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|()
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|pathType
argument_list|)
expr_stmt|;
name|Mapper
name|defaultMapper
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|defaultMapperBuilder
operator|!=
literal|null
condition|)
block|{
name|defaultMapper
operator|=
name|defaultMapperBuilder
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
name|String
name|origSourcePath
init|=
name|context
operator|.
name|path
argument_list|()
operator|.
name|sourcePath
argument_list|(
name|context
operator|.
name|path
argument_list|()
operator|.
name|fullPathAsText
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
name|mappers
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Mapper
operator|.
name|Builder
name|builder
range|:
name|mappersBuilders
control|)
block|{
name|Mapper
name|mapper
init|=
name|builder
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|mappers
operator|.
name|put
argument_list|(
name|mapper
operator|.
name|name
argument_list|()
argument_list|,
name|mapper
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|path
argument_list|()
operator|.
name|remove
argument_list|()
expr_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|sourcePath
argument_list|(
name|origSourcePath
argument_list|)
expr_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|origPathType
argument_list|)
expr_stmt|;
return|return
operator|new
name|MultiFieldMapper
argument_list|(
name|name
argument_list|,
name|pathType
argument_list|,
name|mappers
argument_list|,
name|defaultMapper
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
name|MultiFieldMapper
operator|.
name|Builder
name|builder
init|=
name|multiField
argument_list|(
name|name
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
name|pathType
argument_list|(
name|parsePathType
argument_list|(
name|name
argument_list|,
name|fieldNode
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"fields"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|fieldsNode
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
name|entry1
range|:
name|fieldsNode
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|propName
init|=
name|entry1
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|propNode
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|entry1
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|type
decl_stmt|;
name|Object
name|typeNode
init|=
name|propNode
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeNode
operator|!=
literal|null
condition|)
block|{
name|type
operator|=
name|typeNode
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"No type specified for property ["
operator|+
name|propName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Mapper
operator|.
name|TypeParser
name|typeParser
init|=
name|parserContext
operator|.
name|typeParser
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeParser
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"No handler for type ["
operator|+
name|type
operator|+
literal|"] declared on field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|add
argument_list|(
name|typeParser
operator|.
name|parse
argument_list|(
name|propName
argument_list|,
name|propNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|pathType
specifier|private
specifier|final
name|ContentPath
operator|.
name|Type
name|pathType
decl_stmt|;
DECL|field|mutex
specifier|private
specifier|final
name|Object
name|mutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|mappers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
name|mappers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|defaultMapper
specifier|private
specifier|volatile
name|Mapper
name|defaultMapper
decl_stmt|;
DECL|method|MultiFieldMapper
specifier|public
name|MultiFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|,
name|Mapper
name|defaultMapper
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|pathType
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
argument_list|()
argument_list|,
name|defaultMapper
argument_list|)
expr_stmt|;
block|}
DECL|method|MultiFieldMapper
specifier|public
name|MultiFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
name|mappers
parameter_list|,
name|Mapper
name|defaultMapper
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|pathType
operator|=
name|pathType
expr_stmt|;
name|this
operator|.
name|mappers
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultMapper
operator|=
name|defaultMapper
expr_stmt|;
comment|// we disable the all in mappers, only the default one can be added
for|for
control|(
name|Mapper
name|mapper
range|:
name|mappers
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|mapper
operator|instanceof
name|AllFieldMapper
operator|.
name|IncludeInAll
condition|)
block|{
operator|(
operator|(
name|AllFieldMapper
operator|.
name|IncludeInAll
operator|)
name|mapper
operator|)
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
annotation|@
name|Override
DECL|method|includeInAll
specifier|public
name|void
name|includeInAll
parameter_list|(
name|Boolean
name|includeInAll
parameter_list|)
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
operator|&&
name|defaultMapper
operator|!=
literal|null
operator|&&
operator|(
name|defaultMapper
operator|instanceof
name|AllFieldMapper
operator|.
name|IncludeInAll
operator|)
condition|)
block|{
operator|(
operator|(
name|AllFieldMapper
operator|.
name|IncludeInAll
operator|)
name|defaultMapper
operator|)
operator|.
name|includeInAll
argument_list|(
name|includeInAll
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|includeInAllIfNotSet
specifier|public
name|void
name|includeInAllIfNotSet
parameter_list|(
name|Boolean
name|includeInAll
parameter_list|)
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
operator|&&
name|defaultMapper
operator|!=
literal|null
operator|&&
operator|(
name|defaultMapper
operator|instanceof
name|AllFieldMapper
operator|.
name|IncludeInAll
operator|)
condition|)
block|{
operator|(
operator|(
name|AllFieldMapper
operator|.
name|IncludeInAll
operator|)
name|defaultMapper
operator|)
operator|.
name|includeInAllIfNotSet
argument_list|(
name|includeInAll
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|pathType
specifier|public
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|()
block|{
return|return
name|pathType
return|;
block|}
DECL|method|defaultMapper
specifier|public
name|Mapper
name|defaultMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|defaultMapper
return|;
block|}
DECL|method|mappers
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
name|mappers
parameter_list|()
block|{
return|return
name|this
operator|.
name|mappers
return|;
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
name|ContentPath
operator|.
name|Type
name|origPathType
init|=
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|()
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|pathType
argument_list|)
expr_stmt|;
comment|// do the default mapper without adding the path
if|if
condition|(
name|defaultMapper
operator|!=
literal|null
condition|)
block|{
name|defaultMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|path
argument_list|()
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
for|for
control|(
name|Mapper
name|mapper
range|:
name|mappers
operator|.
name|values
argument_list|()
control|)
block|{
name|mapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|path
argument_list|()
operator|.
name|remove
argument_list|()
expr_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|origPathType
argument_list|)
expr_stmt|;
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
if|if
condition|(
operator|!
operator|(
name|mergeWith
operator|instanceof
name|MultiFieldMapper
operator|)
operator|&&
operator|!
operator|(
name|mergeWith
operator|instanceof
name|AbstractFieldMapper
operator|)
condition|)
block|{
name|mergeContext
operator|.
name|addConflict
argument_list|(
literal|"Can't merge a non multi_field / non simple mapping ["
operator|+
name|mergeWith
operator|.
name|name
argument_list|()
operator|+
literal|"] with a multi_field mapping ["
operator|+
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
return|return;
block|}
synchronized|synchronized
init|(
name|mutex
init|)
block|{
if|if
condition|(
name|mergeWith
operator|instanceof
name|AbstractFieldMapper
condition|)
block|{
comment|// its a single field mapper, upgraded into a multi field mapper, just update the default mapper
if|if
condition|(
name|defaultMapper
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|mergeContext
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|()
condition|)
block|{
name|mergeContext
operator|.
name|docMapper
argument_list|()
operator|.
name|addFieldMappers
argument_list|(
operator|(
name|FieldMapper
operator|)
name|defaultMapper
argument_list|)
expr_stmt|;
name|defaultMapper
operator|=
name|mergeWith
expr_stmt|;
comment|// only set& expose it after adding fieldmapper
block|}
block|}
block|}
else|else
block|{
name|MultiFieldMapper
name|mergeWithMultiField
init|=
operator|(
name|MultiFieldMapper
operator|)
name|mergeWith
decl_stmt|;
name|List
argument_list|<
name|FieldMapper
argument_list|>
name|newFieldMappers
init|=
literal|null
decl_stmt|;
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
name|newMappersBuilder
init|=
literal|null
decl_stmt|;
name|Mapper
name|newDefaultMapper
init|=
literal|null
decl_stmt|;
comment|// merge the default mapper
if|if
condition|(
name|defaultMapper
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|mergeWithMultiField
operator|.
name|defaultMapper
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|mergeContext
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|()
condition|)
block|{
if|if
condition|(
name|newFieldMappers
operator|==
literal|null
condition|)
block|{
name|newFieldMappers
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldMapper
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|newFieldMappers
operator|.
name|add
argument_list|(
operator|(
name|FieldMapper
operator|)
name|defaultMapper
argument_list|)
expr_stmt|;
name|newDefaultMapper
operator|=
name|mergeWithMultiField
operator|.
name|defaultMapper
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|mergeWithMultiField
operator|.
name|defaultMapper
operator|!=
literal|null
condition|)
block|{
name|defaultMapper
operator|.
name|merge
argument_list|(
name|mergeWithMultiField
operator|.
name|defaultMapper
argument_list|,
name|mergeContext
argument_list|)
expr_stmt|;
block|}
block|}
comment|// merge all the other mappers
for|for
control|(
name|Mapper
name|mergeWithMapper
range|:
name|mergeWithMultiField
operator|.
name|mappers
operator|.
name|values
argument_list|()
control|)
block|{
name|Mapper
name|mergeIntoMapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|mergeWithMapper
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mergeIntoMapper
operator|==
literal|null
condition|)
block|{
comment|// no mapping, simply add it if not simulating
if|if
condition|(
operator|!
name|mergeContext
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|()
condition|)
block|{
comment|// disable the mapper from being in all, only the default mapper is in all
if|if
condition|(
name|mergeWithMapper
operator|instanceof
name|AllFieldMapper
operator|.
name|IncludeInAll
condition|)
block|{
operator|(
operator|(
name|AllFieldMapper
operator|.
name|IncludeInAll
operator|)
name|mergeWithMapper
operator|)
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|newMappersBuilder
operator|==
literal|null
condition|)
block|{
name|newMappersBuilder
operator|=
name|newMapBuilder
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
block|}
name|newMappersBuilder
operator|.
name|put
argument_list|(
name|mergeWithMapper
operator|.
name|name
argument_list|()
argument_list|,
name|mergeWithMapper
argument_list|)
expr_stmt|;
if|if
condition|(
name|mergeWithMapper
operator|instanceof
name|AbstractFieldMapper
condition|)
block|{
if|if
condition|(
name|newFieldMappers
operator|==
literal|null
condition|)
block|{
name|newFieldMappers
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldMapper
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|newFieldMappers
operator|.
name|add
argument_list|(
operator|(
name|FieldMapper
operator|)
name|mergeWithMapper
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|mergeIntoMapper
operator|.
name|merge
argument_list|(
name|mergeWithMapper
argument_list|,
name|mergeContext
argument_list|)
expr_stmt|;
block|}
block|}
comment|// first add all field mappers
if|if
condition|(
name|newFieldMappers
operator|!=
literal|null
operator|&&
operator|!
name|newFieldMappers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mergeContext
operator|.
name|docMapper
argument_list|()
operator|.
name|addFieldMappers
argument_list|(
name|newFieldMappers
argument_list|)
expr_stmt|;
block|}
comment|// now publish mappers
if|if
condition|(
name|newDefaultMapper
operator|!=
literal|null
condition|)
block|{
name|defaultMapper
operator|=
name|newDefaultMapper
expr_stmt|;
block|}
if|if
condition|(
name|newMappersBuilder
operator|!=
literal|null
condition|)
block|{
name|mappers
operator|=
name|newMappersBuilder
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|defaultMapper
operator|!=
literal|null
condition|)
block|{
name|defaultMapper
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Mapper
name|mapper
range|:
name|mappers
operator|.
name|values
argument_list|()
control|)
block|{
name|mapper
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|traverse
specifier|public
name|void
name|traverse
parameter_list|(
name|FieldMapperListener
name|fieldMapperListener
parameter_list|)
block|{
if|if
condition|(
name|defaultMapper
operator|!=
literal|null
condition|)
block|{
name|defaultMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Mapper
name|mapper
range|:
name|mappers
operator|.
name|values
argument_list|()
control|)
block|{
name|mapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|traverse
specifier|public
name|void
name|traverse
parameter_list|(
name|ObjectMapperListener
name|objectMapperListener
parameter_list|)
block|{     }
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
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
name|pathType
operator|!=
name|Defaults
operator|.
name|PATH_TYPE
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"path"
argument_list|,
name|pathType
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
block|}
name|builder
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
expr_stmt|;
if|if
condition|(
name|defaultMapper
operator|!=
literal|null
condition|)
block|{
name|defaultMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mappers
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
for|for
control|(
name|Mapper
name|mapper
range|:
name|mappers
operator|.
name|values
argument_list|()
control|)
block|{
name|mapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// sort the mappers (by name) if there is more than one mapping
name|TreeMap
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
name|sortedMappers
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|Mapper
argument_list|>
argument_list|(
name|mappers
argument_list|)
decl_stmt|;
for|for
control|(
name|Mapper
name|mapper
range|:
name|sortedMappers
operator|.
name|values
argument_list|()
control|)
block|{
name|mapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

