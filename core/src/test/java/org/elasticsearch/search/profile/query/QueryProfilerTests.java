begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.profile.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|profile
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
name|Explanation
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
name|RandomApproximationQuery
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
name|Scorer
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
name|ScorerSupplier
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
name|TotalHitCountCollector
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|TestUtil
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
name|profile
operator|.
name|ProfileResult
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
name|greaterThan
import|;
end_import

begin_class
DECL|class|QueryProfilerTests
specifier|public
class|class
name|QueryProfilerTests
extends|extends
name|ESTestCase
block|{
DECL|field|dir
specifier|static
name|Directory
name|dir
decl_stmt|;
DECL|field|reader
specifier|static
name|IndexReader
name|reader
decl_stmt|;
DECL|field|searcher
specifier|static
name|ContextIndexSearcher
name|searcher
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|setup
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|dir
operator|=
name|newDirectory
argument_list|()
expr_stmt|;
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
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|TestUtil
operator|.
name|nextInt
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|20
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
specifier|final
name|int
name|numHoles
init|=
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|5
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
name|numHoles
condition|;
operator|++
name|j
control|)
block|{
name|w
operator|.
name|addDocument
argument_list|(
operator|new
name|Document
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|w
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
name|reader
operator|=
name|w
operator|.
name|getReader
argument_list|()
expr_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
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
literal|"test"
argument_list|,
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
argument_list|)
decl_stmt|;
name|searcher
operator|=
operator|new
name|ContextIndexSearcher
argument_list|(
name|engineSearcher
argument_list|,
name|IndexSearcher
operator|.
name|getDefaultQueryCache
argument_list|()
argument_list|,
name|MAYBE_CACHE_POLICY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|cleanup
specifier|public
specifier|static
name|void
name|cleanup
parameter_list|()
throws|throws
name|IOException
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|reader
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|dir
operator|=
literal|null
expr_stmt|;
name|reader
operator|=
literal|null
expr_stmt|;
name|searcher
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testBasic
specifier|public
name|void
name|testBasic
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryProfiler
name|profiler
init|=
operator|new
name|QueryProfiler
argument_list|()
decl_stmt|;
name|searcher
operator|.
name|setProfiler
argument_list|(
name|profiler
argument_list|)
expr_stmt|;
name|Query
name|query
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
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ProfileResult
argument_list|>
name|results
init|=
name|profiler
operator|.
name|getTree
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|breakdown
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimeBreakdown
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|CREATE_WEIGHT
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|BUILD_SCORER
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|NEXT_DOC
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|ADVANCE
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|SCORE
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|MATCH
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|CREATE_WEIGHT
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|BUILD_SCORER
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|NEXT_DOC
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|ADVANCE
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|SCORE
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|MATCH
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|rewriteTime
init|=
name|profiler
operator|.
name|getRewriteTime
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|rewriteTime
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoScoring
specifier|public
name|void
name|testNoScoring
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryProfiler
name|profiler
init|=
operator|new
name|QueryProfiler
argument_list|()
decl_stmt|;
name|searcher
operator|.
name|setProfiler
argument_list|(
name|profiler
argument_list|)
expr_stmt|;
name|Query
name|query
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
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|1
argument_list|,
name|Sort
operator|.
name|INDEXORDER
argument_list|)
expr_stmt|;
comment|// scores are not needed
name|List
argument_list|<
name|ProfileResult
argument_list|>
name|results
init|=
name|profiler
operator|.
name|getTree
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|breakdown
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimeBreakdown
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|CREATE_WEIGHT
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|BUILD_SCORER
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|NEXT_DOC
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|ADVANCE
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|SCORE
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|MATCH
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|CREATE_WEIGHT
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|BUILD_SCORER
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|NEXT_DOC
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|ADVANCE
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|SCORE
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|MATCH
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|rewriteTime
init|=
name|profiler
operator|.
name|getRewriteTime
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|rewriteTime
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUseIndexStats
specifier|public
name|void
name|testUseIndexStats
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryProfiler
name|profiler
init|=
operator|new
name|QueryProfiler
argument_list|()
decl_stmt|;
name|searcher
operator|.
name|setProfiler
argument_list|(
name|profiler
argument_list|)
expr_stmt|;
name|Query
name|query
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
name|searcher
operator|.
name|count
argument_list|(
name|query
argument_list|)
expr_stmt|;
comment|// will use index stats
name|List
argument_list|<
name|ProfileResult
argument_list|>
name|results
init|=
name|profiler
operator|.
name|getTree
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|rewriteTime
init|=
name|profiler
operator|.
name|getRewriteTime
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|rewriteTime
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testApproximations
specifier|public
name|void
name|testApproximations
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryProfiler
name|profiler
init|=
operator|new
name|QueryProfiler
argument_list|()
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
literal|"test"
argument_list|,
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
argument_list|)
decl_stmt|;
comment|// disable query caching since we want to test approximations, which won't
comment|// be exposed on a cached entry
name|ContextIndexSearcher
name|searcher
init|=
operator|new
name|ContextIndexSearcher
argument_list|(
name|engineSearcher
argument_list|,
literal|null
argument_list|,
name|MAYBE_CACHE_POLICY
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|setProfiler
argument_list|(
name|profiler
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
operator|new
name|RandomApproximationQuery
argument_list|(
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
argument_list|,
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|count
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ProfileResult
argument_list|>
name|results
init|=
name|profiler
operator|.
name|getTree
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|breakdown
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimeBreakdown
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|CREATE_WEIGHT
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|BUILD_SCORER
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|NEXT_DOC
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|ADVANCE
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|SCORE
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|MATCH
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|CREATE_WEIGHT
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|BUILD_SCORER
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|NEXT_DOC
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|ADVANCE
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|SCORE
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|breakdown
operator|.
name|get
argument_list|(
name|QueryTimingType
operator|.
name|MATCH
operator|.
name|toString
argument_list|()
operator|+
literal|"_count"
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|rewriteTime
init|=
name|profiler
operator|.
name|getRewriteTime
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|rewriteTime
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCollector
specifier|public
name|void
name|testCollector
parameter_list|()
throws|throws
name|IOException
block|{
name|TotalHitCountCollector
name|collector
init|=
operator|new
name|TotalHitCountCollector
argument_list|()
decl_stmt|;
name|ProfileCollector
name|profileCollector
init|=
operator|new
name|ProfileCollector
argument_list|(
name|collector
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|profileCollector
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|LeafCollector
name|leafCollector
init|=
name|profileCollector
operator|.
name|getLeafCollector
argument_list|(
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|profileCollector
operator|.
name|getTime
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|time
init|=
name|profileCollector
operator|.
name|getTime
argument_list|()
decl_stmt|;
name|leafCollector
operator|.
name|setScorer
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|profileCollector
operator|.
name|getTime
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
name|time
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|=
name|profileCollector
operator|.
name|getTime
argument_list|()
expr_stmt|;
name|leafCollector
operator|.
name|collect
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|profileCollector
operator|.
name|getTime
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
name|time
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|DummyQuery
specifier|private
specifier|static
class|class
name|DummyQuery
extends|extends
name|Query
block|{
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|this
operator|==
name|obj
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
name|Weight
name|createWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|boolean
name|needsScores
parameter_list|,
name|float
name|boost
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Weight
argument_list|(
name|this
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|extractTerms
parameter_list|(
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Explanation
name|explain
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Scorer
name|scorer
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|ScorerSupplier
name|scorerSupplier
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Weight
name|weight
init|=
name|this
decl_stmt|;
return|return
operator|new
name|ScorerSupplier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Scorer
name|get
parameter_list|(
name|boolean
name|randomAccess
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|cost
parameter_list|()
block|{
return|return
literal|42
return|;
block|}
block|}
return|;
block|}
block|}
return|;
block|}
block|}
DECL|method|testScorerSupplier
specifier|public
name|void
name|testScorerSupplier
parameter_list|()
throws|throws
name|IOException
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|w
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|w
operator|.
name|addDocument
argument_list|(
operator|new
name|Document
argument_list|()
argument_list|)
expr_stmt|;
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w
argument_list|)
decl_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
name|IndexSearcher
name|s
init|=
name|newSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|s
operator|.
name|setQueryCache
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Weight
name|weight
init|=
name|s
operator|.
name|createNormalizedWeight
argument_list|(
operator|new
name|DummyQuery
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
comment|// exception when getting the scorer
name|expectThrows
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|weight
operator|.
name|scorer
argument_list|(
name|s
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// no exception, means scorerSupplier is delegated
name|weight
operator|.
name|scorerSupplier
argument_list|(
name|s
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
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

