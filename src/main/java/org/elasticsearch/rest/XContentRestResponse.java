begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|UnicodeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|XContentRestResponse
specifier|public
class|class
name|XContentRestResponse
extends|extends
name|AbstractRestResponse
block|{
DECL|field|END_JSONP
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|END_JSONP
decl_stmt|;
static|static
block|{
name|BytesRef
name|U_END_JSONP
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|UnicodeUtil
operator|.
name|UTF16toUTF8
argument_list|(
literal|");"
argument_list|,
literal|0
argument_list|,
literal|");"
operator|.
name|length
argument_list|()
argument_list|,
name|U_END_JSONP
argument_list|)
expr_stmt|;
name|END_JSONP
operator|=
operator|new
name|byte
index|[
name|U_END_JSONP
operator|.
name|length
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|U_END_JSONP
operator|.
name|bytes
argument_list|,
name|U_END_JSONP
operator|.
name|offset
argument_list|,
name|END_JSONP
argument_list|,
literal|0
argument_list|,
name|U_END_JSONP
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|field|prefixUtf8Result
specifier|private
specifier|final
name|BytesRef
name|prefixUtf8Result
decl_stmt|;
DECL|field|status
specifier|private
specifier|final
name|RestStatus
name|status
decl_stmt|;
DECL|field|builder
specifier|private
specifier|final
name|XContentBuilder
name|builder
decl_stmt|;
DECL|method|XContentRestResponse
specifier|public
name|XContentRestResponse
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestStatus
name|status
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|request
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"request must be set"
argument_list|)
throw|;
block|}
name|this
operator|.
name|builder
operator|=
name|builder
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|prefixUtf8Result
operator|=
name|startJsonp
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
DECL|method|builder
specifier|public
name|XContentBuilder
name|builder
parameter_list|()
block|{
return|return
name|this
operator|.
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|public
name|String
name|contentType
parameter_list|()
block|{
return|return
name|builder
operator|.
name|contentType
argument_list|()
operator|.
name|restContentType
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|contentThreadSafe
specifier|public
name|boolean
name|contentThreadSafe
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|content
specifier|public
name|byte
index|[]
name|content
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|builder
operator|.
name|bytes
argument_list|()
operator|.
name|array
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|contentLength
specifier|public
name|int
name|contentLength
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|builder
operator|.
name|bytes
argument_list|()
operator|.
name|length
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|contentOffset
specifier|public
name|int
name|contentOffset
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|this
operator|.
name|status
return|;
block|}
annotation|@
name|Override
DECL|method|prefixContent
specifier|public
name|byte
index|[]
name|prefixContent
parameter_list|()
block|{
if|if
condition|(
name|prefixUtf8Result
operator|!=
literal|null
condition|)
block|{
return|return
name|prefixUtf8Result
operator|.
name|bytes
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|prefixContentLength
specifier|public
name|int
name|prefixContentLength
parameter_list|()
block|{
if|if
condition|(
name|prefixUtf8Result
operator|!=
literal|null
condition|)
block|{
return|return
name|prefixUtf8Result
operator|.
name|length
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|prefixContentOffset
specifier|public
name|int
name|prefixContentOffset
parameter_list|()
block|{
if|if
condition|(
name|prefixUtf8Result
operator|!=
literal|null
condition|)
block|{
return|return
name|prefixUtf8Result
operator|.
name|offset
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|suffixContent
specifier|public
name|byte
index|[]
name|suffixContent
parameter_list|()
block|{
if|if
condition|(
name|prefixUtf8Result
operator|!=
literal|null
condition|)
block|{
return|return
name|END_JSONP
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|suffixContentLength
specifier|public
name|int
name|suffixContentLength
parameter_list|()
block|{
if|if
condition|(
name|prefixUtf8Result
operator|!=
literal|null
condition|)
block|{
return|return
name|END_JSONP
operator|.
name|length
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|suffixContentOffset
specifier|public
name|int
name|suffixContentOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
DECL|method|startJsonp
specifier|private
specifier|static
name|BytesRef
name|startJsonp
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
name|String
name|callback
init|=
name|request
operator|.
name|param
argument_list|(
literal|"callback"
argument_list|)
decl_stmt|;
if|if
condition|(
name|callback
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|BytesRef
name|result
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|UnicodeUtil
operator|.
name|UTF16toUTF8
argument_list|(
name|callback
argument_list|,
literal|0
argument_list|,
name|callback
operator|.
name|length
argument_list|()
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|result
operator|.
name|bytes
index|[
name|result
operator|.
name|length
index|]
operator|=
literal|'('
expr_stmt|;
name|result
operator|.
name|length
operator|++
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

