begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.shards
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|shards
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
import|;
end_import

begin_comment
comment|/**  * Controls the operation threading model for shards operation that are performed  * locally on the executing node.  *  * @author kimchy (Shay Banon)  */
end_comment

begin_enum
DECL|enum|ShardsOperationThreading
specifier|public
enum|enum
name|ShardsOperationThreading
block|{
comment|/**      * No threads are used, all the local shards operations will be performed on the calling      * thread.      */
DECL|enum constant|NO_THREADS
name|NO_THREADS
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
comment|/**      * The local shards operations will be performed in serial manner on a single forked thread.      */
DECL|enum constant|SINGLE_THREAD
name|SINGLE_THREAD
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|,
comment|/**      * Each local shard operation will execute on its own thread.      */
DECL|enum constant|THREAD_PER_SHARD
name|THREAD_PER_SHARD
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|method|ShardsOperationThreading
name|ShardsOperationThreading
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|fromId
specifier|public
specifier|static
name|ShardsOperationThreading
name|fromId
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
if|if
condition|(
name|id
operator|==
literal|0
condition|)
block|{
return|return
name|NO_THREADS
return|;
block|}
if|if
condition|(
name|id
operator|==
literal|1
condition|)
block|{
return|return
name|SINGLE_THREAD
return|;
block|}
if|if
condition|(
name|id
operator|==
literal|2
condition|)
block|{
return|return
name|THREAD_PER_SHARD
return|;
block|}
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"No type matching id ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|fromString
specifier|public
specifier|static
name|ShardsOperationThreading
name|fromString
parameter_list|(
name|String
name|value
parameter_list|,
name|ShardsOperationThreading
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|ShardsOperationThreading
operator|.
name|valueOf
argument_list|(
name|value
operator|.
name|toUpperCase
argument_list|()
argument_list|)
return|;
block|}
block|}
end_enum

end_unit

