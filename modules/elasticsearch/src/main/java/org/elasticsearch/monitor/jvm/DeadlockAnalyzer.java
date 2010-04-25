begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.jvm
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadInfo
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|DeadlockAnalyzer
specifier|public
class|class
name|DeadlockAnalyzer
block|{
DECL|field|NULL_RESULT
specifier|private
specifier|static
specifier|final
name|Deadlock
name|NULL_RESULT
index|[]
init|=
operator|new
name|Deadlock
index|[
literal|0
index|]
decl_stmt|;
DECL|field|threadBean
specifier|private
specifier|final
name|ThreadMXBean
name|threadBean
init|=
name|ManagementFactory
operator|.
name|getThreadMXBean
argument_list|()
decl_stmt|;
DECL|field|INSTANCE
specifier|private
specifier|static
name|DeadlockAnalyzer
name|INSTANCE
init|=
operator|new
name|DeadlockAnalyzer
argument_list|()
decl_stmt|;
DECL|method|deadlockAnalyzer
specifier|public
specifier|static
name|DeadlockAnalyzer
name|deadlockAnalyzer
parameter_list|()
block|{
return|return
name|INSTANCE
return|;
block|}
DECL|method|DeadlockAnalyzer
specifier|private
name|DeadlockAnalyzer
parameter_list|()
block|{      }
DECL|method|findDeadlocks
specifier|public
name|Deadlock
index|[]
name|findDeadlocks
parameter_list|()
block|{
name|long
name|deadlockedThreads
index|[]
init|=
name|threadBean
operator|.
name|findMonitorDeadlockedThreads
argument_list|()
decl_stmt|;
if|if
condition|(
name|deadlockedThreads
operator|==
literal|null
operator|||
name|deadlockedThreads
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|NULL_RESULT
return|;
block|}
name|ImmutableMap
argument_list|<
name|Long
argument_list|,
name|ThreadInfo
argument_list|>
name|threadInfoMap
init|=
name|createThreadInfoMap
argument_list|(
name|deadlockedThreads
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|cycles
init|=
name|calculateCycles
argument_list|(
name|threadInfoMap
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|chains
init|=
name|calculateCycleDeadlockChains
argument_list|(
name|threadInfoMap
argument_list|,
name|cycles
argument_list|)
decl_stmt|;
name|cycles
operator|.
name|addAll
argument_list|(
name|chains
argument_list|)
expr_stmt|;
return|return
name|createDeadlockDescriptions
argument_list|(
name|cycles
argument_list|)
return|;
block|}
DECL|method|createDeadlockDescriptions
specifier|private
name|Deadlock
index|[]
name|createDeadlockDescriptions
parameter_list|(
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|cycles
parameter_list|)
block|{
name|Deadlock
name|result
index|[]
init|=
operator|new
name|Deadlock
index|[
name|cycles
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
name|cycle
range|:
name|cycles
control|)
block|{
name|ThreadInfo
name|asArray
index|[]
init|=
name|cycle
operator|.
name|toArray
argument_list|(
operator|new
name|ThreadInfo
index|[
name|cycle
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|Deadlock
name|d
init|=
operator|new
name|Deadlock
argument_list|(
name|asArray
argument_list|)
decl_stmt|;
name|result
index|[
name|count
operator|++
index|]
operator|=
name|d
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
DECL|method|calculateCycles
specifier|private
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|calculateCycles
parameter_list|(
name|ImmutableMap
argument_list|<
name|Long
argument_list|,
name|ThreadInfo
argument_list|>
name|threadInfoMap
parameter_list|)
block|{
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|cycles
init|=
operator|new
name|HashSet
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|ThreadInfo
argument_list|>
name|entry
range|:
name|threadInfoMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
name|cycle
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ThreadInfo
name|t
init|=
name|entry
operator|.
name|getValue
argument_list|()
init|;
operator|!
name|cycle
operator|.
name|contains
argument_list|(
name|t
argument_list|)
condition|;
name|t
operator|=
name|threadInfoMap
operator|.
name|get
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|t
operator|.
name|getLockOwnerId
argument_list|()
argument_list|)
argument_list|)
control|)
name|cycle
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|cycles
operator|.
name|contains
argument_list|(
name|cycle
argument_list|)
condition|)
name|cycles
operator|.
name|add
argument_list|(
name|cycle
argument_list|)
expr_stmt|;
block|}
return|return
name|cycles
return|;
block|}
DECL|method|calculateCycleDeadlockChains
specifier|private
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|calculateCycleDeadlockChains
parameter_list|(
name|ImmutableMap
argument_list|<
name|Long
argument_list|,
name|ThreadInfo
argument_list|>
name|threadInfoMap
parameter_list|,
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|cycles
parameter_list|)
block|{
name|ThreadInfo
name|allThreads
index|[]
init|=
name|threadBean
operator|.
name|getThreadInfo
argument_list|(
name|threadBean
operator|.
name|getAllThreadIds
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
name|deadlockChain
init|=
operator|new
name|HashSet
argument_list|<
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Long
argument_list|>
name|knownDeadlockedThreads
init|=
name|threadInfoMap
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|ThreadInfo
name|threadInfo
range|:
name|allThreads
control|)
block|{
name|Thread
operator|.
name|State
name|state
init|=
name|threadInfo
operator|.
name|getThreadState
argument_list|()
decl_stmt|;
if|if
condition|(
name|state
operator|==
name|Thread
operator|.
name|State
operator|.
name|BLOCKED
operator|&&
operator|!
name|knownDeadlockedThreads
operator|.
name|contains
argument_list|(
name|threadInfo
operator|.
name|getThreadId
argument_list|()
argument_list|)
condition|)
block|{
for|for
control|(
name|LinkedHashSet
name|cycle
range|:
name|cycles
control|)
block|{
if|if
condition|(
name|cycle
operator|.
name|contains
argument_list|(
name|threadInfoMap
operator|.
name|get
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|threadInfo
operator|.
name|getLockOwnerId
argument_list|()
argument_list|)
argument_list|)
argument_list|)
condition|)
block|{
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
name|chain
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|ThreadInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ThreadInfo
name|node
init|=
name|threadInfo
init|;
operator|!
name|chain
operator|.
name|contains
argument_list|(
name|node
argument_list|)
condition|;
name|node
operator|=
name|threadInfoMap
operator|.
name|get
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|node
operator|.
name|getLockOwnerId
argument_list|()
argument_list|)
argument_list|)
control|)
name|chain
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|deadlockChain
operator|.
name|add
argument_list|(
name|chain
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|deadlockChain
return|;
block|}
DECL|method|createThreadInfoMap
specifier|private
name|ImmutableMap
argument_list|<
name|Long
argument_list|,
name|ThreadInfo
argument_list|>
name|createThreadInfoMap
parameter_list|(
name|long
name|threadIds
index|[]
parameter_list|)
block|{
name|ThreadInfo
name|threadInfos
index|[]
init|=
name|threadBean
operator|.
name|getThreadInfo
argument_list|(
name|threadIds
argument_list|)
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|Long
argument_list|,
name|ThreadInfo
argument_list|>
name|threadInfoMap
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|ThreadInfo
name|threadInfo
range|:
name|threadInfos
control|)
block|{
name|threadInfoMap
operator|.
name|put
argument_list|(
name|threadInfo
operator|.
name|getThreadId
argument_list|()
argument_list|,
name|threadInfo
argument_list|)
expr_stmt|;
block|}
return|return
name|threadInfoMap
operator|.
name|build
argument_list|()
return|;
block|}
DECL|class|Deadlock
specifier|public
specifier|static
class|class
name|Deadlock
block|{
DECL|field|members
specifier|private
specifier|final
name|ThreadInfo
name|members
index|[]
decl_stmt|;
DECL|field|description
specifier|private
specifier|final
name|String
name|description
decl_stmt|;
DECL|field|memberIds
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|Long
argument_list|>
name|memberIds
decl_stmt|;
DECL|method|Deadlock
specifier|public
name|Deadlock
parameter_list|(
name|ThreadInfo
index|[]
name|members
parameter_list|)
block|{
name|this
operator|.
name|members
operator|=
name|members
expr_stmt|;
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|Long
argument_list|>
name|builder
init|=
name|ImmutableSet
operator|.
name|builder
argument_list|()
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|x
init|=
literal|0
init|;
name|x
operator|<
name|members
operator|.
name|length
condition|;
name|x
operator|++
control|)
block|{
name|ThreadInfo
name|ti
init|=
name|members
index|[
name|x
index|]
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|ti
operator|.
name|getThreadName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|x
operator|<
name|members
operator|.
name|length
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|"> "
argument_list|)
expr_stmt|;
if|if
condition|(
name|x
operator|==
name|members
operator|.
name|length
operator|-
literal|1
condition|)
name|sb
operator|.
name|append
argument_list|(
name|ti
operator|.
name|getLockOwnerName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|ti
operator|.
name|getThreadId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|description
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|memberIds
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|members
specifier|public
name|ThreadInfo
index|[]
name|members
parameter_list|()
block|{
return|return
name|members
return|;
block|}
DECL|method|equals
annotation|@
name|Override
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
return|return
literal|true
return|;
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
return|return
literal|false
return|;
name|Deadlock
name|deadlock
init|=
operator|(
name|Deadlock
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|memberIds
operator|!=
literal|null
condition|?
operator|!
name|memberIds
operator|.
name|equals
argument_list|(
name|deadlock
operator|.
name|memberIds
argument_list|)
else|:
name|deadlock
operator|.
name|memberIds
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|members
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|hashCode
argument_list|(
name|members
argument_list|)
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|description
operator|!=
literal|null
condition|?
name|description
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
name|memberIds
operator|!=
literal|null
condition|?
name|memberIds
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|description
return|;
block|}
block|}
block|}
end_class

end_unit

