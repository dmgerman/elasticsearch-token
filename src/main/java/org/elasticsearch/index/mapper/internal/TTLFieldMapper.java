begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Fieldable
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
name|unit
operator|.
name|TimeValue
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
name|AlreadyExpiredException
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
name|LongFieldMapper
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
name|NumberFieldMapper
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
name|Date
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
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
operator|.
name|nodeBooleanValue
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
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
operator|.
name|nodeTimeValue
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

begin_class
DECL|class|TTLFieldMapper
specifier|public
class|class
name|TTLFieldMapper
extends|extends
name|LongFieldMapper
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
literal|"_ttl"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_ttl"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|LongFieldMapper
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
name|TTLFieldMapper
operator|.
name|CONTENT_TYPE
decl_stmt|;
DECL|field|STORE
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Store
name|STORE
init|=
name|Field
operator|.
name|Store
operator|.
name|YES
decl_stmt|;
DECL|field|INDEX
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Index
name|INDEX
init|=
name|Field
operator|.
name|Index
operator|.
name|NOT_ANALYZED
decl_stmt|;
DECL|field|ENABLED
specifier|public
specifier|static
specifier|final
name|boolean
name|ENABLED
init|=
literal|false
decl_stmt|;
DECL|field|DEFAULT
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT
init|=
operator|-
literal|1
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|NumberFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|TTLFieldMapper
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
DECL|field|defaultTTL
specifier|private
name|long
name|defaultTTL
init|=
name|Defaults
operator|.
name|DEFAULT
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
argument_list|)
expr_stmt|;
name|store
operator|=
name|Defaults
operator|.
name|STORE
expr_stmt|;
name|index
operator|=
name|Defaults
operator|.
name|INDEX
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
name|builder
return|;
block|}
DECL|method|defaultTTL
specifier|public
name|Builder
name|defaultTTL
parameter_list|(
name|long
name|defaultTTL
parameter_list|)
block|{
name|this
operator|.
name|defaultTTL
operator|=
name|defaultTTL
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|TTLFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|TTLFieldMapper
argument_list|(
name|store
argument_list|,
name|index
argument_list|,
name|enabled
argument_list|,
name|defaultTTL
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
name|TTLFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|TTLFieldMapper
operator|.
name|Builder
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
literal|"enabled"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|enabled
argument_list|(
name|nodeBooleanValue
argument_list|(
name|fieldNode
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
literal|"default"
argument_list|)
condition|)
block|{
name|TimeValue
name|ttlTimeValue
init|=
name|nodeTimeValue
argument_list|(
name|fieldNode
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|ttlTimeValue
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|defaultTTL
argument_list|(
name|ttlTimeValue
operator|.
name|millis
argument_list|()
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
DECL|field|enabled
specifier|private
name|boolean
name|enabled
decl_stmt|;
DECL|field|defaultTTL
specifier|private
name|long
name|defaultTTL
decl_stmt|;
DECL|method|TTLFieldMapper
specifier|public
name|TTLFieldMapper
parameter_list|()
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|STORE
argument_list|,
name|Defaults
operator|.
name|INDEX
argument_list|,
name|Defaults
operator|.
name|ENABLED
argument_list|,
name|Defaults
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|TTLFieldMapper
specifier|protected
name|TTLFieldMapper
parameter_list|(
name|Field
operator|.
name|Store
name|store
parameter_list|,
name|Field
operator|.
name|Index
name|index
parameter_list|,
name|boolean
name|enabled
parameter_list|,
name|long
name|defaultTTL
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|Names
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|NAME
argument_list|)
argument_list|,
name|Defaults
operator|.
name|PRECISION_STEP
argument_list|,
name|Defaults
operator|.
name|FUZZY_FACTOR
argument_list|,
name|index
argument_list|,
name|store
argument_list|,
name|Defaults
operator|.
name|BOOST
argument_list|,
name|Defaults
operator|.
name|OMIT_NORMS
argument_list|,
name|Defaults
operator|.
name|OMIT_TERM_FREQ_AND_POSITIONS
argument_list|,
name|Defaults
operator|.
name|NULL_VALUE
argument_list|)
expr_stmt|;
name|this
operator|.
name|enabled
operator|=
name|enabled
expr_stmt|;
name|this
operator|.
name|defaultTTL
operator|=
name|defaultTTL
expr_stmt|;
block|}
DECL|method|enabled
specifier|public
name|boolean
name|enabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|enabled
return|;
block|}
DECL|method|defaultTTL
specifier|public
name|long
name|defaultTTL
parameter_list|()
block|{
return|return
name|this
operator|.
name|defaultTTL
return|;
block|}
comment|// Overrides valueForSearch to display live value of remaining ttl
annotation|@
name|Override
DECL|method|valueForSearch
specifier|public
name|Object
name|valueForSearch
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
name|long
name|now
decl_stmt|;
name|SearchContext
name|searchContext
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchContext
operator|!=
literal|null
condition|)
block|{
name|now
operator|=
name|searchContext
operator|.
name|nowInMillis
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|now
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
name|long
name|value
init|=
name|value
argument_list|(
name|field
argument_list|)
decl_stmt|;
return|return
name|value
operator|-
name|now
return|;
block|}
comment|// Other implementation for realtime get display
DECL|method|valueForSearch
specifier|public
name|Object
name|valueForSearch
parameter_list|(
name|long
name|expirationTime
parameter_list|)
block|{
return|return
name|expirationTime
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
return|;
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
block|{     }
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
throws|,
name|MapperParsingException
block|{
if|if
condition|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|ttl
argument_list|()
operator|<
literal|0
condition|)
block|{
comment|// no ttl has been provided externally
name|long
name|ttl
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|parser
argument_list|()
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|ttl
operator|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|context
operator|.
name|parser
argument_list|()
operator|.
name|text
argument_list|()
argument_list|,
literal|null
argument_list|)
operator|.
name|millis
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|ttl
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|ttl
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"TTL value must be> 0. Illegal value provided ["
operator|+
name|ttl
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|ttl
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
block|}
block|}
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
name|Fieldable
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|AlreadyExpiredException
block|{
if|if
condition|(
name|enabled
condition|)
block|{
name|long
name|ttl
init|=
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|ttl
argument_list|()
decl_stmt|;
if|if
condition|(
name|ttl
operator|<=
literal|0
operator|&&
name|defaultTTL
operator|>
literal|0
condition|)
block|{
comment|// no ttl provided so we use the default value
name|ttl
operator|=
name|defaultTTL
expr_stmt|;
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|ttl
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ttl
operator|>
literal|0
condition|)
block|{
comment|// a ttl has been provided either externally or in the _source
name|long
name|timestamp
init|=
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|timestamp
argument_list|()
decl_stmt|;
name|long
name|expire
init|=
operator|new
name|Date
argument_list|(
name|timestamp
operator|+
name|ttl
argument_list|)
operator|.
name|getTime
argument_list|()
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// there is not point indexing already expired doc
if|if
condition|(
name|now
operator|>=
name|expire
condition|)
block|{
throw|throw
operator|new
name|AlreadyExpiredException
argument_list|(
name|context
operator|.
name|index
argument_list|()
argument_list|,
name|context
operator|.
name|type
argument_list|()
argument_list|,
name|context
operator|.
name|id
argument_list|()
argument_list|,
name|timestamp
argument_list|,
name|ttl
argument_list|,
name|now
argument_list|)
throw|;
block|}
comment|// the expiration timestamp (timestamp + ttl) is set as field
return|return
operator|new
name|CustomLongNumericField
argument_list|(
name|this
argument_list|,
name|expire
argument_list|)
return|;
block|}
block|}
return|return
literal|null
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
comment|// if all are defaults, no sense to write it at all
if|if
condition|(
name|enabled
operator|==
name|Defaults
operator|.
name|ENABLED
operator|&&
name|defaultTTL
operator|==
name|Defaults
operator|.
name|DEFAULT
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
name|enabled
operator|!=
name|Defaults
operator|.
name|ENABLED
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
name|enabled
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|defaultTTL
operator|!=
name|Defaults
operator|.
name|DEFAULT
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"default"
argument_list|,
name|defaultTTL
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

