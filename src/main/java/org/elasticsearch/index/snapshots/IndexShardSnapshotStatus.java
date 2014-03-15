begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|snapshots
package|;
end_package

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
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * Represent shard snapshot status  */
end_comment

begin_class
DECL|class|IndexShardSnapshotStatus
specifier|public
class|class
name|IndexShardSnapshotStatus
block|{
comment|/**      * Snapshot stage      */
DECL|enum|Stage
specifier|public
specifier|static
enum|enum
name|Stage
block|{
comment|/**          * Snapshot hasn't started yet          */
DECL|enum constant|INIT
name|INIT
block|,
comment|/**          * Index files are being copied          */
DECL|enum constant|STARTED
name|STARTED
block|,
comment|/**          * Snapshot metadata is being written          */
DECL|enum constant|FINALIZE
name|FINALIZE
block|,
comment|/**          * Snapshot completed successfully          */
DECL|enum constant|DONE
name|DONE
block|,
comment|/**          * Snapshot failed          */
DECL|enum constant|FAILURE
name|FAILURE
block|}
DECL|field|stage
specifier|private
name|Stage
name|stage
init|=
name|Stage
operator|.
name|INIT
decl_stmt|;
DECL|field|startTime
specifier|private
name|long
name|startTime
decl_stmt|;
DECL|field|time
specifier|private
name|long
name|time
decl_stmt|;
DECL|field|numberOfFiles
specifier|private
name|int
name|numberOfFiles
decl_stmt|;
DECL|field|processedFiles
specifier|private
specifier|volatile
name|int
name|processedFiles
decl_stmt|;
DECL|field|totalSize
specifier|private
name|long
name|totalSize
decl_stmt|;
DECL|field|processedSize
specifier|private
specifier|volatile
name|long
name|processedSize
decl_stmt|;
DECL|field|indexVersion
specifier|private
name|long
name|indexVersion
decl_stmt|;
DECL|field|aborted
specifier|private
name|boolean
name|aborted
decl_stmt|;
DECL|field|failure
specifier|private
name|String
name|failure
decl_stmt|;
comment|/**      * Returns current snapshot stage      *      * @return current snapshot stage      */
DECL|method|stage
specifier|public
name|Stage
name|stage
parameter_list|()
block|{
return|return
name|this
operator|.
name|stage
return|;
block|}
comment|/**      * Sets new snapshot stage      *      * @param stage new snapshot stage      */
DECL|method|updateStage
specifier|public
name|void
name|updateStage
parameter_list|(
name|Stage
name|stage
parameter_list|)
block|{
name|this
operator|.
name|stage
operator|=
name|stage
expr_stmt|;
block|}
comment|/**      * Returns snapshot start time      *      * @return snapshot start time      */
DECL|method|startTime
specifier|public
name|long
name|startTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|startTime
return|;
block|}
comment|/**      * Sets snapshot start time      *      * @param startTime snapshot start time      */
DECL|method|startTime
specifier|public
name|void
name|startTime
parameter_list|(
name|long
name|startTime
parameter_list|)
block|{
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
block|}
comment|/**      * Returns snapshot processing time      *      * @return processing time      */
DECL|method|time
specifier|public
name|long
name|time
parameter_list|()
block|{
return|return
name|this
operator|.
name|time
return|;
block|}
comment|/**      * Sets snapshot processing time      *      * @param time snapshot processing time      */
DECL|method|time
specifier|public
name|void
name|time
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|this
operator|.
name|time
operator|=
name|time
expr_stmt|;
block|}
comment|/**      * Returns true if snapshot process was aborted      *      * @return true if snapshot process was aborted      */
DECL|method|aborted
specifier|public
name|boolean
name|aborted
parameter_list|()
block|{
return|return
name|this
operator|.
name|aborted
return|;
block|}
comment|/**      * Marks snapshot as aborted      */
DECL|method|abort
specifier|public
name|void
name|abort
parameter_list|()
block|{
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
block|}
comment|/**      * Sets files stats      *      * @param numberOfFiles number of files in this snapshot      * @param totalSize     total size of files in this snapshot      */
DECL|method|files
specifier|public
name|void
name|files
parameter_list|(
name|int
name|numberOfFiles
parameter_list|,
name|long
name|totalSize
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
block|}
comment|/**      * Sets processed files stats      *      * @param numberOfFiles number of files in this snapshot      * @param totalSize     total size of files in this snapshot      */
DECL|method|processedFiles
specifier|public
specifier|synchronized
name|void
name|processedFiles
parameter_list|(
name|int
name|numberOfFiles
parameter_list|,
name|long
name|totalSize
parameter_list|)
block|{
name|processedFiles
operator|=
name|numberOfFiles
expr_stmt|;
name|processedSize
operator|=
name|totalSize
expr_stmt|;
block|}
comment|/**      * Increments number of processed files      */
DECL|method|addProcessedFile
specifier|public
specifier|synchronized
name|void
name|addProcessedFile
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|processedFiles
operator|++
expr_stmt|;
name|processedSize
operator|+=
name|size
expr_stmt|;
block|}
comment|/**      * Number of files      *      * @return number of files      */
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
comment|/**      * Total snapshot size      *      * @return snapshot size      */
DECL|method|totalSize
specifier|public
name|long
name|totalSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
comment|/**      * Number of processed files      *      * @return number of processed files      */
DECL|method|processedFiles
specifier|public
name|int
name|processedFiles
parameter_list|()
block|{
return|return
name|processedFiles
return|;
block|}
comment|/**      * Size of processed files      *      * @return size of processed files      */
DECL|method|processedSize
specifier|public
name|long
name|processedSize
parameter_list|()
block|{
return|return
name|processedSize
return|;
block|}
comment|/**      * Sets index version      *      * @param indexVersion index version      */
DECL|method|indexVersion
specifier|public
name|void
name|indexVersion
parameter_list|(
name|long
name|indexVersion
parameter_list|)
block|{
name|this
operator|.
name|indexVersion
operator|=
name|indexVersion
expr_stmt|;
block|}
comment|/**      * Returns index version      *      * @return index version      */
DECL|method|indexVersion
specifier|public
name|long
name|indexVersion
parameter_list|()
block|{
return|return
name|indexVersion
return|;
block|}
comment|/**      * Sets the reason for the failure if the snapshot is in the {@link IndexShardSnapshotStatus.Stage#FAILURE} state      */
DECL|method|failure
specifier|public
name|void
name|failure
parameter_list|(
name|String
name|failure
parameter_list|)
block|{
name|this
operator|.
name|failure
operator|=
name|failure
expr_stmt|;
block|}
comment|/**      * Returns the reason for the failure if the snapshot is in the {@link IndexShardSnapshotStatus.Stage#FAILURE} state      */
DECL|method|failure
specifier|public
name|String
name|failure
parameter_list|()
block|{
return|return
name|failure
return|;
block|}
block|}
end_class

end_unit

