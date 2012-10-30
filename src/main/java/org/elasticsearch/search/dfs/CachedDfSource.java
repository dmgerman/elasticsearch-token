begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.dfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|dfs
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
name|*
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
name|*
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
name|similarities
operator|.
name|Similarity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CachedDfSource
specifier|public
class|class
name|CachedDfSource
extends|extends
name|IndexSearcher
block|{
DECL|field|aggregatedDfs
specifier|private
specifier|final
name|AggregatedDfs
name|aggregatedDfs
decl_stmt|;
DECL|field|maxDoc
specifier|private
specifier|final
name|int
name|maxDoc
decl_stmt|;
DECL|method|CachedDfSource
specifier|public
name|CachedDfSource
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|AggregatedDfs
name|aggregatedDfs
parameter_list|,
name|Similarity
name|similarity
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|this
operator|.
name|aggregatedDfs
operator|=
name|aggregatedDfs
expr_stmt|;
name|setSimilarity
argument_list|(
name|similarity
argument_list|)
expr_stmt|;
if|if
condition|(
name|aggregatedDfs
operator|.
name|maxDoc
argument_list|()
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
name|maxDoc
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
else|else
block|{
name|maxDoc
operator|=
operator|(
name|int
operator|)
name|aggregatedDfs
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|termStatistics
specifier|public
name|TermStatistics
name|termStatistics
parameter_list|(
name|Term
name|term
parameter_list|,
name|TermContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|TermStatistics
name|termStatistics
init|=
name|aggregatedDfs
operator|.
name|termStatistics
argument_list|()
operator|.
name|get
argument_list|(
name|term
argument_list|)
decl_stmt|;
if|if
condition|(
name|termStatistics
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Not distributed term statistics for term: "
operator|+
name|term
argument_list|)
throw|;
block|}
return|return
name|termStatistics
return|;
block|}
annotation|@
name|Override
DECL|method|collectionStatistics
specifier|public
name|CollectionStatistics
name|collectionStatistics
parameter_list|(
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
name|CollectionStatistics
name|collectionStatistics
init|=
name|aggregatedDfs
operator|.
name|fieldStatistics
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|collectionStatistics
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Not distributed collection statistics for field: "
operator|+
name|field
argument_list|)
throw|;
block|}
return|return
name|collectionStatistics
return|;
block|}
DECL|method|maxDoc
specifier|public
name|int
name|maxDoc
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxDoc
return|;
block|}
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|Query
name|query
parameter_list|)
block|{
comment|// this is a bit of a hack. We know that a query which
comment|// creates a Weight based on this Dummy-Searcher is
comment|// always already rewritten (see preparedWeight()).
comment|// Therefore we just return the unmodified query here
return|return
name|query
return|;
block|}
DECL|method|doc
specifier|public
name|Document
name|doc
parameter_list|(
name|int
name|i
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|method|doc
specifier|public
name|void
name|doc
parameter_list|(
name|int
name|docID
parameter_list|,
name|StoredFieldVisitor
name|fieldVisitor
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
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|int
name|doc
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
DECL|method|search
specifier|protected
name|void
name|search
parameter_list|(
name|List
argument_list|<
name|AtomicReaderContext
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|search
specifier|protected
name|TopDocs
name|search
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|ScoreDoc
name|after
parameter_list|,
name|int
name|nDocs
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
DECL|method|search
specifier|protected
name|TopDocs
name|search
parameter_list|(
name|List
argument_list|<
name|AtomicReaderContext
argument_list|>
name|leaves
parameter_list|,
name|Weight
name|weight
parameter_list|,
name|ScoreDoc
name|after
parameter_list|,
name|int
name|nDocs
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
DECL|method|search
specifier|protected
name|TopFieldDocs
name|search
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|int
name|nDocs
parameter_list|,
name|Sort
name|sort
parameter_list|,
name|boolean
name|doDocScores
parameter_list|,
name|boolean
name|doMaxScore
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
DECL|method|search
specifier|protected
name|TopFieldDocs
name|search
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|FieldDoc
name|after
parameter_list|,
name|int
name|nDocs
parameter_list|,
name|Sort
name|sort
parameter_list|,
name|boolean
name|fillFields
parameter_list|,
name|boolean
name|doDocScores
parameter_list|,
name|boolean
name|doMaxScore
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
DECL|method|search
specifier|protected
name|TopFieldDocs
name|search
parameter_list|(
name|List
argument_list|<
name|AtomicReaderContext
argument_list|>
name|leaves
parameter_list|,
name|Weight
name|weight
parameter_list|,
name|FieldDoc
name|after
parameter_list|,
name|int
name|nDocs
parameter_list|,
name|Sort
name|sort
parameter_list|,
name|boolean
name|fillFields
parameter_list|,
name|boolean
name|doDocScores
parameter_list|,
name|boolean
name|doMaxScore
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
block|}
end_class

end_unit

