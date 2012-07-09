begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.text
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|text
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
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

begin_comment
comment|/**  * A {@link BytesReference} representation of the text, will always convert on the fly to a {@link String}.  */
end_comment

begin_class
DECL|class|BytesText
specifier|public
class|class
name|BytesText
implements|implements
name|Text
block|{
DECL|field|bytes
specifier|private
name|BytesReference
name|bytes
decl_stmt|;
DECL|method|BytesText
specifier|public
name|BytesText
parameter_list|(
name|BytesReference
name|bytes
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasBytes
specifier|public
name|boolean
name|hasBytes
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|bytes
specifier|public
name|BytesReference
name|bytes
parameter_list|()
block|{
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
DECL|method|hasString
specifier|public
name|boolean
name|hasString
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|string
specifier|public
name|String
name|string
parameter_list|()
block|{
comment|// TODO: we can optimize the conversion based on the bytes reference API similar to UnicodeUtil
if|if
condition|(
operator|!
name|bytes
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|bytes
operator|=
name|bytes
operator|.
name|toBytesArray
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|String
argument_list|(
name|bytes
operator|.
name|array
argument_list|()
argument_list|,
name|bytes
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|bytes
operator|.
name|length
argument_list|()
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
return|;
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
name|string
argument_list|()
return|;
block|}
block|}
end_class

end_unit

