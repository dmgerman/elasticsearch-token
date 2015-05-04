begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.stresstest.search1
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|stresstest
operator|.
name|search1
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|ImmutableSettings
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
name|unit
operator|.
name|SizeValue
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
name|unit
operator|.
name|TimeValue
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
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
name|SearchHit
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
name|sort
operator|.
name|SortOrder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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
name|concurrent
operator|.
name|ThreadLocalRandom
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
name|AtomicLong
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

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|Ignore
argument_list|(
literal|"Stress Test"
argument_list|)
DECL|class|Search1StressTest
specifier|public
class|class
name|Search1StressTest
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|numberOfNodes
specifier|private
name|int
name|numberOfNodes
init|=
literal|4
decl_stmt|;
DECL|field|indexers
specifier|private
name|int
name|indexers
init|=
literal|0
decl_stmt|;
DECL|field|preIndexDocs
specifier|private
name|SizeValue
name|preIndexDocs
init|=
operator|new
name|SizeValue
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|indexerThrottle
specifier|private
name|TimeValue
name|indexerThrottle
init|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
decl_stmt|;
DECL|field|searchers
specifier|private
name|int
name|searchers
init|=
literal|0
decl_stmt|;
DECL|field|searcherThrottle
specifier|private
name|TimeValue
name|searcherThrottle
init|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|20
argument_list|)
decl_stmt|;
DECL|field|numberOfIndices
specifier|private
name|int
name|numberOfIndices
init|=
literal|10
decl_stmt|;
DECL|field|numberOfTypes
specifier|private
name|int
name|numberOfTypes
init|=
literal|4
decl_stmt|;
DECL|field|numberOfValues
specifier|private
name|int
name|numberOfValues
init|=
literal|20
decl_stmt|;
DECL|field|numberOfHits
specifier|private
name|int
name|numberOfHits
init|=
literal|300
decl_stmt|;
DECL|field|flusherThrottle
specifier|private
name|TimeValue
name|flusherThrottle
init|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
DECL|field|settings
specifier|private
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
decl_stmt|;
DECL|field|period
specifier|private
name|TimeValue
name|period
init|=
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|20
argument_list|)
decl_stmt|;
DECL|field|indexCounter
specifier|private
name|AtomicLong
name|indexCounter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|searchCounter
specifier|private
name|AtomicLong
name|searchCounter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|client
specifier|private
name|Node
name|client
decl_stmt|;
DECL|method|setNumberOfNodes
specifier|public
name|Search1StressTest
name|setNumberOfNodes
parameter_list|(
name|int
name|numberOfNodes
parameter_list|)
block|{
name|this
operator|.
name|numberOfNodes
operator|=
name|numberOfNodes
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setPreIndexDocs
specifier|public
name|Search1StressTest
name|setPreIndexDocs
parameter_list|(
name|SizeValue
name|preIndexDocs
parameter_list|)
block|{
name|this
operator|.
name|preIndexDocs
operator|=
name|preIndexDocs
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setIndexers
specifier|public
name|Search1StressTest
name|setIndexers
parameter_list|(
name|int
name|indexers
parameter_list|)
block|{
name|this
operator|.
name|indexers
operator|=
name|indexers
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setIndexerThrottle
specifier|public
name|Search1StressTest
name|setIndexerThrottle
parameter_list|(
name|TimeValue
name|indexerThrottle
parameter_list|)
block|{
name|this
operator|.
name|indexerThrottle
operator|=
name|indexerThrottle
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSearchers
specifier|public
name|Search1StressTest
name|setSearchers
parameter_list|(
name|int
name|searchers
parameter_list|)
block|{
name|this
operator|.
name|searchers
operator|=
name|searchers
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSearcherThrottle
specifier|public
name|Search1StressTest
name|setSearcherThrottle
parameter_list|(
name|TimeValue
name|searcherThrottle
parameter_list|)
block|{
name|this
operator|.
name|searcherThrottle
operator|=
name|searcherThrottle
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setNumberOfIndices
specifier|public
name|Search1StressTest
name|setNumberOfIndices
parameter_list|(
name|int
name|numberOfIndices
parameter_list|)
block|{
name|this
operator|.
name|numberOfIndices
operator|=
name|numberOfIndices
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setNumberOfTypes
specifier|public
name|Search1StressTest
name|setNumberOfTypes
parameter_list|(
name|int
name|numberOfTypes
parameter_list|)
block|{
name|this
operator|.
name|numberOfTypes
operator|=
name|numberOfTypes
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setNumberOfValues
specifier|public
name|Search1StressTest
name|setNumberOfValues
parameter_list|(
name|int
name|numberOfValues
parameter_list|)
block|{
name|this
operator|.
name|numberOfValues
operator|=
name|numberOfValues
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setNumberOfHits
specifier|public
name|Search1StressTest
name|setNumberOfHits
parameter_list|(
name|int
name|numberOfHits
parameter_list|)
block|{
name|this
operator|.
name|numberOfHits
operator|=
name|numberOfHits
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setFlusherThrottle
specifier|public
name|Search1StressTest
name|setFlusherThrottle
parameter_list|(
name|TimeValue
name|flusherThrottle
parameter_list|)
block|{
name|this
operator|.
name|flusherThrottle
operator|=
name|flusherThrottle
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSettings
specifier|public
name|Search1StressTest
name|setSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setPeriod
specifier|public
name|Search1StressTest
name|setPeriod
parameter_list|(
name|TimeValue
name|period
parameter_list|)
block|{
name|this
operator|.
name|period
operator|=
name|period
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|nextIndex
specifier|private
name|String
name|nextIndex
parameter_list|()
block|{
return|return
literal|"test"
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|%
name|numberOfIndices
return|;
block|}
DECL|method|nextType
specifier|private
name|String
name|nextType
parameter_list|()
block|{
return|return
literal|"type"
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|%
name|numberOfTypes
return|;
block|}
DECL|method|nextNumValue
specifier|private
name|int
name|nextNumValue
parameter_list|()
block|{
return|return
name|Math
operator|.
name|abs
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|%
name|numberOfValues
return|;
block|}
DECL|method|nextFieldValue
specifier|private
name|String
name|nextFieldValue
parameter_list|()
block|{
return|return
literal|"value"
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|%
name|numberOfValues
return|;
block|}
DECL|class|Searcher
specifier|private
class|class
name|Searcher
extends|extends
name|Thread
block|{
DECL|field|close
specifier|volatile
name|boolean
name|close
init|=
literal|false
decl_stmt|;
DECL|field|closed
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|close
condition|)
block|{
name|closed
operator|=
literal|true
expr_stmt|;
return|return;
block|}
try|try
block|{
name|String
name|indexName
init|=
name|nextIndex
argument_list|()
decl_stmt|;
name|SearchRequestBuilder
name|builder
init|=
name|client
operator|.
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|addSort
argument_list|(
literal|"num"
argument_list|,
name|SortOrder
operator|.
name|DESC
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// add a _score based sorting, won't do any sorting, just to test...
name|builder
operator|.
name|addSort
argument_list|(
literal|"_score"
argument_list|,
name|SortOrder
operator|.
name|DESC
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|DFS_QUERY_THEN_FETCH
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|Math
operator|.
name|abs
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|%
name|numberOfHits
decl_stmt|;
name|builder
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
if|if
condition|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// update from
name|builder
operator|.
name|setFrom
argument_list|(
name|size
operator|/
literal|2
argument_list|)
expr_stmt|;
block|}
name|String
name|value
init|=
name|nextFieldValue
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"field"
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|searchCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
operator|>
literal|0
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed search "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// verify that all come from the requested index
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|getHits
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|hit
operator|.
name|shard
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|equals
argument_list|(
name|indexName
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"got wrong index, asked for [{}], got [{}]"
argument_list|,
name|indexName
argument_list|,
name|hit
operator|.
name|shard
argument_list|()
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// verify that all has the relevant value
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|getHits
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|hit
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"field"
argument_list|)
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"got wrong field, asked for [{}], got [{}]"
argument_list|,
name|value
argument_list|,
name|hit
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"field"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|searcherThrottle
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to search"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|Indexer
specifier|private
class|class
name|Indexer
extends|extends
name|Thread
block|{
DECL|field|close
specifier|volatile
name|boolean
name|close
init|=
literal|false
decl_stmt|;
DECL|field|closed
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|close
condition|)
block|{
name|closed
operator|=
literal|true
expr_stmt|;
return|return;
block|}
try|try
block|{
name|indexDoc
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|indexerThrottle
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to index / sleep"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|Flusher
specifier|private
class|class
name|Flusher
extends|extends
name|Thread
block|{
DECL|field|close
specifier|volatile
name|boolean
name|close
init|=
literal|false
decl_stmt|;
DECL|field|closed
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|close
condition|)
block|{
name|closed
operator|=
literal|true
expr_stmt|;
return|return;
block|}
try|try
block|{
name|client
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|indexerThrottle
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to flush / sleep"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|indexDoc
specifier|private
name|void
name|indexDoc
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|json
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"num"
argument_list|,
name|nextNumValue
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
name|nextFieldValue
argument_list|()
argument_list|)
decl_stmt|;
name|json
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|client
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|nextIndex
argument_list|()
argument_list|,
name|nextType
argument_list|()
argument_list|)
operator|.
name|setSource
argument_list|(
name|json
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|indexCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Node
index|[]
name|nodes
init|=
operator|new
name|Node
index|[
name|numberOfNodes
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
name|nodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
block|}
name|client
operator|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|client
argument_list|(
literal|true
argument_list|)
operator|.
name|node
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
name|numberOfIndices
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
operator|+
name|i
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Pre indexing docs [{}]..."
argument_list|,
name|preIndexDocs
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|preIndexDocs
operator|.
name|singles
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|indexDoc
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Done pre indexing docs [{}]"
argument_list|,
name|preIndexDocs
argument_list|)
expr_stmt|;
name|Indexer
index|[]
name|indexerThreads
init|=
operator|new
name|Indexer
index|[
name|indexers
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
name|indexerThreads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indexerThreads
index|[
name|i
index|]
operator|=
operator|new
name|Indexer
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Indexer
name|indexerThread
range|:
name|indexerThreads
control|)
block|{
name|indexerThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|Searcher
index|[]
name|searcherThreads
init|=
operator|new
name|Searcher
index|[
name|searchers
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
name|searcherThreads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|searcherThreads
index|[
name|i
index|]
operator|=
operator|new
name|Searcher
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Searcher
name|searcherThread
range|:
name|searcherThreads
control|)
block|{
name|searcherThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|Flusher
name|flusher
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|flusherThrottle
operator|.
name|millis
argument_list|()
operator|>
literal|0
condition|)
block|{
name|flusher
operator|=
operator|new
name|Flusher
argument_list|()
expr_stmt|;
name|flusher
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|long
name|testStart
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|testStart
operator|)
operator|>
name|period
operator|.
name|millis
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"DONE, closing ....."
argument_list|)
expr_stmt|;
if|if
condition|(
name|flusher
operator|!=
literal|null
condition|)
block|{
name|flusher
operator|.
name|close
operator|=
literal|true
expr_stmt|;
block|}
for|for
control|(
name|Searcher
name|searcherThread
range|:
name|searcherThreads
control|)
block|{
name|searcherThread
operator|.
name|close
operator|=
literal|true
expr_stmt|;
block|}
for|for
control|(
name|Indexer
name|indexerThread
range|:
name|indexerThreads
control|)
block|{
name|indexerThread
operator|.
name|close
operator|=
literal|true
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|indexerThrottle
operator|.
name|millis
argument_list|()
operator|+
literal|10000
argument_list|)
expr_stmt|;
if|if
condition|(
name|flusher
operator|!=
literal|null
operator|&&
operator|!
name|flusher
operator|.
name|closed
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"flusher not closed!"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Searcher
name|searcherThread
range|:
name|searcherThreads
control|)
block|{
if|if
condition|(
operator|!
name|searcherThread
operator|.
name|closed
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"search thread not closed!"
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Indexer
name|indexerThread
range|:
name|indexerThreads
control|)
block|{
if|if
condition|(
operator|!
name|indexerThread
operator|.
name|closed
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"index thread not closed!"
argument_list|)
expr_stmt|;
block|}
block|}
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"********** DONE, indexed ["
operator|+
name|indexCounter
operator|.
name|get
argument_list|()
operator|+
literal|"], searched ["
operator|+
name|searchCounter
operator|.
name|get
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Search1StressTest
name|test
init|=
operator|new
name|Search1StressTest
argument_list|()
operator|.
name|setPeriod
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|setNumberOfNodes
argument_list|(
literal|2
argument_list|)
operator|.
name|setPreIndexDocs
argument_list|(
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"100"
argument_list|)
argument_list|)
operator|.
name|setIndexers
argument_list|(
literal|2
argument_list|)
operator|.
name|setIndexerThrottle
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
argument_list|)
operator|.
name|setSearchers
argument_list|(
literal|10
argument_list|)
operator|.
name|setSearcherThrottle
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|setFlusherThrottle
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|1000
argument_list|)
argument_list|)
operator|.
name|setNumberOfIndices
argument_list|(
literal|10
argument_list|)
operator|.
name|setNumberOfTypes
argument_list|(
literal|5
argument_list|)
operator|.
name|setNumberOfValues
argument_list|(
literal|50
argument_list|)
operator|.
name|setNumberOfHits
argument_list|(
literal|300
argument_list|)
decl_stmt|;
name|test
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

