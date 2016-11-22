begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
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
name|Map
import|;
end_import

begin_interface
DECL|interface|ClusterStateTaskExecutor
specifier|public
interface|interface
name|ClusterStateTaskExecutor
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**      * Update the cluster state based on the current state and the given tasks. Return the *same instance* if no state      * should be changed.      */
DECL|method|execute
name|BatchResult
argument_list|<
name|T
argument_list|>
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|T
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**      * indicates whether this task should only run if current node is master      */
DECL|method|runOnlyOnMaster
specifier|default
name|boolean
name|runOnlyOnMaster
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**      * Callback invoked after new cluster state is published. Note that      * this method is not invoked if the cluster state was not updated.      * @param clusterChangedEvent the change event for this cluster state change, containing      *                            both old and new states      */
DECL|method|clusterStatePublished
specifier|default
name|void
name|clusterStatePublished
parameter_list|(
name|ClusterChangedEvent
name|clusterChangedEvent
parameter_list|)
block|{     }
comment|/**      * Builds a concise description of a list of tasks (to be used in logging etc.).      *      * Note that the tasks given are not necessarily the same as those that will be passed to {@link #execute(ClusterState, List)}.      * but are guaranteed to be a subset of them. This method can be called multiple times with different lists before execution.      * This allows groupd task description but the submitting source.      */
DECL|method|describeTasks
specifier|default
name|String
name|describeTasks
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|tasks
parameter_list|)
block|{
return|return
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|T
operator|::
name|toString
argument_list|)
operator|.
name|reduce
argument_list|(
parameter_list|(
name|s1
parameter_list|,
name|s2
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|s1
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|s2
return|;
block|}
elseif|else
if|if
condition|(
name|s2
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|s1
return|;
block|}
else|else
block|{
return|return
name|s1
operator|+
literal|", "
operator|+
name|s2
return|;
block|}
block|}
argument_list|)
operator|.
name|orElse
argument_list|(
literal|""
argument_list|)
return|;
block|}
comment|/**      * Represents the result of a batched execution of cluster state update tasks      * @param<T> the type of the cluster state update task      */
DECL|class|BatchResult
class|class
name|BatchResult
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|resultingState
specifier|public
specifier|final
name|ClusterState
name|resultingState
decl_stmt|;
DECL|field|executionResults
specifier|public
specifier|final
name|Map
argument_list|<
name|T
argument_list|,
name|TaskResult
argument_list|>
name|executionResults
decl_stmt|;
comment|/**          * Construct an execution result instance with a correspondence between the tasks and their execution result          * @param resultingState the resulting cluster state          * @param executionResults the correspondence between tasks and their outcome          */
DECL|method|BatchResult
name|BatchResult
parameter_list|(
name|ClusterState
name|resultingState
parameter_list|,
name|Map
argument_list|<
name|T
argument_list|,
name|TaskResult
argument_list|>
name|executionResults
parameter_list|)
block|{
name|this
operator|.
name|resultingState
operator|=
name|resultingState
expr_stmt|;
name|this
operator|.
name|executionResults
operator|=
name|executionResults
expr_stmt|;
block|}
DECL|method|builder
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Builder
argument_list|<
name|T
argument_list|>
name|builder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|()
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|executionResults
specifier|private
specifier|final
name|Map
argument_list|<
name|T
argument_list|,
name|TaskResult
argument_list|>
name|executionResults
init|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|success
specifier|public
name|Builder
argument_list|<
name|T
argument_list|>
name|success
parameter_list|(
name|T
name|task
parameter_list|)
block|{
return|return
name|result
argument_list|(
name|task
argument_list|,
name|TaskResult
operator|.
name|success
argument_list|()
argument_list|)
return|;
block|}
DECL|method|successes
specifier|public
name|Builder
argument_list|<
name|T
argument_list|>
name|successes
parameter_list|(
name|Iterable
argument_list|<
name|T
argument_list|>
name|tasks
parameter_list|)
block|{
for|for
control|(
name|T
name|task
range|:
name|tasks
control|)
block|{
name|success
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|failure
specifier|public
name|Builder
argument_list|<
name|T
argument_list|>
name|failure
parameter_list|(
name|T
name|task
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
return|return
name|result
argument_list|(
name|task
argument_list|,
name|TaskResult
operator|.
name|failure
argument_list|(
name|e
argument_list|)
argument_list|)
return|;
block|}
DECL|method|failures
specifier|public
name|Builder
argument_list|<
name|T
argument_list|>
name|failures
parameter_list|(
name|Iterable
argument_list|<
name|T
argument_list|>
name|tasks
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
for|for
control|(
name|T
name|task
range|:
name|tasks
control|)
block|{
name|failure
argument_list|(
name|task
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|result
specifier|private
name|Builder
argument_list|<
name|T
argument_list|>
name|result
parameter_list|(
name|T
name|task
parameter_list|,
name|TaskResult
name|executionResult
parameter_list|)
block|{
name|TaskResult
name|existing
init|=
name|executionResults
operator|.
name|put
argument_list|(
name|task
argument_list|,
name|executionResult
argument_list|)
decl_stmt|;
assert|assert
name|existing
operator|==
literal|null
operator|:
name|task
operator|+
literal|" already has result "
operator|+
name|existing
assert|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|BatchResult
argument_list|<
name|T
argument_list|>
name|build
parameter_list|(
name|ClusterState
name|resultingState
parameter_list|)
block|{
return|return
operator|new
name|BatchResult
argument_list|<>
argument_list|(
name|resultingState
argument_list|,
name|executionResults
argument_list|)
return|;
block|}
block|}
block|}
DECL|class|TaskResult
specifier|final
class|class
name|TaskResult
block|{
DECL|field|failure
specifier|private
specifier|final
name|Exception
name|failure
decl_stmt|;
DECL|field|SUCCESS
specifier|private
specifier|static
specifier|final
name|TaskResult
name|SUCCESS
init|=
operator|new
name|TaskResult
argument_list|(
literal|null
argument_list|)
decl_stmt|;
DECL|method|success
specifier|public
specifier|static
name|TaskResult
name|success
parameter_list|()
block|{
return|return
name|SUCCESS
return|;
block|}
DECL|method|failure
specifier|public
specifier|static
name|TaskResult
name|failure
parameter_list|(
name|Exception
name|failure
parameter_list|)
block|{
return|return
operator|new
name|TaskResult
argument_list|(
name|failure
argument_list|)
return|;
block|}
DECL|method|TaskResult
specifier|private
name|TaskResult
parameter_list|(
name|Exception
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
DECL|method|isSuccess
specifier|public
name|boolean
name|isSuccess
parameter_list|()
block|{
return|return
name|this
operator|==
name|SUCCESS
return|;
block|}
DECL|method|getFailure
specifier|public
name|Exception
name|getFailure
parameter_list|()
block|{
assert|assert
operator|!
name|isSuccess
argument_list|()
assert|;
return|return
name|failure
return|;
block|}
block|}
block|}
end_interface

end_unit

