begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.extended
package|package
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
name|bucketmetrics
operator|.
name|stats
operator|.
name|extended
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
name|search
operator|.
name|aggregations
operator|.
name|pipeline
operator|.
name|bucketmetrics
operator|.
name|BucketMetricsParser
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|ParseException
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
DECL|class|ExtendedStatsBucketParser
specifier|public
class|class
name|ExtendedStatsBucketParser
extends|extends
name|BucketMetricsParser
block|{
DECL|field|SIGMA
specifier|static
specifier|final
name|ParseField
name|SIGMA
init|=
operator|new
name|ParseField
argument_list|(
literal|"sigma"
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|ExtendedStatsBucketPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|buildFactory
specifier|protected
name|ExtendedStatsBucketPipelineAggregatorBuilder
name|buildFactory
parameter_list|(
name|String
name|pipelineAggregatorName
parameter_list|,
name|String
name|bucketsPath
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|unparsedParams
parameter_list|)
throws|throws
name|ParseException
block|{
name|Double
name|sigma
init|=
literal|null
decl_stmt|;
name|Object
name|param
init|=
name|unparsedParams
operator|.
name|get
argument_list|(
name|SIGMA
operator|.
name|getPreferredName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|param
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|param
operator|instanceof
name|Double
condition|)
block|{
name|sigma
operator|=
operator|(
name|Double
operator|)
name|param
expr_stmt|;
name|unparsedParams
operator|.
name|remove
argument_list|(
name|SIGMA
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Parameter ["
operator|+
name|SIGMA
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|"] must be a Double, type `"
operator|+
name|param
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"` provided instead"
argument_list|,
literal|0
argument_list|)
throw|;
block|}
block|}
name|ExtendedStatsBucketPipelineAggregatorBuilder
name|factory
init|=
operator|new
name|ExtendedStatsBucketPipelineAggregatorBuilder
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|bucketsPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|sigma
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|sigma
argument_list|(
name|sigma
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototype
specifier|public
name|ExtendedStatsBucketPipelineAggregatorBuilder
name|getFactoryPrototype
parameter_list|()
block|{
return|return
name|ExtendedStatsBucketPipelineAggregatorBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

