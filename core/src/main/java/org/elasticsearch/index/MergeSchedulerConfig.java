begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|ConcurrentMergeScheduler
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
import|;
end_import

begin_comment
comment|/**  * The merge scheduler (<code>ConcurrentMergeScheduler</code>) controls the execution of  * merge operations once they are needed (according to the merge policy).  Merges  * run in separate threads, and when the maximum number of threads is reached,  * further merges will wait until a merge thread becomes available.  *  *<p>The merge scheduler supports the following<b>dynamic</b> settings:  *  *<ul>  *<li><code>index.merge.scheduler.max_thread_count</code>:  *  *     The maximum number of threads that may be merging at once. Defaults to  *<code>Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2))</code>  *     which works well for a good solid-state-disk (SSD).  If your index is on  *     spinning platter drives instead, decrease this to 1.  *  *<li><code>index.merge.scheduler.auto_throttle</code>:  *  *     If this is true (the default), then the merge scheduler will rate-limit IO  *     (writes) for merges to an adaptive value depending on how many merges are  *     requested over time.  An application with a low indexing rate that  *     unluckily suddenly requires a large merge will see that merge aggressively  *     throttled, while an application doing heavy indexing will see the throttle  *     move higher to allow merges to keep up with ongoing indexing.  *</ul>  */
end_comment

begin_class
DECL|class|MergeSchedulerConfig
specifier|public
specifier|final
class|class
name|MergeSchedulerConfig
block|{
DECL|field|MAX_THREAD_COUNT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|MAX_THREAD_COUNT_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"index.merge.scheduler.max_thread_count"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Integer
operator|.
name|toString
argument_list|(
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|Math
operator|.
name|min
argument_list|(
literal|4
argument_list|,
name|EsExecutors
operator|.
name|numberOfProcessors
argument_list|(
name|s
argument_list|)
operator|/
literal|2
argument_list|)
argument_list|)
argument_list|)
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Setting
operator|.
name|parseInt
argument_list|(
name|s
argument_list|,
literal|1
argument_list|,
literal|"index.merge.scheduler.max_thread_count"
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
DECL|field|MAX_MERGE_COUNT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|MAX_MERGE_COUNT_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"index.merge.scheduler.max_merge_count"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Integer
operator|.
name|toString
argument_list|(
name|MAX_THREAD_COUNT_SETTING
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|+
literal|5
argument_list|)
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Setting
operator|.
name|parseInt
argument_list|(
name|s
argument_list|,
literal|1
argument_list|,
literal|"index.merge.scheduler.max_merge_count"
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
DECL|field|AUTO_THROTTLE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|AUTO_THROTTLE_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"index.merge.scheduler.auto_throttle"
argument_list|,
literal|true
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
DECL|field|autoThrottle
specifier|private
specifier|volatile
name|boolean
name|autoThrottle
decl_stmt|;
DECL|field|maxThreadCount
specifier|private
specifier|volatile
name|int
name|maxThreadCount
decl_stmt|;
DECL|field|maxMergeCount
specifier|private
specifier|volatile
name|int
name|maxMergeCount
decl_stmt|;
DECL|method|MergeSchedulerConfig
name|MergeSchedulerConfig
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|)
block|{
name|int
name|maxThread
init|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|MAX_THREAD_COUNT_SETTING
argument_list|)
decl_stmt|;
name|int
name|maxMerge
init|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|MAX_MERGE_COUNT_SETTING
argument_list|)
decl_stmt|;
name|setMaxThreadAndMergeCount
argument_list|(
name|maxThread
argument_list|,
name|maxMerge
argument_list|)
expr_stmt|;
name|this
operator|.
name|autoThrottle
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|AUTO_THROTTLE_SETTING
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns<code>true</code> iff auto throttle is enabled.      *      * @see ConcurrentMergeScheduler#enableAutoIOThrottle()      */
DECL|method|isAutoThrottle
specifier|public
name|boolean
name|isAutoThrottle
parameter_list|()
block|{
return|return
name|autoThrottle
return|;
block|}
comment|/**      * Enables / disables auto throttling on the {@link ConcurrentMergeScheduler}      */
DECL|method|setAutoThrottle
name|void
name|setAutoThrottle
parameter_list|(
name|boolean
name|autoThrottle
parameter_list|)
block|{
name|this
operator|.
name|autoThrottle
operator|=
name|autoThrottle
expr_stmt|;
block|}
comment|/**      * Returns {@code maxThreadCount}.      */
DECL|method|getMaxThreadCount
specifier|public
name|int
name|getMaxThreadCount
parameter_list|()
block|{
return|return
name|maxThreadCount
return|;
block|}
comment|/**      * Expert: directly set the maximum number of merge threads and      * simultaneous merges allowed.      */
DECL|method|setMaxThreadAndMergeCount
name|void
name|setMaxThreadAndMergeCount
parameter_list|(
name|int
name|maxThreadCount
parameter_list|,
name|int
name|maxMergeCount
parameter_list|)
block|{
if|if
condition|(
name|maxThreadCount
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"maxThreadCount should be at least 1"
argument_list|)
throw|;
block|}
if|if
condition|(
name|maxMergeCount
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"maxMergeCount should be at least 1"
argument_list|)
throw|;
block|}
if|if
condition|(
name|maxThreadCount
operator|>
name|maxMergeCount
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"maxThreadCount (= "
operator|+
name|maxThreadCount
operator|+
literal|") should be<= maxMergeCount (= "
operator|+
name|maxMergeCount
operator|+
literal|")"
argument_list|)
throw|;
block|}
name|this
operator|.
name|maxThreadCount
operator|=
name|maxThreadCount
expr_stmt|;
name|this
operator|.
name|maxMergeCount
operator|=
name|maxMergeCount
expr_stmt|;
block|}
comment|/**      * Returns {@code maxMergeCount}.      */
DECL|method|getMaxMergeCount
specifier|public
name|int
name|getMaxMergeCount
parameter_list|()
block|{
return|return
name|maxMergeCount
return|;
block|}
block|}
end_class

end_unit

