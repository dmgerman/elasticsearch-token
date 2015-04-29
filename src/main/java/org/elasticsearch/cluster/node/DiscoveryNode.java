begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
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
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

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
name|Booleans
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
name|io
operator|.
name|stream
operator|.
name|*
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
name|NetworkUtils
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
name|TransportAddress
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
name|TransportAddressSerializers
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|transport
operator|.
name|TransportAddressSerializers
operator|.
name|addressToStream
import|;
end_import

begin_comment
comment|/**  * A discovery node represents a node that is part of the cluster.  */
end_comment

begin_class
DECL|class|DiscoveryNode
specifier|public
class|class
name|DiscoveryNode
implements|implements
name|Streamable
implements|,
name|Serializable
block|{
comment|/**      * Minimum version of a node to communicate with. This version corresponds to the minimum compatibility version      * of the current elasticsearch major version.      */
DECL|field|MINIMUM_DISCOVERY_NODE_VERSION
specifier|public
specifier|static
specifier|final
name|Version
name|MINIMUM_DISCOVERY_NODE_VERSION
init|=
name|Version
operator|.
name|CURRENT
operator|.
name|minimumCompatibilityVersion
argument_list|()
decl_stmt|;
DECL|method|localNode
specifier|public
specifier|static
name|boolean
name|localNode
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
literal|"node.local"
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"node.local"
argument_list|,
literal|false
argument_list|)
return|;
block|}
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
literal|"node.mode"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|String
name|nodeMode
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"node.mode"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"local"
operator|.
name|equals
argument_list|(
name|nodeMode
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
literal|"network"
operator|.
name|equals
argument_list|(
name|nodeMode
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unsupported node.mode ["
operator|+
name|nodeMode
operator|+
literal|"]. Should be one of [local, network]."
argument_list|)
throw|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|nodeRequiresLocalStorage
specifier|public
specifier|static
name|boolean
name|nodeRequiresLocalStorage
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
operator|!
operator|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"node.client"
argument_list|,
literal|false
argument_list|)
operator|||
operator|(
operator|!
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"node.data"
argument_list|,
literal|true
argument_list|)
operator|&&
operator|!
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"node.master"
argument_list|,
literal|true
argument_list|)
operator|)
operator|)
return|;
block|}
DECL|method|clientNode
specifier|public
specifier|static
name|boolean
name|clientNode
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|client
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"node.client"
argument_list|)
decl_stmt|;
return|return
name|Booleans
operator|.
name|isExplicitTrue
argument_list|(
name|client
argument_list|)
return|;
block|}
DECL|method|masterNode
specifier|public
specifier|static
name|boolean
name|masterNode
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|master
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"node.master"
argument_list|)
decl_stmt|;
if|if
condition|(
name|master
operator|==
literal|null
condition|)
block|{
return|return
operator|!
name|clientNode
argument_list|(
name|settings
argument_list|)
return|;
block|}
return|return
name|Booleans
operator|.
name|isExplicitTrue
argument_list|(
name|master
argument_list|)
return|;
block|}
DECL|method|dataNode
specifier|public
specifier|static
name|boolean
name|dataNode
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|data
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"node.data"
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
operator|!
name|clientNode
argument_list|(
name|settings
argument_list|)
return|;
block|}
return|return
name|Booleans
operator|.
name|isExplicitTrue
argument_list|(
name|data
argument_list|)
return|;
block|}
DECL|field|EMPTY_LIST
specifier|public
specifier|static
specifier|final
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|EMPTY_LIST
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|nodeName
specifier|private
name|String
name|nodeName
init|=
literal|""
decl_stmt|;
DECL|field|nodeId
specifier|private
name|String
name|nodeId
decl_stmt|;
DECL|field|hostName
specifier|private
name|String
name|hostName
decl_stmt|;
DECL|field|hostAddress
specifier|private
name|String
name|hostAddress
decl_stmt|;
DECL|field|address
specifier|private
name|TransportAddress
name|address
decl_stmt|;
DECL|field|attributes
specifier|private
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
decl_stmt|;
DECL|field|version
specifier|private
name|Version
name|version
init|=
name|Version
operator|.
name|CURRENT
decl_stmt|;
DECL|method|DiscoveryNode
name|DiscoveryNode
parameter_list|()
block|{     }
comment|/**      * Creates a new {@link DiscoveryNode}      *<p>      *<b>Note:</b> if the version of the node is unknown {@link #MINIMUM_DISCOVERY_NODE_VERSION} should be used.      * it corresponds to the minimum version this elasticsearch version can communicate with. If a higher version is used      * the node might not be able to communicate with the remove node. After initial handshakes node versions will be discovered      * and updated.      *</p>      *      * @param nodeId  the nodes unique id.      * @param address the nodes transport address      * @param version the version of the node.      */
DECL|method|DiscoveryNode
specifier|public
name|DiscoveryNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|TransportAddress
name|address
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
name|this
argument_list|(
literal|""
argument_list|,
name|nodeId
argument_list|,
name|address
argument_list|,
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|of
argument_list|()
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new {@link DiscoveryNode}      *<p>      *<b>Note:</b> if the version of the node is unknown {@link #MINIMUM_DISCOVERY_NODE_VERSION} should be used.      * it corresponds to the minimum version this elasticsearch version can communicate with. If a higher version is used      * the node might not be able to communicate with the remove node. After initial handshakes node versions will be discovered      * and updated.      *</p>      *      * @param nodeName   the nodes name      * @param nodeId     the nodes unique id.      * @param address    the nodes transport address      * @param attributes node attributes      * @param version    the version of the node.      */
DECL|method|DiscoveryNode
specifier|public
name|DiscoveryNode
parameter_list|(
name|String
name|nodeName
parameter_list|,
name|String
name|nodeId
parameter_list|,
name|TransportAddress
name|address
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
name|this
argument_list|(
name|nodeName
argument_list|,
name|nodeId
argument_list|,
name|NetworkUtils
operator|.
name|getLocalHostName
argument_list|(
literal|""
argument_list|)
argument_list|,
name|NetworkUtils
operator|.
name|getLocalHostAddress
argument_list|(
literal|""
argument_list|)
argument_list|,
name|address
argument_list|,
name|attributes
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new {@link DiscoveryNode}      *<p>      *<b>Note:</b> if the version of the node is unknown {@link #MINIMUM_DISCOVERY_NODE_VERSION} should be used.      * it corresponds to the minimum version this elasticsearch version can communicate with. If a higher version is used      * the node might not be able to communicate with the remove node. After initial handshakes node versions will be discovered      * and updated.      *</p>      *      * @param nodeName    the nodes name      * @param nodeId      the nodes unique id.      * @param hostName    the nodes hostname      * @param hostAddress the nodes host address      * @param address     the nodes transport address      * @param attributes  node attributes      * @param version     the version of the node.      */
DECL|method|DiscoveryNode
specifier|public
name|DiscoveryNode
parameter_list|(
name|String
name|nodeName
parameter_list|,
name|String
name|nodeId
parameter_list|,
name|String
name|hostName
parameter_list|,
name|String
name|hostAddress
parameter_list|,
name|TransportAddress
name|address
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
if|if
condition|(
name|nodeName
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|nodeName
operator|=
name|nodeName
operator|.
name|intern
argument_list|()
expr_stmt|;
block|}
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|attributes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|intern
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|intern
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|attributes
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|nodeId
operator|.
name|intern
argument_list|()
expr_stmt|;
name|this
operator|.
name|hostName
operator|=
name|hostName
operator|.
name|intern
argument_list|()
expr_stmt|;
name|this
operator|.
name|hostAddress
operator|=
name|hostAddress
operator|.
name|intern
argument_list|()
expr_stmt|;
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
comment|/**      * Should this node form a connection to the provided node.      */
DECL|method|shouldConnectTo
specifier|public
name|boolean
name|shouldConnectTo
parameter_list|(
name|DiscoveryNode
name|otherNode
parameter_list|)
block|{
if|if
condition|(
name|clientNode
argument_list|()
operator|&&
name|otherNode
operator|.
name|clientNode
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * The address that the node can be communicated with.      */
DECL|method|address
specifier|public
name|TransportAddress
name|address
parameter_list|()
block|{
return|return
name|address
return|;
block|}
comment|/**      * The address that the node can be communicated with.      */
DECL|method|getAddress
specifier|public
name|TransportAddress
name|getAddress
parameter_list|()
block|{
return|return
name|address
argument_list|()
return|;
block|}
comment|/**      * The unique id of the node.      */
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|nodeId
return|;
block|}
comment|/**      * The unique id of the node.      */
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
argument_list|()
return|;
block|}
comment|/**      * The name of the node.      */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeName
return|;
block|}
comment|/**      * The name of the node.      */
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
argument_list|()
return|;
block|}
comment|/**      * The node attributes.      */
DECL|method|attributes
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|()
block|{
return|return
name|this
operator|.
name|attributes
return|;
block|}
comment|/**      * The node attributes.      */
DECL|method|getAttributes
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getAttributes
parameter_list|()
block|{
return|return
name|attributes
argument_list|()
return|;
block|}
comment|/**      * Should this node hold data (shards) or not.      */
DECL|method|dataNode
specifier|public
name|boolean
name|dataNode
parameter_list|()
block|{
name|String
name|data
init|=
name|attributes
operator|.
name|get
argument_list|(
literal|"data"
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
operator|!
name|clientNode
argument_list|()
return|;
block|}
return|return
name|Booleans
operator|.
name|parseBooleanExact
argument_list|(
name|data
argument_list|)
return|;
block|}
comment|/**      * Should this node hold data (shards) or not.      */
DECL|method|isDataNode
specifier|public
name|boolean
name|isDataNode
parameter_list|()
block|{
return|return
name|dataNode
argument_list|()
return|;
block|}
comment|/**      * Is the node a client node or not.      */
DECL|method|clientNode
specifier|public
name|boolean
name|clientNode
parameter_list|()
block|{
name|String
name|client
init|=
name|attributes
operator|.
name|get
argument_list|(
literal|"client"
argument_list|)
decl_stmt|;
return|return
name|client
operator|!=
literal|null
operator|&&
name|Booleans
operator|.
name|parseBooleanExact
argument_list|(
name|client
argument_list|)
return|;
block|}
DECL|method|isClientNode
specifier|public
name|boolean
name|isClientNode
parameter_list|()
block|{
return|return
name|clientNode
argument_list|()
return|;
block|}
comment|/**      * Can this node become master or not.      */
DECL|method|masterNode
specifier|public
name|boolean
name|masterNode
parameter_list|()
block|{
name|String
name|master
init|=
name|attributes
operator|.
name|get
argument_list|(
literal|"master"
argument_list|)
decl_stmt|;
if|if
condition|(
name|master
operator|==
literal|null
condition|)
block|{
return|return
operator|!
name|clientNode
argument_list|()
return|;
block|}
return|return
name|Booleans
operator|.
name|parseBooleanExact
argument_list|(
name|master
argument_list|)
return|;
block|}
comment|/**      * Can this node become master or not.      */
DECL|method|isMasterNode
specifier|public
name|boolean
name|isMasterNode
parameter_list|()
block|{
return|return
name|masterNode
argument_list|()
return|;
block|}
DECL|method|version
specifier|public
name|Version
name|version
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
DECL|method|getHostName
specifier|public
name|String
name|getHostName
parameter_list|()
block|{
return|return
name|this
operator|.
name|hostName
return|;
block|}
DECL|method|getHostAddress
specifier|public
name|String
name|getHostAddress
parameter_list|()
block|{
return|return
name|this
operator|.
name|hostAddress
return|;
block|}
DECL|method|getVersion
specifier|public
name|Version
name|getVersion
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
DECL|method|readNode
specifier|public
specifier|static
name|DiscoveryNode
name|readNode
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|()
decl_stmt|;
name|node
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|node
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
name|nodeName
operator|=
name|in
operator|.
name|readString
argument_list|()
operator|.
name|intern
argument_list|()
expr_stmt|;
name|nodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
operator|.
name|intern
argument_list|()
expr_stmt|;
name|hostName
operator|=
name|in
operator|.
name|readString
argument_list|()
operator|.
name|intern
argument_list|()
expr_stmt|;
name|hostAddress
operator|=
name|in
operator|.
name|readString
argument_list|()
operator|.
name|intern
argument_list|()
expr_stmt|;
name|address
operator|=
name|TransportAddressSerializers
operator|.
name|addressFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
operator|.
name|intern
argument_list|()
argument_list|,
name|in
operator|.
name|readString
argument_list|()
operator|.
name|intern
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|attributes
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
name|version
operator|=
name|Version
operator|.
name|readVersion
argument_list|(
name|in
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
name|out
operator|.
name|writeString
argument_list|(
name|nodeName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|hostName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|hostAddress
argument_list|)
expr_stmt|;
name|addressToStream
argument_list|(
name|out
argument_list|,
name|address
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|attributes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|attributes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Version
operator|.
name|writeVersion
argument_list|(
name|version
argument_list|,
name|out
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
name|obj
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|DiscoveryNode
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|DiscoveryNode
name|other
init|=
operator|(
name|DiscoveryNode
operator|)
name|obj
decl_stmt|;
return|return
name|this
operator|.
name|nodeId
operator|.
name|equals
argument_list|(
name|other
operator|.
name|nodeId
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
name|nodeId
operator|.
name|hashCode
argument_list|()
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodeName
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|nodeName
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodeId
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|nodeId
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|hostName
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|hostName
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|address
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|address
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|attributes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|// we need this custom serialization logic because Version is not serializable (because org.apache.lucene.util.Version is not serializable)
DECL|method|writeObject
specifier|private
name|void
name|writeObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|StreamOutput
name|streamOutput
init|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|streamOutput
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|CURRENT
operator|.
name|minimumCompatibilityVersion
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeTo
argument_list|(
name|streamOutput
argument_list|)
expr_stmt|;
block|}
DECL|method|readObject
specifier|private
name|void
name|readObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|StreamInput
name|streamInput
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|streamInput
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|CURRENT
operator|.
name|minimumCompatibilityVersion
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|readFrom
argument_list|(
name|streamInput
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

