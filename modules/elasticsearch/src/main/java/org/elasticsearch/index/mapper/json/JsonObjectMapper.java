begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|json
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
name|codehaus
operator|.
name|jackson
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonToken
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|InternalMapper
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
name|concurrent
operator|.
name|ThreadSafe
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
name|joda
operator|.
name|FormatDateTimeFormatter
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
name|json
operator|.
name|JsonBuilder
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|json
operator|.
name|JsonMapperBuilders
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
annotation|@
name|ThreadSafe
DECL|class|JsonObjectMapper
specifier|public
class|class
name|JsonObjectMapper
implements|implements
name|JsonMapper
block|{
DECL|field|JSON_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|JSON_TYPE
init|=
literal|"object"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|ENABLED
specifier|public
specifier|static
specifier|final
name|boolean
name|ENABLED
init|=
literal|true
decl_stmt|;
DECL|field|DYNAMIC
specifier|public
specifier|static
specifier|final
name|boolean
name|DYNAMIC
init|=
literal|true
decl_stmt|;
DECL|field|PATH_TYPE
specifier|public
specifier|static
specifier|final
name|JsonPath
operator|.
name|Type
name|PATH_TYPE
init|=
name|JsonPath
operator|.
name|Type
operator|.
name|FULL
decl_stmt|;
DECL|field|DATE_TIME_FORMATTERS
specifier|public
specifier|static
specifier|final
name|FormatDateTimeFormatter
index|[]
name|DATE_TIME_FORMATTERS
init|=
operator|new
name|FormatDateTimeFormatter
index|[]
block|{
name|JsonDateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
block|}
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|JsonMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|JsonObjectMapper
argument_list|>
block|{
DECL|field|enabled
specifier|private
name|boolean
name|enabled
init|=
name|Defaults
operator|.
name|ENABLED
decl_stmt|;
DECL|field|dynamic
specifier|private
name|boolean
name|dynamic
init|=
name|Defaults
operator|.
name|DYNAMIC
decl_stmt|;
DECL|field|pathType
specifier|private
name|JsonPath
operator|.
name|Type
name|pathType
init|=
name|Defaults
operator|.
name|PATH_TYPE
decl_stmt|;
DECL|field|dateTimeFormatters
specifier|private
name|List
argument_list|<
name|FormatDateTimeFormatter
argument_list|>
name|dateTimeFormatters
init|=
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|mappersBuilders
specifier|private
specifier|final
name|List
argument_list|<
name|JsonMapper
operator|.
name|Builder
argument_list|>
name|mappersBuilders
init|=
name|newArrayList
argument_list|()
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
DECL|method|enabled
specifier|public
name|Builder
name|enabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|enabled
operator|=
name|enabled
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|dynamic
specifier|public
name|Builder
name|dynamic
parameter_list|(
name|boolean
name|dynamic
parameter_list|)
block|{
name|this
operator|.
name|dynamic
operator|=
name|dynamic
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|pathType
specifier|public
name|Builder
name|pathType
parameter_list|(
name|JsonPath
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
DECL|method|noDateTimeFormatter
specifier|public
name|Builder
name|noDateTimeFormatter
parameter_list|()
block|{
name|this
operator|.
name|dateTimeFormatters
operator|=
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|dateTimeFormatter
specifier|public
name|Builder
name|dateTimeFormatter
parameter_list|(
name|Iterable
argument_list|<
name|FormatDateTimeFormatter
argument_list|>
name|dateTimeFormatters
parameter_list|)
block|{
for|for
control|(
name|FormatDateTimeFormatter
name|dateTimeFormatter
range|:
name|dateTimeFormatters
control|)
block|{
name|this
operator|.
name|dateTimeFormatters
operator|.
name|add
argument_list|(
name|dateTimeFormatter
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|dateTimeFormatter
specifier|public
name|Builder
name|dateTimeFormatter
parameter_list|(
name|FormatDateTimeFormatter
index|[]
name|dateTimeFormatters
parameter_list|)
block|{
name|this
operator|.
name|dateTimeFormatters
operator|.
name|addAll
argument_list|(
name|newArrayList
argument_list|(
name|dateTimeFormatters
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|dateTimeFormatter
specifier|public
name|Builder
name|dateTimeFormatter
parameter_list|(
name|FormatDateTimeFormatter
name|dateTimeFormatter
parameter_list|)
block|{
name|this
operator|.
name|dateTimeFormatters
operator|.
name|add
argument_list|(
name|dateTimeFormatter
argument_list|)
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
name|JsonMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|mappersBuilders
operator|.
name|add
argument_list|(
name|builder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
annotation|@
name|Override
specifier|public
name|JsonObjectMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|dateTimeFormatters
operator|==
literal|null
condition|)
block|{
name|dateTimeFormatters
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|dateTimeFormatters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// add the default one
name|dateTimeFormatters
operator|.
name|addAll
argument_list|(
name|newArrayList
argument_list|(
name|Defaults
operator|.
name|DATE_TIME_FORMATTERS
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|JsonPath
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
name|JsonMapper
argument_list|>
name|mappers
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|JsonMapper
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|JsonMapper
operator|.
name|Builder
name|builder
range|:
name|mappersBuilders
control|)
block|{
name|JsonMapper
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
name|JsonObjectMapper
name|objectMapper
init|=
operator|new
name|JsonObjectMapper
argument_list|(
name|name
argument_list|,
name|enabled
argument_list|,
name|dynamic
argument_list|,
name|pathType
argument_list|,
name|dateTimeFormatters
operator|.
name|toArray
argument_list|(
operator|new
name|FormatDateTimeFormatter
index|[
name|dateTimeFormatters
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|mappers
argument_list|)
decl_stmt|;
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
name|context
operator|.
name|path
argument_list|()
operator|.
name|remove
argument_list|()
expr_stmt|;
return|return
name|objectMapper
return|;
block|}
block|}
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|enabled
specifier|private
specifier|final
name|boolean
name|enabled
decl_stmt|;
DECL|field|dynamic
specifier|private
specifier|final
name|boolean
name|dynamic
decl_stmt|;
DECL|field|pathType
specifier|private
specifier|final
name|JsonPath
operator|.
name|Type
name|pathType
decl_stmt|;
DECL|field|dateTimeFormatters
specifier|private
specifier|final
name|FormatDateTimeFormatter
index|[]
name|dateTimeFormatters
decl_stmt|;
DECL|field|mappers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|JsonMapper
argument_list|>
name|mappers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
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
DECL|method|JsonObjectMapper
specifier|protected
name|JsonObjectMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|Defaults
operator|.
name|ENABLED
argument_list|,
name|Defaults
operator|.
name|DYNAMIC
argument_list|,
name|Defaults
operator|.
name|PATH_TYPE
argument_list|)
expr_stmt|;
block|}
DECL|method|JsonObjectMapper
specifier|protected
name|JsonObjectMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|enabled
parameter_list|,
name|boolean
name|dynamic
parameter_list|,
name|JsonPath
operator|.
name|Type
name|pathType
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|enabled
argument_list|,
name|dynamic
argument_list|,
name|pathType
argument_list|,
name|Defaults
operator|.
name|DATE_TIME_FORMATTERS
argument_list|)
expr_stmt|;
block|}
DECL|method|JsonObjectMapper
specifier|protected
name|JsonObjectMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|enabled
parameter_list|,
name|boolean
name|dynamic
parameter_list|,
name|JsonPath
operator|.
name|Type
name|pathType
parameter_list|,
name|FormatDateTimeFormatter
index|[]
name|dateTimeFormatters
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|enabled
argument_list|,
name|dynamic
argument_list|,
name|pathType
argument_list|,
name|dateTimeFormatters
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|JsonObjectMapper
name|JsonObjectMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|enabled
parameter_list|,
name|boolean
name|dynamic
parameter_list|,
name|JsonPath
operator|.
name|Type
name|pathType
parameter_list|,
name|FormatDateTimeFormatter
index|[]
name|dateTimeFormatters
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|JsonMapper
argument_list|>
name|mappers
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
name|enabled
operator|=
name|enabled
expr_stmt|;
name|this
operator|.
name|dynamic
operator|=
name|dynamic
expr_stmt|;
name|this
operator|.
name|pathType
operator|=
name|pathType
expr_stmt|;
name|this
operator|.
name|dateTimeFormatters
operator|=
name|dateTimeFormatters
expr_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|mappers
operator|=
name|copyOf
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
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
DECL|method|putMapper
specifier|public
name|JsonObjectMapper
name|putMapper
parameter_list|(
name|JsonMapper
name|mapper
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|mappers
operator|=
name|newMapBuilder
argument_list|(
name|mappers
argument_list|)
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
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
return|return
name|this
return|;
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
for|for
control|(
name|JsonMapper
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
DECL|method|parse
specifier|public
name|void
name|parse
parameter_list|(
name|JsonParseContext
name|jsonContext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
name|jsonContext
operator|.
name|jp
argument_list|()
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
return|return;
block|}
name|JsonParser
name|jp
init|=
name|jsonContext
operator|.
name|jp
argument_list|()
decl_stmt|;
name|JsonPath
operator|.
name|Type
name|origPathType
init|=
name|jsonContext
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|()
decl_stmt|;
name|jsonContext
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|pathType
argument_list|)
expr_stmt|;
name|String
name|currentFieldName
init|=
name|jp
operator|.
name|getCurrentName
argument_list|()
decl_stmt|;
name|JsonToken
name|token
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
name|serializeObject
argument_list|(
name|jsonContext
argument_list|,
name|currentFieldName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|START_ARRAY
condition|)
block|{
name|serializeArray
argument_list|(
name|jsonContext
argument_list|,
name|currentFieldName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|jp
operator|.
name|getCurrentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_NULL
condition|)
block|{
name|serializeNullValue
argument_list|(
name|jsonContext
argument_list|,
name|currentFieldName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serializeValue
argument_list|(
name|jsonContext
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|)
expr_stmt|;
block|}
block|}
comment|// restore the enable path flag
name|jsonContext
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
DECL|method|serializeNullValue
specifier|private
name|void
name|serializeNullValue
parameter_list|(
name|JsonParseContext
name|jsonContext
parameter_list|,
name|String
name|lastFieldName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we can only handle null values if we have mappings for them
name|JsonMapper
name|mapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|lastFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|mapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|serializeObject
specifier|private
name|void
name|serializeObject
parameter_list|(
name|JsonParseContext
name|jsonContext
parameter_list|,
name|String
name|currentFieldName
parameter_list|)
throws|throws
name|IOException
block|{
name|jsonContext
operator|.
name|path
argument_list|()
operator|.
name|add
argument_list|(
name|currentFieldName
argument_list|)
expr_stmt|;
name|JsonMapper
name|objectMapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|currentFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|objectMapper
operator|!=
literal|null
condition|)
block|{
name|objectMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|dynamic
condition|)
block|{
comment|// we sync here just so we won't add it twice. Its not the end of the world
comment|// to sync here since next operations will get it before
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|objectMapper
operator|=
name|mappers
operator|.
name|get
argument_list|(
name|currentFieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|objectMapper
operator|!=
literal|null
condition|)
block|{
name|objectMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
name|BuilderContext
name|builderContext
init|=
operator|new
name|BuilderContext
argument_list|(
name|jsonContext
operator|.
name|path
argument_list|()
argument_list|)
decl_stmt|;
name|objectMapper
operator|=
name|JsonMapperBuilders
operator|.
name|object
argument_list|(
name|currentFieldName
argument_list|)
operator|.
name|enabled
argument_list|(
literal|true
argument_list|)
operator|.
name|dynamic
argument_list|(
name|dynamic
argument_list|)
operator|.
name|pathType
argument_list|(
name|pathType
argument_list|)
operator|.
name|dateTimeFormatter
argument_list|(
name|dateTimeFormatters
argument_list|)
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
name|putMapper
argument_list|(
name|objectMapper
argument_list|)
expr_stmt|;
name|objectMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
name|jsonContext
operator|.
name|addedMapper
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// not dynamic, read everything up to end object
name|jsonContext
operator|.
name|jp
argument_list|()
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
name|jsonContext
operator|.
name|path
argument_list|()
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
DECL|method|serializeArray
specifier|private
name|void
name|serializeArray
parameter_list|(
name|JsonParseContext
name|jsonContext
parameter_list|,
name|String
name|lastFieldName
parameter_list|)
throws|throws
name|IOException
block|{
name|JsonParser
name|jp
init|=
name|jsonContext
operator|.
name|jp
argument_list|()
decl_stmt|;
name|JsonToken
name|token
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_ARRAY
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
name|serializeObject
argument_list|(
name|jsonContext
argument_list|,
name|lastFieldName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|START_ARRAY
condition|)
block|{
name|serializeArray
argument_list|(
name|jsonContext
argument_list|,
name|lastFieldName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
name|lastFieldName
operator|=
name|jp
operator|.
name|getCurrentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_NULL
condition|)
block|{
name|serializeNullValue
argument_list|(
name|jsonContext
argument_list|,
name|lastFieldName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serializeValue
argument_list|(
name|jsonContext
argument_list|,
name|lastFieldName
argument_list|,
name|token
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|serializeValue
specifier|private
name|void
name|serializeValue
parameter_list|(
name|JsonParseContext
name|jsonContext
parameter_list|,
name|String
name|currentFieldName
parameter_list|,
name|JsonToken
name|token
parameter_list|)
throws|throws
name|IOException
block|{
name|JsonMapper
name|mapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|currentFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|mapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|dynamic
condition|)
block|{
return|return;
block|}
comment|// we sync here since we don't want to add this field twice to the document mapper
comment|// its not the end of the world, since we add it to the mappers once we create it
comment|// so next time we won't even get here for this field
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|mapper
operator|=
name|mappers
operator|.
name|get
argument_list|(
name|currentFieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|mapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
return|return;
block|}
name|BuilderContext
name|builderContext
init|=
operator|new
name|BuilderContext
argument_list|(
name|jsonContext
operator|.
name|path
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
condition|)
block|{
comment|// check if it fits one of the date formats
name|boolean
name|isDate
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FormatDateTimeFormatter
name|dateTimeFormatter
range|:
name|dateTimeFormatters
control|)
block|{
try|try
block|{
name|dateTimeFormatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
name|jsonContext
operator|.
name|jp
argument_list|()
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|mapper
operator|=
name|dateField
argument_list|(
name|currentFieldName
argument_list|)
operator|.
name|dateTimeFormatter
argument_list|(
name|dateTimeFormatter
argument_list|)
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
name|isDate
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// failure to parse this, continue
block|}
block|}
if|if
condition|(
operator|!
name|isDate
condition|)
block|{
name|mapper
operator|=
name|stringField
argument_list|(
name|currentFieldName
argument_list|)
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_NUMBER_INT
condition|)
block|{
name|mapper
operator|=
name|longField
argument_list|(
name|currentFieldName
argument_list|)
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_NUMBER_FLOAT
condition|)
block|{
name|mapper
operator|=
name|doubleField
argument_list|(
name|currentFieldName
argument_list|)
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_TRUE
condition|)
block|{
name|mapper
operator|=
name|booleanField
argument_list|(
name|currentFieldName
argument_list|)
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_FALSE
condition|)
block|{
name|mapper
operator|=
name|booleanField
argument_list|(
name|currentFieldName
argument_list|)
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO how do we identify dynamically that its a binary value?
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Can't handle serializing a dynamic type with json token ["
operator|+
name|token
operator|+
literal|"] and field name ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|putMapper
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
name|jsonContext
operator|.
name|docMapper
argument_list|()
operator|.
name|addFieldMapper
argument_list|(
operator|(
name|FieldMapper
operator|)
name|mapper
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
name|jsonContext
operator|.
name|addedMapper
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|merge
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|JsonMapper
name|mergeWith
parameter_list|,
name|JsonMergeContext
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
name|JsonObjectMapper
operator|)
condition|)
block|{
name|mergeContext
operator|.
name|addConflict
argument_list|(
literal|"Can't merge a non object mapping ["
operator|+
name|mergeWith
operator|.
name|name
argument_list|()
operator|+
literal|"] with an object mapping ["
operator|+
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
return|return;
block|}
name|JsonObjectMapper
name|mergeWithObject
init|=
operator|(
name|JsonObjectMapper
operator|)
name|mergeWith
decl_stmt|;
synchronized|synchronized
init|(
name|mutex
init|)
block|{
for|for
control|(
name|JsonMapper
name|mergeWithMapper
range|:
name|mergeWithObject
operator|.
name|mappers
operator|.
name|values
argument_list|()
control|)
block|{
name|JsonMapper
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
name|putMapper
argument_list|(
name|mergeWithMapper
argument_list|)
expr_stmt|;
if|if
condition|(
name|mergeWithMapper
operator|instanceof
name|JsonFieldMapper
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
if|if
condition|(
operator|(
name|mergeWithMapper
operator|instanceof
name|JsonMultiFieldMapper
operator|)
operator|&&
operator|!
operator|(
name|mergeIntoMapper
operator|instanceof
name|JsonMultiFieldMapper
operator|)
condition|)
block|{
name|JsonMultiFieldMapper
name|mergeWithMultiField
init|=
operator|(
name|JsonMultiFieldMapper
operator|)
name|mergeWithMapper
decl_stmt|;
name|mergeWithMultiField
operator|.
name|merge
argument_list|(
name|mergeIntoMapper
argument_list|,
name|mergeContext
argument_list|)
expr_stmt|;
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
name|putMapper
argument_list|(
name|mergeWithMultiField
argument_list|)
expr_stmt|;
comment|// now, raise events for all mappers
for|for
control|(
name|JsonMapper
name|mapper
range|:
name|mergeWithMultiField
operator|.
name|mappers
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|mapper
operator|instanceof
name|JsonFieldMapper
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
name|mapper
argument_list|)
expr_stmt|;
block|}
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
block|}
DECL|method|toJson
annotation|@
name|Override
specifier|public
name|void
name|toJson
parameter_list|(
name|JsonBuilder
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
name|JSON_TYPE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"dynamic"
argument_list|,
name|dynamic
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
name|enabled
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"pathType"
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
if|if
condition|(
name|dateTimeFormatters
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"dateFormats"
argument_list|)
expr_stmt|;
for|for
control|(
name|FormatDateTimeFormatter
name|dateTimeFormatter
range|:
name|dateTimeFormatters
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|dateTimeFormatter
operator|.
name|format
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
comment|// check internal mappers first (this is only relevant for root object)
for|for
control|(
name|JsonMapper
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
name|InternalMapper
condition|)
block|{
name|mapper
operator|.
name|toJson
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|mappers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
expr_stmt|;
for|for
control|(
name|JsonMapper
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
operator|!
operator|(
name|mapper
operator|instanceof
name|InternalMapper
operator|)
condition|)
block|{
name|mapper
operator|.
name|toJson
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
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

