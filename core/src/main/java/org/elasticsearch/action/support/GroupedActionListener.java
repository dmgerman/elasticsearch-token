begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
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
name|ActionListener
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
name|AtomicArray
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
name|CountDown
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|AtomicReference
import|;
end_import

begin_comment
comment|/**  * An action listener that delegates it's results to another listener once  * it has received one or more failures or N results. This allows synchronous  * tasks to be forked off in a loop with the same listener and respond to a  * higher level listener once all tasks responded.  */
end_comment

begin_class
DECL|class|GroupedActionListener
specifier|public
specifier|final
class|class
name|GroupedActionListener
parameter_list|<
name|T
parameter_list|>
implements|implements
name|ActionListener
argument_list|<
name|T
argument_list|>
block|{
DECL|field|countDown
specifier|private
specifier|final
name|CountDown
name|countDown
decl_stmt|;
DECL|field|pos
specifier|private
specifier|final
name|AtomicInteger
name|pos
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|roles
specifier|private
specifier|final
name|AtomicArray
argument_list|<
name|T
argument_list|>
name|roles
decl_stmt|;
DECL|field|delegate
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|Collection
argument_list|<
name|T
argument_list|>
argument_list|>
name|delegate
decl_stmt|;
DECL|field|defaults
specifier|private
specifier|final
name|Collection
argument_list|<
name|T
argument_list|>
name|defaults
decl_stmt|;
DECL|field|failure
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|failure
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Creates a new listener      * @param delegate the delegate listener      * @param groupSize the group size      */
DECL|method|GroupedActionListener
specifier|public
name|GroupedActionListener
parameter_list|(
name|ActionListener
argument_list|<
name|Collection
argument_list|<
name|T
argument_list|>
argument_list|>
name|delegate
parameter_list|,
name|int
name|groupSize
parameter_list|,
name|Collection
argument_list|<
name|T
argument_list|>
name|defaults
parameter_list|)
block|{
name|roles
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|groupSize
argument_list|)
expr_stmt|;
name|countDown
operator|=
operator|new
name|CountDown
argument_list|(
name|groupSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
name|this
operator|.
name|defaults
operator|=
name|defaults
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
name|T
name|element
parameter_list|)
block|{
name|roles
operator|.
name|set
argument_list|(
name|pos
operator|.
name|incrementAndGet
argument_list|()
operator|-
literal|1
argument_list|,
name|element
argument_list|)
expr_stmt|;
if|if
condition|(
name|countDown
operator|.
name|countDown
argument_list|()
condition|)
block|{
if|if
condition|(
name|failure
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|delegate
operator|.
name|onFailure
argument_list|(
name|failure
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|T
argument_list|>
name|collect
init|=
name|this
operator|.
name|roles
operator|.
name|asList
argument_list|()
decl_stmt|;
name|collect
operator|.
name|addAll
argument_list|(
name|defaults
argument_list|)
expr_stmt|;
name|delegate
operator|.
name|onResponse
argument_list|(
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|collect
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|failure
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
operator|==
literal|false
condition|)
block|{
name|failure
operator|.
name|get
argument_list|()
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|countDown
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|delegate
operator|.
name|onFailure
argument_list|(
name|failure
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
