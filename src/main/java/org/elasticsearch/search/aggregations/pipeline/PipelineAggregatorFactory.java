begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline
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
name|AggregatorFactory
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
comment|/**  * A factory that knows how to create an {@link PipelineAggregator} of a  * specific type.  */
end_comment

begin_class
DECL|class|PipelineAggregatorFactory
specifier|public
specifier|abstract
class|class
name|PipelineAggregatorFactory
block|{
DECL|field|name
specifier|protected
name|String
name|name
decl_stmt|;
DECL|field|type
specifier|protected
name|String
name|type
decl_stmt|;
DECL|field|bucketsPaths
specifier|protected
name|String
index|[]
name|bucketsPaths
decl_stmt|;
DECL|field|metaData
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
decl_stmt|;
comment|/**      * Constructs a new pipeline aggregator factory.      *       * @param name      *            The aggregation name      * @param type      *            The aggregation type      */
DECL|method|PipelineAggregatorFactory
specifier|public
name|PipelineAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|,
name|String
index|[]
name|bucketsPaths
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
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|bucketsPaths
operator|=
name|bucketsPaths
expr_stmt|;
block|}
comment|/**      * Validates the state of this factory (makes sure the factory is properly      * configured)      *       * @param pipelineAggregatorFactories      * @param factories      * @param parent      */
DECL|method|validate
specifier|public
specifier|final
name|void
name|validate
parameter_list|(
name|AggregatorFactory
name|parent
parameter_list|,
name|AggregatorFactory
index|[]
name|factories
parameter_list|,
name|List
argument_list|<
name|PipelineAggregatorFactory
argument_list|>
name|pipelineAggregatorFactories
parameter_list|)
block|{
name|doValidate
argument_list|(
name|parent
argument_list|,
name|factories
argument_list|,
name|pipelineAggregatorFactories
argument_list|)
expr_stmt|;
block|}
DECL|method|createInternal
specifier|protected
specifier|abstract
name|PipelineAggregator
name|createInternal
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Creates the pipeline aggregator      *       * @param context      *            The aggregation context      * @param parent      *            The parent aggregator (if this is a top level factory, the      *            parent will be {@code null})      * @param collectsFromSingleBucket      *            If true then the created aggregator will only be collected      *            with<tt>0</tt> as a bucket ordinal. Some factories can take      *            advantage of this in order to return more optimized      *            implementations.      *       * @return The created aggregator      */
DECL|method|create
specifier|public
specifier|final
name|PipelineAggregator
name|create
parameter_list|()
throws|throws
name|IOException
block|{
name|PipelineAggregator
name|aggregator
init|=
name|createInternal
argument_list|(
name|this
operator|.
name|metaData
argument_list|)
decl_stmt|;
return|return
name|aggregator
return|;
block|}
DECL|method|doValidate
specifier|public
name|void
name|doValidate
parameter_list|(
name|AggregatorFactory
name|parent
parameter_list|,
name|AggregatorFactory
index|[]
name|factories
parameter_list|,
name|List
argument_list|<
name|PipelineAggregatorFactory
argument_list|>
name|pipelineAggregatorFactories
parameter_list|)
block|{     }
DECL|method|setMetaData
specifier|public
name|void
name|setMetaData
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
block|{
name|this
operator|.
name|metaData
operator|=
name|metaData
expr_stmt|;
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|getBucketsPaths
specifier|public
name|String
index|[]
name|getBucketsPaths
parameter_list|()
block|{
return|return
name|bucketsPaths
return|;
block|}
block|}
end_class

end_unit
