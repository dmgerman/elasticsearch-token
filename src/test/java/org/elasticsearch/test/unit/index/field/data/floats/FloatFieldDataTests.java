begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.index.field.data.floats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|index
operator|.
name|field
operator|.
name|data
operator|.
name|floats
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
name|FloatField
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
name|IndexWriter
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
name|IndexWriterConfig
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
name|store
operator|.
name|Directory
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
name|store
operator|.
name|RAMDirectory
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
name|lucene
operator|.
name|Lucene
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
name|field
operator|.
name|data
operator|.
name|floats
operator|.
name|FloatFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FloatFieldDataTests
specifier|public
class|class
name|FloatFieldDataTests
block|{
annotation|@
name|Test
DECL|method|intFieldDataTests
specifier|public
name|void
name|intFieldDataTests
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|indexWriter
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Lucene
operator|.
name|VERSION
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"svalue"
argument_list|,
literal|4
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"mvalue"
argument_list|,
literal|104
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"svalue"
argument_list|,
literal|3
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"mvalue"
argument_list|,
literal|104
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"mvalue"
argument_list|,
literal|105
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"svalue"
argument_list|,
literal|7
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"mvalue"
argument_list|,
literal|102
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|FloatField
argument_list|(
literal|"svalue"
argument_list|,
literal|4
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|IndexReader
operator|.
name|open
argument_list|(
name|indexWriter
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|FloatFieldData
name|sFieldData
init|=
name|FloatFieldData
operator|.
name|load
argument_list|(
name|reader
argument_list|,
literal|"svalue"
argument_list|)
decl_stmt|;
name|FloatFieldData
name|mFieldData
init|=
name|FloatFieldData
operator|.
name|load
argument_list|(
name|reader
argument_list|,
literal|"mvalue"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|fieldName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"svalue"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|multiValued
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|fieldName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"mvalue"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|multiValued
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// svalue
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|hasValue
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|value
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|4f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|values
argument_list|(
literal|0
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|getValues
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|4f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|hasValue
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|value
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|values
argument_list|(
literal|1
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|values
argument_list|(
literal|1
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|3f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|hasValue
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|value
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|7f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|values
argument_list|(
literal|2
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|values
argument_list|(
literal|2
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|7f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|hasValue
argument_list|(
literal|3
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|hasValue
argument_list|(
literal|4
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|value
argument_list|(
literal|4
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|4f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|values
argument_list|(
literal|4
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sFieldData
operator|.
name|values
argument_list|(
literal|4
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|4f
argument_list|)
argument_list|)
expr_stmt|;
comment|// check order is correct
specifier|final
name|ArrayList
argument_list|<
name|Float
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|Float
argument_list|>
argument_list|()
decl_stmt|;
name|sFieldData
operator|.
name|forEachValue
argument_list|(
operator|new
name|FloatFieldData
operator|.
name|ValueProc
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onValue
parameter_list|(
name|float
name|value
parameter_list|)
block|{
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|4f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|7f
argument_list|)
argument_list|)
expr_stmt|;
comment|// mvalue
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|hasValue
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|value
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|values
argument_list|(
literal|0
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|values
argument_list|(
literal|0
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|docFieldData
argument_list|(
literal|0
argument_list|)
operator|.
name|getValues
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|hasValue
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|value
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|values
argument_list|(
literal|1
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|docFieldData
argument_list|(
literal|1
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|values
argument_list|(
literal|1
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|docFieldData
argument_list|(
literal|1
argument_list|)
operator|.
name|getValues
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|docFieldData
argument_list|(
literal|1
argument_list|)
operator|.
name|getValues
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|105f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|hasValue
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|hasValue
argument_list|(
literal|3
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|value
argument_list|(
literal|3
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|102f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|values
argument_list|(
literal|3
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|values
argument_list|(
literal|3
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|102f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mFieldData
operator|.
name|hasValue
argument_list|(
literal|4
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// check order is correct
name|values
operator|.
name|clear
argument_list|()
expr_stmt|;
name|mFieldData
operator|.
name|forEachValue
argument_list|(
operator|new
name|FloatFieldData
operator|.
name|ValueProc
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onValue
parameter_list|(
name|float
name|value
parameter_list|)
block|{
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|102f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|104f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|105f
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

