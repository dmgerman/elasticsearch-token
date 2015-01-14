begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.tophits
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|tophits
package|;
end_package

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
name|TopFieldDocs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|search
operator|.
name|SearchHits
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
name|AggregationStreams
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
name|InternalAggregation
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
name|metrics
operator|.
name|InternalMetricsAggregation
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
name|InternalSearchHit
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
name|InternalSearchHits
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|InternalTopHits
specifier|public
class|class
name|InternalTopHits
extends|extends
name|InternalMetricsAggregation
implements|implements
name|TopHits
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|InternalAggregation
operator|.
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
literal|"top_hits"
argument_list|)
decl_stmt|;
DECL|field|STREAM
specifier|public
specifier|static
specifier|final
name|AggregationStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|AggregationStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InternalTopHits
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalTopHits
name|buckets
init|=
operator|new
name|InternalTopHits
argument_list|()
decl_stmt|;
name|buckets
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|buckets
return|;
block|}
block|}
decl_stmt|;
DECL|method|registerStreams
specifier|public
specifier|static
name|void
name|registerStreams
parameter_list|()
block|{
name|AggregationStreams
operator|.
name|registerStream
argument_list|(
name|STREAM
argument_list|,
name|TYPE
operator|.
name|stream
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|field|from
specifier|private
name|int
name|from
decl_stmt|;
DECL|field|size
specifier|private
name|int
name|size
decl_stmt|;
DECL|field|topDocs
specifier|private
name|TopDocs
name|topDocs
decl_stmt|;
DECL|field|searchHits
specifier|private
name|InternalSearchHits
name|searchHits
decl_stmt|;
DECL|method|InternalTopHits
name|InternalTopHits
parameter_list|()
block|{     }
DECL|method|InternalTopHits
specifier|public
name|InternalTopHits
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|from
parameter_list|,
name|int
name|size
parameter_list|,
name|TopDocs
name|topDocs
parameter_list|,
name|InternalSearchHits
name|searchHits
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|topDocs
operator|=
name|topDocs
expr_stmt|;
name|this
operator|.
name|searchHits
operator|=
name|searchHits
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|getHits
specifier|public
name|SearchHits
name|getHits
parameter_list|()
block|{
return|return
name|searchHits
return|;
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|InternalAggregation
name|reduce
parameter_list|(
name|ReduceContext
name|reduceContext
parameter_list|)
block|{
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
init|=
name|reduceContext
operator|.
name|aggregations
argument_list|()
decl_stmt|;
name|InternalSearchHits
index|[]
name|shardHits
init|=
operator|new
name|InternalSearchHits
index|[
name|aggregations
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
specifier|final
name|TopDocs
name|reducedTopDocs
decl_stmt|;
specifier|final
name|TopDocs
index|[]
name|shardDocs
decl_stmt|;
try|try
block|{
if|if
condition|(
name|topDocs
operator|instanceof
name|TopFieldDocs
condition|)
block|{
name|Sort
name|sort
init|=
operator|new
name|Sort
argument_list|(
operator|(
operator|(
name|TopFieldDocs
operator|)
name|topDocs
operator|)
operator|.
name|fields
argument_list|)
decl_stmt|;
name|shardDocs
operator|=
operator|new
name|TopFieldDocs
index|[
name|aggregations
operator|.
name|size
argument_list|()
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
name|shardDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|InternalTopHits
name|topHitsAgg
init|=
operator|(
name|InternalTopHits
operator|)
name|aggregations
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|shardDocs
index|[
name|i
index|]
operator|=
operator|(
name|TopFieldDocs
operator|)
name|topHitsAgg
operator|.
name|topDocs
expr_stmt|;
name|shardHits
index|[
name|i
index|]
operator|=
name|topHitsAgg
operator|.
name|searchHits
expr_stmt|;
block|}
name|reducedTopDocs
operator|=
name|TopDocs
operator|.
name|merge
argument_list|(
name|sort
argument_list|,
name|from
argument_list|,
name|size
argument_list|,
operator|(
name|TopFieldDocs
index|[]
operator|)
name|shardDocs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shardDocs
operator|=
operator|new
name|TopDocs
index|[
name|aggregations
operator|.
name|size
argument_list|()
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
name|shardDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|InternalTopHits
name|topHitsAgg
init|=
operator|(
name|InternalTopHits
operator|)
name|aggregations
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|shardDocs
index|[
name|i
index|]
operator|=
name|topHitsAgg
operator|.
name|topDocs
expr_stmt|;
name|shardHits
index|[
name|i
index|]
operator|=
name|topHitsAgg
operator|.
name|searchHits
expr_stmt|;
block|}
name|reducedTopDocs
operator|=
name|TopDocs
operator|.
name|merge
argument_list|(
name|from
argument_list|,
name|size
argument_list|,
name|shardDocs
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
index|[]
name|tracker
init|=
operator|new
name|int
index|[
name|shardHits
operator|.
name|length
index|]
decl_stmt|;
name|InternalSearchHit
index|[]
name|hits
init|=
operator|new
name|InternalSearchHit
index|[
name|reducedTopDocs
operator|.
name|scoreDocs
operator|.
name|length
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
name|reducedTopDocs
operator|.
name|scoreDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ScoreDoc
name|scoreDoc
init|=
name|reducedTopDocs
operator|.
name|scoreDocs
index|[
name|i
index|]
decl_stmt|;
name|int
name|position
decl_stmt|;
do|do
block|{
name|position
operator|=
name|tracker
index|[
name|scoreDoc
operator|.
name|shardIndex
index|]
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|shardDocs
index|[
name|scoreDoc
operator|.
name|shardIndex
index|]
operator|.
name|scoreDocs
index|[
name|position
index|]
operator|!=
name|scoreDoc
condition|)
do|;
name|hits
index|[
name|i
index|]
operator|=
operator|(
name|InternalSearchHit
operator|)
name|shardHits
index|[
name|scoreDoc
operator|.
name|shardIndex
index|]
operator|.
name|getAt
argument_list|(
name|position
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalTopHits
argument_list|(
name|name
argument_list|,
name|from
argument_list|,
name|size
argument_list|,
name|reducedTopDocs
argument_list|,
operator|new
name|InternalSearchHits
argument_list|(
name|hits
argument_list|,
name|reducedTopDocs
operator|.
name|totalHits
argument_list|,
name|reducedTopDocs
operator|.
name|getMaxScore
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|ExceptionsHelper
operator|.
name|convertToElastic
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|getProperty
specifier|public
name|Object
name|getProperty
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"path not supported for ["
operator|+
name|getName
argument_list|()
operator|+
literal|"]: "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|void
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|from
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|topDocs
operator|=
name|Lucene
operator|.
name|readTopDocs
argument_list|(
name|in
argument_list|)
expr_stmt|;
assert|assert
name|topDocs
operator|!=
literal|null
assert|;
name|searchHits
operator|=
name|InternalSearchHits
operator|.
name|readSearchHits
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|from
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|Lucene
operator|.
name|writeTopDocs
argument_list|(
name|out
argument_list|,
name|topDocs
argument_list|)
expr_stmt|;
name|searchHits
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|public
name|XContentBuilder
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|searchHits
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

