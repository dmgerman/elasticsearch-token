begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ingest
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
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionRequestValidationException
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
name|master
operator|.
name|AcknowledgedRequest
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
name|bytes
operator|.
name|BytesReference
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|Objects
import|;
end_import

begin_class
DECL|class|PutPipelineRequest
specifier|public
class|class
name|PutPipelineRequest
extends|extends
name|AcknowledgedRequest
argument_list|<
name|PutPipelineRequest
argument_list|>
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
DECL|field|xContentType
specifier|private
name|XContentType
name|xContentType
decl_stmt|;
comment|/**      * Create a new pipeline request      * @deprecated use {@link #PutPipelineRequest(String, BytesReference, XContentType)} to avoid content type auto-detection      */
annotation|@
name|Deprecated
DECL|method|PutPipelineRequest
specifier|public
name|PutPipelineRequest
parameter_list|(
name|String
name|id
parameter_list|,
name|BytesReference
name|source
parameter_list|)
block|{
name|this
argument_list|(
name|id
argument_list|,
name|source
argument_list|,
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a new pipeline request with the id and source along with the content type of the source      */
DECL|method|PutPipelineRequest
specifier|public
name|PutPipelineRequest
parameter_list|(
name|String
name|id
parameter_list|,
name|BytesReference
name|source
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|this
operator|.
name|xContentType
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|xContentType
argument_list|)
expr_stmt|;
block|}
DECL|method|PutPipelineRequest
name|PutPipelineRequest
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|getSource
specifier|public
name|BytesReference
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|getXContentType
specifier|public
name|XContentType
name|getXContentType
parameter_list|()
block|{
return|return
name|xContentType
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|source
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_3_0
argument_list|)
condition|)
block|{
name|xContentType
operator|=
name|XContentType
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|xContentType
operator|=
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|source
argument_list|)
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|source
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_3_0
argument_list|)
condition|)
block|{
name|xContentType
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

