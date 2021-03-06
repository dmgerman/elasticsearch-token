begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.geodistance
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
name|range
operator|.
name|geodistance
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
name|DocValueFormat
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
name|bucket
operator|.
name|range
operator|.
name|InternalRange
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
name|pipeline
operator|.
name|PipelineAggregator
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
name|ValueType
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
name|ValuesSourceType
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

begin_class
DECL|class|InternalGeoDistance
specifier|public
class|class
name|InternalGeoDistance
extends|extends
name|InternalRange
argument_list|<
name|InternalGeoDistance
operator|.
name|Bucket
argument_list|,
name|InternalGeoDistance
argument_list|>
block|{
DECL|field|FACTORY
specifier|public
specifier|static
specifier|final
name|Factory
name|FACTORY
init|=
operator|new
name|Factory
argument_list|()
decl_stmt|;
DECL|class|Bucket
specifier|static
class|class
name|Bucket
extends|extends
name|InternalRange
operator|.
name|Bucket
block|{
DECL|method|Bucket
name|Bucket
parameter_list|(
name|boolean
name|keyed
parameter_list|)
block|{
name|super
argument_list|(
name|keyed
argument_list|,
name|DocValueFormat
operator|.
name|RAW
argument_list|)
expr_stmt|;
block|}
DECL|method|Bucket
name|Bucket
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|,
name|long
name|docCount
parameter_list|,
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
name|this
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|docCount
argument_list|,
operator|new
name|InternalAggregations
argument_list|(
name|aggregations
argument_list|)
argument_list|,
name|keyed
argument_list|)
expr_stmt|;
block|}
DECL|method|Bucket
name|Bucket
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
name|super
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|docCount
argument_list|,
name|aggregations
argument_list|,
name|keyed
argument_list|,
name|DocValueFormat
operator|.
name|RAW
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getFactory
specifier|protected
name|InternalRange
operator|.
name|Factory
argument_list|<
name|Bucket
argument_list|,
name|?
argument_list|>
name|getFactory
parameter_list|()
block|{
return|return
name|FACTORY
return|;
block|}
DECL|method|keyed
name|boolean
name|keyed
parameter_list|()
block|{
return|return
name|keyed
return|;
block|}
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
extends|extends
name|InternalRange
operator|.
name|Factory
argument_list|<
name|InternalGeoDistance
operator|.
name|Bucket
argument_list|,
name|InternalGeoDistance
argument_list|>
block|{
annotation|@
name|Override
DECL|method|getValueSourceType
specifier|public
name|ValuesSourceType
name|getValueSourceType
parameter_list|()
block|{
return|return
name|ValuesSourceType
operator|.
name|GEOPOINT
return|;
block|}
annotation|@
name|Override
DECL|method|getValueType
specifier|public
name|ValueType
name|getValueType
parameter_list|()
block|{
return|return
name|ValueType
operator|.
name|GEOPOINT
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|InternalGeoDistance
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|Bucket
argument_list|>
name|ranges
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
block|{
return|return
operator|new
name|InternalGeoDistance
argument_list|(
name|name
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|InternalGeoDistance
name|create
parameter_list|(
name|List
argument_list|<
name|Bucket
argument_list|>
name|ranges
parameter_list|,
name|InternalGeoDistance
name|prototype
parameter_list|)
block|{
return|return
operator|new
name|InternalGeoDistance
argument_list|(
name|prototype
operator|.
name|name
argument_list|,
name|ranges
argument_list|,
name|prototype
operator|.
name|keyed
argument_list|,
name|prototype
operator|.
name|pipelineAggregators
argument_list|()
argument_list|,
name|prototype
operator|.
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createBucket
specifier|public
name|Bucket
name|createBucket
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|DocValueFormat
name|format
parameter_list|)
block|{
return|return
operator|new
name|Bucket
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|docCount
argument_list|,
name|aggregations
argument_list|,
name|keyed
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createBucket
specifier|public
name|Bucket
name|createBucket
parameter_list|(
name|InternalAggregations
name|aggregations
parameter_list|,
name|Bucket
name|prototype
parameter_list|)
block|{
return|return
operator|new
name|Bucket
argument_list|(
name|prototype
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
operator|(
name|Number
operator|)
name|prototype
operator|.
name|getFrom
argument_list|()
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|,
operator|(
operator|(
name|Number
operator|)
name|prototype
operator|.
name|getTo
argument_list|()
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|,
name|prototype
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|aggregations
argument_list|,
name|prototype
operator|.
name|getKeyed
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|InternalGeoDistance
specifier|public
name|InternalGeoDistance
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|Bucket
argument_list|>
name|ranges
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|ranges
argument_list|,
name|DocValueFormat
operator|.
name|RAW
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|InternalGeoDistance
specifier|public
name|InternalGeoDistance
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getFactory
specifier|public
name|InternalRange
operator|.
name|Factory
argument_list|<
name|Bucket
argument_list|,
name|InternalGeoDistance
argument_list|>
name|getFactory
parameter_list|()
block|{
return|return
name|FACTORY
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|GeoDistanceAggregationBuilder
operator|.
name|NAME
return|;
block|}
block|}
end_class

end_unit

