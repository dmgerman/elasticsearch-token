begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.ipv4
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
name|ipv4
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
name|ParseField
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
name|ParseFieldMatcher
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
name|XContentParser
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
name|AggregatorBuilder
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
name|RangeAggregator
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
name|RangeAggregator
operator|.
name|Range
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
name|RangeParser
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IpRangeParser
specifier|public
class|class
name|IpRangeParser
extends|extends
name|RangeParser
block|{
DECL|method|IpRangeParser
specifier|public
name|IpRangeParser
parameter_list|()
block|{
name|super
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|InternalIPv4Range
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|parseRange
specifier|protected
name|Range
name|parseRange
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|IPv4RangeAggregatorBuilder
operator|.
name|Range
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createFactory
specifier|protected
name|IPv4RangeAggregatorBuilder
name|createFactory
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
block|{
name|IPv4RangeAggregatorBuilder
name|factory
init|=
operator|new
name|IPv4RangeAggregatorBuilder
argument_list|(
name|aggregationName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IPv4RangeAggregatorBuilder
operator|.
name|Range
argument_list|>
name|ranges
init|=
operator|(
name|List
argument_list|<
name|IPv4RangeAggregatorBuilder
operator|.
name|Range
argument_list|>
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|RangeAggregator
operator|.
name|RANGES_FIELD
argument_list|)
decl_stmt|;
for|for
control|(
name|IPv4RangeAggregatorBuilder
operator|.
name|Range
name|range
range|:
name|ranges
control|)
block|{
name|factory
operator|.
name|addRange
argument_list|(
name|range
argument_list|)
expr_stmt|;
block|}
name|Boolean
name|keyed
init|=
operator|(
name|Boolean
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|RangeAggregator
operator|.
name|KEYED_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyed
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|keyed
argument_list|(
name|keyed
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototypes
specifier|public
name|AggregatorBuilder
argument_list|<
name|?
argument_list|>
name|getFactoryPrototypes
parameter_list|()
block|{
return|return
operator|new
name|IPv4RangeAggregatorBuilder
argument_list|(
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

