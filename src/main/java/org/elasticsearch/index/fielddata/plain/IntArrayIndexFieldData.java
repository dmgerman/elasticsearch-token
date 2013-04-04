begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.plain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
package|;
end_package

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|list
operator|.
name|array
operator|.
name|TIntArrayList
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
name|AtomicReader
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
name|Terms
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
name|BytesRefIterator
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
name|FixedBitSet
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
name|NumericUtils
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
name|fielddata
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
name|fielddata
operator|.
name|fieldcomparator
operator|.
name|IntValuesComparatorSource
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
name|fieldcomparator
operator|.
name|SortMode
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
name|Ordinals
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
name|Ordinals
operator|.
name|Docs
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
name|OrdinalsBuilder
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|IntArrayIndexFieldData
specifier|public
class|class
name|IntArrayIndexFieldData
extends|extends
name|AbstractIndexFieldData
argument_list|<
name|IntArrayAtomicFieldData
argument_list|>
implements|implements
name|IndexNumericFieldData
argument_list|<
name|IntArrayAtomicFieldData
argument_list|>
block|{
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
implements|implements
name|IndexFieldData
operator|.
name|Builder
block|{
annotation|@
name|Override
DECL|method|build
specifier|public
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
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|type
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|)
block|{
return|return
operator|new
name|IntArrayIndexFieldData
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|fieldNames
argument_list|,
name|type
argument_list|,
name|cache
argument_list|)
return|;
block|}
block|}
DECL|method|IntArrayIndexFieldData
specifier|public
name|IntArrayIndexFieldData
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
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|fieldNames
argument_list|,
name|fieldDataType
argument_list|,
name|cache
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getNumericType
specifier|public
name|NumericType
name|getNumericType
parameter_list|()
block|{
return|return
name|NumericType
operator|.
name|INT
return|;
block|}
annotation|@
name|Override
DECL|method|valuesOrdered
specifier|public
name|boolean
name|valuesOrdered
parameter_list|()
block|{
comment|// because we might have single values? we can dynamically update a flag to reflect that
comment|// based on the atomic field data loaded
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|load
specifier|public
name|IntArrayAtomicFieldData
name|load
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
block|{
try|try
block|{
return|return
name|cache
operator|.
name|load
argument_list|(
name|context
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ElasticSearchException
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchException
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|loadDirect
specifier|public
name|IntArrayAtomicFieldData
name|loadDirect
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|AtomicReader
name|reader
init|=
name|context
operator|.
name|reader
argument_list|()
decl_stmt|;
name|Terms
name|terms
init|=
name|reader
operator|.
name|terms
argument_list|(
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|==
literal|null
condition|)
block|{
return|return
name|IntArrayAtomicFieldData
operator|.
name|EMPTY
return|;
block|}
comment|// TODO: how can we guess the number of terms? numerics end up creating more terms per value...
specifier|final
name|TIntArrayList
name|values
init|=
operator|new
name|TIntArrayList
argument_list|()
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// first "t" indicates null value
name|OrdinalsBuilder
name|builder
init|=
operator|new
name|OrdinalsBuilder
argument_list|(
name|terms
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|BytesRefIterator
name|iter
init|=
name|builder
operator|.
name|buildFromTerms
argument_list|(
name|builder
operator|.
name|wrapNumeric32Bit
argument_list|(
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
argument_list|)
argument_list|,
name|reader
operator|.
name|getLiveDocs
argument_list|()
argument_list|)
decl_stmt|;
name|BytesRef
name|term
decl_stmt|;
while|while
condition|(
operator|(
name|term
operator|=
name|iter
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|values
operator|.
name|add
argument_list|(
name|NumericUtils
operator|.
name|prefixCodedToInt
argument_list|(
name|term
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Ordinals
name|build
init|=
name|builder
operator|.
name|build
argument_list|(
name|fieldDataType
operator|.
name|getSettings
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|build
argument_list|(
name|reader
argument_list|,
name|fieldDataType
argument_list|,
name|builder
argument_list|,
name|build
argument_list|,
operator|new
name|BuilderIntegers
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|values
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
index|[]
name|toArray
parameter_list|()
block|{
return|return
name|values
operator|.
name|toArray
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
finally|finally
block|{
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|interface|BuilderIntegers
specifier|static
interface|interface
name|BuilderIntegers
block|{
DECL|method|get
name|int
name|get
parameter_list|(
name|int
name|index
parameter_list|)
function_decl|;
DECL|method|toArray
name|int
index|[]
name|toArray
parameter_list|()
function_decl|;
block|}
DECL|method|build
specifier|static
name|IntArrayAtomicFieldData
name|build
parameter_list|(
name|AtomicReader
name|reader
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|OrdinalsBuilder
name|builder
parameter_list|,
name|Ordinals
name|build
parameter_list|,
name|BuilderIntegers
name|values
parameter_list|)
block|{
if|if
condition|(
operator|!
name|build
operator|.
name|isMultiValued
argument_list|()
operator|&&
name|CommonSettings
operator|.
name|removeOrdsOnSingleValue
argument_list|(
name|fieldDataType
argument_list|)
condition|)
block|{
name|Docs
name|ordinals
init|=
name|build
operator|.
name|ordinals
argument_list|()
decl_stmt|;
name|int
index|[]
name|sValues
init|=
operator|new
name|int
index|[
name|reader
operator|.
name|maxDoc
argument_list|()
index|]
decl_stmt|;
name|int
name|maxDoc
init|=
name|reader
operator|.
name|maxDoc
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxDoc
condition|;
name|i
operator|++
control|)
block|{
name|sValues
index|[
name|i
index|]
operator|=
name|values
operator|.
name|get
argument_list|(
name|ordinals
operator|.
name|getOrd
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|FixedBitSet
name|set
init|=
name|builder
operator|.
name|buildDocsWithValuesSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|set
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|IntArrayAtomicFieldData
operator|.
name|Single
argument_list|(
name|sValues
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|IntArrayAtomicFieldData
operator|.
name|SingleFixedSet
argument_list|(
name|sValues
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|set
argument_list|)
return|;
block|}
block|}
else|else
block|{
return|return
operator|new
name|IntArrayAtomicFieldData
operator|.
name|WithOrdinals
argument_list|(
name|values
operator|.
name|toArray
argument_list|()
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|build
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|comparatorSource
specifier|public
name|XFieldComparatorSource
name|comparatorSource
parameter_list|(
annotation|@
name|Nullable
name|Object
name|missingValue
parameter_list|,
name|SortMode
name|sortMode
parameter_list|)
block|{
return|return
operator|new
name|IntValuesComparatorSource
argument_list|(
name|this
argument_list|,
name|missingValue
argument_list|,
name|sortMode
argument_list|)
return|;
block|}
block|}
end_class

end_unit

