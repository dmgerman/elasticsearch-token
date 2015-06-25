begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.having
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
name|having
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
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
operator|.
name|ScriptField
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
name|BucketHelpers
operator|.
name|GapPolicy
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
name|pipeline
operator|.
name|PipelineAggregatorBuilder
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
name|Map
import|;
end_import

begin_class
DECL|class|BucketSelectorBuilder
specifier|public
class|class
name|BucketSelectorBuilder
extends|extends
name|PipelineAggregatorBuilder
argument_list|<
name|BucketSelectorBuilder
argument_list|>
block|{
DECL|field|gapPolicy
specifier|private
name|GapPolicy
name|gapPolicy
decl_stmt|;
DECL|field|script
specifier|private
name|Script
name|script
decl_stmt|;
DECL|field|bucketsPathsMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bucketsPathsMap
decl_stmt|;
DECL|method|BucketSelectorBuilder
specifier|public
name|BucketSelectorBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|BucketSelectorPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|script
specifier|public
name|BucketSelectorBuilder
name|script
parameter_list|(
name|Script
name|script
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|gapPolicy
specifier|public
name|BucketSelectorBuilder
name|gapPolicy
parameter_list|(
name|GapPolicy
name|gapPolicy
parameter_list|)
block|{
name|this
operator|.
name|gapPolicy
operator|=
name|gapPolicy
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the paths to the buckets to use for this pipeline aggregator. The      * map given to this method must contain script variable name as keys with      * bucket paths values to the metrics to use for each variable.      */
DECL|method|setBucketsPathsMap
specifier|public
name|BucketSelectorBuilder
name|setBucketsPathsMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bucketsPathsMap
parameter_list|)
block|{
name|this
operator|.
name|bucketsPathsMap
operator|=
name|bucketsPathsMap
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|protected
name|XContentBuilder
name|internalXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|builderParams
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|ScriptField
operator|.
name|SCRIPT
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|script
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|gapPolicy
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|BucketSelectorParser
operator|.
name|GAP_POLICY
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|gapPolicy
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bucketsPathsMap
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|PipelineAggregator
operator|.
name|Parser
operator|.
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|bucketsPathsMap
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

