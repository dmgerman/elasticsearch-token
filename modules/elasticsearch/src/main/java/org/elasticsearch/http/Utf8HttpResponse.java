begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
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
name|UnicodeUtil
import|;
end_import

begin_comment
comment|/**  * An http response that is built on top of {@link org.apache.lucene.util.UnicodeUtil.UTF8Result}.  *<p/>  *<p>Note, this class assumes that the utf8 result is not thread safe.  *  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|Utf8HttpResponse
specifier|public
class|class
name|Utf8HttpResponse
extends|extends
name|AbstractHttpResponse
implements|implements
name|HttpResponse
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|UnicodeUtil
operator|.
name|UTF8Result
name|EMPTY
decl_stmt|;
static|static
block|{
name|UnicodeUtil
operator|.
name|UTF8Result
name|temp
init|=
operator|new
name|UnicodeUtil
operator|.
name|UTF8Result
argument_list|()
decl_stmt|;
name|temp
operator|.
name|result
operator|=
operator|new
name|byte
index|[
literal|0
index|]
expr_stmt|;
name|temp
operator|.
name|length
operator|=
literal|0
expr_stmt|;
name|EMPTY
operator|=
name|temp
expr_stmt|;
block|}
DECL|field|status
specifier|private
specifier|final
name|Status
name|status
decl_stmt|;
DECL|field|utf8Result
specifier|private
specifier|final
name|UnicodeUtil
operator|.
name|UTF8Result
name|utf8Result
decl_stmt|;
DECL|field|prefixUtf8Result
specifier|private
specifier|final
name|UnicodeUtil
operator|.
name|UTF8Result
name|prefixUtf8Result
decl_stmt|;
DECL|field|suffixUtf8Result
specifier|private
specifier|final
name|UnicodeUtil
operator|.
name|UTF8Result
name|suffixUtf8Result
decl_stmt|;
DECL|method|Utf8HttpResponse
specifier|public
name|Utf8HttpResponse
parameter_list|(
name|Status
name|status
parameter_list|)
block|{
name|this
argument_list|(
name|status
argument_list|,
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|Utf8HttpResponse
specifier|public
name|Utf8HttpResponse
parameter_list|(
name|Status
name|status
parameter_list|,
name|UnicodeUtil
operator|.
name|UTF8Result
name|utf8Result
parameter_list|)
block|{
name|this
argument_list|(
name|status
argument_list|,
name|utf8Result
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|Utf8HttpResponse
specifier|public
name|Utf8HttpResponse
parameter_list|(
name|Status
name|status
parameter_list|,
name|UnicodeUtil
operator|.
name|UTF8Result
name|utf8Result
parameter_list|,
name|UnicodeUtil
operator|.
name|UTF8Result
name|prefixUtf8Result
parameter_list|,
name|UnicodeUtil
operator|.
name|UTF8Result
name|suffixUtf8Result
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|utf8Result
operator|=
name|utf8Result
expr_stmt|;
name|this
operator|.
name|prefixUtf8Result
operator|=
name|prefixUtf8Result
expr_stmt|;
name|this
operator|.
name|suffixUtf8Result
operator|=
name|suffixUtf8Result
expr_stmt|;
block|}
DECL|method|contentThreadSafe
annotation|@
name|Override
specifier|public
name|boolean
name|contentThreadSafe
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|contentType
annotation|@
name|Override
specifier|public
name|String
name|contentType
parameter_list|()
block|{
return|return
literal|"text/plain; charset=UTF-8"
return|;
block|}
DECL|method|content
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|content
parameter_list|()
block|{
return|return
name|utf8Result
operator|.
name|result
return|;
block|}
DECL|method|contentLength
annotation|@
name|Override
specifier|public
name|int
name|contentLength
parameter_list|()
block|{
return|return
name|utf8Result
operator|.
name|length
return|;
block|}
DECL|method|status
annotation|@
name|Override
specifier|public
name|Status
name|status
parameter_list|()
block|{
return|return
name|status
return|;
block|}
DECL|method|prefixContent
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|prefixContent
parameter_list|()
block|{
return|return
name|prefixUtf8Result
operator|!=
literal|null
condition|?
name|prefixUtf8Result
operator|.
name|result
else|:
literal|null
return|;
block|}
DECL|method|prefixContentLength
annotation|@
name|Override
specifier|public
name|int
name|prefixContentLength
parameter_list|()
block|{
return|return
name|prefixUtf8Result
operator|!=
literal|null
condition|?
name|prefixUtf8Result
operator|.
name|length
else|:
operator|-
literal|1
return|;
block|}
DECL|method|suffixContent
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|suffixContent
parameter_list|()
block|{
return|return
name|suffixUtf8Result
operator|!=
literal|null
condition|?
name|suffixUtf8Result
operator|.
name|result
else|:
literal|null
return|;
block|}
DECL|method|suffixContentLength
annotation|@
name|Override
specifier|public
name|int
name|suffixContentLength
parameter_list|()
block|{
return|return
name|suffixUtf8Result
operator|!=
literal|null
condition|?
name|suffixUtf8Result
operator|.
name|length
else|:
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

