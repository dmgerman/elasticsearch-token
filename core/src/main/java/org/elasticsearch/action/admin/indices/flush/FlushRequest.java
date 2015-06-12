begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.flush
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|flush
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|BroadcastRequest
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * A flush request to flush one or more indices. The flush process of an index basically frees memory from the index  * by flushing data to the index storage and clearing the internal transaction log. By default, Elasticsearch uses  * memory heuristics in order to automatically trigger flush operations as required in order to clear memory.  *<p/>  *<p>Best created with {@link org.elasticsearch.client.Requests#flushRequest(String...)}.  *  * @see org.elasticsearch.client.Requests#flushRequest(String...)  * @see org.elasticsearch.client.IndicesAdminClient#flush(FlushRequest)  * @see FlushResponse  */
end_comment

begin_class
DECL|class|FlushRequest
specifier|public
class|class
name|FlushRequest
extends|extends
name|BroadcastRequest
argument_list|<
name|FlushRequest
argument_list|>
block|{
DECL|field|force
specifier|private
name|boolean
name|force
init|=
literal|false
decl_stmt|;
DECL|field|waitIfOngoing
specifier|private
name|boolean
name|waitIfOngoing
init|=
literal|false
decl_stmt|;
DECL|method|FlushRequest
name|FlushRequest
parameter_list|()
block|{     }
comment|/**      * Copy constructor that creates a new flush request that is a copy of the one provided as an argument.      * The new request will inherit though headers and context from the original request that caused it.      */
DECL|method|FlushRequest
specifier|public
name|FlushRequest
parameter_list|(
name|ActionRequest
name|originalRequest
parameter_list|)
block|{
name|super
argument_list|(
name|originalRequest
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs a new flush request against one or more indices. If nothing is provided, all indices will      * be flushed.      */
DECL|method|FlushRequest
specifier|public
name|FlushRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|super
argument_list|(
name|indices
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns<tt>true</tt> iff a flush should block      * if a another flush operation is already running. Otherwise<tt>false</tt>      */
DECL|method|waitIfOngoing
specifier|public
name|boolean
name|waitIfOngoing
parameter_list|()
block|{
return|return
name|this
operator|.
name|waitIfOngoing
return|;
block|}
comment|/**      * if set to<tt>true</tt> the flush will block      * if a another flush operation is already running until the flush can be performed.      */
DECL|method|waitIfOngoing
specifier|public
name|FlushRequest
name|waitIfOngoing
parameter_list|(
name|boolean
name|waitIfOngoing
parameter_list|)
block|{
name|this
operator|.
name|waitIfOngoing
operator|=
name|waitIfOngoing
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Force flushing, even if one is possibly not needed.      */
DECL|method|force
specifier|public
name|boolean
name|force
parameter_list|()
block|{
return|return
name|force
return|;
block|}
comment|/**      * Force flushing, even if one is possibly not needed.      */
DECL|method|force
specifier|public
name|FlushRequest
name|force
parameter_list|(
name|boolean
name|force
parameter_list|)
block|{
name|this
operator|.
name|force
operator|=
name|force
expr_stmt|;
return|return
name|this
return|;
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|force
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|waitIfOngoing
argument_list|)
expr_stmt|;
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|force
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|waitIfOngoing
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"FlushRequest{"
operator|+
literal|"waitIfOngoing="
operator|+
name|waitIfOngoing
operator|+
literal|", force="
operator|+
name|force
operator|+
literal|"}"
return|;
block|}
block|}
end_class

end_unit
