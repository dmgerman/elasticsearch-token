begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
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
name|histogram
package|;
end_package

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
name|InternalAggregations
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
name|numeric
operator|.
name|ValueFormatter
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
comment|/**  * TODO should be renamed to InternalNumericHistogram (see comment on {@link Histogram})?  */
end_comment

begin_class
DECL|class|InternalHistogram
specifier|public
class|class
name|InternalHistogram
extends|extends
name|AbstractHistogramBase
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
implements|implements
name|Histogram
block|{
DECL|field|TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
literal|"histogram"
argument_list|,
literal|"histo"
argument_list|)
decl_stmt|;
DECL|field|FACTORY
specifier|public
specifier|final
specifier|static
name|Factory
name|FACTORY
init|=
operator|new
name|Factory
argument_list|()
decl_stmt|;
DECL|field|STREAM
specifier|private
specifier|final
specifier|static
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
name|InternalHistogram
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalHistogram
name|histogram
init|=
operator|new
name|InternalHistogram
argument_list|()
decl_stmt|;
name|histogram
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|histogram
return|;
block|}
block|}
decl_stmt|;
DECL|method|registerStream
specifier|public
specifier|static
name|void
name|registerStream
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
DECL|class|Bucket
specifier|static
class|class
name|Bucket
extends|extends
name|AbstractHistogramBase
operator|.
name|Bucket
implements|implements
name|Histogram
operator|.
name|Bucket
block|{
DECL|method|Bucket
name|Bucket
parameter_list|(
name|long
name|key
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
name|super
argument_list|(
name|key
argument_list|,
name|docCount
argument_list|,
name|aggregations
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Factory
specifier|static
class|class
name|Factory
implements|implements
name|AbstractHistogramBase
operator|.
name|Factory
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
block|{
DECL|method|Factory
specifier|private
name|Factory
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
DECL|method|create
specifier|public
name|AbstractHistogramBase
argument_list|<
name|?
argument_list|>
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|EmptyBucketInfo
name|emptyBucketInfo
parameter_list|,
name|ValueFormatter
name|formatter
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
return|return
operator|new
name|InternalHistogram
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|order
argument_list|,
name|emptyBucketInfo
argument_list|,
name|formatter
argument_list|,
name|keyed
argument_list|)
return|;
block|}
DECL|method|createBucket
specifier|public
name|Bucket
name|createBucket
parameter_list|(
name|long
name|key
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
return|return
operator|new
name|Bucket
argument_list|(
name|key
argument_list|,
name|docCount
argument_list|,
name|aggregations
argument_list|)
return|;
block|}
block|}
DECL|method|InternalHistogram
specifier|public
name|InternalHistogram
parameter_list|()
block|{}
comment|// for serialization
DECL|method|InternalHistogram
specifier|public
name|InternalHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|EmptyBucketInfo
name|emptyBucketInfo
parameter_list|,
name|ValueFormatter
name|formatter
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|order
argument_list|,
name|emptyBucketInfo
argument_list|,
name|formatter
argument_list|,
name|keyed
argument_list|)
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
DECL|method|createBucket
specifier|protected
name|Bucket
name|createBucket
parameter_list|(
name|long
name|key
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
return|return
operator|new
name|Bucket
argument_list|(
name|key
argument_list|,
name|docCount
argument_list|,
name|aggregations
argument_list|)
return|;
block|}
block|}
end_class

end_unit

