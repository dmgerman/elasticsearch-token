begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
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
name|InternalAggregations
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
name|ArrayList
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

begin_comment
comment|/**  * A base class for all the single bucket aggregations.  */
end_comment

begin_class
DECL|class|InternalSingleBucketAggregation
specifier|public
specifier|abstract
class|class
name|InternalSingleBucketAggregation
extends|extends
name|InternalAggregation
implements|implements
name|SingleBucketAggregation
block|{
DECL|field|docCount
specifier|protected
name|long
name|docCount
decl_stmt|;
DECL|field|aggregations
specifier|protected
name|InternalAggregations
name|aggregations
decl_stmt|;
DECL|method|InternalSingleBucketAggregation
specifier|protected
name|InternalSingleBucketAggregation
parameter_list|()
block|{}
comment|// for serialization
comment|/**      * Creates a single bucket aggregation.      *      * @param name          The aggregation name.      * @param docCount      The document count in the single bucket.      * @param aggregations  The already built sub-aggregations that are associated with the bucket.      */
DECL|method|InternalSingleBucketAggregation
specifier|protected
name|InternalSingleBucketAggregation
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|docCount
operator|=
name|docCount
expr_stmt|;
name|this
operator|.
name|aggregations
operator|=
name|aggregations
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDocCount
specifier|public
name|long
name|getDocCount
parameter_list|()
block|{
return|return
name|docCount
return|;
block|}
annotation|@
name|Override
DECL|method|getAggregations
specifier|public
name|InternalAggregations
name|getAggregations
parameter_list|()
block|{
return|return
name|aggregations
return|;
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|InternalAggregation
name|reduce
parameter_list|(
name|ReduceContext
name|reduceContext
parameter_list|)
block|{
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
init|=
name|reduceContext
operator|.
name|aggregations
argument_list|()
decl_stmt|;
if|if
condition|(
name|aggregations
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|InternalSingleBucketAggregation
name|reduced
init|=
operator|(
operator|(
name|InternalSingleBucketAggregation
operator|)
name|aggregations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
decl_stmt|;
name|reduced
operator|.
name|aggregations
operator|.
name|reduce
argument_list|(
name|reduceContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|reduced
return|;
block|}
name|InternalSingleBucketAggregation
name|reduced
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|InternalAggregations
argument_list|>
name|subAggregationsList
init|=
operator|new
name|ArrayList
argument_list|<
name|InternalAggregations
argument_list|>
argument_list|(
name|aggregations
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|InternalAggregation
name|aggregation
range|:
name|aggregations
control|)
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
operator|(
name|InternalSingleBucketAggregation
operator|)
name|aggregation
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|docCount
operator|+=
operator|(
operator|(
name|InternalSingleBucketAggregation
operator|)
name|aggregation
operator|)
operator|.
name|docCount
expr_stmt|;
block|}
name|subAggregationsList
operator|.
name|add
argument_list|(
operator|(
operator|(
name|InternalSingleBucketAggregation
operator|)
name|aggregation
operator|)
operator|.
name|aggregations
argument_list|)
expr_stmt|;
block|}
name|reduced
operator|.
name|aggregations
operator|=
name|InternalAggregations
operator|.
name|reduce
argument_list|(
name|subAggregationsList
argument_list|,
name|reduceContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|reduced
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|docCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|aggregations
operator|=
name|InternalAggregations
operator|.
name|readAggregations
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|docCount
argument_list|)
expr_stmt|;
name|aggregations
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|DOC_COUNT
argument_list|,
name|docCount
argument_list|)
expr_stmt|;
name|aggregations
operator|.
name|toXContentInternal
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

