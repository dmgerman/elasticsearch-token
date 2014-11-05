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
name|IndexReader
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
name|IndexFieldData
operator|.
name|XFieldComparatorSource
operator|.
name|Nested
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
name|BytesRefFieldComparatorSource
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
operator|.
name|Names
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
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/** Returns an implementation based on paged bytes which doesn't implement WithOrdinals in order to visit different paths in the code,  *  eg. BytesRefFieldComparatorSource makes decisions based on whether the field data implements WithOrdinals. */
end_comment

begin_class
DECL|class|NoOrdinalsStringFieldDataTests
specifier|public
class|class
name|NoOrdinalsStringFieldDataTests
extends|extends
name|PagedBytesStringFieldDataTests
block|{
DECL|method|hideOrdinals
specifier|public
specifier|static
name|IndexFieldData
argument_list|<
name|AtomicFieldData
argument_list|>
name|hideOrdinals
parameter_list|(
specifier|final
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|in
parameter_list|)
block|{
return|return
operator|new
name|IndexFieldData
argument_list|<
name|AtomicFieldData
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Index
name|index
parameter_list|()
block|{
return|return
name|in
operator|.
name|index
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Names
name|getFieldNames
parameter_list|()
block|{
return|return
name|in
operator|.
name|getFieldNames
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|FieldDataType
name|getFieldDataType
parameter_list|()
block|{
return|return
name|in
operator|.
name|getFieldDataType
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|AtomicFieldData
name|load
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
return|return
name|in
operator|.
name|load
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AtomicFieldData
name|loadDirect
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|in
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|XFieldComparatorSource
name|comparatorSource
parameter_list|(
name|Object
name|missingValue
parameter_list|,
name|MultiValueMode
name|sortMode
parameter_list|,
name|Nested
name|nested
parameter_list|)
block|{
return|return
operator|new
name|BytesRefFieldComparatorSource
argument_list|(
name|this
argument_list|,
name|missingValue
argument_list|,
name|sortMode
argument_list|,
name|nested
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|in
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
name|in
operator|.
name|clear
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
DECL|method|getForField
specifier|public
name|IndexFieldData
argument_list|<
name|AtomicFieldData
argument_list|>
name|getForField
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|hideOrdinals
argument_list|(
name|super
operator|.
name|getForField
argument_list|(
name|fieldName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
annotation|@
name|Override
DECL|method|testTermsEnum
specifier|public
name|void
name|testTermsEnum
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We can't test this, since the returned IFD instance doesn't implement IndexFieldData.WithOrdinals
block|}
block|}
end_class

end_unit

