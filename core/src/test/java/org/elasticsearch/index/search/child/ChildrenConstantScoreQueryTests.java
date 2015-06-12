begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.child
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|child
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntHashSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectHashMap
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
name|MockAnalyzer
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
name|LeafReader
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
name|PostingsEnum
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
name|SlowCompositeReaderWrapper
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
name|Term
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
name|index
operator|.
name|TermsEnum
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
name|QueryWrapperFilter
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
name|TermQuery
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
name|join
operator|.
name|BitDocIdSetFilter
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
name|LuceneTestCase
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
name|lease
operator|.
name|Releasables
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
name|search
operator|.
name|Queries
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
name|engine
operator|.
name|Engine
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
name|ParentChildIndexFieldData
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
name|Uid
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
name|ParentFieldMapper
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
name|TypeFieldMapper
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
name|UidFieldMapper
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
name|query
operator|.
name|QueryBuilder
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
name|internal
operator|.
name|ContextIndexSearcher
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
name|internal
operator|.
name|SearchContext
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
name|TestSearchContext
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
name|org
operator|.
name|junit
operator|.
name|Test
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
name|NavigableSet
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
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|constantScoreQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|filteredQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|hasChildQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|notQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|termQuery
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
DECL|class|ChildrenConstantScoreQueryTests
specifier|public
class|class
name|ChildrenConstantScoreQueryTests
extends|extends
name|AbstractChildTests
block|{
annotation|@
name|BeforeClass
DECL|method|before
specifier|public
specifier|static
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|SearchContext
operator|.
name|setCurrent
argument_list|(
name|createSearchContext
argument_list|(
literal|"test"
argument_list|,
literal|"parent"
argument_list|,
literal|"child"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|after
specifier|public
specifier|static
name|void
name|after
parameter_list|()
throws|throws
name|IOException
block|{
name|SearchContext
name|current
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
name|SearchContext
operator|.
name|removeCurrent
argument_list|()
expr_stmt|;
name|Releasables
operator|.
name|close
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testBasicQuerySanities
specifier|public
name|void
name|testBasicQuerySanities
parameter_list|()
block|{
name|Query
name|childQuery
init|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
decl_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
literal|"child"
argument_list|)
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
init|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|parentFieldMapper
operator|.
name|fieldType
argument_list|()
argument_list|)
decl_stmt|;
name|BitDocIdSetFilter
name|parentFilter
init|=
name|wrapWithBitSetFilter
argument_list|(
operator|new
name|QueryWrapperFilter
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"parent"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|ChildrenConstantScoreQuery
argument_list|(
name|parentChildIndexFieldData
argument_list|,
name|childQuery
argument_list|,
literal|"parent"
argument_list|,
literal|"child"
argument_list|,
name|parentFilter
argument_list|,
literal|12
argument_list|,
name|wrapWithBitSetFilter
argument_list|(
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|QueryUtils
operator|.
name|check
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSimple
specifier|public
name|void
name|testSimple
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|directory
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|RandomIndexWriter
name|indexWriter
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|directory
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|parent
init|=
literal|1
init|;
name|parent
operator|<=
literal|5
condition|;
name|parent
operator|++
control|)
block|{
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
name|StringField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
literal|"parent"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|parent
argument_list|)
argument_list|)
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
name|StringField
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"parent"
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
for|for
control|(
name|int
name|child
init|=
literal|1
init|;
name|child
operator|<=
literal|3
condition|;
name|child
operator|++
control|)
block|{
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
name|StringField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
literal|"child"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|parent
operator|*
literal|3
operator|+
name|child
argument_list|)
argument_list|)
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
name|StringField
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"child"
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
name|StringField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
literal|"parent"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|parent
argument_list|)
argument_list|)
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
name|StringField
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
operator|+
name|child
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
block|}
block|}
name|IndexReader
name|indexReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
operator|.
name|w
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
operator|(
operator|(
name|TestSearchContext
operator|)
name|SearchContext
operator|.
name|current
argument_list|()
operator|)
operator|.
name|setSearcher
argument_list|(
operator|new
name|ContextIndexSearcher
argument_list|(
name|SearchContext
operator|.
name|current
argument_list|()
argument_list|,
operator|new
name|Engine
operator|.
name|Searcher
argument_list|(
name|ChildrenConstantScoreQueryTests
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|searcher
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|TermQuery
name|childQuery
init|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
operator|+
operator|(
literal|1
operator|+
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
operator|)
argument_list|)
argument_list|)
decl_stmt|;
name|BitDocIdSetFilter
name|parentFilter
init|=
name|wrapWithBitSetFilter
argument_list|(
operator|new
name|QueryWrapperFilter
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"parent"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|shortCircuitParentDocSet
init|=
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
literal|"child"
argument_list|)
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
init|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|parentFieldMapper
operator|.
name|fieldType
argument_list|()
argument_list|)
decl_stmt|;
name|ChildrenConstantScoreQuery
name|query
init|=
operator|new
name|ChildrenConstantScoreQuery
argument_list|(
name|parentChildIndexFieldData
argument_list|,
name|childQuery
argument_list|,
literal|"parent"
argument_list|,
literal|"child"
argument_list|,
name|parentFilter
argument_list|,
name|shortCircuitParentDocSet
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|BitSetCollector
name|collector
init|=
operator|new
name|BitSetCollector
argument_list|(
name|indexReader
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|FixedBitSet
name|actualResult
init|=
name|collector
operator|.
name|getResult
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|actualResult
operator|.
name|cardinality
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|directory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRandom
specifier|public
name|void
name|testRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|directory
init|=
name|newDirectory
argument_list|()
decl_stmt|;
specifier|final
name|Random
name|r
init|=
name|random
argument_list|()
decl_stmt|;
specifier|final
name|IndexWriterConfig
name|iwc
init|=
name|LuceneTestCase
operator|.
name|newIndexWriterConfig
argument_list|(
name|r
argument_list|,
operator|new
name|MockAnalyzer
argument_list|(
name|r
argument_list|)
argument_list|)
operator|.
name|setMaxBufferedDocs
argument_list|(
name|IndexWriterConfig
operator|.
name|DISABLE_AUTO_FLUSH
argument_list|)
operator|.
name|setRAMBufferSizeMB
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|16
argument_list|,
literal|64
argument_list|)
argument_list|)
decl_stmt|;
comment|// we might index a lot - don't go crazy here
name|RandomIndexWriter
name|indexWriter
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|r
argument_list|,
name|directory
argument_list|,
name|iwc
argument_list|)
decl_stmt|;
name|int
name|numUniqueChildValues
init|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|2000
argument_list|)
decl_stmt|;
name|String
index|[]
name|childValues
init|=
operator|new
name|String
index|[
name|numUniqueChildValues
index|]
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
name|numUniqueChildValues
condition|;
name|i
operator|++
control|)
block|{
name|childValues
index|[
name|i
index|]
operator|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|IntHashSet
name|filteredOrDeletedDocs
init|=
operator|new
name|IntHashSet
argument_list|()
decl_stmt|;
name|int
name|childDocId
init|=
literal|0
decl_stmt|;
name|int
name|numParentDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
name|numUniqueChildValues
argument_list|)
decl_stmt|;
name|ObjectObjectHashMap
argument_list|<
name|String
argument_list|,
name|NavigableSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|childValueToParentIds
init|=
operator|new
name|ObjectObjectHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|parentDocId
init|=
literal|0
init|;
name|parentDocId
operator|<
name|numParentDocs
condition|;
name|parentDocId
operator|++
control|)
block|{
name|boolean
name|markParentAsDeleted
init|=
name|rarely
argument_list|()
decl_stmt|;
name|boolean
name|filterMe
init|=
name|rarely
argument_list|()
decl_stmt|;
name|String
name|parent
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|parentDocId
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
name|StringField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
literal|"parent"
argument_list|,
name|parent
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"parent"
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
name|markParentAsDeleted
condition|)
block|{
name|filteredOrDeletedDocs
operator|.
name|add
argument_list|(
name|parentDocId
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"delete"
argument_list|,
literal|"me"
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
name|filterMe
condition|)
block|{
name|filteredOrDeletedDocs
operator|.
name|add
argument_list|(
name|parentDocId
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"filter"
argument_list|,
literal|"me"
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
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numChildDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
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
name|numChildDocs
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|markChildAsDeleted
init|=
name|rarely
argument_list|()
decl_stmt|;
name|String
name|childValue
init|=
name|childValues
index|[
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
name|childValues
operator|.
name|length
argument_list|)
index|]
decl_stmt|;
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
name|StringField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
literal|"child"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|childDocId
operator|++
argument_list|)
argument_list|)
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
name|StringField
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"child"
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
name|StringField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
literal|"parent"
argument_list|,
name|parent
argument_list|)
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
name|StringField
argument_list|(
literal|"field1"
argument_list|,
name|childValue
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
name|markChildAsDeleted
condition|)
block|{
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"delete"
argument_list|,
literal|"me"
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
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|markChildAsDeleted
condition|)
block|{
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|parentIds
decl_stmt|;
if|if
condition|(
name|childValueToParentIds
operator|.
name|containsKey
argument_list|(
name|childValue
argument_list|)
condition|)
block|{
name|parentIds
operator|=
name|childValueToParentIds
operator|.
name|get
argument_list|(
name|childValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|childValueToParentIds
operator|.
name|put
argument_list|(
name|childValue
argument_list|,
name|parentIds
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|markParentAsDeleted
operator|&&
operator|!
name|filterMe
condition|)
block|{
name|parentIds
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// Delete docs that are marked to be deleted.
name|indexWriter
operator|.
name|deleteDocuments
argument_list|(
operator|new
name|Term
argument_list|(
literal|"delete"
argument_list|,
literal|"me"
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|IndexReader
name|indexReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
name|Engine
operator|.
name|Searcher
name|engineSearcher
init|=
operator|new
name|Engine
operator|.
name|Searcher
argument_list|(
name|ChildrenConstantScoreQueryTests
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|searcher
argument_list|)
decl_stmt|;
operator|(
operator|(
name|TestSearchContext
operator|)
name|SearchContext
operator|.
name|current
argument_list|()
operator|)
operator|.
name|setSearcher
argument_list|(
operator|new
name|ContextIndexSearcher
argument_list|(
name|SearchContext
operator|.
name|current
argument_list|()
argument_list|,
name|engineSearcher
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|max
init|=
name|numUniqueChildValues
operator|/
literal|4
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
name|max
condition|;
name|i
operator|++
control|)
block|{
comment|// Simulate a parent update
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
specifier|final
name|int
name|numberOfUpdatableParents
init|=
name|numParentDocs
operator|-
name|filteredOrDeletedDocs
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|numberOfUpdates
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
name|numberOfUpdatableParents
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numberOfUpdates
condition|;
name|j
operator|++
control|)
block|{
name|int
name|parentId
decl_stmt|;
do|do
block|{
name|parentId
operator|=
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
name|numParentDocs
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|filteredOrDeletedDocs
operator|.
name|contains
argument_list|(
name|parentId
argument_list|)
condition|)
do|;
name|String
name|parentUid
init|=
name|Uid
operator|.
name|createUid
argument_list|(
literal|"parent"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|parentId
argument_list|)
argument_list|)
decl_stmt|;
name|indexWriter
operator|.
name|deleteDocuments
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|parentUid
argument_list|)
argument_list|)
expr_stmt|;
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
name|StringField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|parentUid
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"parent"
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
block|}
name|indexReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexReader
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
operator|.
name|w
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|searcher
operator|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
expr_stmt|;
name|engineSearcher
operator|=
operator|new
name|Engine
operator|.
name|Searcher
argument_list|(
name|ChildrenConstantScoreQueryTests
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|searcher
argument_list|)
expr_stmt|;
operator|(
operator|(
name|TestSearchContext
operator|)
name|SearchContext
operator|.
name|current
argument_list|()
operator|)
operator|.
name|setSearcher
argument_list|(
operator|new
name|ContextIndexSearcher
argument_list|(
name|SearchContext
operator|.
name|current
argument_list|()
argument_list|,
name|engineSearcher
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|childValue
init|=
name|childValues
index|[
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
name|numUniqueChildValues
argument_list|)
index|]
decl_stmt|;
name|int
name|shortCircuitParentDocSet
init|=
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
name|numParentDocs
argument_list|)
decl_stmt|;
name|QueryBuilder
name|queryBuilder
decl_stmt|;
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|queryBuilder
operator|=
name|hasChildQuery
argument_list|(
literal|"child"
argument_list|,
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
name|childValue
argument_list|)
argument_list|)
operator|.
name|setShortCircuitCutoff
argument_list|(
name|shortCircuitParentDocSet
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|queryBuilder
operator|=
name|constantScoreQuery
argument_list|(
name|hasChildQuery
argument_list|(
literal|"child"
argument_list|,
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
name|childValue
argument_list|)
argument_list|)
operator|.
name|setShortCircuitCutoff
argument_list|(
name|shortCircuitParentDocSet
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Using a FQ, will invoke / test the Scorer#advance(..) and also let the Weight#scorer not get live docs as acceptedDocs
name|queryBuilder
operator|=
name|filteredQuery
argument_list|(
name|queryBuilder
argument_list|,
name|notQuery
argument_list|(
name|termQuery
argument_list|(
literal|"filter"
argument_list|,
literal|"me"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|parseQuery
argument_list|(
name|queryBuilder
argument_list|)
decl_stmt|;
name|BitSetCollector
name|collector
init|=
operator|new
name|BitSetCollector
argument_list|(
name|indexReader
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|FixedBitSet
name|actualResult
init|=
name|collector
operator|.
name|getResult
argument_list|()
decl_stmt|;
name|FixedBitSet
name|expectedResult
init|=
operator|new
name|FixedBitSet
argument_list|(
name|indexReader
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|childValueToParentIds
operator|.
name|containsKey
argument_list|(
name|childValue
argument_list|)
condition|)
block|{
name|LeafReader
name|slowLeafReader
init|=
name|SlowCompositeReaderWrapper
operator|.
name|wrap
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
name|Terms
name|terms
init|=
name|slowLeafReader
operator|.
name|terms
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|!=
literal|null
condition|)
block|{
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|parentIds
init|=
name|childValueToParentIds
operator|.
name|get
argument_list|(
name|childValue
argument_list|)
decl_stmt|;
name|TermsEnum
name|termsEnum
init|=
name|terms
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|PostingsEnum
name|docsEnum
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|id
range|:
name|parentIds
control|)
block|{
name|TermsEnum
operator|.
name|SeekStatus
name|seekStatus
init|=
name|termsEnum
operator|.
name|seekCeil
argument_list|(
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
literal|"parent"
argument_list|,
name|id
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|seekStatus
operator|==
name|TermsEnum
operator|.
name|SeekStatus
operator|.
name|FOUND
condition|)
block|{
name|docsEnum
operator|=
name|termsEnum
operator|.
name|postings
argument_list|(
name|slowLeafReader
operator|.
name|getLiveDocs
argument_list|()
argument_list|,
name|docsEnum
argument_list|,
name|PostingsEnum
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|expectedResult
operator|.
name|set
argument_list|(
name|docsEnum
operator|.
name|nextDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|seekStatus
operator|==
name|TermsEnum
operator|.
name|SeekStatus
operator|.
name|END
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|assertBitSet
argument_list|(
name|actualResult
argument_list|,
name|expectedResult
argument_list|,
name|searcher
argument_list|)
expr_stmt|;
block|}
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|directory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
