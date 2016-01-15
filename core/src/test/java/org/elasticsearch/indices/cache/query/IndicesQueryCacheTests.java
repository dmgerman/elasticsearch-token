begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.cache.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cache
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
name|search
operator|.
name|ConstantScoreScorer
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
name|ConstantScoreWeight
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
name|QueryCachingPolicy
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
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|index
operator|.
name|ElasticsearchDirectoryReader
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
name|index
operator|.
name|Index
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
name|cache
operator|.
name|query
operator|.
name|QueryCacheStats
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
name|shard
operator|.
name|ShardId
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

begin_class
DECL|class|IndicesQueryCacheTests
specifier|public
class|class
name|IndicesQueryCacheTests
extends|extends
name|ESTestCase
block|{
DECL|class|DummyQuery
specifier|private
specifier|static
class|class
name|DummyQuery
extends|extends
name|Query
block|{
DECL|field|id
specifier|private
specifier|final
name|int
name|id
decl_stmt|;
DECL|method|DummyQuery
name|DummyQuery
parameter_list|(
name|int
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
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
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
operator|&&
name|id
operator|==
operator|(
operator|(
name|DummyQuery
operator|)
name|obj
operator|)
operator|.
name|id
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
literal|31
operator|*
name|super
operator|.
name|hashCode
argument_list|()
operator|+
name|id
return|;
block|}
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
literal|"dummy"
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
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ConstantScoreWeight
argument_list|(
name|this
argument_list|)
block|{
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
return|return
operator|new
name|ConstantScoreScorer
argument_list|(
name|this
argument_list|,
name|score
argument_list|()
argument_list|,
name|DocIdSetIterator
operator|.
name|all
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
DECL|method|testBasics
specifier|public
name|void
name|testBasics
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
name|r
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
name|ShardId
name|shard
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|r
operator|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|r
argument_list|,
name|shard
argument_list|)
expr_stmt|;
name|IndexSearcher
name|s
init|=
operator|new
name|IndexSearcher
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|s
operator|.
name|setQueryCachingPolicy
argument_list|(
name|QueryCachingPolicy
operator|.
name|ALWAYS_CACHE
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndicesQueryCache
operator|.
name|INDICES_CACHE_QUERY_COUNT
argument_list|,
literal|10
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndicesQueryCache
name|cache
init|=
operator|new
name|IndicesQueryCache
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|s
operator|.
name|setQueryCache
argument_list|(
name|cache
argument_list|)
expr_stmt|;
name|QueryCacheStats
name|stats
init|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|s
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|stats
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|20
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|s
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|stats
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|stats
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|s
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|stats
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|stats
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|r
argument_list|,
name|dir
argument_list|)
expr_stmt|;
comment|// got emptied, but no changes to other metrics
name|stats
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|cache
operator|.
name|onClose
argument_list|(
name|shard
argument_list|)
expr_stmt|;
comment|// forgot everything
name|stats
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|cache
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// this triggers some assertions
block|}
DECL|method|testTwoShards
specifier|public
name|void
name|testTwoShards
parameter_list|()
throws|throws
name|IOException
block|{
name|Directory
name|dir1
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|w1
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir1
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|w1
operator|.
name|addDocument
argument_list|(
operator|new
name|Document
argument_list|()
argument_list|)
expr_stmt|;
name|DirectoryReader
name|r1
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|w1
operator|.
name|close
argument_list|()
expr_stmt|;
name|ShardId
name|shard1
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|r1
operator|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|r1
argument_list|,
name|shard1
argument_list|)
expr_stmt|;
name|IndexSearcher
name|s1
init|=
operator|new
name|IndexSearcher
argument_list|(
name|r1
argument_list|)
decl_stmt|;
name|s1
operator|.
name|setQueryCachingPolicy
argument_list|(
name|QueryCachingPolicy
operator|.
name|ALWAYS_CACHE
argument_list|)
expr_stmt|;
name|Directory
name|dir2
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|w2
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir2
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|w2
operator|.
name|addDocument
argument_list|(
operator|new
name|Document
argument_list|()
argument_list|)
expr_stmt|;
name|DirectoryReader
name|r2
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w2
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|w2
operator|.
name|close
argument_list|()
expr_stmt|;
name|ShardId
name|shard2
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|r2
operator|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|r2
argument_list|,
name|shard2
argument_list|)
expr_stmt|;
name|IndexSearcher
name|s2
init|=
operator|new
name|IndexSearcher
argument_list|(
name|r2
argument_list|)
decl_stmt|;
name|s2
operator|.
name|setQueryCachingPolicy
argument_list|(
name|QueryCachingPolicy
operator|.
name|ALWAYS_CACHE
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndicesQueryCache
operator|.
name|INDICES_CACHE_QUERY_COUNT
argument_list|,
literal|10
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndicesQueryCache
name|cache
init|=
operator|new
name|IndicesQueryCache
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|s1
operator|.
name|setQueryCache
argument_list|(
name|cache
argument_list|)
expr_stmt|;
name|s2
operator|.
name|setQueryCache
argument_list|(
name|cache
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|s1
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|QueryCacheStats
name|stats1
init|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|QueryCacheStats
name|stats2
init|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|s2
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|stats2
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats2
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats2
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats2
operator|.
name|getMissCount
argument_list|()
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
literal|20
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|s2
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|stats1
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// evicted
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|stats2
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|stats2
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats2
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats2
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats2
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|r1
argument_list|,
name|dir1
argument_list|)
expr_stmt|;
comment|// no changes
name|stats1
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|stats2
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|stats2
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats2
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats2
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats2
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|cache
operator|.
name|onClose
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
comment|// forgot everything about shard1
name|stats1
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|stats2
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|stats2
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats2
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats2
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20L
argument_list|,
name|stats2
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|r2
argument_list|,
name|dir2
argument_list|)
expr_stmt|;
name|cache
operator|.
name|onClose
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
comment|// forgot everything about shard2
name|stats1
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|stats2
operator|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats2
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|cache
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// this triggers some assertions
block|}
comment|// Make sure the cache behaves correctly when a segment that is associated
comment|// with an empty cache gets closed. In that particular case, the eviction
comment|// callback is called with a number of evicted entries equal to 0
comment|// see https://github.com/elastic/elasticsearch/issues/15043
DECL|method|testStatsOnEviction
specifier|public
name|void
name|testStatsOnEviction
parameter_list|()
throws|throws
name|IOException
block|{
name|Directory
name|dir1
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|w1
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir1
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|w1
operator|.
name|addDocument
argument_list|(
operator|new
name|Document
argument_list|()
argument_list|)
expr_stmt|;
name|DirectoryReader
name|r1
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|w1
operator|.
name|close
argument_list|()
expr_stmt|;
name|ShardId
name|shard1
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|r1
operator|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|r1
argument_list|,
name|shard1
argument_list|)
expr_stmt|;
name|IndexSearcher
name|s1
init|=
operator|new
name|IndexSearcher
argument_list|(
name|r1
argument_list|)
decl_stmt|;
name|s1
operator|.
name|setQueryCachingPolicy
argument_list|(
name|QueryCachingPolicy
operator|.
name|ALWAYS_CACHE
argument_list|)
expr_stmt|;
name|Directory
name|dir2
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|w2
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir2
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|w2
operator|.
name|addDocument
argument_list|(
operator|new
name|Document
argument_list|()
argument_list|)
expr_stmt|;
name|DirectoryReader
name|r2
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w2
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|w2
operator|.
name|close
argument_list|()
expr_stmt|;
name|ShardId
name|shard2
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|r2
operator|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|r2
argument_list|,
name|shard2
argument_list|)
expr_stmt|;
name|IndexSearcher
name|s2
init|=
operator|new
name|IndexSearcher
argument_list|(
name|r2
argument_list|)
decl_stmt|;
name|s2
operator|.
name|setQueryCachingPolicy
argument_list|(
name|QueryCachingPolicy
operator|.
name|ALWAYS_CACHE
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndicesQueryCache
operator|.
name|INDICES_CACHE_QUERY_COUNT
argument_list|,
literal|10
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndicesQueryCache
name|cache
init|=
operator|new
name|IndicesQueryCache
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|s1
operator|.
name|setQueryCache
argument_list|(
name|cache
argument_list|)
expr_stmt|;
name|s2
operator|.
name|setQueryCache
argument_list|(
name|cache
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|s1
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|20
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|s2
operator|.
name|count
argument_list|(
operator|new
name|DummyQuery
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|QueryCacheStats
name|stats1
init|=
name|cache
operator|.
name|getStats
argument_list|(
name|shard1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|stats1
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|stats1
operator|.
name|getCacheCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// this used to fail because we were evicting an empty cache on
comment|// the segment from r1
name|IOUtils
operator|.
name|close
argument_list|(
name|r1
argument_list|,
name|dir1
argument_list|)
expr_stmt|;
name|cache
operator|.
name|onClose
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|r2
argument_list|,
name|dir2
argument_list|)
expr_stmt|;
name|cache
operator|.
name|onClose
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|cache
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// this triggers some assertions
block|}
block|}
end_class

end_unit

