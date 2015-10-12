begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DocValues
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
name|index
operator|.
name|LeafReaderContext
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
name|RandomAccessOrds
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
name|SortedDocValues
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
name|Accountable
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
name|AtomicOrdinalsFieldData
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
name|IndexFieldDataCache
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
name|IndexOrdinalsFieldData
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
name|MapperService
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
name|breaker
operator|.
name|CircuitBreakerService
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
name|Collections
import|;
end_import

begin_class
DECL|class|IndexIndexFieldData
specifier|public
class|class
name|IndexIndexFieldData
extends|extends
name|AbstractIndexOrdinalsFieldData
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
argument_list|<
name|?
argument_list|>
name|build
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|,
name|CircuitBreakerService
name|breakerService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
return|return
operator|new
name|IndexIndexFieldData
argument_list|(
name|index
argument_list|,
name|fieldType
operator|.
name|names
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|class|IndexAtomicFieldData
specifier|private
specifier|static
class|class
name|IndexAtomicFieldData
extends|extends
name|AbstractAtomicOrdinalsFieldData
block|{
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|method|IndexAtomicFieldData
name|IndexAtomicFieldData
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Collection
argument_list|<
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getOrdinalsValues
specifier|public
name|RandomAccessOrds
name|getOrdinalsValues
parameter_list|()
block|{
specifier|final
name|BytesRef
name|term
init|=
operator|new
name|BytesRef
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|SortedDocValues
name|sortedValues
init|=
operator|new
name|SortedDocValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BytesRef
name|lookupOrd
parameter_list|(
name|int
name|ord
parameter_list|)
block|{
return|return
name|term
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueCount
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOrd
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
block|}
decl_stmt|;
return|return
operator|(
name|RandomAccessOrds
operator|)
name|DocValues
operator|.
name|singleton
argument_list|(
name|sortedValues
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{         }
block|}
DECL|field|atomicFieldData
specifier|private
specifier|final
name|AtomicOrdinalsFieldData
name|atomicFieldData
decl_stmt|;
DECL|method|IndexIndexFieldData
specifier|private
name|IndexIndexFieldData
parameter_list|(
name|Index
name|index
parameter_list|,
name|MappedFieldType
operator|.
name|Names
name|names
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|names
argument_list|,
operator|new
name|FieldDataType
argument_list|(
literal|"string"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|atomicFieldData
operator|=
operator|new
name|IndexAtomicFieldData
argument_list|(
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|load
specifier|public
specifier|final
name|AtomicOrdinalsFieldData
name|load
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
return|return
name|atomicFieldData
return|;
block|}
annotation|@
name|Override
DECL|method|loadDirect
specifier|public
name|AtomicOrdinalsFieldData
name|loadDirect
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|atomicFieldData
return|;
block|}
annotation|@
name|Override
DECL|method|loadGlobal
specifier|public
name|IndexOrdinalsFieldData
name|loadGlobal
parameter_list|(
name|IndexReader
name|indexReader
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|localGlobalDirect
specifier|public
name|IndexOrdinalsFieldData
name|localGlobalDirect
parameter_list|(
name|IndexReader
name|indexReader
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|loadGlobal
argument_list|(
name|indexReader
argument_list|)
return|;
block|}
block|}
end_class

end_unit

