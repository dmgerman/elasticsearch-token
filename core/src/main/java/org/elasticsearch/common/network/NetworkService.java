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
name|Strings
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
name|settings
operator|.
name|Setting
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
name|Setting
operator|.
name|Property
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
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|DiscoveryPlugin
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
comment|/** By default, we bind to loopback interfaces */
DECL|field|DEFAULT_NETWORK_HOST
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_NETWORK_HOST
init|=
literal|"_local_"
decl_stmt|;
DECL|field|GLOBAL_NETWORK_HOST_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|GLOBAL_NETWORK_HOST_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"network.host"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|DEFAULT_NETWORK_HOST
argument_list|)
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|GLOBAL_NETWORK_BINDHOST_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|GLOBAL_NETWORK_BINDHOST_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"network.bind_host"
argument_list|,
name|GLOBAL_NETWORK_HOST_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|GLOBAL_NETWORK_PUBLISHHOST_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|GLOBAL_NETWORK_PUBLISHHOST_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"network.publish_host"
argument_list|,
name|GLOBAL_NETWORK_HOST_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|NETWORK_SERVER
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|NETWORK_SERVER
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"network.server"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
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
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|TCP_NO_DELAY
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"network.tcp.no_delay"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_KEEP_ALIVE
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|TCP_KEEP_ALIVE
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"network.tcp.keep_alive"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_REUSE_ADDRESS
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|TCP_REUSE_ADDRESS
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"network.tcp.reuse_address"
argument_list|,
name|NetworkUtils
operator|.
name|defaultReuseAddress
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_SEND_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|TCP_SEND_BUFFER_SIZE
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"network.tcp.send_buffer_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_RECEIVE_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|TCP_RECEIVE_BUFFER_SIZE
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"network.tcp.receive_buffer_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_BLOCKING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|TCP_BLOCKING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"network.tcp.blocking"
argument_list|,
literal|false
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_BLOCKING_SERVER
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|TCP_BLOCKING_SERVER
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"network.tcp.blocking_server"
argument_list|,
name|TCP_BLOCKING
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_BLOCKING_CLIENT
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|TCP_BLOCKING_CLIENT
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"network.tcp.blocking_client"
argument_list|,
name|TCP_BLOCKING
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|TCP_CONNECT_TIMEOUT
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|TCP_CONNECT_TIMEOUT
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
literal|"network.tcp.connect_timeout"
argument_list|,
operator|new
name|TimeValue
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
block|}
comment|/**      * A custom name resolver can support custom lookup keys (my_net_key:ipv4) and also change      * the default inet address used in case no settings is provided.      */
DECL|interface|CustomNameResolver
specifier|public
interface|interface
name|CustomNameResolver
block|{
comment|/**          * Resolves the default value if possible. If not, return<tt>null</tt>.          */
DECL|method|resolveDefault
name|InetAddress
index|[]
name|resolveDefault
parameter_list|()
function_decl|;
comment|/**          * Resolves a custom value handling, return<tt>null</tt> if can't handle it.          */
DECL|method|resolveIfPossible
name|InetAddress
index|[]
name|resolveIfPossible
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|IOException
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
decl_stmt|;
DECL|method|NetworkService
specifier|public
name|NetworkService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|List
argument_list|<
name|CustomNameResolver
argument_list|>
name|customNameResolvers
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|IfConfig
operator|.
name|logIfNecessary
argument_list|()
expr_stmt|;
name|this
operator|.
name|customNameResolvers
operator|=
name|customNameResolvers
expr_stmt|;
block|}
comment|/**      * Resolves {@code bindHosts} to a list of internet addresses. The list will      * not contain duplicate addresses.      *      * @param bindHosts list of hosts to bind to. this may contain special pseudo-hostnames      *                  such as _local_ (see the documentation). if it is null, it will be populated      *                  based on global default settings.      * @return unique set of internet addresses      */
DECL|method|resolveBindHostAddresses
specifier|public
name|InetAddress
index|[]
name|resolveBindHostAddresses
parameter_list|(
name|String
name|bindHosts
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
comment|// first check settings
if|if
condition|(
name|bindHosts
operator|==
literal|null
operator|||
name|bindHosts
operator|.
name|length
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|GLOBAL_NETWORK_BINDHOST_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|||
name|GLOBAL_NETWORK_HOST_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
comment|// if we have settings use them (we have a fallback to GLOBAL_NETWORK_HOST_SETTING inline
name|bindHosts
operator|=
name|GLOBAL_NETWORK_BINDHOST_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|toArray
argument_list|(
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// next check any registered custom resolvers if any
if|if
condition|(
name|customNameResolvers
operator|!=
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
name|addresses
index|[]
init|=
name|customNameResolver
operator|.
name|resolveDefault
argument_list|()
decl_stmt|;
if|if
condition|(
name|addresses
operator|!=
literal|null
condition|)
block|{
return|return
name|addresses
return|;
block|}
block|}
block|}
comment|// we know it's not here. get the defaults
name|bindHosts
operator|=
name|GLOBAL_NETWORK_BINDHOST_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|toArray
argument_list|(
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
block|}
block|}
name|InetAddress
name|addresses
index|[]
init|=
name|resolveInetAddresses
argument_list|(
name|bindHosts
argument_list|)
decl_stmt|;
comment|// try to deal with some (mis)configuration
for|for
control|(
name|InetAddress
name|address
range|:
name|addresses
control|)
block|{
comment|// check if its multicast: flat out mistake
if|if
condition|(
name|address
operator|.
name|isMulticastAddress
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"bind address: {"
operator|+
name|NetworkAddress
operator|.
name|format
argument_list|(
name|address
argument_list|)
operator|+
literal|"} is invalid: multicast address"
argument_list|)
throw|;
block|}
comment|// check if its a wildcard address: this is only ok if its the only address!
if|if
condition|(
name|address
operator|.
name|isAnyLocalAddress
argument_list|()
operator|&&
name|addresses
operator|.
name|length
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"bind address: {"
operator|+
name|NetworkAddress
operator|.
name|format
argument_list|(
name|address
argument_list|)
operator|+
literal|"} is wildcard, but multiple addresses specified: this makes no sense"
argument_list|)
throw|;
block|}
block|}
return|return
name|addresses
return|;
block|}
comment|/**      * Resolves {@code publishHosts} to a single publish address. The fact that it returns      * only one address is just a current limitation.      *<p>      * If {@code publishHosts} resolves to more than one address,<b>then one is selected with magic</b>      *      * @param publishHosts list of hosts to publish as. this may contain special pseudo-hostnames      *                     such as _local_ (see the documentation). if it is null, it will be populated      *                     based on global default settings.      * @return single internet address      */
comment|// TODO: needs to be InetAddress[]
DECL|method|resolvePublishHostAddresses
specifier|public
name|InetAddress
name|resolvePublishHostAddresses
parameter_list|(
name|String
name|publishHosts
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|publishHosts
operator|==
literal|null
operator|||
name|publishHosts
operator|.
name|length
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|GLOBAL_NETWORK_PUBLISHHOST_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|||
name|GLOBAL_NETWORK_HOST_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
comment|// if we have settings use them (we have a fallback to GLOBAL_NETWORK_HOST_SETTING inline
name|publishHosts
operator|=
name|GLOBAL_NETWORK_PUBLISHHOST_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|toArray
argument_list|(
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// next check any registered custom resolvers if any
if|if
condition|(
name|customNameResolvers
operator|!=
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
name|addresses
index|[]
init|=
name|customNameResolver
operator|.
name|resolveDefault
argument_list|()
decl_stmt|;
if|if
condition|(
name|addresses
operator|!=
literal|null
condition|)
block|{
return|return
name|addresses
index|[
literal|0
index|]
return|;
block|}
block|}
block|}
comment|// we know it's not here. get the defaults
name|publishHosts
operator|=
name|GLOBAL_NETWORK_PUBLISHHOST_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|toArray
argument_list|(
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
block|}
block|}
name|InetAddress
name|addresses
index|[]
init|=
name|resolveInetAddresses
argument_list|(
name|publishHosts
argument_list|)
decl_stmt|;
comment|// TODO: allow publishing multiple addresses
comment|// for now... the hack begins
comment|// 1. single wildcard address, probably set by network.host: expand to all interface addresses.
if|if
condition|(
name|addresses
operator|.
name|length
operator|==
literal|1
operator|&&
name|addresses
index|[
literal|0
index|]
operator|.
name|isAnyLocalAddress
argument_list|()
condition|)
block|{
name|HashSet
argument_list|<
name|InetAddress
argument_list|>
name|all
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|NetworkUtils
operator|.
name|getAllAddresses
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|addresses
operator|=
name|all
operator|.
name|toArray
argument_list|(
operator|new
name|InetAddress
index|[
name|all
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
comment|// 2. try to deal with some (mis)configuration
for|for
control|(
name|InetAddress
name|address
range|:
name|addresses
control|)
block|{
comment|// check if its multicast: flat out mistake
if|if
condition|(
name|address
operator|.
name|isMulticastAddress
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"publish address: {"
operator|+
name|NetworkAddress
operator|.
name|format
argument_list|(
name|address
argument_list|)
operator|+
literal|"} is invalid: multicast address"
argument_list|)
throw|;
block|}
comment|// check if its a wildcard address: this is only ok if its the only address!
comment|// (if it was a single wildcard address, it was replaced by step 1 above)
if|if
condition|(
name|address
operator|.
name|isAnyLocalAddress
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"publish address: {"
operator|+
name|NetworkAddress
operator|.
name|format
argument_list|(
name|address
argument_list|)
operator|+
literal|"} is wildcard, but multiple addresses specified: this makes no sense"
argument_list|)
throw|;
block|}
block|}
comment|// 3. if we end out with multiple publish addresses, select by preference.
comment|// don't warn the user, or they will get confused by bind_host vs publish_host etc.
if|if
condition|(
name|addresses
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|List
argument_list|<
name|InetAddress
argument_list|>
name|sorted
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|addresses
argument_list|)
argument_list|)
decl_stmt|;
name|NetworkUtils
operator|.
name|sortAddresses
argument_list|(
name|sorted
argument_list|)
expr_stmt|;
name|addresses
operator|=
operator|new
name|InetAddress
index|[]
block|{
name|sorted
operator|.
name|get
argument_list|(
literal|0
argument_list|)
block|}
expr_stmt|;
block|}
return|return
name|addresses
index|[
literal|0
index|]
return|;
block|}
comment|/** resolves (and deduplicates) host specification */
DECL|method|resolveInetAddresses
specifier|private
name|InetAddress
index|[]
name|resolveInetAddresses
parameter_list|(
name|String
name|hosts
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|hosts
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"empty host specification"
argument_list|)
throw|;
block|}
comment|// deduplicate, in case of resolver misconfiguration
comment|// stuff like https://bugzilla.redhat.com/show_bug.cgi?id=496300
name|HashSet
argument_list|<
name|InetAddress
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|host
range|:
name|hosts
control|)
block|{
name|set
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|resolveInternal
argument_list|(
name|host
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|set
operator|.
name|toArray
argument_list|(
operator|new
name|InetAddress
index|[
name|set
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/** resolves a single host specification */
DECL|method|resolveInternal
specifier|private
name|InetAddress
index|[]
name|resolveInternal
parameter_list|(
name|String
name|host
parameter_list|)
throws|throws
name|IOException
block|{
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
comment|// next check any registered custom resolvers if any
if|if
condition|(
name|customNameResolvers
operator|!=
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
name|addresses
index|[]
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
name|addresses
operator|!=
literal|null
condition|)
block|{
return|return
name|addresses
return|;
block|}
block|}
block|}
switch|switch
condition|(
name|host
condition|)
block|{
case|case
literal|"local"
case|:
return|return
name|NetworkUtils
operator|.
name|getLoopbackAddresses
argument_list|()
return|;
case|case
literal|"local:ipv4"
case|:
return|return
name|NetworkUtils
operator|.
name|filterIPV4
argument_list|(
name|NetworkUtils
operator|.
name|getLoopbackAddresses
argument_list|()
argument_list|)
return|;
case|case
literal|"local:ipv6"
case|:
return|return
name|NetworkUtils
operator|.
name|filterIPV6
argument_list|(
name|NetworkUtils
operator|.
name|getLoopbackAddresses
argument_list|()
argument_list|)
return|;
case|case
literal|"site"
case|:
return|return
name|NetworkUtils
operator|.
name|getSiteLocalAddresses
argument_list|()
return|;
case|case
literal|"site:ipv4"
case|:
return|return
name|NetworkUtils
operator|.
name|filterIPV4
argument_list|(
name|NetworkUtils
operator|.
name|getSiteLocalAddresses
argument_list|()
argument_list|)
return|;
case|case
literal|"site:ipv6"
case|:
return|return
name|NetworkUtils
operator|.
name|filterIPV6
argument_list|(
name|NetworkUtils
operator|.
name|getSiteLocalAddresses
argument_list|()
argument_list|)
return|;
case|case
literal|"global"
case|:
return|return
name|NetworkUtils
operator|.
name|getGlobalAddresses
argument_list|()
return|;
case|case
literal|"global:ipv4"
case|:
return|return
name|NetworkUtils
operator|.
name|filterIPV4
argument_list|(
name|NetworkUtils
operator|.
name|getGlobalAddresses
argument_list|()
argument_list|)
return|;
case|case
literal|"global:ipv6"
case|:
return|return
name|NetworkUtils
operator|.
name|filterIPV6
argument_list|(
name|NetworkUtils
operator|.
name|getGlobalAddresses
argument_list|()
argument_list|)
return|;
default|default:
comment|/* an interface specification */
if|if
condition|(
name|host
operator|.
name|endsWith
argument_list|(
literal|":ipv4"
argument_list|)
condition|)
block|{
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
return|return
name|NetworkUtils
operator|.
name|filterIPV4
argument_list|(
name|NetworkUtils
operator|.
name|getAddressesForInterface
argument_list|(
name|host
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|host
operator|.
name|endsWith
argument_list|(
literal|":ipv6"
argument_list|)
condition|)
block|{
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
return|return
name|NetworkUtils
operator|.
name|filterIPV6
argument_list|(
name|NetworkUtils
operator|.
name|getAddressesForInterface
argument_list|(
name|host
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|NetworkUtils
operator|.
name|getAddressesForInterface
argument_list|(
name|host
argument_list|)
return|;
block|}
block|}
block|}
return|return
name|InetAddress
operator|.
name|getAllByName
argument_list|(
name|host
argument_list|)
return|;
block|}
comment|/**      * Register custom name resolver a DiscoveryPlugin might provide      * @param discoveryPlugins Discovery plugins      */
DECL|method|registerCustomNameResolvers
specifier|public
specifier|static
name|List
argument_list|<
name|CustomNameResolver
argument_list|>
name|registerCustomNameResolvers
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|List
argument_list|<
name|DiscoveryPlugin
argument_list|>
name|discoveryPlugins
parameter_list|)
block|{
name|List
argument_list|<
name|CustomNameResolver
argument_list|>
name|customNameResolvers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryPlugin
name|discoveryPlugin
range|:
name|discoveryPlugins
control|)
block|{
name|NetworkService
operator|.
name|CustomNameResolver
name|customNameResolver
init|=
name|discoveryPlugin
operator|.
name|getCustomNameResolver
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|customNameResolver
operator|!=
literal|null
condition|)
block|{
name|customNameResolvers
operator|.
name|add
argument_list|(
name|customNameResolver
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|customNameResolvers
return|;
block|}
block|}
end_class

end_unit

