begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|gateway
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|CloseableIndexComponent
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
name|SizeValue
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
name|index
operator|.
name|deletionpolicy
operator|.
name|SnapshotIndexCommit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|IndexShardComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
operator|.
name|Translog
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
name|unit
operator|.
name|TimeValue
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_interface
DECL|interface|IndexShardGateway
specifier|public
interface|interface
name|IndexShardGateway
extends|extends
name|IndexShardComponent
extends|,
name|CloseableIndexComponent
block|{
comment|/**      * Recovers the state of the shard from the gateway.      */
DECL|method|recover
name|RecoveryStatus
name|recover
parameter_list|()
throws|throws
name|IndexShardGatewayRecoveryException
function_decl|;
comment|/**      * Snapshots the given shard into the gateway.      */
DECL|method|snapshot
name|SnapshotStatus
name|snapshot
parameter_list|(
name|Snapshot
name|snapshot
parameter_list|)
function_decl|;
comment|/**      * Returns<tt>true</tt> if this gateway requires scheduling management for snapshot      * operations.      */
DECL|method|requiresSnapshotScheduling
name|boolean
name|requiresSnapshotScheduling
parameter_list|()
function_decl|;
DECL|class|Snapshot
specifier|public
specifier|static
class|class
name|Snapshot
block|{
DECL|field|indexCommit
specifier|private
specifier|final
name|SnapshotIndexCommit
name|indexCommit
decl_stmt|;
DECL|field|translogSnapshot
specifier|private
specifier|final
name|Translog
operator|.
name|Snapshot
name|translogSnapshot
decl_stmt|;
DECL|field|lastIndexVersion
specifier|private
specifier|final
name|long
name|lastIndexVersion
decl_stmt|;
DECL|field|lastTranslogId
specifier|private
specifier|final
name|long
name|lastTranslogId
decl_stmt|;
DECL|field|lastTranslogSize
specifier|private
specifier|final
name|int
name|lastTranslogSize
decl_stmt|;
DECL|method|Snapshot
specifier|public
name|Snapshot
parameter_list|(
name|SnapshotIndexCommit
name|indexCommit
parameter_list|,
name|Translog
operator|.
name|Snapshot
name|translogSnapshot
parameter_list|,
name|long
name|lastIndexVersion
parameter_list|,
name|long
name|lastTranslogId
parameter_list|,
name|int
name|lastTranslogSize
parameter_list|)
block|{
name|this
operator|.
name|indexCommit
operator|=
name|indexCommit
expr_stmt|;
name|this
operator|.
name|translogSnapshot
operator|=
name|translogSnapshot
expr_stmt|;
name|this
operator|.
name|lastIndexVersion
operator|=
name|lastIndexVersion
expr_stmt|;
name|this
operator|.
name|lastTranslogId
operator|=
name|lastTranslogId
expr_stmt|;
name|this
operator|.
name|lastTranslogSize
operator|=
name|lastTranslogSize
expr_stmt|;
block|}
comment|/**          * Indicates that the index has changed from the latest snapshot.          */
DECL|method|indexChanged
specifier|public
name|boolean
name|indexChanged
parameter_list|()
block|{
return|return
name|lastIndexVersion
operator|!=
name|indexCommit
operator|.
name|getVersion
argument_list|()
return|;
block|}
comment|/**          * Indicates that a new transaction log has been created. Note check this<b>before</b> you          * check {@link #sameTranslogNewOperations()}.          */
DECL|method|newTranslogCreated
specifier|public
name|boolean
name|newTranslogCreated
parameter_list|()
block|{
return|return
name|translogSnapshot
operator|.
name|translogId
argument_list|()
operator|!=
name|lastTranslogId
return|;
block|}
comment|/**          * Indicates that the same translog exists, but new operations have been appended to it. Throws          * {@link ElasticSearchIllegalStateException} if {@link #newTranslogCreated()} is<tt>true</tt>, so          * always check that first.          */
DECL|method|sameTranslogNewOperations
specifier|public
name|boolean
name|sameTranslogNewOperations
parameter_list|()
block|{
if|if
condition|(
name|newTranslogCreated
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Should not be called when there is a new translog"
argument_list|)
throw|;
block|}
return|return
name|translogSnapshot
operator|.
name|size
argument_list|()
operator|>
name|lastTranslogSize
return|;
block|}
DECL|method|indexCommit
specifier|public
name|SnapshotIndexCommit
name|indexCommit
parameter_list|()
block|{
return|return
name|indexCommit
return|;
block|}
DECL|method|translogSnapshot
specifier|public
name|Translog
operator|.
name|Snapshot
name|translogSnapshot
parameter_list|()
block|{
return|return
name|translogSnapshot
return|;
block|}
DECL|method|lastIndexVersion
specifier|public
name|long
name|lastIndexVersion
parameter_list|()
block|{
return|return
name|lastIndexVersion
return|;
block|}
DECL|method|lastTranslogId
specifier|public
name|long
name|lastTranslogId
parameter_list|()
block|{
return|return
name|lastTranslogId
return|;
block|}
DECL|method|lastTranslogSize
specifier|public
name|int
name|lastTranslogSize
parameter_list|()
block|{
return|return
name|lastTranslogSize
return|;
block|}
block|}
DECL|class|SnapshotStatus
class|class
name|SnapshotStatus
block|{
DECL|field|NA
specifier|public
specifier|static
name|SnapshotStatus
name|NA
init|=
operator|new
name|SnapshotStatus
argument_list|(
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|Index
argument_list|(
literal|0
argument_list|,
operator|new
name|SizeValue
argument_list|(
literal|0
argument_list|)
argument_list|,
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
operator|new
name|Translog
argument_list|(
literal|0
argument_list|,
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|totalTime
specifier|private
name|TimeValue
name|totalTime
decl_stmt|;
DECL|field|index
specifier|private
name|Index
name|index
decl_stmt|;
DECL|field|translog
specifier|private
name|Translog
name|translog
decl_stmt|;
DECL|method|SnapshotStatus
specifier|public
name|SnapshotStatus
parameter_list|(
name|TimeValue
name|totalTime
parameter_list|,
name|Index
name|index
parameter_list|,
name|Translog
name|translog
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|translog
operator|=
name|translog
expr_stmt|;
name|this
operator|.
name|totalTime
operator|=
name|totalTime
expr_stmt|;
block|}
DECL|method|totalTime
specifier|public
name|TimeValue
name|totalTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalTime
return|;
block|}
DECL|method|index
specifier|public
name|Index
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|translog
specifier|public
name|Translog
name|translog
parameter_list|()
block|{
return|return
name|translog
return|;
block|}
DECL|class|Translog
specifier|public
specifier|static
class|class
name|Translog
block|{
DECL|field|numberOfOperations
specifier|private
name|int
name|numberOfOperations
decl_stmt|;
DECL|field|time
specifier|private
name|TimeValue
name|time
decl_stmt|;
DECL|method|Translog
specifier|public
name|Translog
parameter_list|(
name|int
name|numberOfOperations
parameter_list|,
name|TimeValue
name|time
parameter_list|)
block|{
name|this
operator|.
name|numberOfOperations
operator|=
name|numberOfOperations
expr_stmt|;
name|this
operator|.
name|time
operator|=
name|time
expr_stmt|;
block|}
DECL|method|numberOfOperations
specifier|public
name|int
name|numberOfOperations
parameter_list|()
block|{
return|return
name|numberOfOperations
return|;
block|}
DECL|method|time
specifier|public
name|TimeValue
name|time
parameter_list|()
block|{
return|return
name|time
return|;
block|}
block|}
DECL|class|Index
specifier|public
specifier|static
class|class
name|Index
block|{
DECL|field|numberOfFiles
specifier|private
name|int
name|numberOfFiles
decl_stmt|;
DECL|field|totalSize
specifier|private
name|SizeValue
name|totalSize
decl_stmt|;
DECL|field|time
specifier|private
name|TimeValue
name|time
decl_stmt|;
DECL|method|Index
specifier|public
name|Index
parameter_list|(
name|int
name|numberOfFiles
parameter_list|,
name|SizeValue
name|totalSize
parameter_list|,
name|TimeValue
name|time
parameter_list|)
block|{
name|this
operator|.
name|numberOfFiles
operator|=
name|numberOfFiles
expr_stmt|;
name|this
operator|.
name|totalSize
operator|=
name|totalSize
expr_stmt|;
name|this
operator|.
name|time
operator|=
name|time
expr_stmt|;
block|}
DECL|method|time
specifier|public
name|TimeValue
name|time
parameter_list|()
block|{
return|return
name|this
operator|.
name|time
return|;
block|}
DECL|method|numberOfFiles
specifier|public
name|int
name|numberOfFiles
parameter_list|()
block|{
return|return
name|numberOfFiles
return|;
block|}
DECL|method|totalSize
specifier|public
name|SizeValue
name|totalSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
block|}
block|}
DECL|class|RecoveryStatus
class|class
name|RecoveryStatus
block|{
DECL|field|index
specifier|private
name|Index
name|index
decl_stmt|;
DECL|field|translog
specifier|private
name|Translog
name|translog
decl_stmt|;
DECL|method|RecoveryStatus
specifier|public
name|RecoveryStatus
parameter_list|(
name|Index
name|index
parameter_list|,
name|Translog
name|translog
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|translog
operator|=
name|translog
expr_stmt|;
block|}
DECL|method|index
specifier|public
name|Index
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|translog
specifier|public
name|Translog
name|translog
parameter_list|()
block|{
return|return
name|translog
return|;
block|}
DECL|class|Translog
specifier|public
specifier|static
class|class
name|Translog
block|{
DECL|field|translogId
specifier|private
name|long
name|translogId
decl_stmt|;
DECL|field|numberOfOperations
specifier|private
name|int
name|numberOfOperations
decl_stmt|;
DECL|field|totalSize
specifier|private
name|SizeValue
name|totalSize
decl_stmt|;
DECL|method|Translog
specifier|public
name|Translog
parameter_list|(
name|long
name|translogId
parameter_list|,
name|int
name|numberOfOperations
parameter_list|,
name|SizeValue
name|totalSize
parameter_list|)
block|{
name|this
operator|.
name|translogId
operator|=
name|translogId
expr_stmt|;
name|this
operator|.
name|numberOfOperations
operator|=
name|numberOfOperations
expr_stmt|;
name|this
operator|.
name|totalSize
operator|=
name|totalSize
expr_stmt|;
block|}
comment|/**              * The translog id recovered,<tt>-1</tt> indicating no translog.              */
DECL|method|translogId
specifier|public
name|long
name|translogId
parameter_list|()
block|{
return|return
name|translogId
return|;
block|}
DECL|method|numberOfOperations
specifier|public
name|int
name|numberOfOperations
parameter_list|()
block|{
return|return
name|numberOfOperations
return|;
block|}
DECL|method|totalSize
specifier|public
name|SizeValue
name|totalSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
block|}
DECL|class|Index
specifier|public
specifier|static
class|class
name|Index
block|{
DECL|field|version
specifier|private
name|long
name|version
decl_stmt|;
DECL|field|numberOfFiles
specifier|private
name|int
name|numberOfFiles
decl_stmt|;
DECL|field|totalSize
specifier|private
name|SizeValue
name|totalSize
decl_stmt|;
DECL|field|throttlingWaitTime
specifier|private
name|TimeValue
name|throttlingWaitTime
decl_stmt|;
DECL|method|Index
specifier|public
name|Index
parameter_list|(
name|long
name|version
parameter_list|,
name|int
name|numberOfFiles
parameter_list|,
name|SizeValue
name|totalSize
parameter_list|,
name|TimeValue
name|throttlingWaitTime
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|numberOfFiles
operator|=
name|numberOfFiles
expr_stmt|;
name|this
operator|.
name|totalSize
operator|=
name|totalSize
expr_stmt|;
name|this
operator|.
name|throttlingWaitTime
operator|=
name|throttlingWaitTime
expr_stmt|;
block|}
DECL|method|version
specifier|public
name|long
name|version
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
DECL|method|numberOfFiles
specifier|public
name|int
name|numberOfFiles
parameter_list|()
block|{
return|return
name|numberOfFiles
return|;
block|}
DECL|method|totalSize
specifier|public
name|SizeValue
name|totalSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
DECL|method|throttlingWaitTime
specifier|public
name|TimeValue
name|throttlingWaitTime
parameter_list|()
block|{
return|return
name|throttlingWaitTime
return|;
block|}
block|}
block|}
block|}
end_interface

end_unit

