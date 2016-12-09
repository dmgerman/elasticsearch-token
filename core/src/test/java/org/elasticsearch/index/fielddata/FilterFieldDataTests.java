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
name|document
operator|.
name|Document
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
name|StringField
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
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|ContentPath
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
name|TextFieldMapper
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
operator|.
name|BuilderContext
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
name|Random
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|FilterFieldDataTests
specifier|public
class|class
name|FilterFieldDataTests
extends|extends
name|AbstractFieldDataTestCase
block|{
annotation|@
name|Override
DECL|method|getFieldDataType
specifier|protected
name|String
name|getFieldDataType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|testFilterByFrequency
specifier|public
name|void
name|testFilterByFrequency
parameter_list|()
throws|throws
name|Exception
block|{
name|Random
name|random
init|=
name|random
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"id"
argument_list|,
literal|""
operator|+
name|i
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|100
operator|==
literal|0
condition|)
block|{
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"high_freq"
argument_list|,
literal|"100"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"low_freq"
argument_list|,
literal|"100"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"med_freq"
argument_list|,
literal|"100"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|%
literal|10
operator|==
literal|0
condition|)
block|{
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"high_freq"
argument_list|,
literal|"10"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"med_freq"
argument_list|,
literal|"10"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|%
literal|5
operator|==
literal|0
condition|)
block|{
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"high_freq"
argument_list|,
literal|"5"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|forceMerge
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|contexts
init|=
name|refreshReader
argument_list|()
decl_stmt|;
specifier|final
name|BuilderContext
name|builderCtx
init|=
operator|new
name|BuilderContext
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
block|{
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
name|MappedFieldType
name|ft
init|=
operator|new
name|TextFieldMapper
operator|.
name|Builder
argument_list|(
literal|"high_freq"
argument_list|)
operator|.
name|fielddata
argument_list|(
literal|true
argument_list|)
operator|.
name|fielddataFrequencyFilter
argument_list|(
literal|0
argument_list|,
name|random
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|100
else|:
literal|0.5d
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|(
name|builderCtx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|IndexOrdinalsFieldData
name|fieldData
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|ft
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|contexts
control|)
block|{
name|AtomicOrdinalsFieldData
name|loadDirect
init|=
name|fieldData
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|RandomAccessOrds
name|bytesValues
init|=
name|loadDirect
operator|.
name|getOrdinalsValues
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|2L
argument_list|,
name|equalTo
argument_list|(
name|bytesValues
operator|.
name|getValueCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|lookupOrd
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|lookupOrd
argument_list|(
literal|1
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"100"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|{
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
name|MappedFieldType
name|ft
init|=
operator|new
name|TextFieldMapper
operator|.
name|Builder
argument_list|(
literal|"high_freq"
argument_list|)
operator|.
name|fielddata
argument_list|(
literal|true
argument_list|)
operator|.
name|fielddataFrequencyFilter
argument_list|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|101
else|:
literal|101d
operator|/
literal|200.0d
argument_list|,
literal|201
argument_list|,
literal|100
argument_list|)
operator|.
name|build
argument_list|(
name|builderCtx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|IndexOrdinalsFieldData
name|fieldData
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|ft
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|contexts
control|)
block|{
name|AtomicOrdinalsFieldData
name|loadDirect
init|=
name|fieldData
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|RandomAccessOrds
name|bytesValues
init|=
name|loadDirect
operator|.
name|getOrdinalsValues
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|1L
argument_list|,
name|equalTo
argument_list|(
name|bytesValues
operator|.
name|getValueCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|lookupOrd
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|{
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// test # docs with value
name|MappedFieldType
name|ft
init|=
operator|new
name|TextFieldMapper
operator|.
name|Builder
argument_list|(
literal|"med_freq"
argument_list|)
operator|.
name|fielddata
argument_list|(
literal|true
argument_list|)
operator|.
name|fielddataFrequencyFilter
argument_list|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|101
else|:
literal|101d
operator|/
literal|200.0d
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|101
argument_list|)
operator|.
name|build
argument_list|(
name|builderCtx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|IndexOrdinalsFieldData
name|fieldData
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|ft
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|contexts
control|)
block|{
name|AtomicOrdinalsFieldData
name|loadDirect
init|=
name|fieldData
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|RandomAccessOrds
name|bytesValues
init|=
name|loadDirect
operator|.
name|getOrdinalsValues
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|2L
argument_list|,
name|equalTo
argument_list|(
name|bytesValues
operator|.
name|getValueCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|lookupOrd
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|lookupOrd
argument_list|(
literal|1
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"100"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|{
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
name|MappedFieldType
name|ft
init|=
operator|new
name|TextFieldMapper
operator|.
name|Builder
argument_list|(
literal|"med_freq"
argument_list|)
operator|.
name|fielddata
argument_list|(
literal|true
argument_list|)
operator|.
name|fielddataFrequencyFilter
argument_list|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|101
else|:
literal|101d
operator|/
literal|200.0d
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|101
argument_list|)
operator|.
name|build
argument_list|(
name|builderCtx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|IndexOrdinalsFieldData
name|fieldData
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|ft
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|contexts
control|)
block|{
name|AtomicOrdinalsFieldData
name|loadDirect
init|=
name|fieldData
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|RandomAccessOrds
name|bytesValues
init|=
name|loadDirect
operator|.
name|getOrdinalsValues
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|2L
argument_list|,
name|equalTo
argument_list|(
name|bytesValues
operator|.
name|getValueCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|lookupOrd
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|lookupOrd
argument_list|(
literal|1
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"100"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|testEmpty
specifier|public
name|void
name|testEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"No need to test empty usage here"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

