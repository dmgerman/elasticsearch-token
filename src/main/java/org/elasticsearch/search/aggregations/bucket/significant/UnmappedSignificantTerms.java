begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.significant
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
name|significant
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
name|AggregationStreams
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
DECL|class|UnmappedSignificantTerms
specifier|public
class|class
name|UnmappedSignificantTerms
extends|extends
name|InternalSignificantTerms
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
literal|"significant_terms"
argument_list|,
literal|"umsigterms"
argument_list|)
decl_stmt|;
DECL|field|BUCKETS
specifier|private
specifier|static
specifier|final
name|Collection
argument_list|<
name|Bucket
argument_list|>
name|BUCKETS
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
DECL|field|BUCKETS_MAP
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Bucket
argument_list|>
name|BUCKETS_MAP
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
DECL|field|STREAM
specifier|public
specifier|static
specifier|final
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
name|UnmappedSignificantTerms
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|UnmappedSignificantTerms
name|buckets
init|=
operator|new
name|UnmappedSignificantTerms
argument_list|()
decl_stmt|;
name|buckets
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|buckets
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
DECL|method|UnmappedSignificantTerms
name|UnmappedSignificantTerms
parameter_list|()
block|{}
comment|// for serialization
DECL|method|UnmappedSignificantTerms
specifier|public
name|UnmappedSignificantTerms
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|long
name|minDocCount
parameter_list|)
block|{
comment|//We pass zero for index/subset sizes because for the purpose of significant term analysis
comment|// we assume an unmapped index's size is irrelevant to the proceedings.
name|super
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
name|name
argument_list|,
name|requiredSize
argument_list|,
name|minDocCount
argument_list|,
name|BUCKETS
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|requiredSize
operator|=
name|readSize
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|minDocCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|buckets
operator|=
name|BUCKETS
expr_stmt|;
name|this
operator|.
name|bucketMap
operator|=
name|BUCKETS_MAP
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
name|writeSize
argument_list|(
name|requiredSize
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|minDocCount
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
name|startArray
argument_list|(
name|CommonFields
operator|.
name|BUCKETS
argument_list|)
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

