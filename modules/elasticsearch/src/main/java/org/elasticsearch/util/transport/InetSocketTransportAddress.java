begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|transport
package|;
end_package

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
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|InetSocketTransportAddress
specifier|public
class|class
name|InetSocketTransportAddress
implements|implements
name|TransportAddress
block|{
DECL|field|address
specifier|private
name|InetSocketAddress
name|address
decl_stmt|;
DECL|method|InetSocketTransportAddress
name|InetSocketTransportAddress
parameter_list|()
block|{      }
DECL|method|InetSocketTransportAddress
specifier|public
name|InetSocketTransportAddress
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|InetSocketTransportAddress
specifier|public
name|InetSocketTransportAddress
parameter_list|(
name|InetSocketAddress
name|address
parameter_list|)
block|{
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
block|}
DECL|method|readInetSocketTransportAddress
specifier|public
specifier|static
name|InetSocketTransportAddress
name|readInetSocketTransportAddress
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|InetSocketTransportAddress
name|address
init|=
operator|new
name|InetSocketTransportAddress
argument_list|()
decl_stmt|;
name|address
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|address
return|;
block|}
DECL|method|uniqueAddressTypeId
annotation|@
name|Override
specifier|public
name|short
name|uniqueAddressTypeId
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
DECL|method|address
specifier|public
name|InetSocketAddress
name|address
parameter_list|()
block|{
return|return
name|this
operator|.
name|address
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
name|address
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|,
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
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
name|out
operator|.
name|writeUTF
argument_list|(
name|address
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|address
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|InetSocketTransportAddress
name|address1
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|address
operator|!=
literal|null
condition|?
operator|!
name|address
operator|.
name|equals
argument_list|(
name|address1
operator|.
name|address
argument_list|)
else|:
name|address1
operator|.
name|address
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|address
operator|!=
literal|null
condition|?
name|address
operator|.
name|hashCode
argument_list|()
else|:
literal|0
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"inet["
operator|+
name|address
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

