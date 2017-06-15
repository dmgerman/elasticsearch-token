begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.queries
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queries
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
name|SortedDocValuesField
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
name|RandomIndexWriter
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
name|ReaderUtil
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
name|QueryUtils
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
name|ScoreDoc
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|TopDocs
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
name|FixedBitSet
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
name|ESTestCase
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|SearchAfterSortedDocQueryTests
specifier|public
class|class
name|SearchAfterSortedDocQueryTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBasics
specifier|public
name|void
name|testBasics
parameter_list|()
block|{
name|Sort
name|sort1
init|=
operator|new
name|Sort
argument_list|(
operator|new
name|SortedNumericSortField
argument_list|(
literal|"field1"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|INT
argument_list|)
argument_list|,
operator|new
name|SortedSetSortField
argument_list|(
literal|"field2"
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|Sort
name|sort2
init|=
operator|new
name|Sort
argument_list|(
operator|new
name|SortedNumericSortField
argument_list|(
literal|"field1"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|INT
argument_list|)
argument_list|,
operator|new
name|SortedSetSortField
argument_list|(
literal|"field3"
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|FieldDoc
name|fieldDoc1
init|=
operator|new
name|FieldDoc
argument_list|(
literal|0
argument_list|,
literal|0f
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|5
block|,
operator|new
name|BytesRef
argument_list|(
literal|"foo"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|FieldDoc
name|fieldDoc2
init|=
operator|new
name|FieldDoc
argument_list|(
literal|0
argument_list|,
literal|0f
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|5
block|,
operator|new
name|BytesRef
argument_list|(
literal|"foo"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|SearchAfterSortedDocQuery
name|query1
init|=
operator|new
name|SearchAfterSortedDocQuery
argument_list|(
name|sort1
argument_list|,
name|fieldDoc1
argument_list|)
decl_stmt|;
name|SearchAfterSortedDocQuery
name|query2
init|=
operator|new
name|SearchAfterSortedDocQuery
argument_list|(
name|sort1
argument_list|,
name|fieldDoc2
argument_list|)
decl_stmt|;
name|SearchAfterSortedDocQuery
name|query3
init|=
operator|new
name|SearchAfterSortedDocQuery
argument_list|(
name|sort2
argument_list|,
name|fieldDoc2
argument_list|)
decl_stmt|;
name|QueryUtils
operator|.
name|check
argument_list|(
name|query1
argument_list|)
expr_stmt|;
name|QueryUtils
operator|.
name|checkEqual
argument_list|(
name|query1
argument_list|,
name|query2
argument_list|)
expr_stmt|;
name|QueryUtils
operator|.
name|checkUnequal
argument_list|(
name|query1
argument_list|,
name|query3
argument_list|)
expr_stmt|;
block|}
DECL|method|testInvalidSort
specifier|public
name|void
name|testInvalidSort
parameter_list|()
block|{
name|Sort
name|sort
init|=
operator|new
name|Sort
argument_list|(
operator|new
name|SortedNumericSortField
argument_list|(
literal|"field1"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|INT
argument_list|)
argument_list|)
decl_stmt|;
name|FieldDoc
name|fieldDoc
init|=
operator|new
name|FieldDoc
argument_list|(
literal|0
argument_list|,
literal|0f
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|4
block|,
literal|5
block|}
argument_list|)
decl_stmt|;
name|IllegalArgumentException
name|ex
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|SearchAfterSortedDocQuery
argument_list|(
name|sort
argument_list|,
name|fieldDoc
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"after doc  has 2 value(s) but sort has 1."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRandom
specifier|public
name|void
name|testRandom
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|200
argument_list|)
decl_stmt|;
specifier|final
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
specifier|final
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|Sort
name|sort
init|=
operator|new
name|Sort
argument_list|(
operator|new
name|SortedNumericSortField
argument_list|(
literal|"number1"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|INT
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
argument_list|,
operator|new
name|SortField
argument_list|(
literal|"string"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|IndexWriterConfig
name|config
init|=
operator|new
name|IndexWriterConfig
argument_list|()
decl_stmt|;
name|config
operator|.
name|setIndexSort
argument_list|(
name|sort
argument_list|)
expr_stmt|;
specifier|final
name|RandomIndexWriter
name|w
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|dir
argument_list|,
name|config
argument_list|)
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
name|int
name|rand
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|SortedNumericDocValuesField
argument_list|(
literal|"number"
argument_list|,
name|rand
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|SortedDocValuesField
argument_list|(
literal|"string"
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|randomAlphaOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|50
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|doc
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|w
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
block|}
specifier|final
name|IndexReader
name|reader
init|=
name|w
operator|.
name|getReader
argument_list|()
decl_stmt|;
specifier|final
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|int
name|step
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|FixedBitSet
name|bitSet
init|=
operator|new
name|FixedBitSet
argument_list|(
name|numDocs
argument_list|)
decl_stmt|;
name|TopDocs
name|topDocs
init|=
literal|null
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
name|numDocs
condition|;
control|)
block|{
if|if
condition|(
name|topDocs
operator|!=
literal|null
condition|)
block|{
name|FieldDoc
name|after
init|=
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|topDocs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|SearchAfterSortedDocQuery
argument_list|(
name|sort
argument_list|,
name|after
argument_list|)
argument_list|,
name|step
argument_list|,
name|sort
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|step
argument_list|,
name|sort
argument_list|)
expr_stmt|;
block|}
name|i
operator|+=
name|step
expr_stmt|;
for|for
control|(
name|ScoreDoc
name|topDoc
range|:
name|topDocs
operator|.
name|scoreDocs
control|)
block|{
name|int
name|readerIndex
init|=
name|ReaderUtil
operator|.
name|subIndex
argument_list|(
name|topDoc
operator|.
name|doc
argument_list|,
name|reader
operator|.
name|leaves
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|LeafReaderContext
name|leafReaderContext
init|=
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
name|readerIndex
argument_list|)
decl_stmt|;
name|int
name|docRebase
init|=
name|topDoc
operator|.
name|doc
operator|-
name|leafReaderContext
operator|.
name|docBase
decl_stmt|;
if|if
condition|(
name|leafReaderContext
operator|.
name|reader
argument_list|()
operator|.
name|hasDeletions
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|leafReaderContext
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
operator|.
name|get
argument_list|(
name|docRebase
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|bitSet
operator|.
name|get
argument_list|(
name|topDoc
operator|.
name|doc
argument_list|)
argument_list|)
expr_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
name|topDoc
operator|.
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|bitSet
operator|.
name|cardinality
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|reader
operator|.
name|numDocs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

