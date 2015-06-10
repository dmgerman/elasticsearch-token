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
name|FloatArrayList
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
name|IntIntHashMap
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
name|DocIdSetIterator
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
name|LeafCollector
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
name|MultiCollector
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
name|TopScoreDocCollector
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
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
name|TreeMap
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
name|hasParentQuery
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

begin_class
DECL|class|ParentQueryTests
specifier|public
class|class
name|ParentQueryTests
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
name|parentQuery
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
name|childrenFilter
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
literal|"child"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|ParentQuery
argument_list|(
name|parentChildIndexFieldData
argument_list|,
name|parentQuery
argument_list|,
literal|"parent"
argument_list|,
name|childrenFilter
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
name|numUniqueParentValues
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
name|parentValues
init|=
operator|new
name|String
index|[
name|numUniqueParentValues
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
name|numUniqueParentValues
condition|;
name|i
operator|++
control|)
block|{
name|parentValues
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
name|numUniqueParentValues
argument_list|)
decl_stmt|;
name|ObjectObjectHashMap
argument_list|<
name|String
argument_list|,
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
argument_list|>
name|parentValueToChildIds
init|=
operator|new
name|ObjectObjectHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|IntIntHashMap
name|childIdToParentId
init|=
operator|new
name|IntIntHashMap
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
name|String
name|parentValue
init|=
name|parentValues
index|[
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
name|parentValues
operator|.
name|length
argument_list|)
index|]
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
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"field1"
argument_list|,
name|parentValue
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
if|if
condition|(
name|parentDocId
operator|==
name|numParentDocs
operator|-
literal|1
operator|&&
name|childIdToParentId
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// ensure there is at least one child in the index
name|numChildDocs
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|numChildDocs
argument_list|)
expr_stmt|;
block|}
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
name|String
name|child
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|childDocId
operator|++
argument_list|)
decl_stmt|;
name|boolean
name|markChildAsDeleted
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
name|child
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
if|if
condition|(
name|filterMe
condition|)
block|{
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
if|if
condition|(
operator|!
name|markParentAsDeleted
condition|)
block|{
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|childIdToScore
init|=
name|parentValueToChildIds
operator|.
name|getOrDefault
argument_list|(
name|parentValue
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|childIdToScore
operator|==
literal|null
condition|)
block|{
name|parentValueToChildIds
operator|.
name|put
argument_list|(
name|parentValue
argument_list|,
name|childIdToScore
operator|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|markChildAsDeleted
operator|&&
operator|!
name|filterMe
condition|)
block|{
name|assertFalse
argument_list|(
literal|"child ["
operator|+
name|child
operator|+
literal|"] already has a score"
argument_list|,
name|childIdToScore
operator|.
name|containsKey
argument_list|(
name|child
argument_list|)
argument_list|)
expr_stmt|;
name|childIdToScore
operator|.
name|put
argument_list|(
name|child
argument_list|,
literal|1f
argument_list|)
expr_stmt|;
name|childIdToParentId
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|child
argument_list|)
argument_list|,
name|parentDocId
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
name|ParentQueryTests
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
name|numUniqueParentValues
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
comment|// Simulate a child update
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|int
name|numberOfUpdates
init|=
name|childIdToParentId
operator|.
name|isEmpty
argument_list|()
condition|?
literal|0
else|:
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|int
index|[]
name|childIds
init|=
name|childIdToParentId
operator|.
name|keys
argument_list|()
operator|.
name|toArray
argument_list|()
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
name|childId
init|=
name|childIds
index|[
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
name|childIds
operator|.
name|length
argument_list|)
index|]
decl_stmt|;
name|String
name|childUid
init|=
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
name|childId
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
name|childUid
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
name|childUid
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
name|childIdToParentId
operator|.
name|get
argument_list|(
name|childId
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
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
name|parentUid
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
name|ParentConstantScoreQueryTests
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
name|parentValue
init|=
name|parentValues
index|[
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
name|numUniqueParentValues
argument_list|)
index|]
decl_stmt|;
name|QueryBuilder
name|queryBuilder
init|=
name|hasParentQuery
argument_list|(
literal|"parent"
argument_list|,
name|constantScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
name|parentValue
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
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
name|int
name|numHits
init|=
literal|1
operator|+
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|25
argument_list|)
decl_stmt|;
name|TopScoreDocCollector
name|actualTopDocsCollector
init|=
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|numHits
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|MultiCollector
operator|.
name|wrap
argument_list|(
name|collector
argument_list|,
name|actualTopDocsCollector
argument_list|)
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
name|TopScoreDocCollector
name|expectedTopDocsCollector
init|=
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|numHits
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentValueToChildIds
operator|.
name|containsKey
argument_list|(
name|parentValue
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
specifier|final
name|FloatArrayList
index|[]
name|scores
init|=
operator|new
name|FloatArrayList
index|[
name|slowLeafReader
operator|.
name|maxDoc
argument_list|()
index|]
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
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|childIdsAndScore
init|=
name|parentValueToChildIds
operator|.
name|get
argument_list|(
name|parentValue
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
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|entry
range|:
name|childIdsAndScore
operator|.
name|entrySet
argument_list|()
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
literal|"child"
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
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
name|FloatArrayList
name|s
init|=
name|scores
index|[
name|docsEnum
operator|.
name|docID
argument_list|()
index|]
decl_stmt|;
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
name|scores
index|[
name|docsEnum
operator|.
name|docID
argument_list|()
index|]
operator|=
name|s
operator|=
operator|new
name|FloatArrayList
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
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
name|MockScorer
name|mockScorer
init|=
operator|new
name|MockScorer
argument_list|(
name|ScoreType
operator|.
name|MAX
argument_list|)
decl_stmt|;
name|mockScorer
operator|.
name|scores
operator|=
operator|new
name|FloatArrayList
argument_list|()
expr_stmt|;
specifier|final
name|LeafCollector
name|leafCollector
init|=
name|expectedTopDocsCollector
operator|.
name|getLeafCollector
argument_list|(
name|slowLeafReader
operator|.
name|getContext
argument_list|()
argument_list|)
decl_stmt|;
name|leafCollector
operator|.
name|setScorer
argument_list|(
name|mockScorer
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|doc
init|=
name|expectedResult
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
init|;
name|doc
operator|<
name|slowLeafReader
operator|.
name|maxDoc
argument_list|()
condition|;
name|doc
operator|=
name|doc
operator|+
literal|1
operator|>=
name|expectedResult
operator|.
name|length
argument_list|()
condition|?
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
else|:
name|expectedResult
operator|.
name|nextSetBit
argument_list|(
name|doc
operator|+
literal|1
argument_list|)
control|)
block|{
name|mockScorer
operator|.
name|scores
operator|.
name|clear
argument_list|()
expr_stmt|;
name|mockScorer
operator|.
name|scores
operator|.
name|addAll
argument_list|(
name|scores
index|[
name|doc
index|]
argument_list|)
expr_stmt|;
name|leafCollector
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
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
name|assertTopDocs
argument_list|(
name|actualTopDocsCollector
operator|.
name|topDocs
argument_list|()
argument_list|,
name|expectedTopDocsCollector
operator|.
name|topDocs
argument_list|()
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

