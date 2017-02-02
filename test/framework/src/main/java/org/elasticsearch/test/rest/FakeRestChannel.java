begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
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
name|BytesStreamOutput
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
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|AbstractRestChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
DECL|class|FakeRestChannel
specifier|public
specifier|final
class|class
name|FakeRestChannel
extends|extends
name|AbstractRestChannel
block|{
DECL|field|latch
specifier|private
specifier|final
name|CountDownLatch
name|latch
decl_stmt|;
DECL|field|responses
specifier|private
specifier|final
name|AtomicInteger
name|responses
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|errors
specifier|private
specifier|final
name|AtomicInteger
name|errors
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|method|FakeRestChannel
specifier|public
name|FakeRestChannel
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|boolean
name|detailedErrorsEnabled
parameter_list|,
name|int
name|responseCount
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|,
name|detailedErrorsEnabled
argument_list|)
expr_stmt|;
name|this
operator|.
name|latch
operator|=
operator|new
name|CountDownLatch
argument_list|(
name|responseCount
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newBuilder
specifier|public
name|XContentBuilder
name|newBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|newBuilder
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newErrorBuilder
specifier|public
name|XContentBuilder
name|newErrorBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|newErrorBuilder
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newBuilder
specifier|public
name|XContentBuilder
name|newBuilder
parameter_list|(
annotation|@
name|Nullable
name|XContentType
name|requestContentType
parameter_list|,
name|boolean
name|useFiltering
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|newBuilder
argument_list|(
name|requestContentType
argument_list|,
name|useFiltering
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newBytesOutput
specifier|protected
name|BytesStreamOutput
name|newBytesOutput
parameter_list|()
block|{
return|return
name|super
operator|.
name|newBytesOutput
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|request
specifier|public
name|RestRequest
name|request
parameter_list|()
block|{
return|return
name|super
operator|.
name|request
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|sendResponse
specifier|public
name|void
name|sendResponse
parameter_list|(
name|RestResponse
name|response
parameter_list|)
block|{
if|if
condition|(
name|response
operator|.
name|status
argument_list|()
operator|==
name|RestStatus
operator|.
name|OK
condition|)
block|{
name|responses
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|errors
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
DECL|method|await
specifier|public
name|boolean
name|await
parameter_list|()
throws|throws
name|InterruptedException
block|{
return|return
name|latch
operator|.
name|await
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
DECL|method|responses
specifier|public
name|AtomicInteger
name|responses
parameter_list|()
block|{
return|return
name|responses
return|;
block|}
DECL|method|errors
specifier|public
name|AtomicInteger
name|errors
parameter_list|()
block|{
return|return
name|errors
return|;
block|}
block|}
end_class

end_unit

