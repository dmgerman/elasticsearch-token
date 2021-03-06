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
name|common
operator|.
name|ParseField
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
name|xcontent
operator|.
name|ConstructingObjectParser
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
name|Objects
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|ConstructingObjectParser
operator|.
name|constructorArg
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
name|xcontent
operator|.
name|ConstructingObjectParser
operator|.
name|optionalConstructorArg
import|;
end_import

begin_comment
comment|/**  * Information about a currently running task.  *<p>  * Tasks are used for communication with transport actions. As a result, they can contain callback  * references as well as mutable state. That makes it impractical to send tasks over transport channels  * and use in APIs. Instead, immutable and streamable TaskInfo objects are used to represent  * snapshot information about currently running tasks.  */
end_comment

begin_class
DECL|class|TaskInfo
specifier|public
specifier|final
class|class
name|TaskInfo
implements|implements
name|Writeable
implements|,
name|ToXContent
block|{
DECL|field|taskId
specifier|private
specifier|final
name|TaskId
name|taskId
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
DECL|field|startTime
specifier|private
specifier|final
name|long
name|startTime
decl_stmt|;
DECL|field|runningTimeNanos
specifier|private
specifier|final
name|long
name|runningTimeNanos
decl_stmt|;
DECL|field|status
specifier|private
specifier|final
name|Task
operator|.
name|Status
name|status
decl_stmt|;
DECL|field|cancellable
specifier|private
specifier|final
name|boolean
name|cancellable
decl_stmt|;
DECL|field|parentTaskId
specifier|private
specifier|final
name|TaskId
name|parentTaskId
decl_stmt|;
DECL|method|TaskInfo
specifier|public
name|TaskInfo
parameter_list|(
name|TaskId
name|taskId
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
name|Task
operator|.
name|Status
name|status
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|runningTimeNanos
parameter_list|,
name|boolean
name|cancellable
parameter_list|,
name|TaskId
name|parentTaskId
parameter_list|)
block|{
name|this
operator|.
name|taskId
operator|=
name|taskId
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
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|runningTimeNanos
operator|=
name|runningTimeNanos
expr_stmt|;
name|this
operator|.
name|cancellable
operator|=
name|cancellable
expr_stmt|;
name|this
operator|.
name|parentTaskId
operator|=
name|parentTaskId
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|TaskInfo
specifier|public
name|TaskInfo
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|taskId
operator|=
name|TaskId
operator|.
name|readFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|action
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|description
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|status
operator|=
name|in
operator|.
name|readOptionalNamedWriteable
argument_list|(
name|Task
operator|.
name|Status
operator|.
name|class
argument_list|)
expr_stmt|;
name|startTime
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|runningTimeNanos
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|cancellable
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|parentTaskId
operator|=
name|TaskId
operator|.
name|readFromStream
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
name|taskId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|action
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalNamedWriteable
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|runningTimeNanos
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|cancellable
argument_list|)
expr_stmt|;
name|parentTaskId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
DECL|method|getTaskId
specifier|public
name|TaskId
name|getTaskId
parameter_list|()
block|{
return|return
name|taskId
return|;
block|}
DECL|method|getId
specifier|public
name|long
name|getId
parameter_list|()
block|{
return|return
name|taskId
operator|.
name|getId
argument_list|()
return|;
block|}
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
comment|/**      * The status of the running task. Only available if TaskInfos were build      * with the detailed flag.      */
DECL|method|getStatus
specifier|public
name|Task
operator|.
name|Status
name|getStatus
parameter_list|()
block|{
return|return
name|status
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
comment|/**      * Returns the task running time      */
DECL|method|getRunningTimeNanos
specifier|public
name|long
name|getRunningTimeNanos
parameter_list|()
block|{
return|return
name|runningTimeNanos
return|;
block|}
comment|/**      * Returns true if the task supports cancellation      */
DECL|method|isCancellable
specifier|public
name|boolean
name|isCancellable
parameter_list|()
block|{
return|return
name|cancellable
return|;
block|}
comment|/**      * Returns the parent task id      */
DECL|method|getParentTaskId
specifier|public
name|TaskId
name|getParentTaskId
parameter_list|()
block|{
return|return
name|parentTaskId
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
name|field
argument_list|(
literal|"node"
argument_list|,
name|taskId
operator|.
name|getNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|taskId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"action"
argument_list|,
name|action
argument_list|)
expr_stmt|;
if|if
condition|(
name|status
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"status"
argument_list|,
name|status
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|description
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"description"
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|dateField
argument_list|(
literal|"start_time_in_millis"
argument_list|,
literal|"start_time"
argument_list|,
name|startTime
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
literal|"running_time_in_nanos"
argument_list|,
literal|"running_time"
argument_list|,
name|runningTimeNanos
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"cancellable"
argument_list|,
name|cancellable
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentTaskId
operator|.
name|isSet
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"parent_task_id"
argument_list|,
name|parentTaskId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|field|PARSER
specifier|public
specifier|static
specifier|final
name|ConstructingObjectParser
argument_list|<
name|TaskInfo
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ConstructingObjectParser
argument_list|<>
argument_list|(
literal|"task_info"
argument_list|,
literal|true
argument_list|,
name|a
lambda|->
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
name|TaskId
name|id
init|=
operator|new
name|TaskId
argument_list|(
operator|(
name|String
operator|)
name|a
index|[
name|i
operator|++
index|]
argument_list|,
operator|(
name|Long
operator|)
name|a
index|[
name|i
operator|++
index|]
argument_list|)
decl_stmt|;
name|String
name|type
init|=
operator|(
name|String
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|String
name|action
init|=
operator|(
name|String
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|String
name|description
init|=
operator|(
name|String
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|BytesReference
name|statusBytes
init|=
operator|(
name|BytesReference
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|long
name|startTime
init|=
operator|(
name|Long
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|long
name|runningTimeNanos
init|=
operator|(
name|Long
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|boolean
name|cancellable
init|=
operator|(
name|Boolean
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|String
name|parentTaskIdString
init|=
operator|(
name|String
operator|)
name|a
index|[
name|i
operator|++
index|]
decl_stmt|;
name|RawTaskStatus
name|status
init|=
name|statusBytes
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|RawTaskStatus
argument_list|(
name|statusBytes
argument_list|)
decl_stmt|;
name|TaskId
name|parentTaskId
init|=
name|parentTaskIdString
operator|==
literal|null
condition|?
name|TaskId
operator|.
name|EMPTY_TASK_ID
else|:
operator|new
name|TaskId
argument_list|(
name|parentTaskIdString
argument_list|)
decl_stmt|;
return|return
operator|new
name|TaskInfo
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|description
argument_list|,
name|status
argument_list|,
name|startTime
argument_list|,
name|runningTimeNanos
argument_list|,
name|cancellable
argument_list|,
name|parentTaskId
argument_list|)
return|;
block|}
argument_list|)
decl_stmt|;
static|static
block|{
comment|// Note for the future: this has to be backwards and forwards compatible with all changes to the task storage format
name|PARSER
operator|.
name|declareString
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"node"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareLong
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"action"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
name|optionalConstructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"description"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareRawObject
argument_list|(
name|optionalConstructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"status"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareLong
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"start_time_in_millis"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareLong
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"running_time_in_nanos"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareBoolean
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"cancellable"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
name|optionalConstructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"parent_task_id"
argument_list|)
argument_list|)
expr_stmt|;
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
name|Strings
operator|.
name|toString
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|// Implements equals and hashCode for testing
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
name|obj
operator|==
literal|null
operator|||
name|obj
operator|.
name|getClass
argument_list|()
operator|!=
name|TaskInfo
operator|.
name|class
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TaskInfo
name|other
init|=
operator|(
name|TaskInfo
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|taskId
argument_list|,
name|other
operator|.
name|taskId
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|type
argument_list|,
name|other
operator|.
name|type
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|action
argument_list|,
name|other
operator|.
name|action
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|description
argument_list|,
name|other
operator|.
name|description
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|startTime
argument_list|,
name|other
operator|.
name|startTime
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|runningTimeNanos
argument_list|,
name|other
operator|.
name|runningTimeNanos
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|parentTaskId
argument_list|,
name|other
operator|.
name|parentTaskId
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|cancellable
argument_list|,
name|other
operator|.
name|cancellable
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|status
argument_list|,
name|other
operator|.
name|status
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
name|Objects
operator|.
name|hash
argument_list|(
name|taskId
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|description
argument_list|,
name|startTime
argument_list|,
name|runningTimeNanos
argument_list|,
name|parentTaskId
argument_list|,
name|cancellable
argument_list|,
name|status
argument_list|)
return|;
block|}
block|}
end_class

end_unit

