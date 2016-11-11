begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|StoreStats
specifier|public
class|class
name|StoreStats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|sizeInBytes
specifier|private
name|long
name|sizeInBytes
decl_stmt|;
DECL|field|throttleTimeInNanos
specifier|private
name|long
name|throttleTimeInNanos
decl_stmt|;
DECL|method|StoreStats
specifier|public
name|StoreStats
parameter_list|()
block|{      }
DECL|method|StoreStats
specifier|public
name|StoreStats
parameter_list|(
name|long
name|sizeInBytes
parameter_list|,
name|long
name|throttleTimeInNanos
parameter_list|)
block|{
name|this
operator|.
name|sizeInBytes
operator|=
name|sizeInBytes
expr_stmt|;
name|this
operator|.
name|throttleTimeInNanos
operator|=
name|throttleTimeInNanos
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|StoreStats
name|stats
parameter_list|)
block|{
if|if
condition|(
name|stats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|sizeInBytes
operator|+=
name|stats
operator|.
name|sizeInBytes
expr_stmt|;
name|throttleTimeInNanos
operator|+=
name|stats
operator|.
name|throttleTimeInNanos
expr_stmt|;
block|}
DECL|method|sizeInBytes
specifier|public
name|long
name|sizeInBytes
parameter_list|()
block|{
return|return
name|sizeInBytes
return|;
block|}
DECL|method|getSizeInBytes
specifier|public
name|long
name|getSizeInBytes
parameter_list|()
block|{
return|return
name|sizeInBytes
return|;
block|}
DECL|method|size
specifier|public
name|ByteSizeValue
name|size
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|sizeInBytes
argument_list|)
return|;
block|}
DECL|method|getSize
specifier|public
name|ByteSizeValue
name|getSize
parameter_list|()
block|{
return|return
name|size
argument_list|()
return|;
block|}
DECL|method|throttleTime
specifier|public
name|TimeValue
name|throttleTime
parameter_list|()
block|{
return|return
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|throttleTimeInNanos
argument_list|)
return|;
block|}
DECL|method|getThrottleTime
specifier|public
name|TimeValue
name|getThrottleTime
parameter_list|()
block|{
return|return
name|throttleTime
argument_list|()
return|;
block|}
DECL|method|readStoreStats
specifier|public
specifier|static
name|StoreStats
name|readStoreStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|StoreStats
name|store
init|=
operator|new
name|StoreStats
argument_list|()
decl_stmt|;
name|store
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|store
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
name|sizeInBytes
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|throttleTimeInNanos
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
name|sizeInBytes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|throttleTimeInNanos
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
name|Fields
operator|.
name|STORE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|byteSizeField
argument_list|(
name|Fields
operator|.
name|SIZE_IN_BYTES
argument_list|,
name|Fields
operator|.
name|SIZE
argument_list|,
name|sizeInBytes
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|THROTTLE_TIME_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|THROTTLE_TIME
argument_list|,
name|throttleTime
argument_list|()
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
DECL|field|STORE
specifier|static
specifier|final
name|String
name|STORE
init|=
literal|"store"
decl_stmt|;
DECL|field|SIZE
specifier|static
specifier|final
name|String
name|SIZE
init|=
literal|"size"
decl_stmt|;
DECL|field|SIZE_IN_BYTES
specifier|static
specifier|final
name|String
name|SIZE_IN_BYTES
init|=
literal|"size_in_bytes"
decl_stmt|;
DECL|field|THROTTLE_TIME
specifier|static
specifier|final
name|String
name|THROTTLE_TIME
init|=
literal|"throttle_time"
decl_stmt|;
DECL|field|THROTTLE_TIME_IN_MILLIS
specifier|static
specifier|final
name|String
name|THROTTLE_TIME_IN_MILLIS
init|=
literal|"throttle_time_in_millis"
decl_stmt|;
block|}
block|}
end_class

end_unit

