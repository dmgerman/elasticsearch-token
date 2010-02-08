begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.memory
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|memory
package|;
end_package

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|MemoryFile
specifier|public
class|class
name|MemoryFile
block|{
DECL|field|dir
specifier|private
specifier|final
name|MemoryDirectory
name|dir
decl_stmt|;
DECL|field|lastModified
specifier|private
specifier|volatile
name|long
name|lastModified
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
DECL|field|length
specifier|private
specifier|volatile
name|long
name|length
decl_stmt|;
DECL|field|buffers
specifier|private
specifier|volatile
name|byte
index|[]
index|[]
name|buffers
decl_stmt|;
DECL|method|MemoryFile
specifier|public
name|MemoryFile
parameter_list|(
name|MemoryDirectory
name|dir
parameter_list|)
block|{
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
block|}
DECL|method|lastModified
name|long
name|lastModified
parameter_list|()
block|{
return|return
name|lastModified
return|;
block|}
DECL|method|lastModified
name|void
name|lastModified
parameter_list|(
name|long
name|lastModified
parameter_list|)
block|{
name|this
operator|.
name|lastModified
operator|=
name|lastModified
expr_stmt|;
block|}
DECL|method|length
name|long
name|length
parameter_list|()
block|{
return|return
name|length
return|;
block|}
DECL|method|length
name|void
name|length
parameter_list|(
name|long
name|length
parameter_list|)
block|{
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
DECL|method|buffer
name|byte
index|[]
name|buffer
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|this
operator|.
name|buffers
index|[
name|i
index|]
return|;
block|}
DECL|method|numberOfBuffers
name|int
name|numberOfBuffers
parameter_list|()
block|{
return|return
name|this
operator|.
name|buffers
operator|.
name|length
return|;
block|}
DECL|method|buffers
name|void
name|buffers
parameter_list|(
name|byte
index|[]
index|[]
name|buffers
parameter_list|)
block|{
name|this
operator|.
name|buffers
operator|=
name|buffers
expr_stmt|;
block|}
DECL|method|clean
name|void
name|clean
parameter_list|()
block|{
if|if
condition|(
name|buffers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|byte
index|[]
name|buffer
range|:
name|buffers
control|)
block|{
name|dir
operator|.
name|releaseBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
name|buffers
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

