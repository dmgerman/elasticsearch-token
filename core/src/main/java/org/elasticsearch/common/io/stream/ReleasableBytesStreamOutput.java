begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io.stream
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
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
name|bytes
operator|.
name|ReleasablePagedBytesReference
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
name|ReleasableBytesStream
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
name|util
operator|.
name|BigArrays
import|;
end_import

begin_comment
comment|/**  * An bytes stream output that allows providing a {@link BigArrays} instance  * expecting it to require releasing its content ({@link #bytes()}) once done.  *<p>  * Please note, its is the responsibility of the caller to make sure the bytes  * reference do not "escape" and are released only once.  */
end_comment

begin_class
DECL|class|ReleasableBytesStreamOutput
specifier|public
class|class
name|ReleasableBytesStreamOutput
extends|extends
name|BytesStreamOutput
implements|implements
name|ReleasableBytesStream
block|{
DECL|method|ReleasableBytesStreamOutput
specifier|public
name|ReleasableBytesStreamOutput
parameter_list|(
name|BigArrays
name|bigarrays
parameter_list|)
block|{
name|super
argument_list|(
name|BigArrays
operator|.
name|PAGE_SIZE_IN_BYTES
argument_list|,
name|bigarrays
argument_list|)
expr_stmt|;
block|}
DECL|method|ReleasableBytesStreamOutput
specifier|public
name|ReleasableBytesStreamOutput
parameter_list|(
name|int
name|expectedSize
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|)
block|{
name|super
argument_list|(
name|expectedSize
argument_list|,
name|bigArrays
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|bytes
specifier|public
name|ReleasablePagedBytesReference
name|bytes
parameter_list|()
block|{
return|return
operator|new
name|ReleasablePagedBytesReference
argument_list|(
name|bigArrays
argument_list|,
name|bytes
argument_list|,
name|count
argument_list|)
return|;
block|}
block|}
end_class

end_unit

