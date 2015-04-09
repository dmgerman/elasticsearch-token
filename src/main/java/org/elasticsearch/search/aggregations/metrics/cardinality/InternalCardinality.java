begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.cardinality
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
name|cardinality
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
name|inject
operator|.
name|internal
operator|.
name|Nullable
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
name|util
operator|.
name|BigArrays
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
name|aggregations
operator|.
name|AggregationStreams
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
name|support
operator|.
name|format
operator|.
name|ValueFormatter
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
name|format
operator|.
name|ValueFormatterStreams
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
DECL|class|InternalCardinality
specifier|public
specifier|final
class|class
name|InternalCardinality
extends|extends
name|InternalNumericMetricsAggregation
operator|.
name|SingleValue
implements|implements
name|Cardinality
block|{
DECL|field|TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
literal|"cardinality"
argument_list|)
decl_stmt|;
DECL|field|STREAM
specifier|public
specifier|final
specifier|static
name|AggregationStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|AggregationStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InternalCardinality
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalCardinality
name|result
init|=
operator|new
name|InternalCardinality
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
decl_stmt|;
DECL|method|registerStreams
specifier|public
specifier|static
name|void
name|registerStreams
parameter_list|()
block|{
name|AggregationStreams
operator|.
name|registerStream
argument_list|(
name|STREAM
argument_list|,
name|TYPE
operator|.
name|stream
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|field|counts
specifier|private
name|HyperLogLogPlusPlus
name|counts
decl_stmt|;
DECL|method|InternalCardinality
name|InternalCardinality
parameter_list|(
name|String
name|name
parameter_list|,
name|HyperLogLogPlusPlus
name|counts
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
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
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|counts
operator|=
name|counts
expr_stmt|;
name|this
operator|.
name|valueFormatter
operator|=
name|formatter
expr_stmt|;
block|}
DECL|method|InternalCardinality
specifier|private
name|InternalCardinality
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|value
specifier|public
name|double
name|value
parameter_list|()
block|{
return|return
name|getValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getValue
specifier|public
name|long
name|getValue
parameter_list|()
block|{
return|return
name|counts
operator|==
literal|null
condition|?
literal|0
else|:
name|counts
operator|.
name|cardinality
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
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
name|valueFormatter
operator|=
name|ValueFormatterStreams
operator|.
name|readOptional
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|counts
operator|=
name|HyperLogLogPlusPlus
operator|.
name|readFrom
argument_list|(
name|in
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|counts
operator|=
literal|null
expr_stmt|;
block|}
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
name|ValueFormatterStreams
operator|.
name|writeOptional
argument_list|(
name|valueFormatter
argument_list|,
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|counts
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|counts
operator|.
name|writeTo
argument_list|(
literal|0
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|InternalAggregation
name|reduce
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
name|InternalCardinality
name|reduced
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
name|InternalCardinality
name|cardinality
init|=
operator|(
name|InternalCardinality
operator|)
name|aggregation
decl_stmt|;
if|if
condition|(
name|cardinality
operator|.
name|counts
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
name|reduced
operator|=
operator|new
name|InternalCardinality
argument_list|(
name|name
argument_list|,
operator|new
name|HyperLogLogPlusPlus
argument_list|(
name|cardinality
operator|.
name|counts
operator|.
name|precision
argument_list|()
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
literal|1
argument_list|)
argument_list|,
name|this
operator|.
name|valueFormatter
argument_list|,
name|getMetaData
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|reduced
operator|.
name|merge
argument_list|(
name|cardinality
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
comment|// all empty
return|return
name|aggregations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|reduced
return|;
block|}
block|}
DECL|method|merge
specifier|public
name|void
name|merge
parameter_list|(
name|InternalCardinality
name|other
parameter_list|)
block|{
assert|assert
name|counts
operator|!=
literal|null
operator|&&
name|other
operator|!=
literal|null
assert|;
name|counts
operator|.
name|merge
argument_list|(
literal|0
argument_list|,
name|other
operator|.
name|counts
argument_list|,
literal|0
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
specifier|final
name|long
name|cardinality
init|=
name|getValue
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|VALUE
argument_list|,
name|cardinality
argument_list|)
expr_stmt|;
if|if
condition|(
name|valueFormatter
operator|!=
literal|null
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
name|valueFormatter
operator|.
name|format
argument_list|(
name|cardinality
argument_list|)
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

