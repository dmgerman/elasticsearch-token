begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
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
name|bytes
operator|.
name|BytesReference
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
name|store
operator|.
name|Store
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
name|store
operator|.
name|StoreFileMetaData
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
name|List
import|;
end_import

begin_interface
DECL|interface|RecoveryTargetHandler
specifier|public
interface|interface
name|RecoveryTargetHandler
block|{
comment|/**      * Prepares the tranget to receive translog operations, after all file have been copied      *      * @param totalTranslogOps total translog operations expected to be sent      * @param maxUnsafeAutoIdTimestamp the max timestamp that is used to de-optimize documents with auto-generated IDs in the engine.      * This is used to ensure we don't add duplicate documents when we assume an append only case based on auto-generated IDs      */
DECL|method|prepareForTranslogOperations
name|void
name|prepareForTranslogOperations
parameter_list|(
name|int
name|totalTranslogOps
parameter_list|,
name|long
name|maxUnsafeAutoIdTimestamp
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * The finalize request refreshes the engine now that new segments are available, enables garbage collection of tombstone files, and      * updates the global checkpoint.      *      * @param globalCheckpoint the global checkpoint on the recovery source      */
DECL|method|finalizeRecovery
name|void
name|finalizeRecovery
parameter_list|(
name|long
name|globalCheckpoint
parameter_list|)
function_decl|;
comment|/**      * Blockingly waits for cluster state with at least clusterStateVersion to be available      */
DECL|method|ensureClusterStateVersion
name|void
name|ensureClusterStateVersion
parameter_list|(
name|long
name|clusterStateVersion
parameter_list|)
function_decl|;
comment|/**      * Index a set of translog operations on the target      * @param operations operations to index      * @param totalTranslogOps current number of total operations expected to be indexed      */
DECL|method|indexTranslogOperations
name|void
name|indexTranslogOperations
parameter_list|(
name|List
argument_list|<
name|Translog
operator|.
name|Operation
argument_list|>
name|operations
parameter_list|,
name|int
name|totalTranslogOps
parameter_list|)
function_decl|;
comment|/**      * Notifies the target of the files it is going to receive      */
DECL|method|receiveFileInfo
name|void
name|receiveFileInfo
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|phase1FileNames
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|phase1FileSizes
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|phase1ExistingFileNames
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|phase1ExistingFileSizes
parameter_list|,
name|int
name|totalTranslogOps
parameter_list|)
function_decl|;
comment|/**      * After all source files has been sent over, this command is sent to the target so it can clean any local      * files that are not part of the source store      * @param totalTranslogOps an update number of translog operations that will be replayed later on      * @param sourceMetaData meta data of the source store      */
DECL|method|cleanFiles
name|void
name|cleanFiles
parameter_list|(
name|int
name|totalTranslogOps
parameter_list|,
name|Store
operator|.
name|MetadataSnapshot
name|sourceMetaData
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** writes a partial file chunk to the target store */
DECL|method|writeFileChunk
name|void
name|writeFileChunk
parameter_list|(
name|StoreFileMetaData
name|fileMetaData
parameter_list|,
name|long
name|position
parameter_list|,
name|BytesReference
name|content
parameter_list|,
name|boolean
name|lastChunk
parameter_list|,
name|int
name|totalTranslogOps
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/***      * @return the allocation id of the target shard.      */
DECL|method|getTargetAllocationId
name|String
name|getTargetAllocationId
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

