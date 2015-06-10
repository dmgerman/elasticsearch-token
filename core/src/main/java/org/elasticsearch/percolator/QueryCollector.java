begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
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
name|SimpleCollector
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
name|fielddata
operator|.
name|IndexFieldData
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
name|SortedBinaryDocValues
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
name|FieldMapper
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
name|MappedFieldType
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
name|ParsedQuery
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
name|aggregations
operator|.
name|Aggregator
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
name|aggregations
operator|.
name|BucketCollector
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
name|aggregations
operator|.
name|bucket
operator|.
name|global
operator|.
name|GlobalAggregator
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
name|aggregations
operator|.
name|support
operator|.
name|AggregationContext
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
name|highlight
operator|.
name|HighlightField
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
name|highlight
operator|.
name|HighlightPhase
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
name|ArrayList
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
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|QueryCollector
specifier|abstract
class|class
name|QueryCollector
extends|extends
name|SimpleCollector
block|{
DECL|field|uidFieldData
specifier|final
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|uidFieldData
decl_stmt|;
DECL|field|searcher
specifier|final
name|IndexSearcher
name|searcher
decl_stmt|;
DECL|field|queries
specifier|final
name|ConcurrentMap
argument_list|<
name|BytesRef
argument_list|,
name|Query
argument_list|>
name|queries
decl_stmt|;
DECL|field|logger
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|isNestedDoc
name|boolean
name|isNestedDoc
init|=
literal|false
decl_stmt|;
DECL|field|collector
specifier|final
name|Lucene
operator|.
name|EarlyTerminatingCollector
name|collector
init|=
name|Lucene
operator|.
name|createExistsCollector
argument_list|()
decl_stmt|;
DECL|field|current
name|BytesRef
name|current
decl_stmt|;
DECL|field|values
name|SortedBinaryDocValues
name|values
decl_stmt|;
DECL|field|aggregatorCollector
specifier|final
name|BucketCollector
name|aggregatorCollector
decl_stmt|;
DECL|field|aggregatorLeafCollector
name|LeafCollector
name|aggregatorLeafCollector
decl_stmt|;
DECL|method|QueryCollector
name|QueryCollector
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|queries
operator|=
name|context
operator|.
name|percolateQueries
argument_list|()
expr_stmt|;
name|this
operator|.
name|searcher
operator|=
name|context
operator|.
name|docSearcher
argument_list|()
expr_stmt|;
specifier|final
name|MappedFieldType
name|uidMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldType
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|this
operator|.
name|uidFieldData
operator|=
name|context
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|uidMapper
argument_list|)
expr_stmt|;
name|this
operator|.
name|isNestedDoc
operator|=
name|isNestedDoc
expr_stmt|;
name|List
argument_list|<
name|Aggregator
argument_list|>
name|aggregatorCollectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|aggregations
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|AggregationContext
name|aggregationContext
init|=
operator|new
name|AggregationContext
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|aggregationContext
argument_list|(
name|aggregationContext
argument_list|)
expr_stmt|;
name|Aggregator
index|[]
name|aggregators
init|=
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|factories
argument_list|()
operator|.
name|createTopLevelAggregators
argument_list|(
name|aggregationContext
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
name|aggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|aggregators
index|[
name|i
index|]
operator|instanceof
name|GlobalAggregator
operator|)
condition|)
block|{
name|Aggregator
name|aggregator
init|=
name|aggregators
index|[
name|i
index|]
decl_stmt|;
name|aggregatorCollectors
operator|.
name|add
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|aggregators
argument_list|(
name|aggregators
argument_list|)
expr_stmt|;
block|}
name|aggregatorCollector
operator|=
name|BucketCollector
operator|.
name|wrap
argument_list|(
name|aggregatorCollectors
argument_list|)
expr_stmt|;
name|aggregatorCollector
operator|.
name|preCollection
argument_list|()
expr_stmt|;
block|}
DECL|method|postMatch
specifier|public
name|void
name|postMatch
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|aggregatorLeafCollector
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|aggregatorLeafCollector
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
name|aggregatorCollector
operator|.
name|needsScores
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doSetNextReader
specifier|public
name|void
name|doSetNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we use the UID because id might not be indexed
name|values
operator|=
name|uidFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getBytesValues
argument_list|()
expr_stmt|;
name|aggregatorLeafCollector
operator|=
name|aggregatorCollector
operator|.
name|getLeafCollector
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
DECL|method|match
specifier|static
name|Match
name|match
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|HighlightPhase
name|highlightPhase
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Match
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|highlightPhase
argument_list|,
name|isNestedDoc
argument_list|)
return|;
block|}
DECL|method|count
specifier|static
name|Count
name|count
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Count
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|isNestedDoc
argument_list|)
return|;
block|}
DECL|method|matchAndScore
specifier|static
name|MatchAndScore
name|matchAndScore
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|HighlightPhase
name|highlightPhase
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MatchAndScore
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|highlightPhase
argument_list|,
name|isNestedDoc
argument_list|)
return|;
block|}
DECL|method|matchAndSort
specifier|static
name|MatchAndSort
name|matchAndSort
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MatchAndSort
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|isNestedDoc
argument_list|)
return|;
block|}
DECL|method|getQuery
specifier|protected
specifier|final
name|Query
name|getQuery
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numValues
init|=
name|values
operator|.
name|count
argument_list|()
decl_stmt|;
if|if
condition|(
name|numValues
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
assert|assert
name|numValues
operator|==
literal|1
assert|;
name|current
operator|=
name|Uid
operator|.
name|splitUidIntoTypeAndId
argument_list|(
name|values
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
argument_list|)
index|[
literal|1
index|]
expr_stmt|;
return|return
name|queries
operator|.
name|get
argument_list|(
name|current
argument_list|)
return|;
block|}
DECL|class|Match
specifier|final
specifier|static
class|class
name|Match
extends|extends
name|QueryCollector
block|{
DECL|field|context
specifier|final
name|PercolateContext
name|context
decl_stmt|;
DECL|field|highlightPhase
specifier|final
name|HighlightPhase
name|highlightPhase
decl_stmt|;
DECL|field|matches
specifier|final
name|List
argument_list|<
name|BytesRef
argument_list|>
name|matches
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|hls
specifier|final
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|limit
specifier|final
name|boolean
name|limit
decl_stmt|;
DECL|field|size
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|counter
name|long
name|counter
init|=
literal|0
decl_stmt|;
DECL|method|Match
name|Match
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|HighlightPhase
name|highlightPhase
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|isNestedDoc
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|context
operator|.
name|limit
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|context
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|highlightPhase
operator|=
name|highlightPhase
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Query
name|query
init|=
name|getQuery
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
comment|// log???
return|return;
block|}
comment|// run the query
try|try
block|{
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|!=
literal|null
condition|)
block|{
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
name|hitContext
argument_list|()
operator|.
name|cache
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isNestedDoc
condition|)
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|collector
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|limit
operator|||
name|counter
operator|<
name|size
condition|)
block|{
name|matches
operator|.
name|add
argument_list|(
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|current
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|highlightPhase
operator|.
name|hitExecute
argument_list|(
name|context
argument_list|,
name|context
operator|.
name|hitContext
argument_list|()
argument_list|)
expr_stmt|;
name|hls
operator|.
name|add
argument_list|(
name|context
operator|.
name|hitContext
argument_list|()
operator|.
name|hit
argument_list|()
operator|.
name|getHighlightFields
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|counter
operator|++
expr_stmt|;
name|postMatch
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"["
operator|+
name|current
operator|.
name|utf8ToString
argument_list|()
operator|+
literal|"] failed to execute query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|counter
name|long
name|counter
parameter_list|()
block|{
return|return
name|counter
return|;
block|}
DECL|method|matches
name|List
argument_list|<
name|BytesRef
argument_list|>
name|matches
parameter_list|()
block|{
return|return
name|matches
return|;
block|}
DECL|method|hls
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
parameter_list|()
block|{
return|return
name|hls
return|;
block|}
block|}
DECL|class|MatchAndSort
specifier|final
specifier|static
class|class
name|MatchAndSort
extends|extends
name|QueryCollector
block|{
DECL|field|topDocsCollector
specifier|private
specifier|final
name|TopScoreDocCollector
name|topDocsCollector
decl_stmt|;
DECL|field|topDocsLeafCollector
specifier|private
name|LeafCollector
name|topDocsLeafCollector
decl_stmt|;
DECL|method|MatchAndSort
name|MatchAndSort
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|isNestedDoc
argument_list|)
expr_stmt|;
comment|// TODO: Use TopFieldCollector.create(...) for ascending and descending scoring?
name|topDocsCollector
operator|=
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|context
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
name|super
operator|.
name|needsScores
argument_list|()
operator|||
name|topDocsCollector
operator|.
name|needsScores
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Query
name|query
init|=
name|getQuery
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
comment|// log???
return|return;
block|}
comment|// run the query
try|try
block|{
if|if
condition|(
name|isNestedDoc
condition|)
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|collector
operator|.
name|exists
argument_list|()
condition|)
block|{
name|topDocsLeafCollector
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|postMatch
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"["
operator|+
name|current
operator|.
name|utf8ToString
argument_list|()
operator|+
literal|"] failed to execute query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doSetNextReader
specifier|public
name|void
name|doSetNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|doSetNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|topDocsLeafCollector
operator|=
name|topDocsCollector
operator|.
name|getLeafCollector
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|topDocsLeafCollector
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
DECL|method|topDocs
name|TopDocs
name|topDocs
parameter_list|()
block|{
return|return
name|topDocsCollector
operator|.
name|topDocs
argument_list|()
return|;
block|}
block|}
DECL|class|MatchAndScore
specifier|final
specifier|static
class|class
name|MatchAndScore
extends|extends
name|QueryCollector
block|{
DECL|field|context
specifier|final
name|PercolateContext
name|context
decl_stmt|;
DECL|field|highlightPhase
specifier|final
name|HighlightPhase
name|highlightPhase
decl_stmt|;
DECL|field|matches
specifier|final
name|List
argument_list|<
name|BytesRef
argument_list|>
name|matches
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|hls
specifier|final
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// TODO: Use thread local in order to cache the scores lists?
DECL|field|scores
specifier|final
name|FloatArrayList
name|scores
init|=
operator|new
name|FloatArrayList
argument_list|()
decl_stmt|;
DECL|field|limit
specifier|final
name|boolean
name|limit
decl_stmt|;
DECL|field|size
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|counter
name|long
name|counter
init|=
literal|0
decl_stmt|;
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
DECL|method|MatchAndScore
name|MatchAndScore
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|HighlightPhase
name|highlightPhase
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|isNestedDoc
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|context
operator|.
name|limit
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|context
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|highlightPhase
operator|=
name|highlightPhase
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Query
name|query
init|=
name|getQuery
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
comment|// log???
return|return;
block|}
comment|// run the query
try|try
block|{
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|!=
literal|null
condition|)
block|{
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
name|hitContext
argument_list|()
operator|.
name|cache
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isNestedDoc
condition|)
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|collector
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|limit
operator|||
name|counter
operator|<
name|size
condition|)
block|{
name|matches
operator|.
name|add
argument_list|(
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|current
argument_list|)
argument_list|)
expr_stmt|;
name|scores
operator|.
name|add
argument_list|(
name|scorer
operator|.
name|score
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|highlightPhase
operator|.
name|hitExecute
argument_list|(
name|context
argument_list|,
name|context
operator|.
name|hitContext
argument_list|()
argument_list|)
expr_stmt|;
name|hls
operator|.
name|add
argument_list|(
name|context
operator|.
name|hitContext
argument_list|()
operator|.
name|hit
argument_list|()
operator|.
name|getHighlightFields
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|counter
operator|++
expr_stmt|;
name|postMatch
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"["
operator|+
name|current
operator|.
name|utf8ToString
argument_list|()
operator|+
literal|"] failed to execute query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
block|}
DECL|method|counter
name|long
name|counter
parameter_list|()
block|{
return|return
name|counter
return|;
block|}
DECL|method|matches
name|List
argument_list|<
name|BytesRef
argument_list|>
name|matches
parameter_list|()
block|{
return|return
name|matches
return|;
block|}
DECL|method|scores
name|FloatArrayList
name|scores
parameter_list|()
block|{
return|return
name|scores
return|;
block|}
DECL|method|hls
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
parameter_list|()
block|{
return|return
name|hls
return|;
block|}
block|}
DECL|class|Count
specifier|final
specifier|static
class|class
name|Count
extends|extends
name|QueryCollector
block|{
DECL|field|counter
specifier|private
name|long
name|counter
init|=
literal|0
decl_stmt|;
DECL|method|Count
name|Count
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|boolean
name|isNestedDoc
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|context
argument_list|,
name|isNestedDoc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Query
name|query
init|=
name|getQuery
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
comment|// log???
return|return;
block|}
comment|// run the query
try|try
block|{
if|if
condition|(
name|isNestedDoc
condition|)
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|collector
operator|.
name|exists
argument_list|()
condition|)
block|{
name|counter
operator|++
expr_stmt|;
name|postMatch
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"["
operator|+
name|current
operator|.
name|utf8ToString
argument_list|()
operator|+
literal|"] failed to execute query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|counter
name|long
name|counter
parameter_list|()
block|{
return|return
name|counter
return|;
block|}
block|}
block|}
end_class

end_unit

