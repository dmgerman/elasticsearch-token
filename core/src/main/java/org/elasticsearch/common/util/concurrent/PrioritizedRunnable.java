begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Priority
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|LongSupplier
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PrioritizedRunnable
specifier|public
specifier|abstract
class|class
name|PrioritizedRunnable
implements|implements
name|Runnable
implements|,
name|Comparable
argument_list|<
name|PrioritizedRunnable
argument_list|>
block|{
DECL|field|priority
specifier|private
specifier|final
name|Priority
name|priority
decl_stmt|;
DECL|field|creationDate
specifier|private
specifier|final
name|long
name|creationDate
decl_stmt|;
DECL|field|relativeTimeProvider
specifier|private
specifier|final
name|LongSupplier
name|relativeTimeProvider
decl_stmt|;
DECL|method|wrap
specifier|public
specifier|static
name|PrioritizedRunnable
name|wrap
parameter_list|(
name|Runnable
name|runnable
parameter_list|,
name|Priority
name|priority
parameter_list|)
block|{
return|return
operator|new
name|Wrapped
argument_list|(
name|runnable
argument_list|,
name|priority
argument_list|)
return|;
block|}
DECL|method|PrioritizedRunnable
specifier|protected
name|PrioritizedRunnable
parameter_list|(
name|Priority
name|priority
parameter_list|)
block|{
name|this
argument_list|(
name|priority
argument_list|,
name|System
operator|::
name|nanoTime
argument_list|)
expr_stmt|;
block|}
comment|// package visible for testing
DECL|method|PrioritizedRunnable
name|PrioritizedRunnable
parameter_list|(
name|Priority
name|priority
parameter_list|,
name|LongSupplier
name|relativeTimeProvider
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
name|this
operator|.
name|creationDate
operator|=
name|relativeTimeProvider
operator|.
name|getAsLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|relativeTimeProvider
operator|=
name|relativeTimeProvider
expr_stmt|;
block|}
DECL|method|getCreationDateInNanos
specifier|public
name|long
name|getCreationDateInNanos
parameter_list|()
block|{
return|return
name|creationDate
return|;
block|}
comment|/**      * The elapsed time in milliseconds since this instance was created,      * as calculated by the difference between {@link System#nanoTime()}      * at the time of creation, and {@link System#nanoTime()} at the      * time of invocation of this method      *      * @return the age in milliseconds calculated      */
DECL|method|getAgeInMillis
specifier|public
name|long
name|getAgeInMillis
parameter_list|()
block|{
return|return
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|convert
argument_list|(
name|relativeTimeProvider
operator|.
name|getAsLong
argument_list|()
operator|-
name|creationDate
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|PrioritizedRunnable
name|pr
parameter_list|)
block|{
return|return
name|priority
operator|.
name|compareTo
argument_list|(
name|pr
operator|.
name|priority
argument_list|)
return|;
block|}
DECL|method|priority
specifier|public
name|Priority
name|priority
parameter_list|()
block|{
return|return
name|priority
return|;
block|}
DECL|class|Wrapped
specifier|static
class|class
name|Wrapped
extends|extends
name|PrioritizedRunnable
block|{
DECL|field|runnable
specifier|private
specifier|final
name|Runnable
name|runnable
decl_stmt|;
DECL|method|Wrapped
specifier|private
name|Wrapped
parameter_list|(
name|Runnable
name|runnable
parameter_list|,
name|Priority
name|priority
parameter_list|)
block|{
name|super
argument_list|(
name|priority
argument_list|)
expr_stmt|;
name|this
operator|.
name|runnable
operator|=
name|runnable
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|runnable
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

