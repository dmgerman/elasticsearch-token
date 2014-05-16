begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|index
operator|.
name|AtomicReaderContext
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
name|search
operator|.
name|FieldComparatorSource
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
name|SortField
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|UnicodeUtil
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
name|Settings
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
name|IndexComponent
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
name|MultiValueMode
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
name|ordinals
operator|.
name|GlobalOrdinalsBuilder
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
name|indices
operator|.
name|fielddata
operator|.
name|breaker
operator|.
name|CircuitBreakerService
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_interface
DECL|interface|IndexFieldData
specifier|public
interface|interface
name|IndexFieldData
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|>
extends|extends
name|IndexComponent
block|{
DECL|class|CommonSettings
specifier|public
specifier|static
class|class
name|CommonSettings
block|{
DECL|field|SETTING_MEMORY_STORAGE_HINT
specifier|public
specifier|static
specifier|final
name|String
name|SETTING_MEMORY_STORAGE_HINT
init|=
literal|"memory_storage_hint"
decl_stmt|;
DECL|enum|MemoryStorageFormat
specifier|public
enum|enum
name|MemoryStorageFormat
block|{
DECL|enum constant|ORDINALS
DECL|enum constant|PACKED
DECL|enum constant|PAGED
name|ORDINALS
block|,
name|PACKED
block|,
name|PAGED
block|;
DECL|method|fromString
specifier|public
specifier|static
name|MemoryStorageFormat
name|fromString
parameter_list|(
name|String
name|string
parameter_list|)
block|{
for|for
control|(
name|MemoryStorageFormat
name|e
range|:
name|MemoryStorageFormat
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|name
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|string
argument_list|)
condition|)
block|{
return|return
name|e
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**          * Gets a memory storage hint that should be honored if possible but is not mandatory          */
DECL|method|getMemoryStorageHint
specifier|public
specifier|static
name|MemoryStorageFormat
name|getMemoryStorageHint
parameter_list|(
name|FieldDataType
name|fieldDataType
parameter_list|)
block|{
comment|// backwards compatibility
name|String
name|s
init|=
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"ordinals"
argument_list|)
decl_stmt|;
if|if
condition|(
name|s
operator|!=
literal|null
condition|)
block|{
return|return
literal|"always"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|?
name|MemoryStorageFormat
operator|.
name|ORDINALS
else|:
literal|null
return|;
block|}
return|return
name|MemoryStorageFormat
operator|.
name|fromString
argument_list|(
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
name|SETTING_MEMORY_STORAGE_HINT
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**      * The field name.      */
DECL|method|getFieldNames
name|FieldMapper
operator|.
name|Names
name|getFieldNames
parameter_list|()
function_decl|;
comment|/**      * The field data type.      */
DECL|method|getFieldDataType
name|FieldDataType
name|getFieldDataType
parameter_list|()
function_decl|;
comment|/**      * Are the values ordered? (in ascending manner).      */
DECL|method|valuesOrdered
name|boolean
name|valuesOrdered
parameter_list|()
function_decl|;
comment|/**      * Loads the atomic field data for the reader, possibly cached.      */
DECL|method|load
name|FD
name|load
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
function_decl|;
comment|/**      * Loads directly the atomic field data for the reader, ignoring any caching involved.      */
DECL|method|loadDirect
name|FD
name|loadDirect
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**      * Comparator used for sorting.      */
DECL|method|comparatorSource
name|XFieldComparatorSource
name|comparatorSource
parameter_list|(
annotation|@
name|Nullable
name|Object
name|missingValue
parameter_list|,
name|MultiValueMode
name|sortMode
parameter_list|)
function_decl|;
comment|/**      * Clears any resources associated with this field data.      */
DECL|method|clear
name|void
name|clear
parameter_list|()
function_decl|;
DECL|method|clear
name|void
name|clear
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
function_decl|;
comment|// we need this extended source we we have custom comparators to reuse our field data
comment|// in this case, we need to reduce type that will be used when search results are reduced
comment|// on another node (we don't have the custom source them...)
DECL|class|XFieldComparatorSource
specifier|public
specifier|abstract
class|class
name|XFieldComparatorSource
extends|extends
name|FieldComparatorSource
block|{
comment|/** UTF-8 term containing a single code point: {@link Character#MAX_CODE_POINT} which will compare greater than all other index terms          *  since {@link Character#MAX_CODE_POINT} is a noncharacter and thus shouldn't appear in an index term. */
DECL|field|MAX_TERM
specifier|public
specifier|static
specifier|final
name|BytesRef
name|MAX_TERM
decl_stmt|;
static|static
block|{
name|MAX_TERM
operator|=
operator|new
name|BytesRef
argument_list|()
expr_stmt|;
specifier|final
name|char
index|[]
name|chars
init|=
name|Character
operator|.
name|toChars
argument_list|(
name|Character
operator|.
name|MAX_CODE_POINT
argument_list|)
decl_stmt|;
name|UnicodeUtil
operator|.
name|UTF16toUTF8
argument_list|(
name|chars
argument_list|,
literal|0
argument_list|,
name|chars
operator|.
name|length
argument_list|,
name|MAX_TERM
argument_list|)
expr_stmt|;
block|}
comment|/** Whether missing values should be sorted first. */
DECL|method|sortMissingFirst
specifier|protected
specifier|final
name|boolean
name|sortMissingFirst
parameter_list|(
name|Object
name|missingValue
parameter_list|)
block|{
return|return
literal|"_first"
operator|.
name|equals
argument_list|(
name|missingValue
argument_list|)
return|;
block|}
comment|/** Whether missing values should be sorted last, this is the default. */
DECL|method|sortMissingLast
specifier|protected
specifier|final
name|boolean
name|sortMissingLast
parameter_list|(
name|Object
name|missingValue
parameter_list|)
block|{
return|return
name|missingValue
operator|==
literal|null
operator|||
literal|"_last"
operator|.
name|equals
argument_list|(
name|missingValue
argument_list|)
return|;
block|}
comment|/** Return the missing object value according to the reduced type of the comparator. */
DECL|method|missingObject
specifier|protected
specifier|final
name|Object
name|missingObject
parameter_list|(
name|Object
name|missingValue
parameter_list|,
name|boolean
name|reversed
parameter_list|)
block|{
if|if
condition|(
name|sortMissingFirst
argument_list|(
name|missingValue
argument_list|)
operator|||
name|sortMissingLast
argument_list|(
name|missingValue
argument_list|)
condition|)
block|{
specifier|final
name|boolean
name|min
init|=
name|sortMissingFirst
argument_list|(
name|missingValue
argument_list|)
operator|^
name|reversed
decl_stmt|;
switch|switch
condition|(
name|reducedType
argument_list|()
condition|)
block|{
case|case
name|INT
case|:
return|return
name|min
condition|?
name|Integer
operator|.
name|MIN_VALUE
else|:
name|Integer
operator|.
name|MAX_VALUE
return|;
case|case
name|LONG
case|:
return|return
name|min
condition|?
name|Long
operator|.
name|MIN_VALUE
else|:
name|Long
operator|.
name|MAX_VALUE
return|;
case|case
name|FLOAT
case|:
return|return
name|min
condition|?
name|Float
operator|.
name|NEGATIVE_INFINITY
else|:
name|Float
operator|.
name|POSITIVE_INFINITY
return|;
case|case
name|DOUBLE
case|:
return|return
name|min
condition|?
name|Double
operator|.
name|NEGATIVE_INFINITY
else|:
name|Double
operator|.
name|POSITIVE_INFINITY
return|;
case|case
name|STRING
case|:
case|case
name|STRING_VAL
case|:
return|return
name|min
condition|?
literal|null
else|:
name|MAX_TERM
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unsupported reduced type: "
operator|+
name|reducedType
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
switch|switch
condition|(
name|reducedType
argument_list|()
condition|)
block|{
case|case
name|INT
case|:
if|if
condition|(
name|missingValue
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|missingValue
operator|)
operator|.
name|intValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|missingValue
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
case|case
name|LONG
case|:
if|if
condition|(
name|missingValue
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|missingValue
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|missingValue
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
case|case
name|FLOAT
case|:
if|if
condition|(
name|missingValue
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|missingValue
operator|)
operator|.
name|floatValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Float
operator|.
name|parseFloat
argument_list|(
name|missingValue
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
case|case
name|DOUBLE
case|:
if|if
condition|(
name|missingValue
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|missingValue
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|missingValue
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
case|case
name|STRING
case|:
case|case
name|STRING_VAL
case|:
if|if
condition|(
name|missingValue
operator|instanceof
name|BytesRef
condition|)
block|{
return|return
operator|(
name|BytesRef
operator|)
name|missingValue
return|;
block|}
elseif|else
if|if
condition|(
name|missingValue
operator|instanceof
name|byte
index|[]
condition|)
block|{
return|return
operator|new
name|BytesRef
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|missingValue
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|BytesRef
argument_list|(
name|missingValue
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unsupported reduced type: "
operator|+
name|reducedType
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|reducedType
specifier|public
specifier|abstract
name|SortField
operator|.
name|Type
name|reducedType
parameter_list|()
function_decl|;
block|}
DECL|interface|Builder
interface|interface
name|Builder
block|{
DECL|method|build
name|IndexFieldData
name|build
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|,
name|CircuitBreakerService
name|breakerService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|GlobalOrdinalsBuilder
name|globalOrdinalBuilder
parameter_list|)
function_decl|;
block|}
DECL|interface|WithOrdinals
specifier|public
interface|interface
name|WithOrdinals
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
operator|.
name|WithOrdinals
parameter_list|>
extends|extends
name|IndexFieldData
argument_list|<
name|FD
argument_list|>
block|{
comment|/**          * Loads the atomic field data for the reader, possibly cached.          */
DECL|method|load
name|FD
name|load
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
function_decl|;
comment|/**          * Loads directly the atomic field data for the reader, ignoring any caching involved.          */
DECL|method|loadDirect
name|FD
name|loadDirect
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|Exception
function_decl|;
DECL|method|loadGlobal
name|WithOrdinals
name|loadGlobal
parameter_list|(
name|IndexReader
name|indexReader
parameter_list|)
function_decl|;
DECL|method|localGlobalDirect
name|WithOrdinals
name|localGlobalDirect
parameter_list|(
name|IndexReader
name|indexReader
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
block|}
end_interface

end_unit

