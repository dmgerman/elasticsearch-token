begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.profile.aggregation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|profile
operator|.
name|aggregation
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
name|AggregatorFactory
operator|.
name|MultiBucketAggregatorWrapper
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
name|profile
operator|.
name|AbstractInternalProfileTree
import|;
end_import

begin_class
DECL|class|InternalAggregationProfileTree
specifier|public
class|class
name|InternalAggregationProfileTree
extends|extends
name|AbstractInternalProfileTree
argument_list|<
name|AggregationProfileBreakdown
argument_list|,
name|Aggregator
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createProfileBreakdown
specifier|protected
name|AggregationProfileBreakdown
name|createProfileBreakdown
parameter_list|()
block|{
return|return
operator|new
name|AggregationProfileBreakdown
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getTypeFromElement
specifier|protected
name|String
name|getTypeFromElement
parameter_list|(
name|Aggregator
name|element
parameter_list|)
block|{
if|if
condition|(
name|element
operator|instanceof
name|MultiBucketAggregatorWrapper
condition|)
block|{
return|return
operator|(
operator|(
name|MultiBucketAggregatorWrapper
operator|)
name|element
operator|)
operator|.
name|getWrappedClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
return|return
name|element
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getDescriptionFromElement
specifier|protected
name|String
name|getDescriptionFromElement
parameter_list|(
name|Aggregator
name|element
parameter_list|)
block|{
return|return
name|element
operator|.
name|name
argument_list|()
return|;
block|}
block|}
end_class

end_unit
