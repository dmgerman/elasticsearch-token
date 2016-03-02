begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.tasks
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|tasks
operator|.
name|list
operator|.
name|TaskInfo
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
name|node
operator|.
name|DiscoveryNode
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
name|NamedWriteable
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

begin_comment
comment|/**  * Current task information  */
end_comment

begin_class
DECL|class|Task
specifier|public
class|class
name|Task
block|{
DECL|field|id
specifier|private
specifier|final
name|long
name|id
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|String
name|action
decl_stmt|;
DECL|field|description
specifier|private
specifier|final
name|String
name|description
decl_stmt|;
DECL|field|parentTask
specifier|private
specifier|final
name|TaskId
name|parentTask
decl_stmt|;
DECL|field|startTime
specifier|private
specifier|final
name|long
name|startTime
decl_stmt|;
DECL|field|startTimeNanos
specifier|private
specifier|final
name|long
name|startTimeNanos
decl_stmt|;
DECL|method|Task
specifier|public
name|Task
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|description
argument_list|,
name|TaskId
operator|.
name|EMPTY_TASK_ID
argument_list|)
expr_stmt|;
block|}
DECL|method|Task
specifier|public
name|Task
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|String
name|description
parameter_list|,
name|TaskId
name|parentTask
parameter_list|)
block|{
name|this
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|description
argument_list|,
name|parentTask
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|Task
specifier|public
name|Task
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|String
name|description
parameter_list|,
name|TaskId
name|parentTask
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|startTimeNanos
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|parentTask
operator|=
name|parentTask
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|startTimeNanos
operator|=
name|startTimeNanos
expr_stmt|;
block|}
comment|/**      * Build a version of the task status you can throw over the wire and back      * to the user.      *      * @param node      *            the node this task is running on      * @param detailed      *            should the information include detailed, potentially slow to      *            generate data?      */
DECL|method|taskInfo
specifier|public
name|TaskInfo
name|taskInfo
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|boolean
name|detailed
parameter_list|)
block|{
name|String
name|description
init|=
literal|null
decl_stmt|;
name|Task
operator|.
name|Status
name|status
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|detailed
condition|)
block|{
name|description
operator|=
name|getDescription
argument_list|()
expr_stmt|;
name|status
operator|=
name|getStatus
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|TaskInfo
argument_list|(
name|node
argument_list|,
name|getId
argument_list|()
argument_list|,
name|getType
argument_list|()
argument_list|,
name|getAction
argument_list|()
argument_list|,
name|description
argument_list|,
name|status
argument_list|,
name|startTime
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTimeNanos
argument_list|,
name|parentTask
argument_list|)
return|;
block|}
comment|/**      * Returns task id      */
DECL|method|getId
specifier|public
name|long
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**      * Returns task channel type (netty, transport, direct)      */
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Returns task action      */
DECL|method|getAction
specifier|public
name|String
name|getAction
parameter_list|()
block|{
return|return
name|action
return|;
block|}
comment|/**      * Generates task description      */
DECL|method|getDescription
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
comment|/**      * Returns the task start time      */
DECL|method|getStartTime
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
comment|/**      * Returns id of the parent task or NO_PARENT_ID if the task doesn't have any parent tasks      */
DECL|method|getParentTaskId
specifier|public
name|TaskId
name|getParentTaskId
parameter_list|()
block|{
return|return
name|parentTask
return|;
block|}
comment|/**      * Build a status for this task or null if this task doesn't have status.      * Since most tasks don't have status this defaults to returning null. While      * this can never perform IO it might be a costly operation, requiring      * collating lists of results, etc. So only use it if you need the value.      */
DECL|method|getStatus
specifier|public
name|Status
name|getStatus
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|interface|Status
specifier|public
interface|interface
name|Status
extends|extends
name|ToXContent
extends|,
name|NamedWriteable
argument_list|<
name|Status
argument_list|>
block|{}
block|}
end_class

end_unit

