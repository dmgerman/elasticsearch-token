begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.completion
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
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
name|text
operator|.
name|Text
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
name|search
operator|.
name|suggest
operator|.
name|Suggest
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
comment|/**  *  */
end_comment

begin_class
DECL|class|CompletionSuggestion
specifier|public
class|class
name|CompletionSuggestion
extends|extends
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|CompletionSuggestion
operator|.
name|Entry
argument_list|>
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|int
name|TYPE
init|=
literal|2
decl_stmt|;
DECL|method|CompletionSuggestion
specifier|public
name|CompletionSuggestion
parameter_list|()
block|{     }
DECL|method|CompletionSuggestion
specifier|public
name|CompletionSuggestion
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|int
name|getType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|newEntry
specifier|protected
name|Entry
name|newEntry
parameter_list|()
block|{
return|return
operator|new
name|Entry
argument_list|()
return|;
block|}
DECL|class|Entry
specifier|public
specifier|static
class|class
name|Entry
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
block|{
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|Text
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|Entry
specifier|protected
name|Entry
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newOption
specifier|protected
name|Option
name|newOption
parameter_list|()
block|{
return|return
operator|new
name|Option
argument_list|()
return|;
block|}
DECL|class|Option
specifier|public
specifier|static
class|class
name|Option
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
block|{
DECL|field|payload
specifier|private
name|BytesReference
name|payload
decl_stmt|;
DECL|method|Option
specifier|public
name|Option
parameter_list|(
name|Text
name|text
parameter_list|,
name|float
name|score
parameter_list|,
name|BytesReference
name|payload
parameter_list|)
block|{
name|super
argument_list|(
name|text
argument_list|,
name|score
argument_list|)
expr_stmt|;
name|this
operator|.
name|payload
operator|=
name|payload
expr_stmt|;
block|}
DECL|method|Option
specifier|protected
name|Option
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|setPayload
specifier|public
name|void
name|setPayload
parameter_list|(
name|BytesReference
name|payload
parameter_list|)
block|{
name|this
operator|.
name|payload
operator|=
name|payload
expr_stmt|;
block|}
DECL|method|getPayload
specifier|public
name|BytesReference
name|getPayload
parameter_list|()
block|{
return|return
name|payload
return|;
block|}
DECL|method|setScore
specifier|public
name|void
name|setScore
parameter_list|(
name|float
name|score
parameter_list|)
block|{
name|super
operator|.
name|setScore
argument_list|(
name|score
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|protected
name|XContentBuilder
name|innerToXContent
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
name|super
operator|.
name|innerToXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|payload
operator|!=
literal|null
operator|&&
name|payload
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|rawField
argument_list|(
literal|"payload"
argument_list|,
name|payload
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
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
name|payload
operator|=
name|in
operator|.
name|readBytesReference
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|payload
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

