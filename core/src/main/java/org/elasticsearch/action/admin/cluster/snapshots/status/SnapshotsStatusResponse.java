begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.snapshots.status
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|snapshots
operator|.
name|status
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionResponse
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
name|xcontent
operator|.
name|ToXContentObject
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

begin_comment
comment|/**  * Snapshot status response  */
end_comment

begin_class
DECL|class|SnapshotsStatusResponse
specifier|public
class|class
name|SnapshotsStatusResponse
extends|extends
name|ActionResponse
implements|implements
name|ToXContentObject
block|{
DECL|field|snapshots
specifier|private
name|List
argument_list|<
name|SnapshotStatus
argument_list|>
name|snapshots
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
DECL|method|SnapshotsStatusResponse
name|SnapshotsStatusResponse
parameter_list|()
block|{     }
DECL|method|SnapshotsStatusResponse
name|SnapshotsStatusResponse
parameter_list|(
name|List
argument_list|<
name|SnapshotStatus
argument_list|>
name|snapshots
parameter_list|)
block|{
name|this
operator|.
name|snapshots
operator|=
name|snapshots
expr_stmt|;
block|}
comment|/**      * Returns the list of snapshots      *      * @return the list of snapshots      */
DECL|method|getSnapshots
specifier|public
name|List
argument_list|<
name|SnapshotStatus
argument_list|>
name|getSnapshots
parameter_list|()
block|{
return|return
name|snapshots
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
name|super
operator|.
name|readFrom
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
name|SnapshotStatus
argument_list|>
name|builder
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
name|builder
operator|.
name|add
argument_list|(
name|SnapshotStatus
operator|.
name|readSnapshotStatus
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|snapshots
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|builder
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
name|super
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
name|snapshots
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SnapshotStatus
name|snapshotInfo
range|:
name|snapshots
control|)
block|{
name|snapshotInfo
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"snapshots"
argument_list|)
expr_stmt|;
for|for
control|(
name|SnapshotStatus
name|snapshot
range|:
name|snapshots
control|)
block|{
name|snapshot
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
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
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

