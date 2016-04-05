begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles.tdigest
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
name|percentiles
operator|.
name|tdigest
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
name|metrics
operator|.
name|InternalNumericMetricsAggregation
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
DECL|class|AbstractInternalTDigestPercentiles
specifier|abstract
class|class
name|AbstractInternalTDigestPercentiles
extends|extends
name|InternalNumericMetricsAggregation
operator|.
name|MultiValue
block|{
DECL|field|keys
specifier|protected
name|double
index|[]
name|keys
decl_stmt|;
DECL|field|state
specifier|protected
name|TDigestState
name|state
decl_stmt|;
DECL|field|keyed
specifier|private
name|boolean
name|keyed
decl_stmt|;
DECL|method|AbstractInternalTDigestPercentiles
name|AbstractInternalTDigestPercentiles
parameter_list|()
block|{}
comment|// for serialization
DECL|method|AbstractInternalTDigestPercentiles
specifier|public
name|AbstractInternalTDigestPercentiles
parameter_list|(
name|String
name|name
parameter_list|,
name|double
index|[]
name|keys
parameter_list|,
name|TDigestState
name|state
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|DocValueFormat
name|formatter
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
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|keys
operator|=
name|keys
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
name|this
operator|.
name|format
operator|=
name|formatter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|double
name|value
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|value
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|name
argument_list|)
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
specifier|abstract
name|double
name|value
parameter_list|(
name|double
name|key
parameter_list|)
function_decl|;
DECL|method|getEstimatedMemoryFootprint
specifier|public
name|long
name|getEstimatedMemoryFootprint
parameter_list|()
block|{
return|return
name|state
operator|.
name|byteSize
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doReduce
specifier|public
name|AbstractInternalTDigestPercentiles
name|doReduce
parameter_list|(
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
parameter_list|,
name|ReduceContext
name|reduceContext
parameter_list|)
block|{
name|TDigestState
name|merged
init|=
literal|null
decl_stmt|;
for|for
control|(
name|InternalAggregation
name|aggregation
range|:
name|aggregations
control|)
block|{
specifier|final
name|AbstractInternalTDigestPercentiles
name|percentiles
init|=
operator|(
name|AbstractInternalTDigestPercentiles
operator|)
name|aggregation
decl_stmt|;
if|if
condition|(
name|merged
operator|==
literal|null
condition|)
block|{
name|merged
operator|=
operator|new
name|TDigestState
argument_list|(
name|percentiles
operator|.
name|state
operator|.
name|compression
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|merged
operator|.
name|add
argument_list|(
name|percentiles
operator|.
name|state
argument_list|)
expr_stmt|;
block|}
return|return
name|createReduced
argument_list|(
name|getName
argument_list|()
argument_list|,
name|keys
argument_list|,
name|merged
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|getMetaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|createReduced
specifier|protected
specifier|abstract
name|AbstractInternalTDigestPercentiles
name|createReduced
parameter_list|(
name|String
name|name
parameter_list|,
name|double
index|[]
name|keys
parameter_list|,
name|TDigestState
name|merged
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
function_decl|;
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|void
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|format
operator|=
name|in
operator|.
name|readValueFormat
argument_list|()
expr_stmt|;
name|keys
operator|=
operator|new
name|double
index|[
name|in
operator|.
name|readInt
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|keys
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
block|}
name|state
operator|=
name|TDigestState
operator|.
name|read
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|keyed
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeValueFormat
argument_list|(
name|format
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|keys
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|TDigestState
operator|.
name|write
argument_list|(
name|state
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|keyed
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|keyed
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|CommonFields
operator|.
name|VALUES
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|String
name|key
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|double
name|value
init|=
name|value
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|format
operator|!=
name|DocValueFormat
operator|.
name|RAW
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|key
operator|+
literal|"_as_string"
argument_list|,
name|format
operator|.
name|format
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|CommonFields
operator|.
name|VALUES
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|double
name|value
init|=
name|value
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|KEY
argument_list|,
name|keys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|VALUE
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|format
operator|!=
name|DocValueFormat
operator|.
name|RAW
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|VALUE_AS_STRING
argument_list|,
name|format
operator|.
name|format
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

