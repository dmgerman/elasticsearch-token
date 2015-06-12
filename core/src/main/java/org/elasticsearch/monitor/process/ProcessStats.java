begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.process
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|process
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
comment|/**  *  */
end_comment

begin_class
DECL|class|ProcessStats
specifier|public
class|class
name|ProcessStats
implements|implements
name|Streamable
implements|,
name|Serializable
implements|,
name|ToXContent
block|{
DECL|field|timestamp
name|long
name|timestamp
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|openFileDescriptors
name|long
name|openFileDescriptors
decl_stmt|;
DECL|field|cpu
name|Cpu
name|cpu
init|=
literal|null
decl_stmt|;
DECL|field|mem
name|Mem
name|mem
init|=
literal|null
decl_stmt|;
DECL|method|ProcessStats
name|ProcessStats
parameter_list|()
block|{     }
DECL|method|timestamp
specifier|public
name|long
name|timestamp
parameter_list|()
block|{
return|return
name|this
operator|.
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
DECL|method|openFileDescriptors
specifier|public
name|long
name|openFileDescriptors
parameter_list|()
block|{
return|return
name|this
operator|.
name|openFileDescriptors
return|;
block|}
DECL|method|getOpenFileDescriptors
specifier|public
name|long
name|getOpenFileDescriptors
parameter_list|()
block|{
return|return
name|openFileDescriptors
return|;
block|}
DECL|method|cpu
specifier|public
name|Cpu
name|cpu
parameter_list|()
block|{
return|return
name|cpu
return|;
block|}
DECL|method|getCpu
specifier|public
name|Cpu
name|getCpu
parameter_list|()
block|{
return|return
name|cpu
argument_list|()
return|;
block|}
DECL|method|mem
specifier|public
name|Mem
name|mem
parameter_list|()
block|{
return|return
name|mem
return|;
block|}
DECL|method|getMem
specifier|public
name|Mem
name|getMem
parameter_list|()
block|{
return|return
name|mem
argument_list|()
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|PROCESS
specifier|static
specifier|final
name|XContentBuilderString
name|PROCESS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"process"
argument_list|)
decl_stmt|;
DECL|field|TIMESTAMP
specifier|static
specifier|final
name|XContentBuilderString
name|TIMESTAMP
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"timestamp"
argument_list|)
decl_stmt|;
DECL|field|OPEN_FILE_DESCRIPTORS
specifier|static
specifier|final
name|XContentBuilderString
name|OPEN_FILE_DESCRIPTORS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"open_file_descriptors"
argument_list|)
decl_stmt|;
DECL|field|CPU
specifier|static
specifier|final
name|XContentBuilderString
name|CPU
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"cpu"
argument_list|)
decl_stmt|;
DECL|field|PERCENT
specifier|static
specifier|final
name|XContentBuilderString
name|PERCENT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"percent"
argument_list|)
decl_stmt|;
DECL|field|SYS
specifier|static
specifier|final
name|XContentBuilderString
name|SYS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"sys"
argument_list|)
decl_stmt|;
DECL|field|SYS_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|SYS_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"sys_in_millis"
argument_list|)
decl_stmt|;
DECL|field|USER
specifier|static
specifier|final
name|XContentBuilderString
name|USER
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"user"
argument_list|)
decl_stmt|;
DECL|field|USER_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|USER_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"user_in_millis"
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
DECL|field|TOTAL_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_in_millis"
argument_list|)
decl_stmt|;
DECL|field|MEM
specifier|static
specifier|final
name|XContentBuilderString
name|MEM
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"mem"
argument_list|)
decl_stmt|;
DECL|field|RESIDENT
specifier|static
specifier|final
name|XContentBuilderString
name|RESIDENT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"resident"
argument_list|)
decl_stmt|;
DECL|field|RESIDENT_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|RESIDENT_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"resident_in_bytes"
argument_list|)
decl_stmt|;
DECL|field|SHARE
specifier|static
specifier|final
name|XContentBuilderString
name|SHARE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"share"
argument_list|)
decl_stmt|;
DECL|field|SHARE_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|SHARE_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"share_in_bytes"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_VIRTUAL
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_VIRTUAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_virtual"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_VIRTUAL_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_VIRTUAL_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_virtual_in_bytes"
argument_list|)
decl_stmt|;
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
name|PROCESS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TIMESTAMP
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|OPEN_FILE_DESCRIPTORS
argument_list|,
name|openFileDescriptors
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpu
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|CPU
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|PERCENT
argument_list|,
name|cpu
operator|.
name|percent
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|SYS_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|SYS
argument_list|,
name|cpu
operator|.
name|sys
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|USER_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|USER
argument_list|,
name|cpu
operator|.
name|user
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|TOTAL_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|TOTAL
argument_list|,
name|cpu
operator|.
name|total
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|mem
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|MEM
argument_list|)
expr_stmt|;
name|builder
operator|.
name|byteSizeField
argument_list|(
name|Fields
operator|.
name|RESIDENT_IN_BYTES
argument_list|,
name|Fields
operator|.
name|RESIDENT
argument_list|,
name|mem
operator|.
name|resident
argument_list|)
expr_stmt|;
name|builder
operator|.
name|byteSizeField
argument_list|(
name|Fields
operator|.
name|SHARE_IN_BYTES
argument_list|,
name|Fields
operator|.
name|SHARE
argument_list|,
name|mem
operator|.
name|share
argument_list|)
expr_stmt|;
name|builder
operator|.
name|byteSizeField
argument_list|(
name|Fields
operator|.
name|TOTAL_VIRTUAL_IN_BYTES
argument_list|,
name|Fields
operator|.
name|TOTAL_VIRTUAL
argument_list|,
name|mem
operator|.
name|totalVirtual
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|readProcessStats
specifier|public
specifier|static
name|ProcessStats
name|readProcessStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ProcessStats
name|stats
init|=
operator|new
name|ProcessStats
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
name|openFileDescriptors
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|cpu
operator|=
name|Cpu
operator|.
name|readCpu
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|mem
operator|=
name|Mem
operator|.
name|readMem
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
name|writeLong
argument_list|(
name|openFileDescriptors
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpu
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cpu
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mem
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|mem
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Mem
specifier|public
specifier|static
class|class
name|Mem
implements|implements
name|Streamable
implements|,
name|Serializable
block|{
DECL|field|totalVirtual
name|long
name|totalVirtual
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|resident
name|long
name|resident
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|share
name|long
name|share
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|Mem
name|Mem
parameter_list|()
block|{         }
DECL|method|readMem
specifier|public
specifier|static
name|Mem
name|readMem
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Mem
name|mem
init|=
operator|new
name|Mem
argument_list|()
decl_stmt|;
name|mem
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|mem
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
name|totalVirtual
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|resident
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|share
operator|=
name|in
operator|.
name|readLong
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
name|writeLong
argument_list|(
name|totalVirtual
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|resident
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|share
argument_list|)
expr_stmt|;
block|}
DECL|method|totalVirtual
specifier|public
name|ByteSizeValue
name|totalVirtual
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|totalVirtual
argument_list|)
return|;
block|}
DECL|method|getTotalVirtual
specifier|public
name|ByteSizeValue
name|getTotalVirtual
parameter_list|()
block|{
return|return
name|totalVirtual
argument_list|()
return|;
block|}
DECL|method|resident
specifier|public
name|ByteSizeValue
name|resident
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|resident
argument_list|)
return|;
block|}
DECL|method|getResident
specifier|public
name|ByteSizeValue
name|getResident
parameter_list|()
block|{
return|return
name|resident
argument_list|()
return|;
block|}
DECL|method|share
specifier|public
name|ByteSizeValue
name|share
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|share
argument_list|)
return|;
block|}
DECL|method|getShare
specifier|public
name|ByteSizeValue
name|getShare
parameter_list|()
block|{
return|return
name|share
argument_list|()
return|;
block|}
block|}
DECL|class|Cpu
specifier|public
specifier|static
class|class
name|Cpu
implements|implements
name|Streamable
implements|,
name|Serializable
block|{
DECL|field|percent
name|short
name|percent
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|sys
name|long
name|sys
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|user
name|long
name|user
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|total
name|long
name|total
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|Cpu
name|Cpu
parameter_list|()
block|{          }
DECL|method|readCpu
specifier|public
specifier|static
name|Cpu
name|readCpu
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Cpu
name|cpu
init|=
operator|new
name|Cpu
argument_list|()
decl_stmt|;
name|cpu
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|cpu
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
name|percent
operator|=
name|in
operator|.
name|readShort
argument_list|()
expr_stmt|;
name|sys
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|user
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|total
operator|=
name|in
operator|.
name|readLong
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
name|writeShort
argument_list|(
name|percent
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|sys
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|total
argument_list|)
expr_stmt|;
block|}
comment|/**          * Get the Process cpu usage.          *<p/>          *<p>Supported Platforms: All.          */
DECL|method|percent
specifier|public
name|short
name|percent
parameter_list|()
block|{
return|return
name|percent
return|;
block|}
comment|/**          * Get the Process cpu usage.          *<p/>          *<p>Supported Platforms: All.          */
DECL|method|getPercent
specifier|public
name|short
name|getPercent
parameter_list|()
block|{
return|return
name|percent
argument_list|()
return|;
block|}
comment|/**          * Get the Process cpu kernel time.          *<p/>          *<p>Supported Platforms: All.          */
DECL|method|sys
specifier|public
name|TimeValue
name|sys
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|sys
argument_list|)
return|;
block|}
comment|/**          * Get the Process cpu kernel time.          *<p/>          *<p>Supported Platforms: All.          */
DECL|method|getSys
specifier|public
name|TimeValue
name|getSys
parameter_list|()
block|{
return|return
name|sys
argument_list|()
return|;
block|}
comment|/**          * Get the Process cpu user time.          *<p/>          *<p>Supported Platforms: All.          */
DECL|method|user
specifier|public
name|TimeValue
name|user
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|user
argument_list|)
return|;
block|}
comment|/**          * Get the Process cpu time (sum of User and Sys).          *<p/>          * Supported Platforms: All.          */
DECL|method|total
specifier|public
name|TimeValue
name|total
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|total
argument_list|)
return|;
block|}
comment|/**          * Get the Process cpu time (sum of User and Sys).          *<p/>          * Supported Platforms: All.          */
DECL|method|getTotal
specifier|public
name|TimeValue
name|getTotal
parameter_list|()
block|{
return|return
name|total
argument_list|()
return|;
block|}
comment|/**          * Get the Process cpu user time.          *<p/>          *<p>Supported Platforms: All.          */
DECL|method|getUser
specifier|public
name|TimeValue
name|getUser
parameter_list|()
block|{
return|return
name|user
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit
