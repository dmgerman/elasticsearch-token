begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|Streamable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|TimeValue
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A scroll enables scrolling of search request. It holds a {@link #keepAlive()} time that  * will control how long to keep the scrolling resources open.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|Scroll
specifier|public
class|class
name|Scroll
implements|implements
name|Streamable
block|{
DECL|field|keepAlive
specifier|private
name|TimeValue
name|keepAlive
decl_stmt|;
DECL|method|Scroll
specifier|private
name|Scroll
parameter_list|()
block|{      }
comment|/**      * Constructs a new scroll of the provided keep alive.      */
DECL|method|Scroll
specifier|public
name|Scroll
parameter_list|(
name|TimeValue
name|keepAlive
parameter_list|)
block|{
name|this
operator|.
name|keepAlive
operator|=
name|keepAlive
expr_stmt|;
block|}
comment|/**      * How long the resources will be kept open to support the scroll request.      */
DECL|method|keepAlive
specifier|public
name|TimeValue
name|keepAlive
parameter_list|()
block|{
return|return
name|keepAlive
return|;
block|}
DECL|method|readScroll
specifier|public
specifier|static
name|Scroll
name|readScroll
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|Scroll
name|scroll
init|=
operator|new
name|Scroll
argument_list|()
decl_stmt|;
name|scroll
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|scroll
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|keepAlive
operator|=
name|readTimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|keepAlive
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|keepAlive
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

