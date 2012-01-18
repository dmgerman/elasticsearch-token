begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|fs
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
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
name|Strings
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
name|Iterator
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|FsStats
specifier|public
class|class
name|FsStats
implements|implements
name|Iterable
argument_list|<
name|FsStats
operator|.
name|Info
argument_list|>
implements|,
name|Streamable
implements|,
name|ToXContent
block|{
DECL|class|Info
specifier|public
specifier|static
class|class
name|Info
implements|implements
name|Streamable
block|{
DECL|field|path
name|String
name|path
decl_stmt|;
annotation|@
name|Nullable
DECL|field|mount
name|String
name|mount
decl_stmt|;
annotation|@
name|Nullable
DECL|field|dev
name|String
name|dev
decl_stmt|;
DECL|field|total
name|long
name|total
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|free
name|long
name|free
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|available
name|long
name|available
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|diskReads
name|long
name|diskReads
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|diskWrites
name|long
name|diskWrites
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|diskReadBytes
name|long
name|diskReadBytes
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|diskWriteBytes
name|long
name|diskWriteBytes
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|diskQueue
name|double
name|diskQueue
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|diskServiceTime
name|double
name|diskServiceTime
init|=
operator|-
literal|1
decl_stmt|;
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
name|path
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|mount
operator|=
name|in
operator|.
name|readOptionalUTF
argument_list|()
expr_stmt|;
name|dev
operator|=
name|in
operator|.
name|readOptionalUTF
argument_list|()
expr_stmt|;
name|total
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|free
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|available
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|diskReads
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|diskWrites
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|diskReadBytes
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|diskWriteBytes
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|diskQueue
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
name|diskServiceTime
operator|=
name|in
operator|.
name|readDouble
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
name|writeUTF
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalUTF
argument_list|(
name|mount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalUTF
argument_list|(
name|dev
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|total
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|free
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|available
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|diskReads
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|diskWrites
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|diskReadBytes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|diskWriteBytes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|diskQueue
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|diskServiceTime
argument_list|)
expr_stmt|;
block|}
DECL|method|total
specifier|public
name|ByteSizeValue
name|total
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|total
argument_list|)
return|;
block|}
DECL|method|getTotal
specifier|public
name|ByteSizeValue
name|getTotal
parameter_list|()
block|{
return|return
name|total
argument_list|()
return|;
block|}
DECL|method|free
specifier|public
name|ByteSizeValue
name|free
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|free
argument_list|)
return|;
block|}
DECL|method|getFree
specifier|public
name|ByteSizeValue
name|getFree
parameter_list|()
block|{
return|return
name|free
argument_list|()
return|;
block|}
DECL|method|available
specifier|public
name|ByteSizeValue
name|available
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|available
argument_list|)
return|;
block|}
DECL|method|getAvailable
specifier|public
name|ByteSizeValue
name|getAvailable
parameter_list|()
block|{
return|return
name|available
argument_list|()
return|;
block|}
DECL|method|diskReads
specifier|public
name|long
name|diskReads
parameter_list|()
block|{
return|return
name|this
operator|.
name|diskReads
return|;
block|}
DECL|method|getDiskReads
specifier|public
name|long
name|getDiskReads
parameter_list|()
block|{
return|return
name|this
operator|.
name|diskReads
return|;
block|}
DECL|method|diskWrites
specifier|public
name|long
name|diskWrites
parameter_list|()
block|{
return|return
name|this
operator|.
name|diskWrites
return|;
block|}
DECL|method|getDiskWrites
specifier|public
name|long
name|getDiskWrites
parameter_list|()
block|{
return|return
name|this
operator|.
name|diskWrites
return|;
block|}
DECL|method|diskReadSizeInBytes
specifier|public
name|long
name|diskReadSizeInBytes
parameter_list|()
block|{
return|return
name|diskReadBytes
return|;
block|}
DECL|method|getDiskReadSizeInBytes
specifier|public
name|long
name|getDiskReadSizeInBytes
parameter_list|()
block|{
return|return
name|diskReadBytes
return|;
block|}
DECL|method|diskReadSizeSize
specifier|public
name|ByteSizeValue
name|diskReadSizeSize
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|diskReadBytes
argument_list|)
return|;
block|}
DECL|method|getDiskReadSizeSize
specifier|public
name|ByteSizeValue
name|getDiskReadSizeSize
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|diskReadBytes
argument_list|)
return|;
block|}
DECL|method|diskWriteSizeInBytes
specifier|public
name|long
name|diskWriteSizeInBytes
parameter_list|()
block|{
return|return
name|diskWriteBytes
return|;
block|}
DECL|method|getDiskWriteSizeInBytes
specifier|public
name|long
name|getDiskWriteSizeInBytes
parameter_list|()
block|{
return|return
name|diskWriteBytes
return|;
block|}
DECL|method|diskWriteSizeSize
specifier|public
name|ByteSizeValue
name|diskWriteSizeSize
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|diskWriteBytes
argument_list|)
return|;
block|}
DECL|method|getDiskWriteSizeSize
specifier|public
name|ByteSizeValue
name|getDiskWriteSizeSize
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|diskWriteBytes
argument_list|)
return|;
block|}
DECL|method|diskQueue
specifier|public
name|double
name|diskQueue
parameter_list|()
block|{
return|return
name|diskQueue
return|;
block|}
DECL|method|getDiskQueue
specifier|public
name|double
name|getDiskQueue
parameter_list|()
block|{
return|return
name|diskQueue
return|;
block|}
DECL|method|diskServiceTime
specifier|public
name|double
name|diskServiceTime
parameter_list|()
block|{
return|return
name|diskServiceTime
return|;
block|}
DECL|method|getDiskServiceTime
specifier|public
name|double
name|getDiskServiceTime
parameter_list|()
block|{
return|return
name|diskServiceTime
return|;
block|}
block|}
DECL|field|timestamp
name|long
name|timestamp
decl_stmt|;
DECL|field|infos
name|Info
index|[]
name|infos
decl_stmt|;
DECL|method|FsStats
name|FsStats
parameter_list|()
block|{      }
DECL|method|FsStats
name|FsStats
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|Info
index|[]
name|infos
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|infos
operator|=
name|infos
expr_stmt|;
block|}
DECL|method|timestamp
specifier|public
name|long
name|timestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
DECL|method|getTimestamp
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Info
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|forArray
argument_list|(
name|infos
argument_list|)
return|;
block|}
DECL|method|readFsStats
specifier|public
specifier|static
name|FsStats
name|readFsStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|FsStats
name|stats
init|=
operator|new
name|FsStats
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
name|timestamp
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|infos
operator|=
operator|new
name|Info
index|[
name|in
operator|.
name|readVInt
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
name|infos
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|infos
index|[
name|i
index|]
operator|=
operator|new
name|Info
argument_list|()
expr_stmt|;
name|infos
index|[
name|i
index|]
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|timestamp
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|infos
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Info
name|info
range|:
name|infos
control|)
block|{
name|info
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
literal|"fs"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"timestamp"
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"data"
argument_list|)
expr_stmt|;
for|for
control|(
name|Info
name|info
range|:
name|infos
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"path"
argument_list|,
name|info
operator|.
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|.
name|mount
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"mount"
argument_list|,
name|info
operator|.
name|mount
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|dev
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"dev"
argument_list|,
name|info
operator|.
name|dev
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|total
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"total"
argument_list|,
name|info
operator|.
name|total
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
literal|"total_in_bytes"
argument_list|,
name|info
operator|.
name|total
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|free
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"free"
argument_list|,
name|info
operator|.
name|free
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
literal|"free_in_bytes"
argument_list|,
name|info
operator|.
name|free
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|available
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"available"
argument_list|,
name|info
operator|.
name|available
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
literal|"available_in_bytes"
argument_list|,
name|info
operator|.
name|available
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|diskReads
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"disk_reads"
argument_list|,
name|info
operator|.
name|diskReads
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|diskWrites
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"disk_writes"
argument_list|,
name|info
operator|.
name|diskWrites
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|diskReadBytes
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"disk_read_size"
argument_list|,
name|info
operator|.
name|diskReadSizeSize
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
literal|"disk_read_size_bytes"
argument_list|,
name|info
operator|.
name|diskReadSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|diskWriteBytes
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"disk_write_size"
argument_list|,
name|info
operator|.
name|diskWriteSizeSize
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
literal|"disk_write_size_bytes"
argument_list|,
name|info
operator|.
name|diskWriteSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|diskQueue
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"disk_queue"
argument_list|,
name|Strings
operator|.
name|format1Decimals
argument_list|(
name|info
operator|.
name|diskQueue
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|diskServiceTime
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"disk_service_time"
argument_list|,
name|Strings
operator|.
name|format1Decimals
argument_list|(
name|info
operator|.
name|diskServiceTime
argument_list|,
literal|""
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

