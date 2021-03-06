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
name|Writeable
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|ProcessStats
specifier|public
class|class
name|ProcessStats
implements|implements
name|Writeable
implements|,
name|ToXContent
block|{
DECL|field|timestamp
specifier|private
specifier|final
name|long
name|timestamp
decl_stmt|;
DECL|field|openFileDescriptors
specifier|private
specifier|final
name|long
name|openFileDescriptors
decl_stmt|;
DECL|field|maxFileDescriptors
specifier|private
specifier|final
name|long
name|maxFileDescriptors
decl_stmt|;
DECL|field|cpu
specifier|private
specifier|final
name|Cpu
name|cpu
decl_stmt|;
DECL|field|mem
specifier|private
specifier|final
name|Mem
name|mem
decl_stmt|;
DECL|method|ProcessStats
specifier|public
name|ProcessStats
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|long
name|openFileDescriptors
parameter_list|,
name|long
name|maxFileDescriptors
parameter_list|,
name|Cpu
name|cpu
parameter_list|,
name|Mem
name|mem
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
name|openFileDescriptors
operator|=
name|openFileDescriptors
expr_stmt|;
name|this
operator|.
name|maxFileDescriptors
operator|=
name|maxFileDescriptors
expr_stmt|;
name|this
operator|.
name|cpu
operator|=
name|cpu
expr_stmt|;
name|this
operator|.
name|mem
operator|=
name|mem
expr_stmt|;
block|}
DECL|method|ProcessStats
specifier|public
name|ProcessStats
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
name|maxFileDescriptors
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|cpu
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|Cpu
operator|::
operator|new
argument_list|)
expr_stmt|;
name|mem
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|Mem
operator|::
operator|new
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
name|out
operator|.
name|writeLong
argument_list|(
name|maxFileDescriptors
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|cpu
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|mem
argument_list|)
expr_stmt|;
block|}
DECL|method|getTimestamp
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
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
DECL|method|getMaxFileDescriptors
specifier|public
name|long
name|getMaxFileDescriptors
parameter_list|()
block|{
return|return
name|maxFileDescriptors
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
name|String
name|PROCESS
init|=
literal|"process"
decl_stmt|;
DECL|field|TIMESTAMP
specifier|static
specifier|final
name|String
name|TIMESTAMP
init|=
literal|"timestamp"
decl_stmt|;
DECL|field|OPEN_FILE_DESCRIPTORS
specifier|static
specifier|final
name|String
name|OPEN_FILE_DESCRIPTORS
init|=
literal|"open_file_descriptors"
decl_stmt|;
DECL|field|MAX_FILE_DESCRIPTORS
specifier|static
specifier|final
name|String
name|MAX_FILE_DESCRIPTORS
init|=
literal|"max_file_descriptors"
decl_stmt|;
DECL|field|CPU
specifier|static
specifier|final
name|String
name|CPU
init|=
literal|"cpu"
decl_stmt|;
DECL|field|PERCENT
specifier|static
specifier|final
name|String
name|PERCENT
init|=
literal|"percent"
decl_stmt|;
DECL|field|TOTAL
specifier|static
specifier|final
name|String
name|TOTAL
init|=
literal|"total"
decl_stmt|;
DECL|field|TOTAL_IN_MILLIS
specifier|static
specifier|final
name|String
name|TOTAL_IN_MILLIS
init|=
literal|"total_in_millis"
decl_stmt|;
DECL|field|MEM
specifier|static
specifier|final
name|String
name|MEM
init|=
literal|"mem"
decl_stmt|;
DECL|field|TOTAL_VIRTUAL
specifier|static
specifier|final
name|String
name|TOTAL_VIRTUAL
init|=
literal|"total_virtual"
decl_stmt|;
DECL|field|TOTAL_VIRTUAL_IN_BYTES
specifier|static
specifier|final
name|String
name|TOTAL_VIRTUAL_IN_BYTES
init|=
literal|"total_virtual_in_bytes"
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
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MAX_FILE_DESCRIPTORS
argument_list|,
name|maxFileDescriptors
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
DECL|class|Mem
specifier|public
specifier|static
class|class
name|Mem
implements|implements
name|Writeable
block|{
DECL|field|totalVirtual
specifier|private
specifier|final
name|long
name|totalVirtual
decl_stmt|;
DECL|method|Mem
specifier|public
name|Mem
parameter_list|(
name|long
name|totalVirtual
parameter_list|)
block|{
name|this
operator|.
name|totalVirtual
operator|=
name|totalVirtual
expr_stmt|;
block|}
DECL|method|Mem
specifier|public
name|Mem
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
block|}
DECL|method|getTotalVirtual
specifier|public
name|ByteSizeValue
name|getTotalVirtual
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
block|}
DECL|class|Cpu
specifier|public
specifier|static
class|class
name|Cpu
implements|implements
name|Writeable
block|{
DECL|field|percent
specifier|private
specifier|final
name|short
name|percent
decl_stmt|;
DECL|field|total
specifier|private
specifier|final
name|long
name|total
decl_stmt|;
DECL|method|Cpu
specifier|public
name|Cpu
parameter_list|(
name|short
name|percent
parameter_list|,
name|long
name|total
parameter_list|)
block|{
name|this
operator|.
name|percent
operator|=
name|percent
expr_stmt|;
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
block|}
DECL|method|Cpu
specifier|public
name|Cpu
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
name|total
argument_list|)
expr_stmt|;
block|}
comment|/**          * Get the Process cpu usage.          *<p>          * Supported Platforms: All.          */
DECL|method|getPercent
specifier|public
name|short
name|getPercent
parameter_list|()
block|{
return|return
name|percent
return|;
block|}
comment|/**          * Get the Process cpu time (sum of User and Sys).          *<p>          * Supported Platforms: All.          */
DECL|method|getTotal
specifier|public
name|TimeValue
name|getTotal
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
block|}
block|}
end_class

end_unit

