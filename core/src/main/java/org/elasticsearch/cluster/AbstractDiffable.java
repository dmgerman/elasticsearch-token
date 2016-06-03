begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
comment|/**  * Abstract diffable object with simple diffs implementation that sends the entire object if object has changed or  * nothing is object remained the same.  */
end_comment

begin_class
DECL|class|AbstractDiffable
specifier|public
specifier|abstract
class|class
name|AbstractDiffable
parameter_list|<
name|T
extends|extends
name|Diffable
parameter_list|<
name|T
parameter_list|>
parameter_list|>
implements|implements
name|Diffable
argument_list|<
name|T
argument_list|>
block|{
annotation|@
name|Override
DECL|method|diff
specifier|public
name|Diff
argument_list|<
name|T
argument_list|>
name|diff
parameter_list|(
name|T
name|previousState
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|get
argument_list|()
operator|.
name|equals
argument_list|(
name|previousState
argument_list|)
condition|)
block|{
return|return
operator|new
name|CompleteDiff
argument_list|<>
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|CompleteDiff
argument_list|<>
argument_list|(
name|get
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|readDiffFrom
specifier|public
name|Diff
argument_list|<
name|T
argument_list|>
name|readDiffFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CompleteDiff
argument_list|<>
argument_list|(
name|this
argument_list|,
name|in
argument_list|)
return|;
block|}
DECL|method|readDiffFrom
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Diffable
argument_list|<
name|T
argument_list|>
parameter_list|>
name|Diff
argument_list|<
name|T
argument_list|>
name|readDiffFrom
parameter_list|(
name|T
name|reader
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CompleteDiff
argument_list|<
name|T
argument_list|>
argument_list|(
name|reader
argument_list|,
name|in
argument_list|)
return|;
block|}
DECL|class|CompleteDiff
specifier|private
specifier|static
class|class
name|CompleteDiff
parameter_list|<
name|T
extends|extends
name|Diffable
parameter_list|<
name|T
parameter_list|>
parameter_list|>
implements|implements
name|Diff
argument_list|<
name|T
argument_list|>
block|{
annotation|@
name|Nullable
DECL|field|part
specifier|private
specifier|final
name|T
name|part
decl_stmt|;
comment|/**          * Creates simple diff with changes          */
DECL|method|CompleteDiff
specifier|public
name|CompleteDiff
parameter_list|(
name|T
name|part
parameter_list|)
block|{
name|this
operator|.
name|part
operator|=
name|part
expr_stmt|;
block|}
comment|/**          * Creates simple diff without changes          */
DECL|method|CompleteDiff
specifier|public
name|CompleteDiff
parameter_list|()
block|{
name|this
operator|.
name|part
operator|=
literal|null
expr_stmt|;
block|}
comment|/**          * Read simple diff from the stream          */
DECL|method|CompleteDiff
specifier|public
name|CompleteDiff
parameter_list|(
name|Diffable
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|this
operator|.
name|part
operator|=
name|reader
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|part
operator|=
literal|null
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
if|if
condition|(
name|part
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|part
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
name|T
name|apply
parameter_list|(
name|T
name|part
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|part
operator|!=
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|part
return|;
block|}
else|else
block|{
return|return
name|part
return|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|get
specifier|public
name|T
name|get
parameter_list|()
block|{
return|return
operator|(
name|T
operator|)
name|this
return|;
block|}
block|}
end_class

end_unit

