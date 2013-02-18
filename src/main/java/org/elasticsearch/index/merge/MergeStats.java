begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.merge
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
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
name|unit
operator|.
name|TimeValue
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MergeStats
specifier|public
class|class
name|MergeStats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|total
specifier|private
name|long
name|total
decl_stmt|;
DECL|field|totalTimeInMillis
specifier|private
name|long
name|totalTimeInMillis
decl_stmt|;
DECL|field|totalNumDocs
specifier|private
name|long
name|totalNumDocs
decl_stmt|;
DECL|field|totalSizeInBytes
specifier|private
name|long
name|totalSizeInBytes
decl_stmt|;
DECL|field|current
specifier|private
name|long
name|current
decl_stmt|;
DECL|field|currentNumDocs
specifier|private
name|long
name|currentNumDocs
decl_stmt|;
DECL|field|currentSizeInBytes
specifier|private
name|long
name|currentSizeInBytes
decl_stmt|;
DECL|method|MergeStats
specifier|public
name|MergeStats
parameter_list|()
block|{      }
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|long
name|totalMerges
parameter_list|,
name|long
name|totalMergeTime
parameter_list|,
name|long
name|totalNumDocs
parameter_list|,
name|long
name|totalSizeInBytes
parameter_list|,
name|long
name|currentMerges
parameter_list|,
name|long
name|currentNumDocs
parameter_list|,
name|long
name|currentSizeInBytes
parameter_list|)
block|{
name|this
operator|.
name|total
operator|+=
name|totalMerges
expr_stmt|;
name|this
operator|.
name|totalTimeInMillis
operator|+=
name|totalMergeTime
expr_stmt|;
name|this
operator|.
name|totalNumDocs
operator|+=
name|totalNumDocs
expr_stmt|;
name|this
operator|.
name|totalSizeInBytes
operator|+=
name|totalSizeInBytes
expr_stmt|;
name|this
operator|.
name|current
operator|+=
name|currentMerges
expr_stmt|;
name|this
operator|.
name|currentNumDocs
operator|+=
name|currentNumDocs
expr_stmt|;
name|this
operator|.
name|currentSizeInBytes
operator|+=
name|currentSizeInBytes
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|MergeStats
name|mergeStats
parameter_list|)
block|{
if|if
condition|(
name|mergeStats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|total
operator|+=
name|mergeStats
operator|.
name|total
expr_stmt|;
name|this
operator|.
name|totalTimeInMillis
operator|+=
name|mergeStats
operator|.
name|totalTimeInMillis
expr_stmt|;
name|this
operator|.
name|totalNumDocs
operator|+=
name|mergeStats
operator|.
name|totalNumDocs
expr_stmt|;
name|this
operator|.
name|totalSizeInBytes
operator|+=
name|mergeStats
operator|.
name|totalSizeInBytes
expr_stmt|;
name|this
operator|.
name|current
operator|+=
name|mergeStats
operator|.
name|current
expr_stmt|;
name|this
operator|.
name|currentNumDocs
operator|+=
name|mergeStats
operator|.
name|currentNumDocs
expr_stmt|;
name|this
operator|.
name|currentSizeInBytes
operator|+=
name|mergeStats
operator|.
name|currentSizeInBytes
expr_stmt|;
block|}
comment|/**      * The total number of merges executed.      */
DECL|method|getTotal
specifier|public
name|long
name|getTotal
parameter_list|()
block|{
return|return
name|this
operator|.
name|total
return|;
block|}
comment|/**      * The total time merges have been executed (in milliseconds).      */
DECL|method|getTotalTimeInMillis
specifier|public
name|long
name|getTotalTimeInMillis
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalTimeInMillis
return|;
block|}
comment|/**      * The total time merges have been executed.      */
DECL|method|getTotalTime
specifier|public
name|TimeValue
name|getTotalTime
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|totalTimeInMillis
argument_list|)
return|;
block|}
DECL|method|getTotalNumDocs
specifier|public
name|long
name|getTotalNumDocs
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalNumDocs
return|;
block|}
DECL|method|getTotalSizeInBytes
specifier|public
name|long
name|getTotalSizeInBytes
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalSizeInBytes
return|;
block|}
DECL|method|getTotalSize
specifier|public
name|ByteSizeValue
name|getTotalSize
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|totalSizeInBytes
argument_list|)
return|;
block|}
comment|/**      * The current number of merges executing.      */
DECL|method|getCurrent
specifier|public
name|long
name|getCurrent
parameter_list|()
block|{
return|return
name|this
operator|.
name|current
return|;
block|}
DECL|method|getCurrentNumDocs
specifier|public
name|long
name|getCurrentNumDocs
parameter_list|()
block|{
return|return
name|this
operator|.
name|currentNumDocs
return|;
block|}
DECL|method|getCurrentSizeInBytes
specifier|public
name|long
name|getCurrentSizeInBytes
parameter_list|()
block|{
return|return
name|this
operator|.
name|currentSizeInBytes
return|;
block|}
DECL|method|getCurrentSize
specifier|public
name|ByteSizeValue
name|getCurrentSize
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|currentSizeInBytes
argument_list|)
return|;
block|}
DECL|method|readMergeStats
specifier|public
specifier|static
name|MergeStats
name|readMergeStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|MergeStats
name|stats
init|=
operator|new
name|MergeStats
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
name|Fields
operator|.
name|MERGES
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|CURRENT
argument_list|,
name|current
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|CURRENT_DOCS
argument_list|,
name|currentNumDocs
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|CURRENT_SIZE
argument_list|,
name|getCurrentSize
argument_list|()
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
name|CURRENT_SIZE_IN_BYTES
argument_list|,
name|currentSizeInBytes
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL
argument_list|,
name|total
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL_TIME
argument_list|,
name|getTotalTime
argument_list|()
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
name|TOTAL_TIME_IN_MILLIS
argument_list|,
name|totalTimeInMillis
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL_DOCS
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL_SIZE
argument_list|,
name|getTotalSize
argument_list|()
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
name|TOTAL_SIZE_IN_BYTES
argument_list|,
name|totalSizeInBytes
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
DECL|field|CURRENT_DOCS
specifier|static
specifier|final
name|XContentBuilderString
name|CURRENT_DOCS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"current_docs"
argument_list|)
decl_stmt|;
DECL|field|CURRENT_SIZE
specifier|static
specifier|final
name|XContentBuilderString
name|CURRENT_SIZE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"current_size"
argument_list|)
decl_stmt|;
DECL|field|CURRENT_SIZE_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|CURRENT_SIZE_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"current_size_in_bytes"
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
DECL|field|TOTAL_DOCS
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_DOCS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_docs"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_SIZE
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_SIZE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_size"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_SIZE_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_SIZE_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_size_in_bytes"
argument_list|)
decl_stmt|;
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
name|total
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|totalTimeInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|totalNumDocs
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|totalSizeInBytes
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|current
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|currentNumDocs
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|currentSizeInBytes
operator|=
name|in
operator|.
name|readVLong
argument_list|()
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
name|writeVLong
argument_list|(
name|total
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|totalTimeInMillis
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|totalNumDocs
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|totalSizeInBytes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|current
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|currentNumDocs
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|currentSizeInBytes
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

