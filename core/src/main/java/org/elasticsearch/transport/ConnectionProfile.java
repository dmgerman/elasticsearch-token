begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
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
name|common
operator|.
name|inject
operator|.
name|internal
operator|.
name|Nullable
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|Set
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_comment
comment|/**  * A connection profile describes how many connection are established to specific node for each of the available request types.  * ({@link org.elasticsearch.transport.TransportRequestOptions.Type}). This allows to tailor a connection towards a specific usage.  */
end_comment

begin_class
DECL|class|ConnectionProfile
specifier|public
specifier|final
class|class
name|ConnectionProfile
block|{
comment|/**      * Builds a connection profile that is dedicated to a single channel type. Use this      * when opening single use connections      */
DECL|method|buildSingleChannelProfile
specifier|public
specifier|static
name|ConnectionProfile
name|buildSingleChannelProfile
parameter_list|(
name|TransportRequestOptions
operator|.
name|Type
name|channelType
parameter_list|,
annotation|@
name|Nullable
name|TimeValue
name|connectTimeout
parameter_list|,
annotation|@
name|Nullable
name|TimeValue
name|handshakeTimeout
parameter_list|)
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|addConnections
argument_list|(
literal|1
argument_list|,
name|channelType
argument_list|)
expr_stmt|;
specifier|final
name|EnumSet
argument_list|<
name|TransportRequestOptions
operator|.
name|Type
argument_list|>
name|otherTypes
init|=
name|EnumSet
operator|.
name|allOf
argument_list|(
name|TransportRequestOptions
operator|.
name|Type
operator|.
name|class
argument_list|)
decl_stmt|;
name|otherTypes
operator|.
name|remove
argument_list|(
name|channelType
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addConnections
argument_list|(
literal|0
argument_list|,
name|otherTypes
operator|.
name|stream
argument_list|()
operator|.
name|toArray
argument_list|(
name|TransportRequestOptions
operator|.
name|Type
index|[]
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|connectTimeout
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setConnectTimeout
argument_list|(
name|connectTimeout
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|handshakeTimeout
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setHandshakeTimeout
argument_list|(
name|handshakeTimeout
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|field|handles
specifier|private
specifier|final
name|List
argument_list|<
name|ConnectionTypeHandle
argument_list|>
name|handles
decl_stmt|;
DECL|field|numConnections
specifier|private
specifier|final
name|int
name|numConnections
decl_stmt|;
DECL|field|connectTimeout
specifier|private
specifier|final
name|TimeValue
name|connectTimeout
decl_stmt|;
DECL|field|handshakeTimeout
specifier|private
specifier|final
name|TimeValue
name|handshakeTimeout
decl_stmt|;
DECL|method|ConnectionProfile
specifier|private
name|ConnectionProfile
parameter_list|(
name|List
argument_list|<
name|ConnectionTypeHandle
argument_list|>
name|handles
parameter_list|,
name|int
name|numConnections
parameter_list|,
name|TimeValue
name|connectTimeout
parameter_list|,
name|TimeValue
name|handshakeTimeout
parameter_list|)
block|{
name|this
operator|.
name|handles
operator|=
name|handles
expr_stmt|;
name|this
operator|.
name|numConnections
operator|=
name|numConnections
expr_stmt|;
name|this
operator|.
name|connectTimeout
operator|=
name|connectTimeout
expr_stmt|;
name|this
operator|.
name|handshakeTimeout
operator|=
name|handshakeTimeout
expr_stmt|;
block|}
comment|/**      * A builder to build a new {@link ConnectionProfile}      */
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|handles
specifier|private
specifier|final
name|List
argument_list|<
name|ConnectionTypeHandle
argument_list|>
name|handles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|addedTypes
specifier|private
specifier|final
name|Set
argument_list|<
name|TransportRequestOptions
operator|.
name|Type
argument_list|>
name|addedTypes
init|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|TransportRequestOptions
operator|.
name|Type
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|offset
specifier|private
name|int
name|offset
init|=
literal|0
decl_stmt|;
DECL|field|connectTimeout
specifier|private
name|TimeValue
name|connectTimeout
decl_stmt|;
DECL|field|handshakeTimeout
specifier|private
name|TimeValue
name|handshakeTimeout
decl_stmt|;
comment|/**          * Sets a connect timeout for this connection profile          */
DECL|method|setConnectTimeout
specifier|public
name|void
name|setConnectTimeout
parameter_list|(
name|TimeValue
name|connectTimeout
parameter_list|)
block|{
if|if
condition|(
name|connectTimeout
operator|.
name|millis
argument_list|()
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"connectTimeout must be non-negative but was: "
operator|+
name|connectTimeout
argument_list|)
throw|;
block|}
name|this
operator|.
name|connectTimeout
operator|=
name|connectTimeout
expr_stmt|;
block|}
comment|/**          * Sets a handshake timeout for this connection profile          */
DECL|method|setHandshakeTimeout
specifier|public
name|void
name|setHandshakeTimeout
parameter_list|(
name|TimeValue
name|handshakeTimeout
parameter_list|)
block|{
if|if
condition|(
name|handshakeTimeout
operator|.
name|millis
argument_list|()
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"handshakeTimeout must be non-negative but was: "
operator|+
name|handshakeTimeout
argument_list|)
throw|;
block|}
name|this
operator|.
name|handshakeTimeout
operator|=
name|handshakeTimeout
expr_stmt|;
block|}
comment|/**          * Adds a number of connections for one or more types. Each type can only be added once.          * @param numConnections the number of connections to use in the pool for the given connection types          * @param types a set of types that should share the given number of connections          */
DECL|method|addConnections
specifier|public
name|void
name|addConnections
parameter_list|(
name|int
name|numConnections
parameter_list|,
name|TransportRequestOptions
operator|.
name|Type
modifier|...
name|types
parameter_list|)
block|{
if|if
condition|(
name|types
operator|==
literal|null
operator|||
name|types
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
literal|"types must not be null"
argument_list|)
throw|;
block|}
for|for
control|(
name|TransportRequestOptions
operator|.
name|Type
name|type
range|:
name|types
control|)
block|{
if|if
condition|(
name|addedTypes
operator|.
name|contains
argument_list|(
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"type ["
operator|+
name|type
operator|+
literal|"] is already registered"
argument_list|)
throw|;
block|}
block|}
name|addedTypes
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|types
argument_list|)
argument_list|)
expr_stmt|;
name|handles
operator|.
name|add
argument_list|(
operator|new
name|ConnectionTypeHandle
argument_list|(
name|offset
argument_list|,
name|numConnections
argument_list|,
name|EnumSet
operator|.
name|copyOf
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|types
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|numConnections
expr_stmt|;
block|}
comment|/**          * Creates a new {@link ConnectionProfile} based on the added connections.          * @throws IllegalStateException if any of the {@link org.elasticsearch.transport.TransportRequestOptions.Type} enum is missing          */
DECL|method|build
specifier|public
name|ConnectionProfile
name|build
parameter_list|()
block|{
name|EnumSet
argument_list|<
name|TransportRequestOptions
operator|.
name|Type
argument_list|>
name|types
init|=
name|EnumSet
operator|.
name|allOf
argument_list|(
name|TransportRequestOptions
operator|.
name|Type
operator|.
name|class
argument_list|)
decl_stmt|;
name|types
operator|.
name|removeAll
argument_list|(
name|addedTypes
argument_list|)
expr_stmt|;
if|if
condition|(
name|types
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"not all types are added for this connection profile - missing types: "
operator|+
name|types
argument_list|)
throw|;
block|}
return|return
operator|new
name|ConnectionProfile
argument_list|(
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|handles
argument_list|)
argument_list|,
name|offset
argument_list|,
name|connectTimeout
argument_list|,
name|handshakeTimeout
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns the connect timeout or<code>null</code> if no explicit timeout is set on this profile.      */
DECL|method|getConnectTimeout
specifier|public
name|TimeValue
name|getConnectTimeout
parameter_list|()
block|{
return|return
name|connectTimeout
return|;
block|}
comment|/**      * Returns the handshake timeout or<code>null</code> if no explicit timeout is set on this profile.      */
DECL|method|getHandshakeTimeout
specifier|public
name|TimeValue
name|getHandshakeTimeout
parameter_list|()
block|{
return|return
name|handshakeTimeout
return|;
block|}
comment|/**      * Returns the total number of connections for this profile      */
DECL|method|getNumConnections
specifier|public
name|int
name|getNumConnections
parameter_list|()
block|{
return|return
name|numConnections
return|;
block|}
comment|/**      * Returns the number of connections per type for this profile. This might return a count that is shared with other types such      * that the sum of all connections per type might be higher than {@link #getNumConnections()}. For instance if      * {@link org.elasticsearch.transport.TransportRequestOptions.Type#BULK} shares connections with      * {@link org.elasticsearch.transport.TransportRequestOptions.Type#REG} they will return both the same number of connections from      * this method but the connections are not distinct.      */
DECL|method|getNumConnectionsPerType
specifier|public
name|int
name|getNumConnectionsPerType
parameter_list|(
name|TransportRequestOptions
operator|.
name|Type
name|type
parameter_list|)
block|{
for|for
control|(
name|ConnectionTypeHandle
name|handle
range|:
name|handles
control|)
block|{
if|if
condition|(
name|handle
operator|.
name|getTypes
argument_list|()
operator|.
name|contains
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|handle
operator|.
name|length
return|;
block|}
block|}
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"no handle found for type: "
operator|+
name|type
argument_list|)
throw|;
block|}
comment|/**      * Returns the type handles for this connection profile      */
DECL|method|getHandles
name|List
argument_list|<
name|ConnectionTypeHandle
argument_list|>
name|getHandles
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|handles
argument_list|)
return|;
block|}
comment|/**      * Connection type handle encapsulates the logic which connection      */
DECL|class|ConnectionTypeHandle
specifier|static
specifier|final
class|class
name|ConnectionTypeHandle
block|{
DECL|field|length
specifier|public
specifier|final
name|int
name|length
decl_stmt|;
DECL|field|offset
specifier|public
specifier|final
name|int
name|offset
decl_stmt|;
DECL|field|types
specifier|private
specifier|final
name|Set
argument_list|<
name|TransportRequestOptions
operator|.
name|Type
argument_list|>
name|types
decl_stmt|;
DECL|field|counter
specifier|private
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|method|ConnectionTypeHandle
specifier|private
name|ConnectionTypeHandle
parameter_list|(
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|Set
argument_list|<
name|TransportRequestOptions
operator|.
name|Type
argument_list|>
name|types
parameter_list|)
block|{
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
block|}
comment|/**          * Returns one of the channels out configured for this handle. The channel is selected in a round-robin          * fashion.          */
DECL|method|getChannel
parameter_list|<
name|T
parameter_list|>
name|T
name|getChannel
parameter_list|(
name|T
index|[]
name|channels
parameter_list|)
block|{
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"can't select channel size is 0 for types: "
operator|+
name|types
argument_list|)
throw|;
block|}
assert|assert
name|channels
operator|.
name|length
operator|>=
name|offset
operator|+
name|length
operator|:
literal|"illegal size: "
operator|+
name|channels
operator|.
name|length
operator|+
literal|" expected>= "
operator|+
operator|(
name|offset
operator|+
name|length
operator|)
assert|;
return|return
name|channels
index|[
name|offset
operator|+
name|Math
operator|.
name|floorMod
argument_list|(
name|counter
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|length
argument_list|)
index|]
return|;
block|}
comment|/**          * Returns all types for this handle          */
DECL|method|getTypes
name|Set
argument_list|<
name|TransportRequestOptions
operator|.
name|Type
argument_list|>
name|getTypes
parameter_list|()
block|{
return|return
name|types
return|;
block|}
block|}
block|}
end_class

end_unit

