begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
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
comment|/**  * Represents a connection to a host. It holds the host that the connection points to.  * Allows the transport to deal with very simple connection objects that are immutable.  * Any change to the state of a connection should be made through the connection pool.  */
end_comment

begin_class
DECL|class|Connection
specifier|public
class|class
name|Connection
block|{
DECL|field|DEFAULT_CONNECTION_TIMEOUT_MILLIS
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_CONNECTION_TIMEOUT_MILLIS
init|=
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toMillis
argument_list|(
literal|1
argument_list|)
decl_stmt|;
DECL|field|MAX_CONNECTION_TIMEOUT_MILLIS
specifier|private
specifier|static
specifier|final
name|long
name|MAX_CONNECTION_TIMEOUT_MILLIS
init|=
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toMillis
argument_list|(
literal|30
argument_list|)
decl_stmt|;
DECL|field|host
specifier|private
specifier|final
name|HttpHost
name|host
decl_stmt|;
DECL|field|state
specifier|private
specifier|volatile
name|State
name|state
init|=
name|State
operator|.
name|UNKNOWN
decl_stmt|;
DECL|field|failedAttempts
specifier|private
specifier|volatile
name|int
name|failedAttempts
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|deadUntil
specifier|private
specifier|volatile
name|long
name|deadUntil
init|=
operator|-
literal|1
decl_stmt|;
comment|/**      * Creates a new connection pointing to the provided {@link HttpHost} argument      */
DECL|method|Connection
specifier|public
name|Connection
parameter_list|(
name|HttpHost
name|host
parameter_list|)
block|{
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
block|}
comment|/**      * Returns the {@link HttpHost} that the connection points to      */
DECL|method|getHost
specifier|public
name|HttpHost
name|getHost
parameter_list|()
block|{
return|return
name|host
return|;
block|}
comment|/**      * Marks connection as dead. Should be called in case the corresponding node is not responding or caused failures.      * Once marked dead, the number of failed attempts will be incremented on each call to this method. A dead connection      * should be retried once {@link #shouldBeRetried()} returns true, which depends on the number of previous failed attempts      * and when the last failure was registered.      */
DECL|method|markDead
name|void
name|markDead
parameter_list|()
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|int
name|failedAttempts
init|=
name|Math
operator|.
name|max
argument_list|(
name|this
operator|.
name|failedAttempts
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|long
name|timeoutMillis
init|=
operator|(
name|long
operator|)
name|Math
operator|.
name|min
argument_list|(
name|DEFAULT_CONNECTION_TIMEOUT_MILLIS
operator|*
literal|2
operator|*
name|Math
operator|.
name|pow
argument_list|(
literal|2
argument_list|,
name|failedAttempts
operator|*
literal|0.5
operator|-
literal|1
argument_list|)
argument_list|,
name|MAX_CONNECTION_TIMEOUT_MILLIS
argument_list|)
decl_stmt|;
name|this
operator|.
name|deadUntil
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|+
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|timeoutMillis
argument_list|)
expr_stmt|;
name|this
operator|.
name|failedAttempts
operator|=
operator|++
name|failedAttempts
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|State
operator|.
name|DEAD
expr_stmt|;
block|}
block|}
comment|/**      * Marks this connection alive. Should be called when the corresponding node is working properly.      * Will reset the number of failed attempts that were counted in case the connection was previously dead,      * as well as its dead timeout.      */
DECL|method|markAlive
name|void
name|markAlive
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|state
operator|!=
name|State
operator|.
name|ALIVE
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|this
operator|.
name|deadUntil
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|failedAttempts
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|State
operator|.
name|ALIVE
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Resets the connection to its initial state, so it will be retried. To be called when all the connections in the pool      * are dead, so that one connection can be retried. Note that calling this method only changes the state of the connection,      * it doesn't reset its failed attempts and dead until timestamp. That way if the connection goes back to dead straightaway      * all of its previous failed attempts are taken into account.      */
DECL|method|markResurrected
name|void
name|markResurrected
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|state
operator|==
name|State
operator|.
name|DEAD
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|this
operator|.
name|state
operator|=
name|State
operator|.
name|UNKNOWN
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Returns the timestamp till the connection is supposed to stay dead till it can be retried      */
DECL|method|getDeadUntil
specifier|public
name|long
name|getDeadUntil
parameter_list|()
block|{
return|return
name|deadUntil
return|;
block|}
comment|/**      * Returns true if the connection is alive, false otherwise.      */
DECL|method|isAlive
specifier|public
name|boolean
name|isAlive
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|ALIVE
return|;
block|}
comment|/**      * Returns true in case the connection is not alive but should be used/retried, false otherwise.      * Returns true in case the connection is in unknown state (never used before) or resurrected. When the connection is dead,      * returns true when it is time to retry it, depending on how many failed attempts were registered and when the last failure      * happened (minimum 1 minute, maximum 30 minutes).      */
DECL|method|shouldBeRetried
specifier|public
name|boolean
name|shouldBeRetried
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|UNKNOWN
operator|||
operator|(
name|state
operator|==
name|State
operator|.
name|DEAD
operator|&&
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|deadUntil
operator|>=
literal|0
operator|)
return|;
block|}
DECL|enum|State
specifier|private
enum|enum
name|State
block|{
DECL|enum constant|UNKNOWN
DECL|enum constant|DEAD
DECL|enum constant|ALIVE
name|UNKNOWN
block|,
name|DEAD
block|,
name|ALIVE
block|}
block|}
end_class

end_unit

