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
name|DirectoryReader
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
name|search
operator|.
name|FieldDoc
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
name|IndexSearcher
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
name|MatchAllDocsQuery
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
name|TopFieldDocs
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
name|Strings
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThanOrEqualTo
import|;
end_import

begin_class
DECL|class|AbstractFieldDataImplTestCase
specifier|public
specifier|abstract
class|class
name|AbstractFieldDataImplTestCase
extends|extends
name|AbstractFieldDataTestCase
block|{
DECL|method|one
specifier|protected
name|String
name|one
parameter_list|()
block|{
return|return
literal|"1"
return|;
block|}
DECL|method|two
specifier|protected
name|String
name|two
parameter_list|()
block|{
return|return
literal|"2"
return|;
block|}
DECL|method|three
specifier|protected
name|String
name|three
parameter_list|()
block|{
return|return
literal|"3"
return|;
block|}
DECL|method|four
specifier|protected
name|String
name|four
parameter_list|()
block|{
return|return
literal|"4"
return|;
block|}
DECL|method|toString
specifier|protected
name|String
name|toString
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|BytesRef
condition|)
block|{
return|return
operator|(
operator|(
name|BytesRef
operator|)
name|value
operator|)
operator|.
name|utf8ToString
argument_list|()
return|;
block|}
return|return
name|value
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|fillSingleValueAllSet
specifier|protected
specifier|abstract
name|void
name|fillSingleValueAllSet
parameter_list|()
throws|throws
name|Exception
function_decl|;
DECL|method|add2SingleValuedDocumentsAndDeleteOneOfThem
specifier|protected
specifier|abstract
name|void
name|add2SingleValuedDocumentsAndDeleteOneOfThem
parameter_list|()
throws|throws
name|Exception
function_decl|;
DECL|method|minRamBytesUsed
specifier|protected
name|long
name|minRamBytesUsed
parameter_list|()
block|{
comment|// minimum number of bytes that this fielddata instance is expected to require
return|return
literal|1
return|;
block|}
DECL|method|testDeletedDocs
specifier|public
name|void
name|testDeletedDocs
parameter_list|()
throws|throws
name|Exception
block|{
name|add2SingleValuedDocumentsAndDeleteOneOfThem
argument_list|()
expr_stmt|;
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|readerContexts
init|=
name|refreshReader
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|readerContext
range|:
name|readerContexts
control|)
block|{
name|AtomicFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
decl_stmt|;
name|SortedBinaryDocValues
name|values
init|=
name|fieldData
operator|.
name|getBytesValues
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
name|readerContext
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testSingleValueAllSet
specifier|public
name|void
name|testSingleValueAllSet
parameter_list|()
throws|throws
name|Exception
block|{
name|fillSingleValueAllSet
argument_list|()
expr_stmt|;
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|readerContexts
init|=
name|refreshReader
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|readerContext
range|:
name|readerContexts
control|)
block|{
name|AtomicFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldData
operator|.
name|ramBytesUsed
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|minRamBytesUsed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SortedBinaryDocValues
name|bytesValues
init|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|bytesValues
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|docValueCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|nextValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|two
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bytesValues
operator|.
name|advanceExact
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|docValueCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|nextValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|one
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bytesValues
operator|.
name|advanceExact
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|docValueCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|nextValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|three
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|0
argument_list|,
name|two
argument_list|()
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|1
argument_list|,
name|one
argument_list|()
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|2
argument_list|,
name|three
argument_list|()
argument_list|)
expr_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|readerContext
operator|.
name|reader
argument_list|()
argument_list|)
decl_stmt|;
name|TopFieldDocs
name|topDocs
decl_stmt|;
name|SortField
name|sortField
init|=
name|indexFieldData
operator|.
name|sortField
argument_list|(
literal|null
argument_list|,
name|MultiValueMode
operator|.
name|MIN
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|topDocs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
name|sortField
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|toString
argument_list|(
operator|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|one
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|toString
argument_list|(
operator|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|two
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|toString
argument_list|(
operator|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|three
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sortField
operator|=
name|indexFieldData
operator|.
name|sortField
argument_list|(
literal|null
argument_list|,
name|MultiValueMode
operator|.
name|MAX
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|topDocs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
name|sortField
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fillSingleValueWithMissing
specifier|protected
specifier|abstract
name|void
name|fillSingleValueWithMissing
parameter_list|()
throws|throws
name|Exception
function_decl|;
DECL|method|assertValues
specifier|public
name|void
name|assertValues
parameter_list|(
name|SortedBinaryDocValues
name|values
parameter_list|,
name|int
name|docId
parameter_list|,
name|BytesRef
modifier|...
name|actualValues
parameter_list|)
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
name|actualValues
operator|.
name|length
operator|>
literal|0
argument_list|,
name|values
operator|.
name|advanceExact
argument_list|(
name|docId
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|actualValues
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|assertThat
argument_list|(
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|actualValues
operator|.
name|length
argument_list|)
argument_list|)
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
name|actualValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|values
operator|.
name|nextValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|actualValues
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|assertValues
specifier|public
name|void
name|assertValues
parameter_list|(
name|SortedBinaryDocValues
name|values
parameter_list|,
name|int
name|docId
parameter_list|,
name|String
modifier|...
name|actualValues
parameter_list|)
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
name|actualValues
operator|.
name|length
operator|>
literal|0
argument_list|,
name|values
operator|.
name|advanceExact
argument_list|(
name|docId
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|actualValues
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|assertThat
argument_list|(
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|actualValues
operator|.
name|length
argument_list|)
argument_list|)
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
name|actualValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|values
operator|.
name|nextValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|actualValues
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testSingleValueWithMissing
specifier|public
name|void
name|testSingleValueWithMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|fillSingleValueWithMissing
argument_list|()
expr_stmt|;
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|readerContexts
init|=
name|refreshReader
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|readerContext
range|:
name|readerContexts
control|)
block|{
name|AtomicFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldData
operator|.
name|ramBytesUsed
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|minRamBytesUsed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SortedBinaryDocValues
name|bytesValues
init|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
decl_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|0
argument_list|,
name|two
argument_list|()
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|1
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|2
argument_list|,
name|three
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fillMultiValueAllSet
specifier|protected
specifier|abstract
name|void
name|fillMultiValueAllSet
parameter_list|()
throws|throws
name|Exception
function_decl|;
DECL|method|testMultiValueAllSet
specifier|public
name|void
name|testMultiValueAllSet
parameter_list|()
throws|throws
name|Exception
block|{
name|fillMultiValueAllSet
argument_list|()
expr_stmt|;
comment|// the segments are force merged to a single segment so that the sorted binary doc values can be asserted within a single segment.
comment|// Previously we used the SlowCompositeReaderWrapper but this is an unideal solution so force merging is a better idea.
name|writer
operator|.
name|forceMerge
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|readerContexts
init|=
name|refreshReader
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|readerContext
range|:
name|readerContexts
control|)
block|{
name|AtomicFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldData
operator|.
name|ramBytesUsed
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|minRamBytesUsed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SortedBinaryDocValues
name|bytesValues
init|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
decl_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|0
argument_list|,
name|two
argument_list|()
argument_list|,
name|four
argument_list|()
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|1
argument_list|,
name|one
argument_list|()
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|2
argument_list|,
name|three
argument_list|()
argument_list|)
expr_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|)
argument_list|)
decl_stmt|;
name|SortField
name|sortField
init|=
name|indexFieldData
operator|.
name|sortField
argument_list|(
literal|null
argument_list|,
name|MultiValueMode
operator|.
name|MIN
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|TopFieldDocs
name|topDocs
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
name|sortField
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|sortField
operator|=
name|indexFieldData
operator|.
name|sortField
argument_list|(
literal|null
argument_list|,
name|MultiValueMode
operator|.
name|MAX
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|topDocs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
name|sortField
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fillMultiValueWithMissing
specifier|protected
specifier|abstract
name|void
name|fillMultiValueWithMissing
parameter_list|()
throws|throws
name|Exception
function_decl|;
DECL|method|testMultiValueWithMissing
specifier|public
name|void
name|testMultiValueWithMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|fillMultiValueWithMissing
argument_list|()
expr_stmt|;
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|readerContexts
init|=
name|refreshReader
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|readerContext
range|:
name|readerContexts
control|)
block|{
name|AtomicFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldData
operator|.
name|ramBytesUsed
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|minRamBytesUsed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SortedBinaryDocValues
name|bytesValues
init|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
decl_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|0
argument_list|,
name|two
argument_list|()
argument_list|,
name|four
argument_list|()
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|1
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|2
argument_list|,
name|three
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMissingValueForAll
specifier|public
name|void
name|testMissingValueForAll
parameter_list|()
throws|throws
name|Exception
block|{
name|fillAllMissing
argument_list|()
expr_stmt|;
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|readerContexts
init|=
name|refreshReader
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|readerContext
range|:
name|readerContexts
control|)
block|{
name|AtomicFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
decl_stmt|;
comment|// Some impls (FST) return size 0 and some (PagedBytes) do take size in the case no actual data is loaded
name|assertThat
argument_list|(
name|fieldData
operator|.
name|ramBytesUsed
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|SortedBinaryDocValues
name|bytesValues
init|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
decl_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|0
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|1
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|bytesValues
argument_list|,
literal|2
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|SortedBinaryDocValues
name|hashedBytesValues
init|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
decl_stmt|;
name|assertValues
argument_list|(
name|hashedBytesValues
argument_list|,
literal|0
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|hashedBytesValues
argument_list|,
literal|1
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|assertValues
argument_list|(
name|hashedBytesValues
argument_list|,
literal|2
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fillAllMissing
specifier|protected
specifier|abstract
name|void
name|fillAllMissing
parameter_list|()
throws|throws
name|Exception
function_decl|;
DECL|method|testSortMultiValuesFields
specifier|public
name|void
name|testSortMultiValuesFields
parameter_list|()
throws|throws
name|Exception
block|{
name|fillExtendedMvSet
argument_list|()
expr_stmt|;
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|)
argument_list|)
decl_stmt|;
name|SortField
name|sortField
init|=
name|indexFieldData
operator|.
name|sortField
argument_list|(
literal|null
argument_list|,
name|MultiValueMode
operator|.
name|MIN
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|TopFieldDocs
name|topDocs
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
name|sortField
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"!08"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"02"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"03"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"04"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"06"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"08"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|sortField
operator|=
name|indexFieldData
operator|.
name|sortField
argument_list|(
literal|null
argument_list|,
name|MultiValueMode
operator|.
name|MAX
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|topDocs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
name|sortField
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"08"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"06"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"04"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"03"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"!10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
operator|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
operator|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|fillExtendedMvSet
specifier|protected
specifier|abstract
name|void
name|fillExtendedMvSet
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
end_class

end_unit

