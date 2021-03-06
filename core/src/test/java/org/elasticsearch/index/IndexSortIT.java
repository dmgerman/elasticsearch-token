begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|search
operator|.
name|Sort
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
name|search
operator|.
name|SortedNumericSortField
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
name|SortedSetSortField
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
name|test
operator|.
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|containsString
import|;
end_import

begin_class
DECL|class|IndexSortIT
specifier|public
class|class
name|IndexSortIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|TEST_MAPPING
specifier|private
specifier|static
specifier|final
name|XContentBuilder
name|TEST_MAPPING
init|=
name|createTestMapping
argument_list|()
decl_stmt|;
DECL|method|createTestMapping
specifier|private
specifier|static
name|XContentBuilder
name|createTestMapping
parameter_list|()
block|{
try|try
block|{
return|return
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"date"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"numeric"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|false
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"numeric_dv"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|true
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"keyword_dv"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|true
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"keyword"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|false
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|testIndexSort
specifier|public
name|void
name|testIndexSort
parameter_list|()
block|{
name|SortField
name|dateSort
init|=
operator|new
name|SortedNumericSortField
argument_list|(
literal|"date"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|dateSort
operator|.
name|setMissingValue
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|SortField
name|numericSort
init|=
operator|new
name|SortedNumericSortField
argument_list|(
literal|"numeric_dv"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|numericSort
operator|.
name|setMissingValue
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|SortField
name|keywordSort
init|=
operator|new
name|SortedSetSortField
argument_list|(
literal|"keyword_dv"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|keywordSort
operator|.
name|setMissingValue
argument_list|(
name|SortField
operator|.
name|STRING_LAST
argument_list|)
expr_stmt|;
name|Sort
name|indexSort
init|=
operator|new
name|Sort
argument_list|(
name|dateSort
argument_list|,
name|numericSort
argument_list|,
name|keywordSort
argument_list|)
decl_stmt|;
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.sort.field"
argument_list|,
literal|"date"
argument_list|,
literal|"numeric_dv"
argument_list|,
literal|"keyword_dv"
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"test"
argument_list|,
name|TEST_MAPPING
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"numeric_dv"
argument_list|,
name|randomInt
argument_list|()
argument_list|,
literal|"keyword_dv"
argument_list|,
name|randomAlphaOfLengthBetween
argument_list|(
literal|10
argument_list|,
literal|20
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|flushAndRefresh
argument_list|()
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|assertSortedSegments
argument_list|(
literal|"test"
argument_list|,
name|indexSort
argument_list|)
expr_stmt|;
block|}
DECL|method|testInvalidIndexSort
specifier|public
name|void
name|testInvalidIndexSort
parameter_list|()
block|{
name|IllegalArgumentException
name|exc
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.sort.field"
argument_list|,
literal|"invalid_field"
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"test"
argument_list|,
name|TEST_MAPPING
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|exc
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"unknown index sort field:[invalid_field]"
argument_list|)
argument_list|)
expr_stmt|;
name|exc
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.sort.field"
argument_list|,
literal|"numeric"
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"test"
argument_list|,
name|TEST_MAPPING
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exc
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"docvalues not found for index sort field:[numeric]"
argument_list|)
argument_list|)
expr_stmt|;
name|exc
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.sort.field"
argument_list|,
literal|"keyword"
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"test"
argument_list|,
name|TEST_MAPPING
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exc
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"docvalues not found for index sort field:[keyword]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

