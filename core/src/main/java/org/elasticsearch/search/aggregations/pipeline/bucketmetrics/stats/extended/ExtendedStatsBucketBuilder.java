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
name|aggregations
operator|.
name|pipeline
operator|.
name|bucketmetrics
operator|.
name|BucketMetricsBuilder
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
DECL|class|ExtendedStatsBucketBuilder
specifier|public
class|class
name|ExtendedStatsBucketBuilder
extends|extends
name|BucketMetricsBuilder
argument_list|<
name|ExtendedStatsBucketBuilder
argument_list|>
block|{
DECL|field|sigma
name|Double
name|sigma
decl_stmt|;
DECL|method|ExtendedStatsBucketBuilder
specifier|public
name|ExtendedStatsBucketBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|ExtendedStatsBucketPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|sigma
specifier|public
name|ExtendedStatsBucketBuilder
name|sigma
parameter_list|(
name|Double
name|sigma
parameter_list|)
block|{
name|this
operator|.
name|sigma
operator|=
name|sigma
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doInternalXContent
specifier|protected
name|void
name|doInternalXContent
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
if|if
condition|(
name|sigma
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|ExtendedStatsBucketParser
operator|.
name|SIGMA
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|sigma
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

