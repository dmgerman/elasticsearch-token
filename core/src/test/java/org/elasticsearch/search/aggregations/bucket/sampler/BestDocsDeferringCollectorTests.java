begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.sampler
package|package
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
name|sampler
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
name|util
operator|.
name|MockBigArrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|AggregatorTestCase
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
name|LeafBucketCollector
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
name|Collections
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
name|Set
import|;
end_import

begin_class
DECL|class|BestDocsDeferringCollectorTests
specifier|public
class|class
name|BestDocsDeferringCollectorTests
extends|extends
name|AggregatorTestCase
block|{
DECL|method|testReplay
specifier|public
name|void
name|testReplay
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
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|128
argument_list|)
decl_stmt|;
name|int
name|maxNumValues
init|=
name|randomInt
argument_list|(
literal|16
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
name|i
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
literal|"field"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|randomInt
argument_list|(
name|maxNumValues
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
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
block|}
name|indexWriter
operator|.
name|close
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
name|indexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
name|TermQuery
name|termQuery
init|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|randomInt
argument_list|(
name|maxNumValues
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|TopDocs
name|topDocs
init|=
name|indexSearcher
operator|.
name|search
argument_list|(
name|termQuery
argument_list|,
name|numDocs
argument_list|)
decl_stmt|;
name|BestDocsDeferringCollector
name|collector
init|=
operator|new
name|BestDocsDeferringCollector
argument_list|(
name|numDocs
argument_list|,
operator|new
name|MockBigArrays
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|deferredCollectedDocIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|collector
operator|.
name|setDeferredCollector
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|testCollector
argument_list|(
name|deferredCollectedDocIds
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|collector
operator|.
name|preCollection
argument_list|()
expr_stmt|;
name|indexSearcher
operator|.
name|search
argument_list|(
name|termQuery
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|collector
operator|.
name|postCollection
argument_list|()
expr_stmt|;
name|collector
operator|.
name|replay
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|deferredCollectedDocIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ScoreDoc
name|scoreDoc
range|:
name|topDocs
operator|.
name|scoreDocs
control|)
block|{
name|assertTrue
argument_list|(
literal|"expected docid ["
operator|+
name|scoreDoc
operator|.
name|doc
operator|+
literal|"] is missing"
argument_list|,
name|deferredCollectedDocIds
operator|.
name|contains
argument_list|(
name|scoreDoc
operator|.
name|doc
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|collector
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
DECL|method|testCollector
specifier|private
name|BucketCollector
name|testCollector
parameter_list|(
name|Set
argument_list|<
name|Integer
argument_list|>
name|docIds
parameter_list|)
block|{
return|return
operator|new
name|BucketCollector
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LeafBucketCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|LeafBucketCollector
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
name|docIds
operator|.
name|add
argument_list|(
name|ctx
operator|.
name|docBase
operator|+
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCollection
parameter_list|()
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|void
name|postCollection
parameter_list|()
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

