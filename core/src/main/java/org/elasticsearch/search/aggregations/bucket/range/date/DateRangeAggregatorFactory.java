begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.date
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
name|date
package|;
end_package

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
operator|.
name|Type
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
name|AbstractRangeAggregatorFactory
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
operator|.
name|Factory
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
name|support
operator|.
name|ValuesSource
operator|.
name|Numeric
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_class
DECL|class|DateRangeAggregatorFactory
specifier|public
class|class
name|DateRangeAggregatorFactory
extends|extends
name|AbstractRangeAggregatorFactory
argument_list|<
name|DateRangeAggregatorFactory
argument_list|,
name|Range
argument_list|>
block|{
DECL|method|DateRangeAggregatorFactory
specifier|public
name|DateRangeAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|Numeric
argument_list|>
name|config
parameter_list|,
name|List
argument_list|<
name|Range
argument_list|>
name|ranges
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|Factory
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|rangeFactory
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|config
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|rangeFactory
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

