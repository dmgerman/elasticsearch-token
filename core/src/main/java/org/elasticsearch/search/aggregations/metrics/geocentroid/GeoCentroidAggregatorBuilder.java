begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.geocentroid
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
name|geocentroid
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
name|ToXContent
operator|.
name|Params
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
name|AggregatorFactory
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
name|AggregatorFactories
operator|.
name|Builder
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
name|ValuesSource
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
name|ValuesSourceAggregatorBuilder
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
name|ValuesSourceConfig
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
name|ValuesSource
operator|.
name|GeoPoint
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
DECL|class|GeoCentroidAggregatorBuilder
specifier|public
class|class
name|GeoCentroidAggregatorBuilder
extends|extends
name|ValuesSourceAggregatorBuilder
operator|.
name|LeafOnly
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|,
name|GeoCentroidAggregatorBuilder
argument_list|>
block|{
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|GeoCentroidAggregatorBuilder
name|PROTOTYPE
init|=
operator|new
name|GeoCentroidAggregatorBuilder
argument_list|(
literal|""
argument_list|)
decl_stmt|;
DECL|method|GeoCentroidAggregatorBuilder
specifier|public
name|GeoCentroidAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalGeoCentroid
operator|.
name|TYPE
argument_list|,
name|ValuesSourceType
operator|.
name|GEOPOINT
argument_list|,
name|ValueType
operator|.
name|GEOPOINT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerBuild
specifier|protected
name|GeoCentroidAggregatorFactory
name|innerBuild
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|>
name|config
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|GeoCentroidAggregatorFactory
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|config
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerReadFrom
specifier|protected
name|GeoCentroidAggregatorBuilder
name|innerReadFrom
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|GeoCentroidAggregatorBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerWriteTo
specifier|protected
name|void
name|innerWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
block|{
comment|// Do nothing, no extra state to write to stream
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
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|innerHashCode
specifier|protected
name|int
name|innerHashCode
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|innerEquals
specifier|protected
name|boolean
name|innerEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

