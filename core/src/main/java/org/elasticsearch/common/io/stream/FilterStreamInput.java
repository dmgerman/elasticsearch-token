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
name|Version
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
comment|/**  * Wraps a {@link StreamInput} and delegates to it. To be used to add functionality to an existing stream by subclassing.  */
end_comment

begin_class
DECL|class|FilterStreamInput
specifier|public
specifier|abstract
class|class
name|FilterStreamInput
extends|extends
name|StreamInput
block|{
DECL|field|delegate
specifier|protected
specifier|final
name|StreamInput
name|delegate
decl_stmt|;
DECL|method|FilterStreamInput
specifier|protected
name|FilterStreamInput
parameter_list|(
name|StreamInput
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readByte
specifier|public
name|byte
name|readByte
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|readByte
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readBytes
specifier|public
name|void
name|readBytes
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|readBytes
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|read
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|available
specifier|public
name|int
name|available
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|available
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getVersion
specifier|public
name|Version
name|getVersion
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getVersion
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|setVersion
specifier|public
name|void
name|setVersion
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|delegate
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ensureCanReadBytes
specifier|protected
name|void
name|ensureCanReadBytes
parameter_list|(
name|int
name|length
parameter_list|)
throws|throws
name|EOFException
block|{
name|delegate
operator|.
name|ensureCanReadBytes
argument_list|(
name|length
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

