begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|xcontent
package|;
end_package

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
name|FieldMapper
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
name|FieldMapperListener
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
name|MapperParsingException
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
name|MergeMappingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
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
name|util
operator|.
name|xcontent
operator|.
name|builder
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentMapperBuilders
operator|.
name|*
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
name|xcontent
operator|.
name|XContentTypeParsers
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|MapBuilder
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|XContentMultiFieldMapper
specifier|public
class|class
name|XContentMultiFieldMapper
implements|implements
name|XContentMapper
implements|,
name|XContentIncludeInAllMapper
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
name|XContentMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|XContentMultiFieldMapper
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
name|XContentMapper
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
name|XContentMapper
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
name|XContentMapper
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
DECL|method|build
annotation|@
name|Override
specifier|public
name|XContentMultiFieldMapper
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
name|XContentMapper
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
name|XContentMapper
argument_list|>
name|mappers
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|XContentMapper
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|XContentMapper
operator|.
name|Builder
name|builder
range|:
name|mappersBuilders
control|)
block|{
name|XContentMapper
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
name|pathType
argument_list|(
name|origPathType
argument_list|)
expr_stmt|;
return|return
operator|new
name|XContentMultiFieldMapper
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
name|XContentTypeParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|XContentMapper
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
name|XContentMultiFieldMapper
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
name|XContentTypeParser
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
name|XContentMapper
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
name|XContentMapper
name|defaultMapper
decl_stmt|;
DECL|method|XContentMultiFieldMapper
specifier|public
name|XContentMultiFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|,
name|XContentMapper
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
name|XContentMapper
argument_list|>
argument_list|()
argument_list|,
name|defaultMapper
argument_list|)
expr_stmt|;
block|}
DECL|method|XContentMultiFieldMapper
specifier|public
name|XContentMultiFieldMapper
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
name|XContentMapper
argument_list|>
name|mappers
parameter_list|,
name|XContentMapper
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
name|XContentMapper
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
name|XContentIncludeInAllMapper
condition|)
block|{
operator|(
operator|(
name|XContentIncludeInAllMapper
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
DECL|method|name
annotation|@
name|Override
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
DECL|method|includeInAll
annotation|@
name|Override
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
name|XContentIncludeInAllMapper
operator|)
condition|)
block|{
operator|(
operator|(
name|XContentIncludeInAllMapper
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
name|XContentMapper
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
name|XContentMapper
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
DECL|method|parse
annotation|@
name|Override
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
name|XContentMapper
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
DECL|method|merge
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|XContentMapper
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
name|XContentMultiFieldMapper
operator|)
condition|)
block|{
name|mergeContext
operator|.
name|addConflict
argument_list|(
literal|"Can't merge a non multi_field mapping ["
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
name|XContentMultiFieldMapper
name|mergeWithMultiField
init|=
operator|(
name|XContentMultiFieldMapper
operator|)
name|mergeWith
decl_stmt|;
synchronized|synchronized
init|(
name|mutex
init|)
block|{
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
name|defaultMapper
operator|=
name|mergeWithMultiField
operator|.
name|defaultMapper
expr_stmt|;
name|mergeContext
operator|.
name|docMapper
argument_list|()
operator|.
name|addFieldMapper
argument_list|(
operator|(
name|FieldMapper
operator|)
name|defaultMapper
argument_list|)
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
name|XContentMapper
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
name|XContentMapper
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
name|XContentIncludeInAllMapper
condition|)
block|{
operator|(
operator|(
name|XContentIncludeInAllMapper
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
name|mappers
operator|=
name|newMapBuilder
argument_list|(
name|mappers
argument_list|)
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
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
if|if
condition|(
name|mergeWithMapper
operator|instanceof
name|XContentFieldMapper
condition|)
block|{
name|mergeContext
operator|.
name|docMapper
argument_list|()
operator|.
name|addFieldMapper
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
block|}
block|}
DECL|method|traverse
annotation|@
name|Override
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
name|XContentMapper
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
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
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
argument_list|()
argument_list|)
expr_stmt|;
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
for|for
control|(
name|XContentMapper
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
block|}
block|}
end_class

end_unit

