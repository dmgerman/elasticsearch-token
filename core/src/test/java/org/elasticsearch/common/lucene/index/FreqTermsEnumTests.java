begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
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
name|analysis
operator|.
name|core
operator|.
name|KeywordAnalyzer
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
name|document
operator|.
name|TextField
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
name|index
operator|.
name|IndexableField
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
name|NoMergePolicy
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
name|search
operator|.
name|TermInSetQuery
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
name|IOUtils
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
name|common
operator|.
name|util
operator|.
name|BigArrays
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
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|is
import|;
end_import

begin_class
DECL|class|FreqTermsEnumTests
specifier|public
class|class
name|FreqTermsEnumTests
extends|extends
name|ESTestCase
block|{
DECL|field|terms
specifier|private
name|String
index|[]
name|terms
decl_stmt|;
DECL|field|iw
specifier|private
name|IndexWriter
name|iw
decl_stmt|;
DECL|field|reader
specifier|private
name|IndexReader
name|reader
decl_stmt|;
DECL|field|referenceAll
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FreqHolder
argument_list|>
name|referenceAll
decl_stmt|;
DECL|field|referenceNotDeleted
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FreqHolder
argument_list|>
name|referenceNotDeleted
decl_stmt|;
DECL|field|referenceFilter
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FreqHolder
argument_list|>
name|referenceFilter
decl_stmt|;
DECL|field|filter
specifier|private
name|Query
name|filter
decl_stmt|;
DECL|class|FreqHolder
specifier|static
class|class
name|FreqHolder
block|{
DECL|field|docFreq
name|int
name|docFreq
decl_stmt|;
DECL|field|totalTermFreq
name|long
name|totalTermFreq
decl_stmt|;
block|}
annotation|@
name|Before
annotation|@
name|Override
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|referenceAll
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|referenceNotDeleted
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|referenceFilter
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriterConfig
name|conf
init|=
name|newIndexWriterConfig
argument_list|(
operator|new
name|KeywordAnalyzer
argument_list|()
argument_list|)
decl_stmt|;
comment|// use keyword analyzer we rely on the stored field holding the exact term.
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
comment|// we don't want to do any merges, so we won't expunge deletes
name|conf
operator|.
name|setMergePolicy
argument_list|(
name|NoMergePolicy
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
name|iw
operator|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|terms
operator|=
operator|new
name|String
index|[
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|300
argument_list|)
index|]
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
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|terms
index|[
name|i
index|]
operator|=
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
name|int
name|numberOfDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|30
argument_list|,
literal|300
argument_list|)
decl_stmt|;
name|Document
index|[]
name|docs
init|=
operator|new
name|Document
index|[
name|numberOfDocs
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
name|numberOfDocs
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
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
name|docs
index|[
name|i
index|]
operator|=
name|doc
expr_stmt|;
for|for
control|(
name|String
name|term
range|:
name|terms
control|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|int
name|freq
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|3
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
name|freq
condition|;
name|j
operator|++
control|)
block|{
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"field"
argument_list|,
name|term
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// add all docs
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|docs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|doc
init|=
name|docs
index|[
name|i
index|]
decl_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|deletedIds
init|=
operator|new
name|HashSet
argument_list|<>
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
name|docs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|doc
init|=
name|docs
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|randomInt
argument_list|(
literal|5
argument_list|)
operator|==
literal|2
condition|)
block|{
name|Term
name|idTerm
init|=
operator|new
name|Term
argument_list|(
literal|"id"
argument_list|,
name|doc
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
decl_stmt|;
name|deletedIds
operator|.
name|add
argument_list|(
name|idTerm
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
name|iw
operator|.
name|deleteDocuments
argument_list|(
name|idTerm
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|term
range|:
name|terms
control|)
block|{
name|referenceAll
operator|.
name|put
argument_list|(
name|term
argument_list|,
operator|new
name|FreqHolder
argument_list|()
argument_list|)
expr_stmt|;
name|referenceFilter
operator|.
name|put
argument_list|(
name|term
argument_list|,
operator|new
name|FreqHolder
argument_list|()
argument_list|)
expr_stmt|;
name|referenceNotDeleted
operator|.
name|put
argument_list|(
name|term
argument_list|,
operator|new
name|FreqHolder
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// now go over each doc, build the relevant references and filter
name|reader
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|iw
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|BytesRef
argument_list|>
name|filterTerms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|docId
init|=
literal|0
init|;
name|docId
operator|<
name|reader
operator|.
name|maxDoc
argument_list|()
condition|;
name|docId
operator|++
control|)
block|{
name|Document
name|doc
init|=
name|reader
operator|.
name|document
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|addFreqs
argument_list|(
name|doc
argument_list|,
name|referenceAll
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|deletedIds
operator|.
name|contains
argument_list|(
name|doc
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
condition|)
block|{
name|addFreqs
argument_list|(
name|doc
argument_list|,
name|referenceNotDeleted
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|filterTerms
operator|.
name|add
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|doc
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|addFreqs
argument_list|(
name|doc
argument_list|,
name|referenceFilter
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|filter
operator|=
operator|new
name|TermInSetQuery
argument_list|(
literal|"id"
argument_list|,
name|filterTerms
argument_list|)
expr_stmt|;
block|}
DECL|method|addFreqs
specifier|private
name|void
name|addFreqs
parameter_list|(
name|Document
name|doc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|FreqHolder
argument_list|>
name|reference
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|addedDocFreq
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexableField
name|field
range|:
name|doc
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
control|)
block|{
name|String
name|term
init|=
name|field
operator|.
name|stringValue
argument_list|()
decl_stmt|;
name|FreqHolder
name|freqHolder
init|=
name|reference
operator|.
name|get
argument_list|(
name|term
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|addedDocFreq
operator|.
name|contains
argument_list|(
name|term
argument_list|)
condition|)
block|{
name|freqHolder
operator|.
name|docFreq
operator|++
expr_stmt|;
name|addedDocFreq
operator|.
name|add
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
name|freqHolder
operator|.
name|totalTermFreq
operator|++
expr_stmt|;
block|}
block|}
annotation|@
name|After
annotation|@
name|Override
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|reader
argument_list|,
name|iw
argument_list|,
name|iw
operator|.
name|getDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
DECL|method|testAllFreqs
specifier|public
name|void
name|testAllFreqs
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAgainstReference
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
name|referenceAll
argument_list|)
expr_stmt|;
name|assertAgainstReference
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
name|referenceAll
argument_list|)
expr_stmt|;
name|assertAgainstReference
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
name|referenceAll
argument_list|)
expr_stmt|;
block|}
DECL|method|testNonDeletedFreqs
specifier|public
name|void
name|testNonDeletedFreqs
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAgainstReference
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
argument_list|,
name|referenceNotDeleted
argument_list|)
expr_stmt|;
name|assertAgainstReference
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
argument_list|,
name|referenceNotDeleted
argument_list|)
expr_stmt|;
name|assertAgainstReference
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
argument_list|,
name|referenceNotDeleted
argument_list|)
expr_stmt|;
block|}
DECL|method|testFilterFreqs
specifier|public
name|void
name|testFilterFreqs
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAgainstReference
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
name|filter
argument_list|,
name|referenceFilter
argument_list|)
expr_stmt|;
name|assertAgainstReference
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|filter
argument_list|,
name|referenceFilter
argument_list|)
expr_stmt|;
name|assertAgainstReference
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
name|filter
argument_list|,
name|referenceFilter
argument_list|)
expr_stmt|;
block|}
DECL|method|assertAgainstReference
specifier|private
name|void
name|assertAgainstReference
parameter_list|(
name|boolean
name|docFreq
parameter_list|,
name|boolean
name|totalTermFreq
parameter_list|,
name|Query
name|filter
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|FreqHolder
argument_list|>
name|reference
parameter_list|)
throws|throws
name|Exception
block|{
name|FreqTermsEnum
name|freqTermsEnum
init|=
operator|new
name|FreqTermsEnum
argument_list|(
name|reader
argument_list|,
literal|"field"
argument_list|,
name|docFreq
argument_list|,
name|totalTermFreq
argument_list|,
name|filter
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|)
decl_stmt|;
name|assertAgainstReference
argument_list|(
name|freqTermsEnum
argument_list|,
name|reference
argument_list|,
name|docFreq
argument_list|,
name|totalTermFreq
argument_list|)
expr_stmt|;
block|}
DECL|method|assertAgainstReference
specifier|private
name|void
name|assertAgainstReference
parameter_list|(
name|FreqTermsEnum
name|termsEnum
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|FreqHolder
argument_list|>
name|reference
parameter_list|,
name|boolean
name|docFreq
parameter_list|,
name|boolean
name|totalTermFreq
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|cycles
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
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
name|cycles
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|terms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|this
operator|.
name|terms
argument_list|)
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|terms
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|term
range|:
name|terms
control|)
block|{
if|if
condition|(
operator|!
name|termsEnum
operator|.
name|seekExact
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|term
argument_list|)
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
literal|"term : "
operator|+
name|term
argument_list|,
name|reference
operator|.
name|get
argument_list|(
name|term
argument_list|)
operator|.
name|docFreq
argument_list|,
name|is
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|docFreq
condition|)
block|{
name|assertThat
argument_list|(
literal|"cycle "
operator|+
name|i
operator|+
literal|", term "
operator|+
name|term
operator|+
literal|", docFreq"
argument_list|,
name|termsEnum
operator|.
name|docFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|reference
operator|.
name|get
argument_list|(
name|term
argument_list|)
operator|.
name|docFreq
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|totalTermFreq
condition|)
block|{
name|assertThat
argument_list|(
literal|"cycle "
operator|+
name|i
operator|+
literal|", term "
operator|+
name|term
operator|+
literal|", totalTermFreq"
argument_list|,
name|termsEnum
operator|.
name|totalTermFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|reference
operator|.
name|get
argument_list|(
name|term
argument_list|)
operator|.
name|totalTermFreq
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

