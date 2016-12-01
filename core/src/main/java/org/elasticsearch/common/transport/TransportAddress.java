begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|transport
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|network
operator|.
name|NetworkAddress
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
name|InetAddress
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

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_comment
comment|/**  * A transport address used for IP socket address (wraps {@link java.net.InetSocketAddress}).  */
end_comment

begin_class
DECL|class|TransportAddress
specifier|public
specifier|final
class|class
name|TransportAddress
implements|implements
name|Writeable
block|{
comment|/**      * A<a href="https://en.wikipedia.org/wiki/0.0.0.0">non-routeable v4 meta transport address</a> that can be used for      * testing or in scenarios where targets should be marked as non-applicable from a transport perspective.      */
DECL|field|META_ADDRESS
specifier|public
specifier|static
specifier|final
name|InetAddress
name|META_ADDRESS
decl_stmt|;
static|static
block|{
try|try
block|{
name|META_ADDRESS
operator|=
name|InetAddress
operator|.
name|getByName
argument_list|(
literal|"0.0.0.0"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
DECL|field|address
specifier|private
specifier|final
name|InetSocketAddress
name|address
decl_stmt|;
DECL|method|TransportAddress
specifier|public
name|TransportAddress
parameter_list|(
name|InetAddress
name|address
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
name|address
argument_list|,
name|port
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|TransportAddress
specifier|public
name|TransportAddress
parameter_list|(
name|InetSocketAddress
name|address
parameter_list|)
block|{
if|if
condition|(
name|address
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"InetSocketAddress must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|address
operator|.
name|getAddress
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Address must be resolved but wasn't - InetSocketAddress#getAddress() returned null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|TransportAddress
specifier|public
name|TransportAddress
parameter_list|(
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
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_6_0_0_alpha1_UNRELEASED
argument_list|)
condition|)
block|{
comment|// bwc layer for 5.x where we had more than one transport address
specifier|final
name|short
name|i
init|=
name|in
operator|.
name|readShort
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|!=
literal|1
condition|)
block|{
comment|// we fail hard to ensure nobody tries to use some custom transport address impl even if that is difficult to add
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"illegal transport ID from node of version: "
operator|+
name|in
operator|.
name|getVersion
argument_list|()
operator|+
literal|" got: "
operator|+
name|i
operator|+
literal|" expected: 1"
argument_list|)
throw|;
block|}
block|}
specifier|final
name|int
name|len
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|a
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
comment|// 4 bytes (IPv4) or 16 bytes (IPv6)
name|in
operator|.
name|readFully
argument_list|(
name|a
argument_list|)
expr_stmt|;
specifier|final
name|InetAddress
name|inetAddress
decl_stmt|;
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
name|V_5_0_3_UNRELEASED
argument_list|)
condition|)
block|{
name|String
name|host
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|inetAddress
operator|=
name|InetAddress
operator|.
name|getByAddress
argument_list|(
name|host
argument_list|,
name|a
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inetAddress
operator|=
name|InetAddress
operator|.
name|getByAddress
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
name|int
name|port
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|address
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|inetAddress
argument_list|,
name|port
argument_list|)
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
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_6_0_0_alpha1_UNRELEASED
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeShort
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|)
expr_stmt|;
comment|// this maps to InetSocketTransportAddress in 5.x
block|}
name|byte
index|[]
name|bytes
init|=
name|address
operator|.
name|getAddress
argument_list|()
operator|.
name|getAddress
argument_list|()
decl_stmt|;
comment|// 4 bytes (IPv4) or 16 bytes (IPv6)
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// 1 byte
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
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
name|V_5_0_3_UNRELEASED
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|address
operator|.
name|getHostString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// don't serialize scope ids over the network!!!!
comment|// these only make sense with respect to the local machine, and will only formulate
comment|// the address incorrectly remotely.
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
comment|/**      * Returns a string representation of the enclosed {@link InetSocketAddress}      * @see NetworkAddress#format(InetAddress)      */
DECL|method|getAddress
specifier|public
name|String
name|getAddress
parameter_list|()
block|{
return|return
name|NetworkAddress
operator|.
name|format
argument_list|(
name|address
operator|.
name|getAddress
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Returns the addresses port      */
DECL|method|getPort
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|address
operator|.
name|getPort
argument_list|()
return|;
block|}
comment|/**      * Returns the enclosed {@link InetSocketAddress}      */
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
name|TransportAddress
name|address1
init|=
operator|(
name|TransportAddress
operator|)
name|o
decl_stmt|;
return|return
name|address
operator|.
name|equals
argument_list|(
name|address1
operator|.
name|address
argument_list|)
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
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|NetworkAddress
operator|.
name|format
argument_list|(
name|address
argument_list|)
return|;
block|}
block|}
end_class

end_unit

