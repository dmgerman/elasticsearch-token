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
name|NumericDocValuesField
import|;
end_import

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
name|Explicit
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
name|common
operator|.
name|joda
operator|.
name|Joda
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
name|DateFieldMapper
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|index
operator|.
name|mapper
operator|.
name|MapperBuilders
operator|.
name|timestamp
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
name|parseDateTimeFormatter
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
comment|/**  */
end_comment

begin_class
DECL|class|TimestampFieldMapper
specifier|public
class|class
name|TimestampFieldMapper
extends|extends
name|DateFieldMapper
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
literal|"_timestamp"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_timestamp"
decl_stmt|;
DECL|field|DEFAULT_DATE_TIME_FORMAT
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_DATE_TIME_FORMAT
init|=
literal|"dateOptionalTime"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|DateFieldMapper
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
literal|"_timestamp"
decl_stmt|;
DECL|field|PRE_20_FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|PRE_20_FIELD_TYPE
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
name|DateFieldMapper
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
name|setStored
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setIndexed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|PRE_20_FIELD_TYPE
operator|=
operator|new
name|FieldType
argument_list|(
name|FIELD_TYPE
argument_list|)
expr_stmt|;
name|PRE_20_FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|PRE_20_FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
DECL|field|ENABLED
specifier|public
specifier|static
specifier|final
name|EnabledAttributeMapper
name|ENABLED
init|=
name|EnabledAttributeMapper
operator|.
name|UNSET_DISABLED
decl_stmt|;
DECL|field|PATH
specifier|public
specifier|static
specifier|final
name|String
name|PATH
init|=
literal|null
decl_stmt|;
DECL|field|DATE_TIME_FORMATTER
specifier|public
specifier|static
specifier|final
name|FormatDateTimeFormatter
name|DATE_TIME_FORMATTER
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
name|DEFAULT_DATE_TIME_FORMAT
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_TIMESTAMP
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_TIMESTAMP
init|=
literal|"now"
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
name|TimestampFieldMapper
argument_list|>
block|{
DECL|field|enabledState
specifier|private
name|EnabledAttributeMapper
name|enabledState
init|=
name|EnabledAttributeMapper
operator|.
name|UNSET_DISABLED
decl_stmt|;
DECL|field|path
specifier|private
name|String
name|path
init|=
name|Defaults
operator|.
name|PATH
decl_stmt|;
DECL|field|dateTimeFormatter
specifier|private
name|FormatDateTimeFormatter
name|dateTimeFormatter
init|=
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
decl_stmt|;
DECL|field|defaultTimestamp
specifier|private
name|String
name|defaultTimestamp
init|=
name|Defaults
operator|.
name|DEFAULT_TIMESTAMP
decl_stmt|;
DECL|field|explicitStore
specifier|private
name|boolean
name|explicitStore
init|=
literal|false
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
argument_list|,
name|Defaults
operator|.
name|PRECISION_STEP_64_BIT
argument_list|)
expr_stmt|;
block|}
DECL|method|enabled
specifier|public
name|Builder
name|enabled
parameter_list|(
name|EnabledAttributeMapper
name|enabledState
parameter_list|)
block|{
name|this
operator|.
name|enabledState
operator|=
name|enabledState
expr_stmt|;
return|return
name|builder
return|;
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
name|dateTimeFormatter
operator|=
name|dateTimeFormatter
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|defaultTimestamp
specifier|public
name|Builder
name|defaultTimestamp
parameter_list|(
name|String
name|defaultTimestamp
parameter_list|)
block|{
name|this
operator|.
name|defaultTimestamp
operator|=
name|defaultTimestamp
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|store
specifier|public
name|Builder
name|store
parameter_list|(
name|boolean
name|store
parameter_list|)
block|{
name|explicitStore
operator|=
literal|true
expr_stmt|;
return|return
name|super
operator|.
name|store
argument_list|(
name|store
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|TimestampFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|explicitStore
operator|==
literal|false
operator|&&
name|context
operator|.
name|indexCreatedVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
condition|)
block|{
assert|assert
name|fieldType
operator|.
name|stored
argument_list|()
assert|;
name|fieldType
operator|.
name|setStored
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|boolean
name|roundCeil
init|=
name|Defaults
operator|.
name|ROUND_CEIL
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|indexSettings
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Settings
name|settings
init|=
name|context
operator|.
name|indexSettings
argument_list|()
decl_stmt|;
name|roundCeil
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"index.mapping.date.round_ceil"
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"index.mapping.date.parse_upper_inclusive"
argument_list|,
name|Defaults
operator|.
name|ROUND_CEIL
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TimestampFieldMapper
argument_list|(
name|fieldType
argument_list|,
name|docValues
argument_list|,
name|enabledState
argument_list|,
name|path
argument_list|,
name|dateTimeFormatter
argument_list|,
name|defaultTimestamp
argument_list|,
name|roundCeil
argument_list|,
name|ignoreMalformed
argument_list|(
name|context
argument_list|)
argument_list|,
name|coerce
argument_list|(
name|context
argument_list|)
argument_list|,
name|postingsProvider
argument_list|,
name|docValuesProvider
argument_list|,
name|normsLoading
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
name|TimestampFieldMapper
operator|.
name|Builder
name|builder
init|=
name|timestamp
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
name|EnabledAttributeMapper
name|enabledState
init|=
name|nodeBooleanValue
argument_list|(
name|fieldNode
argument_list|)
condition|?
name|EnabledAttributeMapper
operator|.
name|ENABLED
else|:
name|EnabledAttributeMapper
operator|.
name|DISABLED
decl_stmt|;
name|builder
operator|.
name|enabled
argument_list|(
name|enabledState
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
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"format"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|dateTimeFormatter
argument_list|(
name|parseDateTimeFormatter
argument_list|(
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
literal|"default"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|defaultTimestamp
argument_list|(
name|fieldNode
operator|==
literal|null
condition|?
literal|null
else|:
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
DECL|method|defaultFieldType
specifier|private
specifier|static
name|FieldType
name|defaultFieldType
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|Version
operator|.
name|indexCreated
argument_list|(
name|settings
argument_list|)
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
condition|?
name|Defaults
operator|.
name|FIELD_TYPE
else|:
name|Defaults
operator|.
name|PRE_20_FIELD_TYPE
return|;
block|}
DECL|field|enabledState
specifier|private
name|EnabledAttributeMapper
name|enabledState
decl_stmt|;
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|field|defaultTimestamp
specifier|private
specifier|final
name|String
name|defaultTimestamp
decl_stmt|;
DECL|field|defaultFieldType
specifier|private
specifier|final
name|FieldType
name|defaultFieldType
decl_stmt|;
DECL|method|TimestampFieldMapper
specifier|public
name|TimestampFieldMapper
parameter_list|(
name|Settings
name|indexSettings
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|FieldType
argument_list|(
name|defaultFieldType
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|,
name|Defaults
operator|.
name|ENABLED
argument_list|,
name|Defaults
operator|.
name|PATH
argument_list|,
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
argument_list|,
name|Defaults
operator|.
name|DEFAULT_TIMESTAMP
argument_list|,
name|Defaults
operator|.
name|ROUND_CEIL
argument_list|,
name|Defaults
operator|.
name|IGNORE_MALFORMED
argument_list|,
name|Defaults
operator|.
name|COERCE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|TimestampFieldMapper
specifier|protected
name|TimestampFieldMapper
parameter_list|(
name|FieldType
name|fieldType
parameter_list|,
name|Boolean
name|docValues
parameter_list|,
name|EnabledAttributeMapper
name|enabledState
parameter_list|,
name|String
name|path
parameter_list|,
name|FormatDateTimeFormatter
name|dateTimeFormatter
parameter_list|,
name|String
name|defaultTimestamp
parameter_list|,
name|boolean
name|roundCeil
parameter_list|,
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|ignoreMalformed
parameter_list|,
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|coerce
parameter_list|,
name|PostingsFormatProvider
name|postingsProvider
parameter_list|,
name|DocValuesFormatProvider
name|docValuesProvider
parameter_list|,
name|Loading
name|normsLoading
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
name|dateTimeFormatter
argument_list|,
name|Defaults
operator|.
name|PRECISION_STEP_64_BIT
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
name|NULL_VALUE
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
comment|/*always milliseconds*/
argument_list|,
name|roundCeil
argument_list|,
name|ignoreMalformed
argument_list|,
name|coerce
argument_list|,
name|postingsProvider
argument_list|,
name|docValuesProvider
argument_list|,
literal|null
argument_list|,
name|normsLoading
argument_list|,
name|fieldDataSettings
argument_list|,
name|indexSettings
argument_list|,
name|MultiFields
operator|.
name|empty
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|enabledState
operator|=
name|enabledState
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|defaultTimestamp
operator|=
name|defaultTimestamp
expr_stmt|;
name|this
operator|.
name|defaultFieldType
operator|=
name|defaultFieldType
argument_list|(
name|indexSettings
argument_list|)
expr_stmt|;
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
name|defaultFieldType
return|;
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
name|enabledState
operator|.
name|enabled
return|;
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
DECL|method|defaultTimestamp
specifier|public
name|String
name|defaultTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|defaultTimestamp
return|;
block|}
DECL|method|dateTimeFormatter
specifier|public
name|FormatDateTimeFormatter
name|dateTimeFormatter
parameter_list|()
block|{
return|return
name|this
operator|.
name|dateTimeFormatter
return|;
block|}
comment|/**      * Override the default behavior to return a timestamp      */
annotation|@
name|Override
DECL|method|valueForSearch
specifier|public
name|Object
name|valueForSearch
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|value
argument_list|(
name|value
argument_list|)
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
block|{     }
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
comment|// nothing to do here, we call the parent in preParse
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
DECL|method|innerParseCreateField
specifier|protected
name|void
name|innerParseCreateField
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
if|if
condition|(
name|enabledState
operator|.
name|enabled
condition|)
block|{
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
if|if
condition|(
operator|!
name|fieldType
operator|.
name|indexed
argument_list|()
operator|&&
operator|!
name|fieldType
operator|.
name|stored
argument_list|()
operator|&&
operator|!
name|hasDocValues
argument_list|()
condition|)
block|{
name|context
operator|.
name|ignoredValue
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|LongFieldMapper
operator|.
name|CustomLongNumericField
argument_list|(
name|this
argument_list|,
name|timestamp
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
name|NumericDocValuesField
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|customFieldDataSettings
operator|==
literal|null
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
name|enabledState
operator|==
name|Defaults
operator|.
name|ENABLED
operator|&&
name|path
operator|==
name|Defaults
operator|.
name|PATH
operator|&&
name|dateTimeFormatter
operator|.
name|format
argument_list|()
operator|.
name|equals
argument_list|(
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
operator|.
name|format
argument_list|()
argument_list|)
operator|&&
name|Defaults
operator|.
name|DEFAULT_TIMESTAMP
operator|.
name|equals
argument_list|(
name|defaultTimestamp
argument_list|)
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
name|enabledState
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
name|enabledState
operator|.
name|enabled
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeDefaults
operator|||
operator|(
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
operator|)
operator|||
operator|(
name|fieldType
operator|.
name|tokenized
argument_list|()
operator|!=
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|tokenized
argument_list|()
operator|)
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
name|includeDefaults
operator|||
operator|!
name|dateTimeFormatter
operator|.
name|format
argument_list|()
operator|.
name|equals
argument_list|(
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
operator|.
name|format
argument_list|()
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
name|dateTimeFormatter
operator|.
name|format
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeDefaults
operator|||
operator|!
name|Defaults
operator|.
name|DEFAULT_TIMESTAMP
operator|.
name|equals
argument_list|(
name|defaultTimestamp
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"default"
argument_list|,
name|defaultTimestamp
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
name|TimestampFieldMapper
name|timestampFieldMapperMergeWith
init|=
operator|(
name|TimestampFieldMapper
operator|)
name|mergeWith
decl_stmt|;
name|super
operator|.
name|merge
argument_list|(
name|mergeWith
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
if|if
condition|(
name|timestampFieldMapperMergeWith
operator|.
name|enabledState
operator|!=
name|enabledState
operator|&&
operator|!
name|timestampFieldMapperMergeWith
operator|.
name|enabledState
operator|.
name|unset
argument_list|()
condition|)
block|{
name|this
operator|.
name|enabledState
operator|=
name|timestampFieldMapperMergeWith
operator|.
name|enabledState
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|timestampFieldMapperMergeWith
operator|.
name|defaultTimestamp
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultTimestamp
argument_list|)
condition|)
block|{
name|mergeContext
operator|.
name|addConflict
argument_list|(
literal|"Cannot update default in _timestamp value. Value is "
operator|+
name|defaultTimestamp
operator|.
name|toString
argument_list|()
operator|+
literal|" now encountering "
operator|+
name|timestampFieldMapperMergeWith
operator|.
name|defaultTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|path
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|timestampFieldMapperMergeWith
operator|.
name|path
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|mergeContext
operator|.
name|addConflict
argument_list|(
literal|"Cannot update path in _timestamp value. Value is "
operator|+
name|path
operator|+
literal|" path in merged mapping is "
operator|+
operator|(
name|timestampFieldMapperMergeWith
operator|.
name|path
argument_list|()
operator|==
literal|null
condition|?
literal|"missing"
else|:
name|timestampFieldMapperMergeWith
operator|.
name|path
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|timestampFieldMapperMergeWith
operator|.
name|path
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|mergeContext
operator|.
name|addConflict
argument_list|(
literal|"Cannot update path in _timestamp value. Value is "
operator|+
name|path
operator|+
literal|" path in merged mapping is missing"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

