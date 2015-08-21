begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|query
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
operator|.
name|Store
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
name|MultiReader
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
name|BooleanClause
operator|.
name|Occur
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
name|BooleanQuery
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
name|Collector
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
name|ConstantScoreQuery
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
name|MatchNoDocsQuery
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
name|Weight
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
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|ParsedQuery
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
name|elasticsearch
operator|.
name|test
operator|.
name|TestSearchContext
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_class
DECL|class|QueryPhaseTests
specifier|public
class|class
name|QueryPhaseTests
extends|extends
name|ESTestCase
block|{
DECL|method|countTestCase
specifier|private
name|void
name|countTestCase
parameter_list|(
name|Query
name|query
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|boolean
name|shouldCollect
parameter_list|)
throws|throws
name|Exception
block|{
name|TestSearchContext
name|context
init|=
operator|new
name|TestSearchContext
argument_list|()
decl_stmt|;
name|context
operator|.
name|parsedQuery
argument_list|(
operator|new
name|ParsedQuery
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|collected
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|IndexSearcher
name|contextSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
block|{
specifier|protected
name|void
name|search
parameter_list|(
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
parameter_list|,
name|Weight
name|weight
parameter_list|,
name|Collector
name|collector
parameter_list|)
throws|throws
name|IOException
block|{
name|collected
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|super
operator|.
name|search
argument_list|(
name|leaves
argument_list|,
name|weight
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|final
name|boolean
name|rescore
init|=
name|QueryPhase
operator|.
name|execute
argument_list|(
name|context
argument_list|,
name|contextSearcher
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|rescore
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|searcher
operator|.
name|count
argument_list|(
name|query
argument_list|)
argument_list|,
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shouldCollect
argument_list|,
name|collected
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|countTestCase
specifier|private
name|void
name|countTestCase
parameter_list|(
name|boolean
name|withDeletions
parameter_list|)
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriterConfig
name|iwc
init|=
name|newIndexWriterConfig
argument_list|()
operator|.
name|setMergePolicy
argument_list|(
name|NoMergePolicy
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|RandomIndexWriter
name|w
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|dir
argument_list|,
name|iwc
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|200
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
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|doc
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|doc
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"foo"
argument_list|,
literal|"baz"
argument_list|,
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|withDeletions
operator|&&
operator|(
name|rarely
argument_list|()
operator|||
name|i
operator|==
literal|0
operator|)
condition|)
block|{
name|doc
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"delete"
argument_list|,
literal|"yes"
argument_list|,
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|withDeletions
condition|)
block|{
name|w
operator|.
name|deleteDocuments
argument_list|(
operator|new
name|Term
argument_list|(
literal|"delete"
argument_list|,
literal|"yes"
argument_list|)
argument_list|)
expr_stmt|;
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
name|Query
name|matchAll
init|=
operator|new
name|MatchAllDocsQuery
argument_list|()
decl_stmt|;
name|Query
name|matchAllCsq
init|=
operator|new
name|ConstantScoreQuery
argument_list|(
name|matchAll
argument_list|)
decl_stmt|;
name|Query
name|tq
init|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
decl_stmt|;
name|Query
name|tCsq
init|=
operator|new
name|ConstantScoreQuery
argument_list|(
name|tq
argument_list|)
decl_stmt|;
name|BooleanQuery
name|bq
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
name|bq
operator|.
name|add
argument_list|(
name|matchAll
argument_list|,
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|bq
operator|.
name|add
argument_list|(
name|tq
argument_list|,
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|countTestCase
argument_list|(
name|matchAll
argument_list|,
name|reader
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|countTestCase
argument_list|(
name|matchAllCsq
argument_list|,
name|reader
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|countTestCase
argument_list|(
name|tq
argument_list|,
name|reader
argument_list|,
name|withDeletions
argument_list|)
expr_stmt|;
name|countTestCase
argument_list|(
name|tCsq
argument_list|,
name|reader
argument_list|,
name|withDeletions
argument_list|)
expr_stmt|;
name|countTestCase
argument_list|(
name|bq
argument_list|,
name|reader
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|w
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
DECL|method|testCountWithoutDeletions
specifier|public
name|void
name|testCountWithoutDeletions
parameter_list|()
throws|throws
name|Exception
block|{
name|countTestCase
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testCountWithDeletions
specifier|public
name|void
name|testCountWithDeletions
parameter_list|()
throws|throws
name|Exception
block|{
name|countTestCase
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|testPostFilterDisablesCountOptimization
specifier|public
name|void
name|testPostFilterDisablesCountOptimization
parameter_list|()
throws|throws
name|Exception
block|{
name|TestSearchContext
name|context
init|=
operator|new
name|TestSearchContext
argument_list|()
decl_stmt|;
name|context
operator|.
name|parsedQuery
argument_list|(
operator|new
name|ParsedQuery
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|collected
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|IndexSearcher
name|contextSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
operator|new
name|MultiReader
argument_list|()
argument_list|)
block|{
specifier|protected
name|void
name|search
parameter_list|(
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
parameter_list|,
name|Weight
name|weight
parameter_list|,
name|Collector
name|collector
parameter_list|)
throws|throws
name|IOException
block|{
name|collected
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|super
operator|.
name|search
argument_list|(
name|leaves
argument_list|,
name|weight
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|QueryPhase
operator|.
name|execute
argument_list|(
name|context
argument_list|,
name|contextSearcher
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|collected
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|parsedPostFilter
argument_list|(
operator|new
name|ParsedQuery
argument_list|(
operator|new
name|MatchNoDocsQuery
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|QueryPhase
operator|.
name|execute
argument_list|(
name|context
argument_list|,
name|contextSearcher
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|collected
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMinScoreDisablesCountOptimization
specifier|public
name|void
name|testMinScoreDisablesCountOptimization
parameter_list|()
throws|throws
name|Exception
block|{
name|TestSearchContext
name|context
init|=
operator|new
name|TestSearchContext
argument_list|()
decl_stmt|;
name|context
operator|.
name|parsedQuery
argument_list|(
operator|new
name|ParsedQuery
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|collected
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|IndexSearcher
name|contextSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
operator|new
name|MultiReader
argument_list|()
argument_list|)
block|{
specifier|protected
name|void
name|search
parameter_list|(
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
parameter_list|,
name|Weight
name|weight
parameter_list|,
name|Collector
name|collector
parameter_list|)
throws|throws
name|IOException
block|{
name|collected
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|super
operator|.
name|search
argument_list|(
name|leaves
argument_list|,
name|weight
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|QueryPhase
operator|.
name|execute
argument_list|(
name|context
argument_list|,
name|contextSearcher
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|collected
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|minimumScore
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|QueryPhase
operator|.
name|execute
argument_list|(
name|context
argument_list|,
name|contextSearcher
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|collected
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

