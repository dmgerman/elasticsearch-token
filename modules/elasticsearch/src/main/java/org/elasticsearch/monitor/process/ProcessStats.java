begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|SizeValue
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
name|builder
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
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
DECL|field|fd
name|Fd
name|fd
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
DECL|method|fd
specifier|public
name|Fd
name|fd
parameter_list|()
block|{
return|return
name|fd
return|;
block|}
DECL|method|getFd
specifier|public
name|Fd
name|getFd
parameter_list|()
block|{
return|return
name|fd
argument_list|()
return|;
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
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
literal|"process"
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
literal|"cpu"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"percent"
argument_list|,
name|cpu
operator|.
name|percent
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"sys"
argument_list|,
name|cpu
operator|.
name|sys
argument_list|()
operator|.
name|format
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"sys_in_millis"
argument_list|,
name|cpu
operator|.
name|sys
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"user"
argument_list|,
name|cpu
operator|.
name|user
argument_list|()
operator|.
name|format
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"user_in_millis"
argument_list|,
name|cpu
operator|.
name|user
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"total"
argument_list|,
name|cpu
operator|.
name|total
argument_list|()
operator|.
name|format
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"total_in_millis"
argument_list|,
name|cpu
operator|.
name|total
argument_list|()
operator|.
name|millis
argument_list|()
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
literal|"mem"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"resident"
argument_list|,
name|mem
operator|.
name|resident
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
literal|"resident_in_bytes"
argument_list|,
name|mem
operator|.
name|resident
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"share"
argument_list|,
name|mem
operator|.
name|share
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
literal|"share_in_bytes"
argument_list|,
name|mem
operator|.
name|share
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"total_virtual"
argument_list|,
name|mem
operator|.
name|totalVirtual
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
literal|"total_virtual_in_bytes"
argument_list|,
name|mem
operator|.
name|totalVirtual
argument_list|()
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
block|}
if|if
condition|(
name|fd
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"fd"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"total"
argument_list|,
name|fd
operator|.
name|total
argument_list|()
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
name|timestamp
operator|=
name|in
operator|.
name|readVLong
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
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|fd
operator|=
name|Fd
operator|.
name|readFd
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|out
operator|.
name|writeVLong
argument_list|(
name|timestamp
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
if|if
condition|(
name|fd
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
name|fd
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Fd
specifier|public
specifier|static
class|class
name|Fd
implements|implements
name|Streamable
implements|,
name|Serializable
block|{
DECL|field|total
name|long
name|total
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|Fd
name|Fd
parameter_list|()
block|{         }
DECL|method|readFd
specifier|public
specifier|static
name|Fd
name|readFd
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Fd
name|fd
init|=
operator|new
name|Fd
argument_list|()
decl_stmt|;
name|fd
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|fd
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
name|total
operator|=
name|in
operator|.
name|readLong
argument_list|()
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
name|out
operator|.
name|writeLong
argument_list|(
name|total
argument_list|)
expr_stmt|;
block|}
comment|/**          * Get the Total number of open file descriptors.          *          *<p>Supported Platforms: AIX, HPUX, Linux, Solaris, Win32.          */
DECL|method|total
specifier|public
name|long
name|total
parameter_list|()
block|{
return|return
name|total
return|;
block|}
DECL|method|getTotal
specifier|public
name|long
name|getTotal
parameter_list|()
block|{
return|return
name|total
argument_list|()
return|;
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
name|SizeValue
name|totalVirtual
parameter_list|()
block|{
return|return
operator|new
name|SizeValue
argument_list|(
name|totalVirtual
argument_list|)
return|;
block|}
DECL|method|getTotalVirtual
specifier|public
name|SizeValue
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
name|SizeValue
name|resident
parameter_list|()
block|{
return|return
operator|new
name|SizeValue
argument_list|(
name|resident
argument_list|)
return|;
block|}
DECL|method|getResident
specifier|public
name|SizeValue
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
name|SizeValue
name|share
parameter_list|()
block|{
return|return
operator|new
name|SizeValue
argument_list|(
name|share
argument_list|)
return|;
block|}
DECL|method|getShare
specifier|public
name|SizeValue
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
comment|/**          * Get the Process cpu usage.          *          *<p>Supported Platforms: All.          */
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
comment|/**          * Get the Process cpu usage.          *          *<p>Supported Platforms: All.          */
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
comment|/**          * Get the Process cpu kernel time.          *          *<p>Supported Platforms: All.          */
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
comment|/**          * Get the Process cpu kernel time.          *          *<p>Supported Platforms: All.          */
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
comment|/**          * Get the Process cpu user time.          *          *<p>Supported Platforms: All.          */
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
comment|/**          * Get the Process cpu time (sum of User and Sys).          *          * Supported Platforms: All.          */
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
comment|/**          * Get the Process cpu time (sum of User and Sys).          *          * Supported Platforms: All.          */
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
comment|/**          * Get the Process cpu user time.          *          *<p>Supported Platforms: All.          */
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

