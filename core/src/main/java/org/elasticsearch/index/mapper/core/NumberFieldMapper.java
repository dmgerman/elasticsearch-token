begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.core
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
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
name|Analyzer
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
name|analysis
operator|.
name|LegacyNumericTokenStream
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
name|SortedNumericDocValuesField
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
name|DocValuesType
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
name|IndexableField
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
name|IndexableFieldType
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
name|BytesRef
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
name|settings
operator|.
name|Setting
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
name|Setting
operator|.
name|Property
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
name|unit
operator|.
name|Fuzziness
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
name|analysis
operator|.
name|NamedAnalyzer
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
name|IndexFieldData
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
name|IndexNumericFieldData
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
name|plain
operator|.
name|DocValuesIndexFieldData
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
name|MappedFieldType
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
name|Mapper
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
name|ParseContext
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
name|search
operator|.
name|DocValueFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
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
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NumberFieldMapper
specifier|public
specifier|abstract
class|class
name|NumberFieldMapper
extends|extends
name|FieldMapper
implements|implements
name|AllFieldMapper
operator|.
name|IncludeInAll
block|{
comment|// this is private since it has a different default
DECL|field|COERCE_SETTING
specifier|private
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|COERCE_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"index.mapping.coerce"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|)
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|PRECISION_STEP_8_BIT
specifier|public
specifier|static
specifier|final
name|int
name|PRECISION_STEP_8_BIT
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// 1tpv: 256 terms at most, not useful
DECL|field|PRECISION_STEP_16_BIT
specifier|public
specifier|static
specifier|final
name|int
name|PRECISION_STEP_16_BIT
init|=
literal|8
decl_stmt|;
comment|// 2tpv
DECL|field|PRECISION_STEP_32_BIT
specifier|public
specifier|static
specifier|final
name|int
name|PRECISION_STEP_32_BIT
init|=
literal|8
decl_stmt|;
comment|// 4tpv
DECL|field|PRECISION_STEP_64_BIT
specifier|public
specifier|static
specifier|final
name|int
name|PRECISION_STEP_64_BIT
init|=
literal|16
decl_stmt|;
comment|// 4tpv
DECL|field|IGNORE_MALFORMED
specifier|public
specifier|static
specifier|final
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|IGNORE_MALFORMED
init|=
operator|new
name|Explicit
argument_list|<>
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
DECL|field|COERCE
specifier|public
specifier|static
specifier|final
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|COERCE
init|=
operator|new
name|Explicit
argument_list|<>
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|abstract
specifier|static
class|class
name|Builder
parameter_list|<
name|T
extends|extends
name|Builder
parameter_list|,
name|Y
extends|extends
name|NumberFieldMapper
parameter_list|>
extends|extends
name|FieldMapper
operator|.
name|Builder
argument_list|<
name|T
argument_list|,
name|Y
argument_list|>
block|{
DECL|field|ignoreMalformed
specifier|private
name|Boolean
name|ignoreMalformed
decl_stmt|;
DECL|field|coerce
specifier|private
name|Boolean
name|coerce
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|String
name|name
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|int
name|defaultPrecisionStep
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|fieldType
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldType
operator|.
name|setNumericPrecisionStep
argument_list|(
name|defaultPrecisionStep
argument_list|)
expr_stmt|;
block|}
DECL|method|precisionStep
specifier|public
name|T
name|precisionStep
parameter_list|(
name|int
name|precisionStep
parameter_list|)
block|{
name|fieldType
operator|.
name|setNumericPrecisionStep
argument_list|(
name|precisionStep
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|ignoreMalformed
specifier|public
name|T
name|ignoreMalformed
parameter_list|(
name|boolean
name|ignoreMalformed
parameter_list|)
block|{
name|this
operator|.
name|ignoreMalformed
operator|=
name|ignoreMalformed
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|ignoreMalformed
specifier|protected
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|ignoreMalformed
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|ignoreMalformed
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|Explicit
argument_list|<>
argument_list|(
name|ignoreMalformed
argument_list|,
literal|true
argument_list|)
return|;
block|}
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
return|return
operator|new
name|Explicit
argument_list|<>
argument_list|(
name|IGNORE_MALFORMED_SETTING
operator|.
name|get
argument_list|(
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
return|;
block|}
return|return
name|Defaults
operator|.
name|IGNORE_MALFORMED
return|;
block|}
DECL|method|coerce
specifier|public
name|T
name|coerce
parameter_list|(
name|boolean
name|coerce
parameter_list|)
block|{
name|this
operator|.
name|coerce
operator|=
name|coerce
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|coerce
specifier|protected
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|coerce
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|coerce
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|Explicit
argument_list|<>
argument_list|(
name|coerce
argument_list|,
literal|true
argument_list|)
return|;
block|}
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
return|return
operator|new
name|Explicit
argument_list|<>
argument_list|(
name|COERCE_SETTING
operator|.
name|get
argument_list|(
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
return|;
block|}
return|return
name|Defaults
operator|.
name|COERCE
return|;
block|}
DECL|method|setupFieldType
specifier|protected
name|void
name|setupFieldType
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
name|super
operator|.
name|setupFieldType
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|int
name|precisionStep
init|=
name|fieldType
operator|.
name|numericPrecisionStep
argument_list|()
decl_stmt|;
if|if
condition|(
name|precisionStep
operator|<=
literal|0
operator|||
name|precisionStep
operator|>=
name|maxPrecisionStep
argument_list|()
condition|)
block|{
name|fieldType
operator|.
name|setNumericPrecisionStep
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|maxPrecisionStep
specifier|protected
specifier|abstract
name|int
name|maxPrecisionStep
parameter_list|()
function_decl|;
block|}
DECL|class|NumberFieldType
specifier|public
specifier|static
specifier|abstract
class|class
name|NumberFieldType
extends|extends
name|MappedFieldType
block|{
DECL|method|NumberFieldType
specifier|public
name|NumberFieldType
parameter_list|(
name|LegacyNumericType
name|numericType
parameter_list|)
block|{
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS
argument_list|)
expr_stmt|;
name|setStoreTermVectors
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|setNumericType
argument_list|(
name|numericType
argument_list|)
expr_stmt|;
block|}
DECL|method|NumberFieldType
specifier|protected
name|NumberFieldType
parameter_list|(
name|NumberFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|checkCompatibility
specifier|public
name|void
name|checkCompatibility
parameter_list|(
name|MappedFieldType
name|other
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|conflicts
parameter_list|,
name|boolean
name|strict
parameter_list|)
block|{
name|super
operator|.
name|checkCompatibility
argument_list|(
name|other
argument_list|,
name|conflicts
argument_list|,
name|strict
argument_list|)
expr_stmt|;
if|if
condition|(
name|numericPrecisionStep
argument_list|()
operator|!=
name|other
operator|.
name|numericPrecisionStep
argument_list|()
condition|)
block|{
name|conflicts
operator|.
name|add
argument_list|(
literal|"mapper ["
operator|+
name|name
argument_list|()
operator|+
literal|"] has different [precision_step] values"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|clone
specifier|public
specifier|abstract
name|NumberFieldType
name|clone
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|fuzzyQuery
specifier|public
specifier|abstract
name|Query
name|fuzzyQuery
parameter_list|(
name|Object
name|value
parameter_list|,
name|Fuzziness
name|fuzziness
parameter_list|,
name|int
name|prefixLength
parameter_list|,
name|int
name|maxExpansions
parameter_list|,
name|boolean
name|transpositions
parameter_list|)
function_decl|;
annotation|@
name|Override
DECL|method|docValueFormat
specifier|public
name|DocValueFormat
name|docValueFormat
parameter_list|(
annotation|@
name|Nullable
name|String
name|format
parameter_list|,
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
if|if
condition|(
name|timeZone
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field ["
operator|+
name|name
argument_list|()
operator|+
literal|"] of type ["
operator|+
name|typeName
argument_list|()
operator|+
literal|"] does not support custom time zones"
argument_list|)
throw|;
block|}
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
return|return
name|DocValueFormat
operator|.
name|RAW
return|;
block|}
else|else
block|{
return|return
operator|new
name|DocValueFormat
operator|.
name|Decimal
argument_list|(
name|format
argument_list|)
return|;
block|}
block|}
block|}
DECL|field|includeInAll
specifier|protected
name|Boolean
name|includeInAll
decl_stmt|;
DECL|field|ignoreMalformed
specifier|protected
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|ignoreMalformed
decl_stmt|;
DECL|field|coerce
specifier|protected
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|coerce
decl_stmt|;
DECL|method|NumberFieldMapper
specifier|protected
name|NumberFieldMapper
parameter_list|(
name|String
name|simpleName
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|MappedFieldType
name|defaultFieldType
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
name|Settings
name|indexSettings
parameter_list|,
name|MultiFields
name|multiFields
parameter_list|,
name|CopyTo
name|copyTo
parameter_list|)
block|{
name|super
argument_list|(
name|simpleName
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|indexSettings
argument_list|,
name|multiFields
argument_list|,
name|copyTo
argument_list|)
expr_stmt|;
assert|assert
name|fieldType
operator|.
name|tokenized
argument_list|()
operator|==
literal|false
assert|;
name|this
operator|.
name|ignoreMalformed
operator|=
name|ignoreMalformed
expr_stmt|;
name|this
operator|.
name|coerce
operator|=
name|coerce
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|protected
name|NumberFieldMapper
name|clone
parameter_list|()
block|{
return|return
operator|(
name|NumberFieldMapper
operator|)
name|super
operator|.
name|clone
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|includeInAll
specifier|public
name|Mapper
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
condition|)
block|{
name|NumberFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|includeInAllIfNotSet
specifier|public
name|Mapper
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
name|this
operator|.
name|includeInAll
operator|==
literal|null
condition|)
block|{
name|NumberFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|unsetIncludeInAll
specifier|public
name|Mapper
name|unsetIncludeInAll
parameter_list|()
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
condition|)
block|{
name|NumberFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
literal|null
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
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
name|RuntimeException
name|e
init|=
literal|null
decl_stmt|;
try|try
block|{
name|innerParseCreateField
argument_list|(
name|context
argument_list|,
name|fields
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e1
parameter_list|)
block|{
name|e
operator|=
name|e1
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|e2
parameter_list|)
block|{
name|e
operator|=
name|e2
expr_stmt|;
block|}
if|if
condition|(
name|e
operator|!=
literal|null
operator|&&
operator|!
name|ignoreMalformed
operator|.
name|value
argument_list|()
condition|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
DECL|method|innerParseCreateField
specifier|protected
specifier|abstract
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
function_decl|;
DECL|method|addDocValue
specifier|protected
specifier|final
name|void
name|addDocValue
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Converts an object value into a double      */
DECL|method|parseDoubleValue
specifier|public
specifier|static
name|double
name|parseDoubleValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|BytesRef
condition|)
block|{
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
operator|(
operator|(
name|BytesRef
operator|)
name|value
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|)
return|;
block|}
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Converts an object value into a long      */
DECL|method|parseLongValue
specifier|public
specifier|static
name|long
name|parseLongValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|BytesRef
condition|)
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
operator|(
operator|(
name|BytesRef
operator|)
name|value
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|)
return|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doMerge
specifier|protected
name|void
name|doMerge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|boolean
name|updateAllTypes
parameter_list|)
block|{
name|super
operator|.
name|doMerge
argument_list|(
name|mergeWith
argument_list|,
name|updateAllTypes
argument_list|)
expr_stmt|;
name|NumberFieldMapper
name|nfmMergeWith
init|=
operator|(
name|NumberFieldMapper
operator|)
name|mergeWith
decl_stmt|;
name|this
operator|.
name|includeInAll
operator|=
name|nfmMergeWith
operator|.
name|includeInAll
expr_stmt|;
if|if
condition|(
name|nfmMergeWith
operator|.
name|ignoreMalformed
operator|.
name|explicit
argument_list|()
condition|)
block|{
name|this
operator|.
name|ignoreMalformed
operator|=
name|nfmMergeWith
operator|.
name|ignoreMalformed
expr_stmt|;
block|}
if|if
condition|(
name|nfmMergeWith
operator|.
name|coerce
operator|.
name|explicit
argument_list|()
condition|)
block|{
name|this
operator|.
name|coerce
operator|=
name|nfmMergeWith
operator|.
name|coerce
expr_stmt|;
block|}
block|}
comment|// used to we can use a numeric field in a document that is then parsed twice!
DECL|class|CustomNumericField
specifier|public
specifier|abstract
specifier|static
class|class
name|CustomNumericField
extends|extends
name|Field
block|{
DECL|field|tokenStream
specifier|private
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
name|tokenStream
init|=
operator|new
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|LegacyNumericTokenStream
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|LegacyNumericTokenStream
argument_list|(
name|fieldType
argument_list|()
operator|.
name|numericPrecisionStep
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|tokenStream4
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
name|tokenStream4
init|=
operator|new
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|LegacyNumericTokenStream
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|LegacyNumericTokenStream
argument_list|(
literal|4
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|tokenStream8
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
name|tokenStream8
init|=
operator|new
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|LegacyNumericTokenStream
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|LegacyNumericTokenStream
argument_list|(
literal|8
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|tokenStream16
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
name|tokenStream16
init|=
operator|new
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|LegacyNumericTokenStream
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|LegacyNumericTokenStream
argument_list|(
literal|16
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|tokenStreamMax
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
name|tokenStreamMax
init|=
operator|new
name|ThreadLocal
argument_list|<
name|LegacyNumericTokenStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|LegacyNumericTokenStream
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|LegacyNumericTokenStream
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|CustomNumericField
specifier|public
name|CustomNumericField
parameter_list|(
name|Number
name|value
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
name|super
argument_list|(
name|fieldType
operator|.
name|name
argument_list|()
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|fieldsData
operator|=
name|value
expr_stmt|;
block|}
block|}
DECL|method|getCachedStream
specifier|protected
name|LegacyNumericTokenStream
name|getCachedStream
parameter_list|()
block|{
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|numericPrecisionStep
argument_list|()
operator|==
literal|4
condition|)
block|{
return|return
name|tokenStream4
operator|.
name|get
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|numericPrecisionStep
argument_list|()
operator|==
literal|8
condition|)
block|{
return|return
name|tokenStream8
operator|.
name|get
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|numericPrecisionStep
argument_list|()
operator|==
literal|16
condition|)
block|{
return|return
name|tokenStream16
operator|.
name|get
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|numericPrecisionStep
argument_list|()
operator|==
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
name|tokenStreamMax
operator|.
name|get
argument_list|()
return|;
block|}
return|return
name|tokenStream
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|stringValue
specifier|public
name|String
name|stringValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|readerValue
specifier|public
name|Reader
name|readerValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|numericAsString
specifier|public
specifier|abstract
name|String
name|numericAsString
parameter_list|()
function_decl|;
block|}
DECL|class|CustomNumericDocValuesField
specifier|public
specifier|static
specifier|abstract
class|class
name|CustomNumericDocValuesField
implements|implements
name|IndexableField
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|TYPE
init|=
operator|new
name|FieldType
argument_list|()
decl_stmt|;
static|static
block|{
name|TYPE
operator|.
name|setDocValuesType
argument_list|(
name|DocValuesType
operator|.
name|BINARY
argument_list|)
expr_stmt|;
name|TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|CustomNumericDocValuesField
specifier|public
name|CustomNumericDocValuesField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
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
name|name
return|;
block|}
annotation|@
name|Override
DECL|method|fieldType
specifier|public
name|IndexableFieldType
name|fieldType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|boost
specifier|public
name|float
name|boost
parameter_list|()
block|{
return|return
literal|1f
return|;
block|}
annotation|@
name|Override
DECL|method|stringValue
specifier|public
name|String
name|stringValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|readerValue
specifier|public
name|Reader
name|readerValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|numericValue
specifier|public
name|Number
name|numericValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|tokenStream
specifier|public
name|TokenStream
name|tokenStream
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|,
name|TokenStream
name|reuse
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
name|void
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|boolean
name|includeDefaults
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|doXContentBody
argument_list|(
name|builder
argument_list|,
name|includeDefaults
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeDefaults
operator|||
name|ignoreMalformed
operator|.
name|explicit
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"ignore_malformed"
argument_list|,
name|ignoreMalformed
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeDefaults
operator|||
name|coerce
operator|.
name|explicit
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"coerce"
argument_list|,
name|coerce
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

