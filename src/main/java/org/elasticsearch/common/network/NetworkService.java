begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.network
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|network
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|settings
operator|.
name|Settings
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
name|transport
operator|.
name|InetSocketTransportAddress
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
name|unit
operator|.
name|ByteSizeValue
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
name|unit
operator|.
name|TimeValue
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
name|NetworkInterface
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NetworkService
specifier|public
class|class
name|NetworkService
extends|extends
name|AbstractComponent
block|{
DECL|field|LOCAL
specifier|public
specifier|static
specifier|final
name|String
name|LOCAL
init|=
literal|"#local#"
decl_stmt|;
DECL|field|GLOBAL_NETWORK_HOST_SETTING
specifier|private
specifier|static
specifier|final
name|String
name|GLOBAL_NETWORK_HOST_SETTING
init|=
literal|"network.host"
decl_stmt|;
DECL|field|GLOBAL_NETWORK_BINDHOST_SETTING
specifier|private
specifier|static
specifier|final
name|String
name|GLOBAL_NETWORK_BINDHOST_SETTING
init|=
literal|"network.bind_host"
decl_stmt|;
DECL|field|GLOBAL_NETWORK_PUBLISHHOST_SETTING
specifier|private
specifier|static
specifier|final
name|String
name|GLOBAL_NETWORK_PUBLISHHOST_SETTING
init|=
literal|"network.publish_host"
decl_stmt|;
DECL|class|TcpSettings
specifier|public
specifier|static
specifier|final
class|class
name|TcpSettings
block|{
DECL|field|TCP_NO_DELAY
specifier|public
specifier|static
specifier|final
name|String
name|TCP_NO_DELAY
init|=
literal|"network.tcp.no_delay"
decl_stmt|;
DECL|field|TCP_KEEP_ALIVE
specifier|public
specifier|static
specifier|final
name|String
name|TCP_KEEP_ALIVE
init|=
literal|"network.tcp.keep_alive"
decl_stmt|;
DECL|field|TCP_REUSE_ADDRESS
specifier|public
specifier|static
specifier|final
name|String
name|TCP_REUSE_ADDRESS
init|=
literal|"network.tcp.reuse_address"
decl_stmt|;
DECL|field|TCP_SEND_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|String
name|TCP_SEND_BUFFER_SIZE
init|=
literal|"network.tcp.send_buffer_size"
decl_stmt|;
DECL|field|TCP_RECEIVE_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|String
name|TCP_RECEIVE_BUFFER_SIZE
init|=
literal|"network.tcp.receive_buffer_size"
decl_stmt|;
DECL|field|TCP_BLOCKING
specifier|public
specifier|static
specifier|final
name|String
name|TCP_BLOCKING
init|=
literal|"network.tcp.blocking"
decl_stmt|;
DECL|field|TCP_BLOCKING_SERVER
specifier|public
specifier|static
specifier|final
name|String
name|TCP_BLOCKING_SERVER
init|=
literal|"network.tcp.blocking_server"
decl_stmt|;
DECL|field|TCP_BLOCKING_CLIENT
specifier|public
specifier|static
specifier|final
name|String
name|TCP_BLOCKING_CLIENT
init|=
literal|"network.tcp.blocking_client"
decl_stmt|;
DECL|field|TCP_CONNECT_TIMEOUT
specifier|public
specifier|static
specifier|final
name|String
name|TCP_CONNECT_TIMEOUT
init|=
literal|"network.tcp.connect_timeout"
decl_stmt|;
DECL|field|TCP_DEFAULT_SEND_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|TCP_DEFAULT_SEND_BUFFER_SIZE
init|=
literal|null
decl_stmt|;
DECL|field|TCP_DEFAULT_RECEIVE_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|TCP_DEFAULT_RECEIVE_BUFFER_SIZE
init|=
literal|null
decl_stmt|;
DECL|field|TCP_DEFAULT_CONNECT_TIMEOUT
specifier|public
specifier|static
specifier|final
name|TimeValue
name|TCP_DEFAULT_CONNECT_TIMEOUT
init|=
operator|new
name|TimeValue
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
block|}
comment|/**      * A custom name resolver can support custom lookup keys (my_net_key:ipv4) and also change      * the default inet address used in case no settings is provided.      */
DECL|interface|CustomNameResolver
specifier|public
specifier|static
interface|interface
name|CustomNameResolver
block|{
comment|/**          * Resolves the default value if possible. If not, return<tt>null</tt>.          */
DECL|method|resolveDefault
name|InetAddress
name|resolveDefault
parameter_list|()
function_decl|;
comment|/**          * Resolves a custom value handling, return<tt>null</tt> if can't handle it.          */
DECL|method|resolveIfPossible
name|InetAddress
name|resolveIfPossible
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
block|}
DECL|field|customNameResolvers
specifier|private
specifier|final
name|List
argument_list|<
name|CustomNameResolver
argument_list|>
name|customNameResolvers
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|NetworkService
specifier|public
name|NetworkService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|InetSocketTransportAddress
operator|.
name|setResolveAddress
argument_list|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"network.address.serialization.resolve"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Add a custom name resolver.      */
DECL|method|addCustomNameResolver
specifier|public
name|void
name|addCustomNameResolver
parameter_list|(
name|CustomNameResolver
name|customNameResolver
parameter_list|)
block|{
name|customNameResolvers
operator|.
name|add
argument_list|(
name|customNameResolver
argument_list|)
expr_stmt|;
block|}
DECL|method|resolveBindHostAddress
specifier|public
name|InetAddress
name|resolveBindHostAddress
parameter_list|(
name|String
name|bindHost
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|resolveBindHostAddress
argument_list|(
name|bindHost
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|resolveBindHostAddress
specifier|public
name|InetAddress
name|resolveBindHostAddress
parameter_list|(
name|String
name|bindHost
parameter_list|,
name|String
name|defaultValue2
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|resolveInetAddress
argument_list|(
name|bindHost
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|GLOBAL_NETWORK_BINDHOST_SETTING
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|GLOBAL_NETWORK_HOST_SETTING
argument_list|)
argument_list|)
argument_list|,
name|defaultValue2
argument_list|)
return|;
block|}
DECL|method|resolvePublishHostAddress
specifier|public
name|InetAddress
name|resolvePublishHostAddress
parameter_list|(
name|String
name|publishHost
parameter_list|)
throws|throws
name|IOException
block|{
name|InetAddress
name|address
init|=
name|resolvePublishHostAddress
argument_list|(
name|publishHost
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// verify that its not a local address
if|if
condition|(
name|address
operator|==
literal|null
operator|||
name|address
operator|.
name|isAnyLocalAddress
argument_list|()
condition|)
block|{
name|address
operator|=
name|NetworkUtils
operator|.
name|getFirstNonLoopbackAddress
argument_list|(
name|NetworkUtils
operator|.
name|StackType
operator|.
name|IPv4
argument_list|)
expr_stmt|;
if|if
condition|(
name|address
operator|==
literal|null
condition|)
block|{
name|address
operator|=
name|NetworkUtils
operator|.
name|getFirstNonLoopbackAddress
argument_list|(
name|NetworkUtils
operator|.
name|getIpStackType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|address
operator|==
literal|null
condition|)
block|{
name|address
operator|=
name|NetworkUtils
operator|.
name|getLocalAddress
argument_list|()
expr_stmt|;
if|if
condition|(
name|address
operator|==
literal|null
condition|)
block|{
return|return
name|NetworkUtils
operator|.
name|getLocalhost
argument_list|(
name|NetworkUtils
operator|.
name|StackType
operator|.
name|IPv4
argument_list|)
return|;
block|}
block|}
block|}
block|}
return|return
name|address
return|;
block|}
DECL|method|resolvePublishHostAddress
specifier|public
name|InetAddress
name|resolvePublishHostAddress
parameter_list|(
name|String
name|publishHost
parameter_list|,
name|String
name|defaultValue2
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|resolveInetAddress
argument_list|(
name|publishHost
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|GLOBAL_NETWORK_PUBLISHHOST_SETTING
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|GLOBAL_NETWORK_HOST_SETTING
argument_list|)
argument_list|)
argument_list|,
name|defaultValue2
argument_list|)
return|;
block|}
DECL|method|resolveInetAddress
specifier|public
name|InetAddress
name|resolveInetAddress
parameter_list|(
name|String
name|host
parameter_list|,
name|String
name|defaultValue1
parameter_list|,
name|String
name|defaultValue2
parameter_list|)
throws|throws
name|UnknownHostException
throws|,
name|IOException
block|{
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
name|host
operator|=
name|defaultValue1
expr_stmt|;
block|}
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
name|host
operator|=
name|defaultValue2
expr_stmt|;
block|}
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|CustomNameResolver
name|customNameResolver
range|:
name|customNameResolvers
control|)
block|{
name|InetAddress
name|inetAddress
init|=
name|customNameResolver
operator|.
name|resolveDefault
argument_list|()
decl_stmt|;
if|if
condition|(
name|inetAddress
operator|!=
literal|null
condition|)
block|{
return|return
name|inetAddress
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
name|String
name|origHost
init|=
name|host
decl_stmt|;
if|if
condition|(
operator|(
name|host
operator|.
name|startsWith
argument_list|(
literal|"#"
argument_list|)
operator|&&
name|host
operator|.
name|endsWith
argument_list|(
literal|"#"
argument_list|)
operator|)
operator|||
operator|(
name|host
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
name|host
operator|.
name|endsWith
argument_list|(
literal|"_"
argument_list|)
operator|)
condition|)
block|{
name|host
operator|=
name|host
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|host
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|CustomNameResolver
name|customNameResolver
range|:
name|customNameResolvers
control|)
block|{
name|InetAddress
name|inetAddress
init|=
name|customNameResolver
operator|.
name|resolveIfPossible
argument_list|(
name|host
argument_list|)
decl_stmt|;
if|if
condition|(
name|inetAddress
operator|!=
literal|null
condition|)
block|{
return|return
name|inetAddress
return|;
block|}
block|}
if|if
condition|(
name|host
operator|.
name|equals
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
return|return
name|NetworkUtils
operator|.
name|getLocalAddress
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|host
operator|.
name|startsWith
argument_list|(
literal|"non_loopback"
argument_list|)
condition|)
block|{
if|if
condition|(
name|host
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|endsWith
argument_list|(
literal|":ipv4"
argument_list|)
condition|)
block|{
return|return
name|NetworkUtils
operator|.
name|getFirstNonLoopbackAddress
argument_list|(
name|NetworkUtils
operator|.
name|StackType
operator|.
name|IPv4
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|host
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|endsWith
argument_list|(
literal|":ipv6"
argument_list|)
condition|)
block|{
return|return
name|NetworkUtils
operator|.
name|getFirstNonLoopbackAddress
argument_list|(
name|NetworkUtils
operator|.
name|StackType
operator|.
name|IPv6
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|NetworkUtils
operator|.
name|getFirstNonLoopbackAddress
argument_list|(
name|NetworkUtils
operator|.
name|getIpStackType
argument_list|()
argument_list|)
return|;
block|}
block|}
else|else
block|{
name|NetworkUtils
operator|.
name|StackType
name|stackType
init|=
name|NetworkUtils
operator|.
name|getIpStackType
argument_list|()
decl_stmt|;
if|if
condition|(
name|host
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|endsWith
argument_list|(
literal|":ipv4"
argument_list|)
condition|)
block|{
name|stackType
operator|=
name|NetworkUtils
operator|.
name|StackType
operator|.
name|IPv4
expr_stmt|;
name|host
operator|=
name|host
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|host
operator|.
name|length
argument_list|()
operator|-
literal|5
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|host
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|endsWith
argument_list|(
literal|":ipv6"
argument_list|)
condition|)
block|{
name|stackType
operator|=
name|NetworkUtils
operator|.
name|StackType
operator|.
name|IPv6
expr_stmt|;
name|host
operator|=
name|host
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|host
operator|.
name|length
argument_list|()
operator|-
literal|5
argument_list|)
expr_stmt|;
block|}
name|Collection
argument_list|<
name|NetworkInterface
argument_list|>
name|allInterfs
init|=
name|NetworkUtils
operator|.
name|getAllAvailableInterfaces
argument_list|()
decl_stmt|;
for|for
control|(
name|NetworkInterface
name|ni
range|:
name|allInterfs
control|)
block|{
if|if
condition|(
operator|!
name|ni
operator|.
name|isUp
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|host
operator|.
name|equals
argument_list|(
name|ni
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|host
operator|.
name|equals
argument_list|(
name|ni
operator|.
name|getDisplayName
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|ni
operator|.
name|isLoopback
argument_list|()
condition|)
block|{
return|return
name|NetworkUtils
operator|.
name|getFirstAddress
argument_list|(
name|ni
argument_list|,
name|stackType
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|NetworkUtils
operator|.
name|getFirstNonLoopbackAddress
argument_list|(
name|ni
argument_list|,
name|stackType
argument_list|)
return|;
block|}
block|}
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to find network interface for ["
operator|+
name|origHost
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|InetAddress
operator|.
name|getByName
argument_list|(
name|host
argument_list|)
return|;
block|}
block|}
end_class

end_unit

