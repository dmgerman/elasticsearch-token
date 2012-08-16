begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|Reference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ThreadLocals
specifier|public
class|class
name|ThreadLocals
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ThreadLocals
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|class|CleanableValue
specifier|public
specifier|static
class|class
name|CleanableValue
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|value
specifier|private
name|T
name|value
decl_stmt|;
DECL|method|CleanableValue
specifier|public
name|CleanableValue
parameter_list|(
name|T
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
DECL|method|get
specifier|public
name|T
name|get
parameter_list|()
block|{
return|return
name|value
return|;
block|}
DECL|method|set
specifier|public
name|void
name|set
parameter_list|(
name|T
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
block|}
DECL|method|clearReferencesThreadLocals
specifier|public
specifier|static
name|void
name|clearReferencesThreadLocals
parameter_list|()
block|{
try|try
block|{
name|Thread
index|[]
name|threads
init|=
name|getThreads
argument_list|()
decl_stmt|;
comment|// Make the fields in the Thread class that store ThreadLocals
comment|// accessible
name|Field
name|threadLocalsField
init|=
name|Thread
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"threadLocals"
argument_list|)
decl_stmt|;
name|threadLocalsField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Field
name|inheritableThreadLocalsField
init|=
name|Thread
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"inheritableThreadLocals"
argument_list|)
decl_stmt|;
name|inheritableThreadLocalsField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Make the underlying array of ThreadLoad.ThreadLocalMap.Entry objects
comment|// accessible
name|Class
argument_list|<
name|?
argument_list|>
name|tlmClass
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"java.lang.ThreadLocal$ThreadLocalMap"
argument_list|)
decl_stmt|;
name|Field
name|tableField
init|=
name|tlmClass
operator|.
name|getDeclaredField
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|tableField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|threadLocalMap
decl_stmt|;
if|if
condition|(
name|threads
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
comment|// Clear the first map
name|threadLocalMap
operator|=
name|threadLocalsField
operator|.
name|get
argument_list|(
name|threads
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|clearThreadLocalMap
argument_list|(
name|threadLocalMap
argument_list|,
name|tableField
argument_list|)
expr_stmt|;
comment|// Clear the second map
name|threadLocalMap
operator|=
name|inheritableThreadLocalsField
operator|.
name|get
argument_list|(
name|threads
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|clearThreadLocalMap
argument_list|(
name|threadLocalMap
argument_list|,
name|tableField
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to clean thread locals"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*      * Clears the given thread local map object. Also pass in the field that      * points to the internal table to save re-calculating it on every      * call to this method.      */
DECL|method|clearThreadLocalMap
specifier|private
specifier|static
name|void
name|clearThreadLocalMap
parameter_list|(
name|Object
name|map
parameter_list|,
name|Field
name|internalTableField
parameter_list|)
throws|throws
name|NoSuchMethodException
throws|,
name|IllegalAccessException
throws|,
name|NoSuchFieldException
throws|,
name|InvocationTargetException
block|{
if|if
condition|(
name|map
operator|!=
literal|null
condition|)
block|{
name|Method
name|mapRemove
init|=
name|map
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"remove"
argument_list|,
name|ThreadLocal
operator|.
name|class
argument_list|)
decl_stmt|;
name|mapRemove
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
index|[]
name|table
init|=
operator|(
name|Object
index|[]
operator|)
name|internalTableField
operator|.
name|get
argument_list|(
name|map
argument_list|)
decl_stmt|;
name|int
name|staleEntriesCount
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|table
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Object
name|tableValue
init|=
name|table
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
name|tableValue
operator|!=
literal|null
condition|)
block|{
name|boolean
name|remove
init|=
literal|false
decl_stmt|;
comment|// Check the key
name|Object
name|key
init|=
operator|(
operator|(
name|Reference
argument_list|<
name|?
argument_list|>
operator|)
name|tableValue
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// Check the value
name|Field
name|valueField
init|=
name|tableValue
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|valueField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
name|value
init|=
name|valueField
operator|.
name|get
argument_list|(
name|tableValue
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|value
operator|!=
literal|null
operator|&&
name|CleanableValue
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|value
operator|.
name|getClass
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|remove
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|remove
condition|)
block|{
name|Object
index|[]
name|args
init|=
operator|new
name|Object
index|[
literal|4
index|]
decl_stmt|;
if|if
condition|(
name|key
operator|!=
literal|null
condition|)
block|{
name|args
index|[
literal|0
index|]
operator|=
name|key
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
expr_stmt|;
name|args
index|[
literal|1
index|]
operator|=
name|key
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|args
index|[
literal|2
index|]
operator|=
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
expr_stmt|;
name|args
index|[
literal|3
index|]
operator|=
name|value
operator|.
name|toString
argument_list|()
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"ThreadLocal with key of type [{}] (value [{}]) and a value of type [{}] (value [{}]):  The ThreadLocal has been forcibly removed."
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
name|staleEntriesCount
operator|++
expr_stmt|;
block|}
else|else
block|{
name|mapRemove
operator|.
name|invoke
argument_list|(
name|map
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|staleEntriesCount
operator|>
literal|0
condition|)
block|{
name|Method
name|mapRemoveStale
init|=
name|map
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"expungeStaleEntries"
argument_list|)
decl_stmt|;
name|mapRemoveStale
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|mapRemoveStale
operator|.
name|invoke
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*      * Get the set of current threads as an array.      */
DECL|method|getThreads
specifier|private
specifier|static
name|Thread
index|[]
name|getThreads
parameter_list|()
block|{
comment|// Get the current thread group
name|ThreadGroup
name|tg
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getThreadGroup
argument_list|()
decl_stmt|;
comment|// Find the root thread group
while|while
condition|(
name|tg
operator|.
name|getParent
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tg
operator|=
name|tg
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
name|int
name|threadCountGuess
init|=
name|tg
operator|.
name|activeCount
argument_list|()
operator|+
literal|50
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|threadCountGuess
index|]
decl_stmt|;
name|int
name|threadCountActual
init|=
name|tg
operator|.
name|enumerate
argument_list|(
name|threads
argument_list|)
decl_stmt|;
comment|// Make sure we don't miss any threads
while|while
condition|(
name|threadCountActual
operator|==
name|threadCountGuess
condition|)
block|{
name|threadCountGuess
operator|*=
literal|2
expr_stmt|;
name|threads
operator|=
operator|new
name|Thread
index|[
name|threadCountGuess
index|]
expr_stmt|;
comment|// Note tg.enumerate(Thread[]) silently ignores any threads that
comment|// can't fit into the array
name|threadCountActual
operator|=
name|tg
operator|.
name|enumerate
argument_list|(
name|threads
argument_list|)
expr_stmt|;
block|}
return|return
name|threads
return|;
block|}
block|}
end_class

end_unit

