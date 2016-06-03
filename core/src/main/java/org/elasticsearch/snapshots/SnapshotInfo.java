begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|action
operator|.
name|ShardOperationFailedException
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
name|ParseFieldMatcher
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
name|xcontent
operator|.
name|FromXContentBuilder
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|ArrayList
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
name|List
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
comment|/**  * Information about a snapshot  */
end_comment

begin_class
DECL|class|SnapshotInfo
specifier|public
specifier|final
class|class
name|SnapshotInfo
implements|implements
name|Comparable
argument_list|<
name|SnapshotInfo
argument_list|>
implements|,
name|ToXContent
implements|,
name|FromXContentBuilder
argument_list|<
name|SnapshotInfo
argument_list|>
implements|,
name|Writeable
block|{
DECL|field|PROTO
specifier|public
specifier|static
specifier|final
name|SnapshotInfo
name|PROTO
init|=
operator|new
name|SnapshotInfo
argument_list|(
operator|new
name|SnapshotId
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
DECL|field|CONTEXT_MODE_PARAM
specifier|public
specifier|static
specifier|final
name|String
name|CONTEXT_MODE_PARAM
init|=
literal|"context_mode"
decl_stmt|;
DECL|field|CONTEXT_MODE_SNAPSHOT
specifier|public
specifier|static
specifier|final
name|String
name|CONTEXT_MODE_SNAPSHOT
init|=
literal|"SNAPSHOT"
decl_stmt|;
DECL|field|DATE_TIME_FORMATTER
specifier|private
specifier|static
specifier|final
name|FormatDateTimeFormatter
name|DATE_TIME_FORMATTER
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"strictDateOptionalTime"
argument_list|)
decl_stmt|;
DECL|field|SNAPSHOT
specifier|private
specifier|static
specifier|final
name|String
name|SNAPSHOT
init|=
literal|"snapshot"
decl_stmt|;
DECL|field|UUID
specifier|private
specifier|static
specifier|final
name|String
name|UUID
init|=
literal|"uuid"
decl_stmt|;
DECL|field|INDICES
specifier|private
specifier|static
specifier|final
name|String
name|INDICES
init|=
literal|"indices"
decl_stmt|;
DECL|field|STATE
specifier|private
specifier|static
specifier|final
name|String
name|STATE
init|=
literal|"state"
decl_stmt|;
DECL|field|REASON
specifier|private
specifier|static
specifier|final
name|String
name|REASON
init|=
literal|"reason"
decl_stmt|;
DECL|field|START_TIME
specifier|private
specifier|static
specifier|final
name|String
name|START_TIME
init|=
literal|"start_time"
decl_stmt|;
DECL|field|START_TIME_IN_MILLIS
specifier|private
specifier|static
specifier|final
name|String
name|START_TIME_IN_MILLIS
init|=
literal|"start_time_in_millis"
decl_stmt|;
DECL|field|END_TIME
specifier|private
specifier|static
specifier|final
name|String
name|END_TIME
init|=
literal|"end_time"
decl_stmt|;
DECL|field|END_TIME_IN_MILLIS
specifier|private
specifier|static
specifier|final
name|String
name|END_TIME_IN_MILLIS
init|=
literal|"end_time_in_millis"
decl_stmt|;
DECL|field|DURATION
specifier|private
specifier|static
specifier|final
name|String
name|DURATION
init|=
literal|"duration"
decl_stmt|;
DECL|field|DURATION_IN_MILLIS
specifier|private
specifier|static
specifier|final
name|String
name|DURATION_IN_MILLIS
init|=
literal|"duration_in_millis"
decl_stmt|;
DECL|field|FAILURES
specifier|private
specifier|static
specifier|final
name|String
name|FAILURES
init|=
literal|"failures"
decl_stmt|;
DECL|field|SHARDS
specifier|private
specifier|static
specifier|final
name|String
name|SHARDS
init|=
literal|"shards"
decl_stmt|;
DECL|field|TOTAL
specifier|private
specifier|static
specifier|final
name|String
name|TOTAL
init|=
literal|"total"
decl_stmt|;
DECL|field|FAILED
specifier|private
specifier|static
specifier|final
name|String
name|FAILED
init|=
literal|"failed"
decl_stmt|;
DECL|field|SUCCESSFUL
specifier|private
specifier|static
specifier|final
name|String
name|SUCCESSFUL
init|=
literal|"successful"
decl_stmt|;
DECL|field|VERSION_ID
specifier|private
specifier|static
specifier|final
name|String
name|VERSION_ID
init|=
literal|"version_id"
decl_stmt|;
DECL|field|VERSION
specifier|private
specifier|static
specifier|final
name|String
name|VERSION
init|=
literal|"version"
decl_stmt|;
DECL|field|NAME
specifier|private
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"name"
decl_stmt|;
DECL|field|TOTAL_SHARDS
specifier|private
specifier|static
specifier|final
name|String
name|TOTAL_SHARDS
init|=
literal|"total_shards"
decl_stmt|;
DECL|field|SUCCESSFUL_SHARDS
specifier|private
specifier|static
specifier|final
name|String
name|SUCCESSFUL_SHARDS
init|=
literal|"successful_shards"
decl_stmt|;
DECL|field|snapshotId
specifier|private
specifier|final
name|SnapshotId
name|snapshotId
decl_stmt|;
DECL|field|state
specifier|private
specifier|final
name|SnapshotState
name|state
decl_stmt|;
DECL|field|reason
specifier|private
specifier|final
name|String
name|reason
decl_stmt|;
DECL|field|indices
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|indices
decl_stmt|;
DECL|field|startTime
specifier|private
specifier|final
name|long
name|startTime
decl_stmt|;
DECL|field|endTime
specifier|private
specifier|final
name|long
name|endTime
decl_stmt|;
DECL|field|totalShards
specifier|private
specifier|final
name|int
name|totalShards
decl_stmt|;
DECL|field|successfulShards
specifier|private
specifier|final
name|int
name|successfulShards
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|Version
name|version
decl_stmt|;
DECL|field|shardFailures
specifier|private
specifier|final
name|List
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|shardFailures
decl_stmt|;
DECL|method|SnapshotInfo
specifier|public
name|SnapshotInfo
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|,
name|long
name|startTime
parameter_list|)
block|{
name|this
argument_list|(
name|snapshotId
argument_list|,
name|indices
argument_list|,
name|SnapshotState
operator|.
name|IN_PROGRESS
argument_list|,
literal|null
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|startTime
argument_list|,
literal|0L
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|SnapshotInfo
specifier|public
name|SnapshotInfo
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|,
name|long
name|startTime
parameter_list|,
name|String
name|reason
parameter_list|,
name|long
name|endTime
parameter_list|,
name|int
name|totalShards
parameter_list|,
name|List
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|shardFailures
parameter_list|)
block|{
name|this
argument_list|(
name|snapshotId
argument_list|,
name|indices
argument_list|,
name|snapshotState
argument_list|(
name|reason
argument_list|,
name|shardFailures
argument_list|)
argument_list|,
name|reason
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|startTime
argument_list|,
name|endTime
argument_list|,
name|totalShards
argument_list|,
name|totalShards
operator|-
name|shardFailures
operator|.
name|size
argument_list|()
argument_list|,
name|shardFailures
argument_list|)
expr_stmt|;
block|}
DECL|method|SnapshotInfo
specifier|private
name|SnapshotInfo
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|,
name|SnapshotState
name|state
parameter_list|,
name|String
name|reason
parameter_list|,
name|Version
name|version
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|endTime
parameter_list|,
name|int
name|totalShards
parameter_list|,
name|int
name|successfulShards
parameter_list|,
name|List
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|shardFailures
parameter_list|)
block|{
name|this
operator|.
name|snapshotId
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|snapshotId
argument_list|)
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|endTime
operator|=
name|endTime
expr_stmt|;
name|this
operator|.
name|totalShards
operator|=
name|totalShards
expr_stmt|;
name|this
operator|.
name|successfulShards
operator|=
name|successfulShards
expr_stmt|;
name|this
operator|.
name|shardFailures
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|shardFailures
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs snapshot information from stream input      */
DECL|method|SnapshotInfo
specifier|public
name|SnapshotInfo
parameter_list|(
specifier|final
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|snapshotId
operator|=
operator|new
name|SnapshotId
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
name|List
argument_list|<
name|String
argument_list|>
name|indicesListBuilder
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|indicesListBuilder
operator|.
name|add
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indices
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|indicesListBuilder
argument_list|)
expr_stmt|;
name|state
operator|=
name|SnapshotState
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|reason
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|startTime
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|endTime
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|totalShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|successfulShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|List
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|failureBuilder
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|failureBuilder
operator|.
name|add
argument_list|(
name|SnapshotShardFailure
operator|.
name|readSnapshotShardFailure
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|shardFailures
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|failureBuilder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shardFailures
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
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
comment|/**      * Returns snapshot id      *      * @return snapshot id      */
DECL|method|snapshotId
specifier|public
name|SnapshotId
name|snapshotId
parameter_list|()
block|{
return|return
name|snapshotId
return|;
block|}
comment|/**      * Returns snapshot state      *      * @return snapshot state      */
DECL|method|state
specifier|public
name|SnapshotState
name|state
parameter_list|()
block|{
return|return
name|state
return|;
block|}
comment|/**      * Returns snapshot failure reason      *      * @return snapshot failure reason      */
DECL|method|reason
specifier|public
name|String
name|reason
parameter_list|()
block|{
return|return
name|reason
return|;
block|}
comment|/**      * Returns indices that were included into this snapshot      *      * @return list of indices      */
DECL|method|indices
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
comment|/**      * Returns time when snapshot started      *      * @return snapshot start time      */
DECL|method|startTime
specifier|public
name|long
name|startTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
comment|/**      * Returns time when snapshot ended      *<p>      * Can be 0L if snapshot is still running      *      * @return snapshot end time      */
DECL|method|endTime
specifier|public
name|long
name|endTime
parameter_list|()
block|{
return|return
name|endTime
return|;
block|}
comment|/**      * Returns total number of shards that were snapshotted      *      * @return number of shards      */
DECL|method|totalShards
specifier|public
name|int
name|totalShards
parameter_list|()
block|{
return|return
name|totalShards
return|;
block|}
comment|/**      * Number of failed shards      *      * @return number of failed shards      */
DECL|method|failedShards
specifier|public
name|int
name|failedShards
parameter_list|()
block|{
return|return
name|totalShards
operator|-
name|successfulShards
return|;
block|}
comment|/**      * Returns total number of shards that were successfully snapshotted      *      * @return number of successful shards      */
DECL|method|successfulShards
specifier|public
name|int
name|successfulShards
parameter_list|()
block|{
return|return
name|successfulShards
return|;
block|}
comment|/**      * Returns shard failures      *      * @return shard failures      */
DECL|method|shardFailures
specifier|public
name|List
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|shardFailures
parameter_list|()
block|{
return|return
name|shardFailures
return|;
block|}
comment|/**      * Returns the version of elasticsearch that the snapshot was created with      *      * @return version of elasticsearch that the snapshot was created with      */
DECL|method|version
specifier|public
name|Version
name|version
parameter_list|()
block|{
return|return
name|version
return|;
block|}
comment|/**      * Compares two snapshots by their start time      *      * @param o other snapshot      * @return the value {@code 0} if snapshots were created at the same time;      * a value less than {@code 0} if this snapshot was created before snapshot {@code o}; and      * a value greater than {@code 0} if this snapshot was created after snapshot {@code o};      */
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|SnapshotInfo
name|o
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
name|startTime
argument_list|,
name|o
operator|.
name|startTime
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
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
specifier|final
name|SnapshotInfo
name|that
init|=
operator|(
name|SnapshotInfo
operator|)
name|o
decl_stmt|;
return|return
name|startTime
operator|==
name|that
operator|.
name|startTime
operator|&&
name|snapshotId
operator|.
name|equals
argument_list|(
name|that
operator|.
name|snapshotId
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
name|int
name|result
init|=
name|snapshotId
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
name|Long
operator|.
name|hashCode
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
return|return
name|result
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
literal|"SnapshotInfo[snapshotId="
operator|+
name|snapshotId
operator|+
literal|", state="
operator|+
name|state
operator|+
literal|", indices="
operator|+
name|indices
operator|+
literal|"]"
return|;
block|}
comment|/**      * Returns snapshot REST status      */
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
if|if
condition|(
name|state
operator|==
name|SnapshotState
operator|.
name|FAILED
condition|)
block|{
return|return
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
return|;
block|}
if|if
condition|(
name|shardFailures
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|RestStatus
operator|.
name|OK
return|;
block|}
return|return
name|RestStatus
operator|.
name|status
argument_list|(
name|successfulShards
argument_list|,
name|totalShards
argument_list|,
name|shardFailures
operator|.
name|toArray
argument_list|(
operator|new
name|ShardOperationFailedException
index|[
name|shardFailures
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
specifier|final
name|XContentBuilder
name|builder
parameter_list|,
specifier|final
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
comment|// write snapshot info to repository snapshot blob format
if|if
condition|(
name|CONTEXT_MODE_SNAPSHOT
operator|.
name|equals
argument_list|(
name|params
operator|.
name|param
argument_list|(
name|CONTEXT_MODE_PARAM
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|toXContentSnapshot
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
return|;
block|}
comment|// write snapshot info for the API and any other situations
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|SNAPSHOT
argument_list|,
name|snapshotId
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|UUID
argument_list|,
name|snapshotId
operator|.
name|getUUID
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|VERSION_ID
argument_list|,
name|version
operator|.
name|id
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|VERSION
argument_list|,
name|version
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|INDICES
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|STATE
argument_list|,
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|REASON
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|startTime
operator|!=
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|START_TIME
argument_list|,
name|DATE_TIME_FORMATTER
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|START_TIME_IN_MILLIS
argument_list|,
name|startTime
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|endTime
operator|!=
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|END_TIME
argument_list|,
name|DATE_TIME_FORMATTER
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|endTime
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|END_TIME_IN_MILLIS
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|DURATION_IN_MILLIS
argument_list|,
name|DURATION
argument_list|,
name|endTime
operator|-
name|startTime
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|startArray
argument_list|(
name|FAILURES
argument_list|)
expr_stmt|;
for|for
control|(
name|SnapshotShardFailure
name|shardFailure
range|:
name|shardFailures
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|shardFailure
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|SHARDS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|TOTAL
argument_list|,
name|totalShards
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FAILED
argument_list|,
name|failedShards
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|SUCCESSFUL
argument_list|,
name|successfulShards
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
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
DECL|method|toXContentSnapshot
specifier|private
name|XContentBuilder
name|toXContentSnapshot
parameter_list|(
specifier|final
name|XContentBuilder
name|builder
parameter_list|,
specifier|final
name|ToXContent
operator|.
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
name|SNAPSHOT
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|NAME
argument_list|,
name|snapshotId
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|UUID
argument_list|,
name|snapshotId
operator|.
name|getUUID
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|VERSION_ID
argument_list|,
name|version
operator|.
name|id
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|INDICES
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|STATE
argument_list|,
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|REASON
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|START_TIME
argument_list|,
name|startTime
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|END_TIME
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|TOTAL_SHARDS
argument_list|,
name|totalShards
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|SUCCESSFUL_SHARDS
argument_list|,
name|successfulShards
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|FAILURES
argument_list|)
expr_stmt|;
for|for
control|(
name|SnapshotShardFailure
name|shardFailure
range|:
name|shardFailures
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|shardFailure
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
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
DECL|method|fromXContent
specifier|public
name|SnapshotInfo
name|fromXContent
parameter_list|(
specifier|final
name|XContentParser
name|parser
parameter_list|,
specifier|final
name|ParseFieldMatcher
name|matcher
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromXContent
argument_list|(
name|parser
argument_list|)
return|;
block|}
comment|/**      * This method creates a SnapshotInfo from internal x-content.  It does not      * handle x-content written with the external version as external x-content      * is only for display purposes and does not need to be parsed.      */
DECL|method|fromXContent
specifier|public
specifier|static
name|SnapshotInfo
name|fromXContent
parameter_list|(
specifier|final
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|name
init|=
literal|null
decl_stmt|;
name|String
name|uuid
init|=
literal|null
decl_stmt|;
name|Version
name|version
init|=
name|Version
operator|.
name|CURRENT
decl_stmt|;
name|SnapshotState
name|state
init|=
name|SnapshotState
operator|.
name|IN_PROGRESS
decl_stmt|;
name|String
name|reason
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
name|long
name|startTime
init|=
literal|0
decl_stmt|;
name|long
name|endTime
init|=
literal|0
decl_stmt|;
name|int
name|totalShards
init|=
literal|0
decl_stmt|;
name|int
name|successfulShards
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|shardFailures
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// fresh parser? move to the first token
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
comment|// on a start object move to next token
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
if|if
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|String
name|currentFieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
if|if
condition|(
name|SNAPSHOT
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|NAME
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|name
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|UUID
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|uuid
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|STATE
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|state
operator|=
name|SnapshotState
operator|.
name|valueOf
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|REASON
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|reason
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|START_TIME
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|startTime
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|END_TIME
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|endTime
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|TOTAL_SHARDS
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|totalShards
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|SUCCESSFUL_SHARDS
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|successfulShards
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|VERSION_ID
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|version
operator|=
name|Version
operator|.
name|fromId
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
name|INDICES
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|indicesArray
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|indicesArray
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indices
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|indicesArray
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|FAILURES
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|ArrayList
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|shardFailureArrayList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|shardFailureArrayList
operator|.
name|add
argument_list|(
name|SnapshotShardFailure
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|shardFailures
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|shardFailureArrayList
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// It was probably created by newer version - ignoring
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
comment|// It was probably created by newer version - ignoring
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unexpected token  ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|uuid
operator|==
literal|null
condition|)
block|{
comment|// the old format where there wasn't a UUID
name|uuid
operator|=
name|SnapshotId
operator|.
name|UNASSIGNED_UUID
expr_stmt|;
block|}
return|return
operator|new
name|SnapshotInfo
argument_list|(
operator|new
name|SnapshotId
argument_list|(
name|name
argument_list|,
name|uuid
argument_list|)
argument_list|,
name|indices
argument_list|,
name|state
argument_list|,
name|reason
argument_list|,
name|version
argument_list|,
name|startTime
argument_list|,
name|endTime
argument_list|,
name|totalShards
argument_list|,
name|successfulShards
argument_list|,
name|shardFailures
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
specifier|final
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|snapshotId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indices
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeByte
argument_list|(
name|state
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|reason
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|endTime
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|totalShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|successfulShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|shardFailures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SnapshotShardFailure
name|failure
range|:
name|shardFailures
control|)
block|{
name|failure
operator|.
name|writeTo
argument_list|(
name|out
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
DECL|method|snapshotState
specifier|private
specifier|static
name|SnapshotState
name|snapshotState
parameter_list|(
specifier|final
name|String
name|reason
parameter_list|,
specifier|final
name|List
argument_list|<
name|SnapshotShardFailure
argument_list|>
name|shardFailures
parameter_list|)
block|{
if|if
condition|(
name|reason
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|shardFailures
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|SnapshotState
operator|.
name|SUCCESS
return|;
block|}
else|else
block|{
return|return
name|SnapshotState
operator|.
name|PARTIAL
return|;
block|}
block|}
else|else
block|{
return|return
name|SnapshotState
operator|.
name|FAILED
return|;
block|}
block|}
block|}
end_class

end_unit

