begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.scan
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|scan
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
name|CollectionTerminatedException
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
name|util
operator|.
name|Bits
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
name|search
operator|.
name|internal
operator|.
name|SearchContext
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

begin_comment
comment|/**  * The scan context allows to optimize readers we already processed during scanning. We do that by keeping track  * of the last collected doc ID and only collecting doc IDs that are greater.  */
end_comment

begin_class
DECL|class|ScanContext
specifier|public
class|class
name|ScanContext
block|{
DECL|field|docUpTo
specifier|private
specifier|volatile
name|int
name|docUpTo
decl_stmt|;
DECL|method|execute
specifier|public
name|TopDocs
name|execute
parameter_list|(
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|execute
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
argument_list|,
name|context
operator|.
name|query
argument_list|()
argument_list|,
name|context
operator|.
name|size
argument_list|()
argument_list|,
name|context
operator|.
name|trackScores
argument_list|()
argument_list|)
return|;
block|}
DECL|method|execute
name|TopDocs
name|execute
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|Query
name|query
parameter_list|,
name|int
name|size
parameter_list|,
name|boolean
name|trackScores
parameter_list|)
throws|throws
name|IOException
block|{
name|ScanCollector
name|collector
init|=
operator|new
name|ScanCollector
argument_list|(
name|size
argument_list|,
name|trackScores
argument_list|)
decl_stmt|;
name|Query
name|q
init|=
name|Queries
operator|.
name|filtered
argument_list|(
name|query
argument_list|,
operator|new
name|MinDocQuery
argument_list|(
name|docUpTo
argument_list|)
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|search
argument_list|(
name|q
argument_list|,
name|collector
argument_list|)
expr_stmt|;
return|return
name|collector
operator|.
name|topDocs
argument_list|()
return|;
block|}
DECL|class|ScanCollector
specifier|private
class|class
name|ScanCollector
extends|extends
name|SimpleCollector
block|{
DECL|field|docs
specifier|private
specifier|final
name|List
argument_list|<
name|ScoreDoc
argument_list|>
name|docs
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|trackScores
specifier|private
specifier|final
name|boolean
name|trackScores
decl_stmt|;
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
DECL|field|docBase
specifier|private
name|int
name|docBase
decl_stmt|;
DECL|method|ScanCollector
name|ScanCollector
parameter_list|(
name|int
name|size
parameter_list|,
name|boolean
name|trackScores
parameter_list|)
block|{
name|this
operator|.
name|trackScores
operator|=
name|trackScores
expr_stmt|;
name|this
operator|.
name|docs
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
DECL|method|topDocs
specifier|public
name|TopDocs
name|topDocs
parameter_list|()
block|{
return|return
operator|new
name|TopDocs
argument_list|(
name|docs
operator|.
name|size
argument_list|()
argument_list|,
name|docs
operator|.
name|toArray
argument_list|(
operator|new
name|ScoreDoc
index|[
name|docs
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
literal|0f
argument_list|)
return|;
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
name|trackScores
return|;
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
name|int
name|topLevelDoc
init|=
name|docBase
operator|+
name|doc
decl_stmt|;
name|docs
operator|.
name|add
argument_list|(
operator|new
name|ScoreDoc
argument_list|(
name|topLevelDoc
argument_list|,
name|trackScores
condition|?
name|scorer
operator|.
name|score
argument_list|()
else|:
literal|0f
argument_list|)
argument_list|)
expr_stmt|;
comment|// record that we collected up to this document
assert|assert
name|topLevelDoc
operator|>=
name|docUpTo
assert|;
name|docUpTo
operator|=
name|topLevelDoc
operator|+
literal|1
expr_stmt|;
if|if
condition|(
name|docs
operator|.
name|size
argument_list|()
operator|>=
name|size
condition|)
block|{
throw|throw
operator|new
name|CollectionTerminatedException
argument_list|()
throw|;
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
if|if
condition|(
name|docs
operator|.
name|size
argument_list|()
operator|>=
name|size
operator|||
name|context
operator|.
name|docBase
operator|+
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
operator|<=
name|docUpTo
condition|)
block|{
comment|// no need to collect a new segment, we either already collected enough
comment|// or the segment is not competitive
throw|throw
operator|new
name|CollectionTerminatedException
argument_list|()
throw|;
block|}
name|docBase
operator|=
name|context
operator|.
name|docBase
expr_stmt|;
block|}
block|}
comment|/**      * A filtering query that matches all doc IDs that are not deleted and      * greater than or equal to the configured doc ID.      */
comment|// pkg-private for testing
DECL|class|MinDocQuery
specifier|static
class|class
name|MinDocQuery
extends|extends
name|Query
block|{
DECL|field|minDoc
specifier|private
specifier|final
name|int
name|minDoc
decl_stmt|;
DECL|method|MinDocQuery
name|MinDocQuery
parameter_list|(
name|int
name|minDoc
parameter_list|)
block|{
name|this
operator|.
name|minDoc
operator|=
name|minDoc
expr_stmt|;
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
name|minDoc
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
if|if
condition|(
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|MinDocQuery
name|that
init|=
operator|(
name|MinDocQuery
operator|)
name|obj
decl_stmt|;
return|return
name|minDoc
operator|==
name|that
operator|.
name|minDoc
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
parameter_list|,
specifier|final
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|maxDoc
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|docBase
operator|+
name|maxDoc
operator|<=
name|minDoc
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|int
name|segmentMinDoc
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|minDoc
operator|-
name|context
operator|.
name|docBase
argument_list|)
decl_stmt|;
specifier|final
name|DocIdSetIterator
name|disi
init|=
operator|new
name|DocIdSetIterator
argument_list|()
block|{
name|int
name|doc
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|docID
parameter_list|()
block|{
return|return
name|doc
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|advance
argument_list|(
name|doc
operator|+
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|target
operator|>
name|doc
assert|;
if|if
condition|(
name|doc
operator|==
operator|-
literal|1
condition|)
block|{
comment|// skip directly to minDoc
name|doc
operator|=
name|Math
operator|.
name|max
argument_list|(
name|target
argument_list|,
name|segmentMinDoc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|doc
operator|=
name|target
expr_stmt|;
block|}
while|while
condition|(
name|doc
operator|<
name|maxDoc
condition|)
block|{
if|if
condition|(
name|acceptDocs
operator|==
literal|null
operator|||
name|acceptDocs
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
break|break;
block|}
name|doc
operator|+=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|doc
operator|>=
name|maxDoc
condition|)
block|{
name|doc
operator|=
name|NO_MORE_DOCS
expr_stmt|;
block|}
return|return
name|doc
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|cost
parameter_list|()
block|{
return|return
name|maxDoc
operator|-
name|minDoc
return|;
block|}
block|}
decl_stmt|;
return|return
operator|new
name|ConstantScoreScorer
argument_list|(
name|this
argument_list|,
name|score
argument_list|()
argument_list|,
name|disi
argument_list|)
return|;
block|}
block|}
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
literal|"MinDocQuery(minDoc="
operator|+
name|minDoc
operator|+
literal|")"
return|;
block|}
block|}
block|}
end_class

end_unit

