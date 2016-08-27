begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|Decision
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
name|joda
operator|.
name|FormatDateTimeFormatter
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
name|joda
operator|.
name|Joda
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
name|TimeValue
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentBuilder
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * Holds additional information as to why the shard is in unassigned state.  */
end_comment

begin_class
DECL|class|UnassignedInfo
specifier|public
specifier|final
class|class
name|UnassignedInfo
implements|implements
name|ToXContent
implements|,
name|Writeable
block|{
DECL|field|DATE_TIME_FORMATTER
specifier|public
specifier|static
specifier|final
name|FormatDateTimeFormatter
name|DATE_TIME_FORMATTER
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateOptionalTime"
argument_list|)
decl_stmt|;
DECL|field|INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
literal|"index.unassigned.node_left.delayed_timeout"
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|)
decl_stmt|;
comment|/**      * Reason why the shard is in unassigned state.      *<p>      * Note, ordering of the enum is important, make sure to add new values      * at the end and handle version serialization properly.      */
DECL|enum|Reason
specifier|public
enum|enum
name|Reason
block|{
comment|/**          * Unassigned as a result of an API creation of an index.          */
DECL|enum constant|INDEX_CREATED
name|INDEX_CREATED
block|,
comment|/**          * Unassigned as a result of a full cluster recovery.          */
DECL|enum constant|CLUSTER_RECOVERED
name|CLUSTER_RECOVERED
block|,
comment|/**          * Unassigned as a result of opening a closed index.          */
DECL|enum constant|INDEX_REOPENED
name|INDEX_REOPENED
block|,
comment|/**          * Unassigned as a result of importing a dangling index.          */
DECL|enum constant|DANGLING_INDEX_IMPORTED
name|DANGLING_INDEX_IMPORTED
block|,
comment|/**          * Unassigned as a result of restoring into a new index.          */
DECL|enum constant|NEW_INDEX_RESTORED
name|NEW_INDEX_RESTORED
block|,
comment|/**          * Unassigned as a result of restoring into a closed index.          */
DECL|enum constant|EXISTING_INDEX_RESTORED
name|EXISTING_INDEX_RESTORED
block|,
comment|/**          * Unassigned as a result of explicit addition of a replica.          */
DECL|enum constant|REPLICA_ADDED
name|REPLICA_ADDED
block|,
comment|/**          * Unassigned as a result of a failed allocation of the shard.          */
DECL|enum constant|ALLOCATION_FAILED
name|ALLOCATION_FAILED
block|,
comment|/**          * Unassigned as a result of the node hosting it leaving the cluster.          */
DECL|enum constant|NODE_LEFT
name|NODE_LEFT
block|,
comment|/**          * Unassigned as a result of explicit cancel reroute command.          */
DECL|enum constant|REROUTE_CANCELLED
name|REROUTE_CANCELLED
block|,
comment|/**          * When a shard moves from started back to initializing, for example, during shadow replica          */
DECL|enum constant|REINITIALIZED
name|REINITIALIZED
block|,
comment|/**          * A better replica location is identified and causes the existing replica allocation to be cancelled.          */
DECL|enum constant|REALLOCATED_REPLICA
name|REALLOCATED_REPLICA
block|,
comment|/**          * Unassigned as a result of a failed primary while the replica was initializing.          */
DECL|enum constant|PRIMARY_FAILED
name|PRIMARY_FAILED
block|,
comment|/**          * Unassigned after forcing an empty primary          */
DECL|enum constant|FORCED_EMPTY_PRIMARY
name|FORCED_EMPTY_PRIMARY
block|}
comment|/**      * Captures the status of an unsuccessful allocation attempt for the shard,      * causing it to remain in the unassigned state.      *      * Note, ordering of the enum is important, make sure to add new values      * at the end and handle version serialization properly.      */
DECL|enum|AllocationStatus
specifier|public
enum|enum
name|AllocationStatus
implements|implements
name|Writeable
block|{
comment|/**          * The shard was denied allocation to a node because the allocation deciders all returned a NO decision          */
DECL|enum constant|DECIDERS_NO
name|DECIDERS_NO
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
comment|/**          * The shard was denied allocation to a node because there were no valid shard copies found for it;          * this can happen on node restart with gateway allocation          */
DECL|enum constant|NO_VALID_SHARD_COPY
name|NO_VALID_SHARD_COPY
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|,
comment|/**          * The allocation attempt was throttled on the shard by the allocation deciders          */
DECL|enum constant|DECIDERS_THROTTLED
name|DECIDERS_THROTTLED
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|,
comment|/**          * Waiting on getting shard data from all nodes before making a decision about where to allocate the shard          */
DECL|enum constant|FETCHING_SHARD_DATA
name|FETCHING_SHARD_DATA
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|)
block|,
comment|/**          * Allocation decision has been delayed          */
DECL|enum constant|DELAYED_ALLOCATION
name|DELAYED_ALLOCATION
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|)
block|,
comment|/**          * No allocation attempt has been made yet          */
DECL|enum constant|NO_ATTEMPT
name|NO_ATTEMPT
argument_list|(
operator|(
name|byte
operator|)
literal|5
argument_list|)
block|;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|method|AllocationStatus
name|AllocationStatus
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
comment|// package private for testing
DECL|method|getId
name|byte
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
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
name|writeByte
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|AllocationStatus
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|id
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
name|DECIDERS_NO
return|;
case|case
literal|1
case|:
return|return
name|NO_VALID_SHARD_COPY
return|;
case|case
literal|2
case|:
return|return
name|DECIDERS_THROTTLED
return|;
case|case
literal|3
case|:
return|return
name|FETCHING_SHARD_DATA
return|;
case|case
literal|4
case|:
return|return
name|DELAYED_ALLOCATION
return|;
case|case
literal|5
case|:
return|return
name|NO_ATTEMPT
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown AllocationStatus value ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|fromDecision
specifier|public
specifier|static
name|AllocationStatus
name|fromDecision
parameter_list|(
name|Decision
name|decision
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|decision
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|decision
operator|.
name|type
argument_list|()
condition|)
block|{
case|case
name|NO
case|:
return|return
name|DECIDERS_NO
return|;
case|case
name|THROTTLE
case|:
return|return
name|DECIDERS_THROTTLED
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no allocation attempt from decision["
operator|+
name|decision
operator|.
name|type
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|value
specifier|public
name|String
name|value
parameter_list|()
block|{
return|return
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
block|}
DECL|field|reason
specifier|private
specifier|final
name|Reason
name|reason
decl_stmt|;
DECL|field|unassignedTimeMillis
specifier|private
specifier|final
name|long
name|unassignedTimeMillis
decl_stmt|;
comment|// used for display and log messages, in milliseconds
DECL|field|unassignedTimeNanos
specifier|private
specifier|final
name|long
name|unassignedTimeNanos
decl_stmt|;
comment|// in nanoseconds, used to calculate delay for delayed shard allocation
DECL|field|delayed
specifier|private
specifier|final
name|boolean
name|delayed
decl_stmt|;
comment|// if allocation of this shard is delayed due to INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING
DECL|field|message
specifier|private
specifier|final
name|String
name|message
decl_stmt|;
DECL|field|failure
specifier|private
specifier|final
name|Exception
name|failure
decl_stmt|;
DECL|field|failedAllocations
specifier|private
specifier|final
name|int
name|failedAllocations
decl_stmt|;
DECL|field|lastAllocationStatus
specifier|private
specifier|final
name|AllocationStatus
name|lastAllocationStatus
decl_stmt|;
comment|// result of the last allocation attempt for this shard
comment|/**      * creates an UnassignedInfo object based on **current** time      *      * @param reason  the cause for making this shard unassigned. See {@link Reason} for more information.      * @param message more information about cause.      **/
DECL|method|UnassignedInfo
specifier|public
name|UnassignedInfo
parameter_list|(
name|Reason
name|reason
parameter_list|,
name|String
name|message
parameter_list|)
block|{
name|this
argument_list|(
name|reason
argument_list|,
name|message
argument_list|,
literal|null
argument_list|,
name|reason
operator|==
name|Reason
operator|.
name|ALLOCATION_FAILED
condition|?
literal|1
else|:
literal|0
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|false
argument_list|,
name|AllocationStatus
operator|.
name|NO_ATTEMPT
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param reason               the cause for making this shard unassigned. See {@link Reason} for more information.      * @param message              more information about cause.      * @param failure              the shard level failure that caused this shard to be unassigned, if exists.      * @param unassignedTimeNanos  the time to use as the base for any delayed re-assignment calculation      * @param unassignedTimeMillis the time of unassignment used to display to in our reporting.      * @param delayed              if allocation of this shard is delayed due to INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING.      * @param lastAllocationStatus the result of the last allocation attempt for this shard      */
DECL|method|UnassignedInfo
specifier|public
name|UnassignedInfo
parameter_list|(
name|Reason
name|reason
parameter_list|,
annotation|@
name|Nullable
name|String
name|message
parameter_list|,
annotation|@
name|Nullable
name|Exception
name|failure
parameter_list|,
name|int
name|failedAllocations
parameter_list|,
name|long
name|unassignedTimeNanos
parameter_list|,
name|long
name|unassignedTimeMillis
parameter_list|,
name|boolean
name|delayed
parameter_list|,
name|AllocationStatus
name|lastAllocationStatus
parameter_list|)
block|{
name|this
operator|.
name|reason
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|reason
argument_list|)
expr_stmt|;
name|this
operator|.
name|unassignedTimeMillis
operator|=
name|unassignedTimeMillis
expr_stmt|;
name|this
operator|.
name|unassignedTimeNanos
operator|=
name|unassignedTimeNanos
expr_stmt|;
name|this
operator|.
name|delayed
operator|=
name|delayed
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
name|this
operator|.
name|failure
operator|=
name|failure
expr_stmt|;
name|this
operator|.
name|failedAllocations
operator|=
name|failedAllocations
expr_stmt|;
name|this
operator|.
name|lastAllocationStatus
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|lastAllocationStatus
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|failedAllocations
operator|>
literal|0
operator|)
operator|==
operator|(
name|reason
operator|==
name|Reason
operator|.
name|ALLOCATION_FAILED
operator|)
operator|:
literal|"failedAllocations: "
operator|+
name|failedAllocations
operator|+
literal|" for reason "
operator|+
name|reason
assert|;
assert|assert
operator|!
operator|(
name|message
operator|==
literal|null
operator|&&
name|failure
operator|!=
literal|null
operator|)
operator|:
literal|"provide a message if a failure exception is provided"
assert|;
assert|assert
operator|!
operator|(
name|delayed
operator|&&
name|reason
operator|!=
name|Reason
operator|.
name|NODE_LEFT
operator|)
operator|:
literal|"shard can only be delayed if it is unassigned due to a node leaving"
assert|;
block|}
DECL|method|UnassignedInfo
specifier|public
name|UnassignedInfo
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|reason
operator|=
name|Reason
operator|.
name|values
argument_list|()
index|[
operator|(
name|int
operator|)
name|in
operator|.
name|readByte
argument_list|()
index|]
expr_stmt|;
name|this
operator|.
name|unassignedTimeMillis
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
comment|// As System.nanoTime() cannot be compared across different JVMs, reset it to now.
comment|// This means that in master fail-over situations, elapsed delay time is forgotten.
name|this
operator|.
name|unassignedTimeNanos
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|delayed
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|this
operator|.
name|failure
operator|=
name|in
operator|.
name|readException
argument_list|()
expr_stmt|;
name|this
operator|.
name|failedAllocations
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|lastAllocationStatus
operator|=
name|AllocationStatus
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|reason
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|unassignedTimeMillis
argument_list|)
expr_stmt|;
comment|// Do not serialize unassignedTimeNanos as System.nanoTime() cannot be compared across different JVMs
name|out
operator|.
name|writeBoolean
argument_list|(
name|delayed
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeException
argument_list|(
name|failure
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|failedAllocations
argument_list|)
expr_stmt|;
name|lastAllocationStatus
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
specifier|public
name|UnassignedInfo
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|UnassignedInfo
argument_list|(
name|in
argument_list|)
return|;
block|}
comment|/**      * Returns the number of previously failed allocations of this shard.      */
DECL|method|getNumFailedAllocations
specifier|public
name|int
name|getNumFailedAllocations
parameter_list|()
block|{
return|return
name|failedAllocations
return|;
block|}
comment|/**      * Returns true if allocation of this shard is delayed due to {@link #INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING}      */
DECL|method|isDelayed
specifier|public
name|boolean
name|isDelayed
parameter_list|()
block|{
return|return
name|delayed
return|;
block|}
comment|/**      * The reason why the shard is unassigned.      */
DECL|method|getReason
specifier|public
name|Reason
name|getReason
parameter_list|()
block|{
return|return
name|this
operator|.
name|reason
return|;
block|}
comment|/**      * The timestamp in milliseconds when the shard became unassigned, based on System.currentTimeMillis().      * Note, we use timestamp here since we want to make sure its preserved across node serializations.      */
DECL|method|getUnassignedTimeInMillis
specifier|public
name|long
name|getUnassignedTimeInMillis
parameter_list|()
block|{
return|return
name|this
operator|.
name|unassignedTimeMillis
return|;
block|}
comment|/**      * The timestamp in nanoseconds when the shard became unassigned, based on System.nanoTime().      * Used to calculate the delay for delayed shard allocation.      * ONLY EXPOSED FOR TESTS!      */
DECL|method|getUnassignedTimeInNanos
specifier|public
name|long
name|getUnassignedTimeInNanos
parameter_list|()
block|{
return|return
name|this
operator|.
name|unassignedTimeNanos
return|;
block|}
comment|/**      * Returns optional details explaining the reasons.      */
annotation|@
name|Nullable
DECL|method|getMessage
specifier|public
name|String
name|getMessage
parameter_list|()
block|{
return|return
name|this
operator|.
name|message
return|;
block|}
comment|/**      * Returns additional failure exception details if exists.      */
annotation|@
name|Nullable
DECL|method|getFailure
specifier|public
name|Exception
name|getFailure
parameter_list|()
block|{
return|return
name|failure
return|;
block|}
comment|/**      * Builds a string representation of the message and the failure if exists.      */
annotation|@
name|Nullable
DECL|method|getDetails
specifier|public
name|String
name|getDetails
parameter_list|()
block|{
if|if
condition|(
name|message
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|message
operator|+
operator|(
name|failure
operator|==
literal|null
condition|?
literal|""
else|:
literal|", failure "
operator|+
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|failure
argument_list|)
operator|)
return|;
block|}
comment|/**      * Get the status for the last allocation attempt for this shard.      */
DECL|method|getLastAllocationStatus
specifier|public
name|AllocationStatus
name|getLastAllocationStatus
parameter_list|()
block|{
return|return
name|lastAllocationStatus
return|;
block|}
comment|/**      * Calculates the delay left based on current time (in nanoseconds) and the delay defined by the index settings.      * Only relevant if shard is effectively delayed (see {@link #isDelayed()})      * Returns 0 if delay is negative      *      * @return calculated delay in nanoseconds      */
DECL|method|getRemainingDelay
specifier|public
name|long
name|getRemainingDelay
parameter_list|(
specifier|final
name|long
name|nanoTimeNow
parameter_list|,
specifier|final
name|Settings
name|indexSettings
parameter_list|)
block|{
name|long
name|delayTimeoutNanos
init|=
name|INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
assert|assert
name|nanoTimeNow
operator|>=
name|unassignedTimeNanos
assert|;
return|return
name|Math
operator|.
name|max
argument_list|(
literal|0L
argument_list|,
name|delayTimeoutNanos
operator|-
operator|(
name|nanoTimeNow
operator|-
name|unassignedTimeNanos
operator|)
argument_list|)
return|;
block|}
comment|/**      * Returns the number of shards that are unassigned and currently being delayed.      */
DECL|method|getNumberOfDelayedUnassigned
specifier|public
specifier|static
name|int
name|getNumberOfDelayedUnassigned
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shard
range|:
name|state
operator|.
name|routingTable
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|)
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|isDelayed
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
return|return
name|count
return|;
block|}
comment|/**      * Finds the next (closest) delay expiration of an delayed shard in nanoseconds based on current time.      * Returns 0 if delay is negative.      * Returns -1 if no delayed shard is found.      */
DECL|method|findNextDelayedAllocation
specifier|public
specifier|static
name|long
name|findNextDelayedAllocation
parameter_list|(
name|long
name|currentNanoTime
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|MetaData
name|metaData
init|=
name|state
operator|.
name|metaData
argument_list|()
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|state
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|long
name|nextDelayNanos
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shard
range|:
name|routingTable
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|)
control|)
block|{
name|UnassignedInfo
name|unassignedInfo
init|=
name|shard
operator|.
name|unassignedInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|unassignedInfo
operator|.
name|isDelayed
argument_list|()
condition|)
block|{
name|Settings
name|indexSettings
init|=
name|metaData
operator|.
name|index
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|getSettings
argument_list|()
decl_stmt|;
comment|// calculate next time to schedule
specifier|final
name|long
name|newComputedLeftDelayNanos
init|=
name|unassignedInfo
operator|.
name|getRemainingDelay
argument_list|(
name|currentNanoTime
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
if|if
condition|(
name|newComputedLeftDelayNanos
operator|<
name|nextDelayNanos
condition|)
block|{
name|nextDelayNanos
operator|=
name|newComputedLeftDelayNanos
expr_stmt|;
block|}
block|}
block|}
return|return
name|nextDelayNanos
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|?
operator|-
literal|1L
else|:
name|nextDelayNanos
return|;
block|}
DECL|method|shortSummary
specifier|public
name|String
name|shortSummary
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"[reason="
argument_list|)
operator|.
name|append
argument_list|(
name|reason
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", at["
argument_list|)
operator|.
name|append
argument_list|(
name|DATE_TIME_FORMATTER
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|unassignedTimeMillis
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
if|if
condition|(
name|failedAllocations
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", failed_attempts["
argument_list|)
operator|.
name|append
argument_list|(
name|failedAllocations
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|", delayed="
argument_list|)
operator|.
name|append
argument_list|(
name|delayed
argument_list|)
expr_stmt|;
name|String
name|details
init|=
name|getDetails
argument_list|()
decl_stmt|;
if|if
condition|(
name|details
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", details["
argument_list|)
operator|.
name|append
argument_list|(
name|details
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|", allocation_status["
argument_list|)
operator|.
name|append
argument_list|(
name|lastAllocationStatus
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
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
return|return
literal|"unassigned_info["
operator|+
name|shortSummary
argument_list|()
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"unassigned_info"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"reason"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"at"
argument_list|,
name|DATE_TIME_FORMATTER
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|unassignedTimeMillis
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|failedAllocations
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"failed_attempts"
argument_list|,
name|failedAllocations
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"delayed"
argument_list|,
name|delayed
argument_list|)
expr_stmt|;
name|String
name|details
init|=
name|getDetails
argument_list|()
decl_stmt|;
if|if
condition|(
name|details
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"details"
argument_list|,
name|details
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"allocation_status"
argument_list|,
name|lastAllocationStatus
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
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
block|{
return|return
literal|true
return|;
block|}
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
block|{
return|return
literal|false
return|;
block|}
name|UnassignedInfo
name|that
init|=
operator|(
name|UnassignedInfo
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|unassignedTimeMillis
operator|!=
name|that
operator|.
name|unassignedTimeMillis
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|delayed
operator|!=
name|that
operator|.
name|delayed
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|failedAllocations
operator|!=
name|that
operator|.
name|failedAllocations
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|reason
operator|!=
name|that
operator|.
name|reason
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|message
operator|!=
literal|null
condition|?
operator|!
name|message
operator|.
name|equals
argument_list|(
name|that
operator|.
name|message
argument_list|)
else|:
name|that
operator|.
name|message
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|lastAllocationStatus
operator|!=
name|that
operator|.
name|lastAllocationStatus
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|!
operator|(
name|failure
operator|!=
literal|null
condition|?
operator|!
name|failure
operator|.
name|equals
argument_list|(
name|that
operator|.
name|failure
argument_list|)
else|:
name|that
operator|.
name|failure
operator|!=
literal|null
operator|)
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
name|int
name|result
init|=
name|reason
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Boolean
operator|.
name|hashCode
argument_list|(
name|delayed
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Integer
operator|.
name|hashCode
argument_list|(
name|failedAllocations
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Long
operator|.
name|hashCode
argument_list|(
name|unassignedTimeMillis
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|message
operator|!=
literal|null
condition|?
name|message
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|failure
operator|!=
literal|null
condition|?
name|failure
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|lastAllocationStatus
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

