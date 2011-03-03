begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|unit
operator|.
name|ByteSizeValue
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
name|ToXContent
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilderString
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|CacheStats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
operator|.
name|MergeStats
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * Global information on indices stats running on a specific node.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|NodeIndicesStats
specifier|public
class|class
name|NodeIndicesStats
implements|implements
name|Streamable
implements|,
name|Serializable
implements|,
name|ToXContent
block|{
DECL|field|storeSize
specifier|private
name|ByteSizeValue
name|storeSize
decl_stmt|;
DECL|field|numDocs
specifier|private
name|long
name|numDocs
decl_stmt|;
DECL|field|cacheStats
specifier|private
name|CacheStats
name|cacheStats
decl_stmt|;
DECL|field|mergeStats
specifier|private
name|MergeStats
name|mergeStats
decl_stmt|;
DECL|method|NodeIndicesStats
name|NodeIndicesStats
parameter_list|()
block|{     }
DECL|method|NodeIndicesStats
specifier|public
name|NodeIndicesStats
parameter_list|(
name|ByteSizeValue
name|storeSize
parameter_list|,
name|long
name|numDocs
parameter_list|,
name|CacheStats
name|cacheStats
parameter_list|,
name|MergeStats
name|mergeStats
parameter_list|)
block|{
name|this
operator|.
name|storeSize
operator|=
name|storeSize
expr_stmt|;
name|this
operator|.
name|numDocs
operator|=
name|numDocs
expr_stmt|;
name|this
operator|.
name|cacheStats
operator|=
name|cacheStats
expr_stmt|;
name|this
operator|.
name|mergeStats
operator|=
name|mergeStats
expr_stmt|;
block|}
comment|/**      * The size of the index storage taken on the node.      */
DECL|method|storeSize
specifier|public
name|ByteSizeValue
name|storeSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeSize
return|;
block|}
comment|/**      * The size of the index storage taken on the node.      */
DECL|method|getStoreSize
specifier|public
name|ByteSizeValue
name|getStoreSize
parameter_list|()
block|{
return|return
name|storeSize
return|;
block|}
comment|/**      * The number of docs on the node (an aggregation of the number of docs of all the shards allocated on the node).      */
DECL|method|numDocs
specifier|public
name|long
name|numDocs
parameter_list|()
block|{
return|return
name|numDocs
return|;
block|}
comment|/**      * The number of docs on the node (an aggregation of the number of docs of all the shards allocated on the node).      */
DECL|method|getNumDocs
specifier|public
name|long
name|getNumDocs
parameter_list|()
block|{
return|return
name|numDocs
argument_list|()
return|;
block|}
DECL|method|cache
specifier|public
name|CacheStats
name|cache
parameter_list|()
block|{
return|return
name|this
operator|.
name|cacheStats
return|;
block|}
DECL|method|getCache
specifier|public
name|CacheStats
name|getCache
parameter_list|()
block|{
return|return
name|this
operator|.
name|cache
argument_list|()
return|;
block|}
DECL|method|merge
specifier|public
name|MergeStats
name|merge
parameter_list|()
block|{
return|return
name|this
operator|.
name|mergeStats
return|;
block|}
DECL|method|getMerge
specifier|public
name|MergeStats
name|getMerge
parameter_list|()
block|{
return|return
name|this
operator|.
name|mergeStats
return|;
block|}
DECL|method|readIndicesStats
specifier|public
specifier|static
name|NodeIndicesStats
name|readIndicesStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|NodeIndicesStats
name|stats
init|=
operator|new
name|NodeIndicesStats
argument_list|()
decl_stmt|;
name|stats
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|stats
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
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
name|storeSize
operator|=
name|ByteSizeValue
operator|.
name|readBytesSizeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|numDocs
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|cacheStats
operator|=
name|CacheStats
operator|.
name|readCacheStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|mergeStats
operator|=
name|MergeStats
operator|.
name|readMergeStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
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
name|storeSize
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
name|cacheStats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|mergeStats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
DECL|method|toXContent
annotation|@
name|Override
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
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|STORE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SIZE
argument_list|,
name|storeSize
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SIZE_IN_BYTES
argument_list|,
name|storeSize
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|DOCS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUM_DOCS
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|cacheStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|mergeStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
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
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|INDICES
specifier|static
specifier|final
name|XContentBuilderString
name|INDICES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"indices"
argument_list|)
decl_stmt|;
DECL|field|STORE
specifier|static
specifier|final
name|XContentBuilderString
name|STORE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"store"
argument_list|)
decl_stmt|;
DECL|field|SIZE
specifier|static
specifier|final
name|XContentBuilderString
name|SIZE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"store_size"
argument_list|)
decl_stmt|;
DECL|field|SIZE_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|SIZE_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"store_size_in_bytes"
argument_list|)
decl_stmt|;
DECL|field|DOCS
specifier|static
specifier|final
name|XContentBuilderString
name|DOCS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"docs"
argument_list|)
decl_stmt|;
DECL|field|NUM_DOCS
specifier|static
specifier|final
name|XContentBuilderString
name|NUM_DOCS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"num_docs"
argument_list|)
decl_stmt|;
DECL|field|MERGES
specifier|static
specifier|final
name|XContentBuilderString
name|MERGES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"merges"
argument_list|)
decl_stmt|;
DECL|field|CURRENT
specifier|static
specifier|final
name|XContentBuilderString
name|CURRENT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"current"
argument_list|)
decl_stmt|;
DECL|field|TOTAL
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_time"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_TIME_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_TIME_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_time_in_millis"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

