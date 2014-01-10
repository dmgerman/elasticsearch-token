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
name|Callable
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PrioritizedCallable
specifier|public
specifier|abstract
class|class
name|PrioritizedCallable
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Callable
argument_list|<
name|T
argument_list|>
implements|,
name|Comparable
argument_list|<
name|PrioritizedCallable
argument_list|>
block|{
DECL|field|priority
specifier|private
specifier|final
name|Priority
name|priority
decl_stmt|;
DECL|method|wrap
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|PrioritizedCallable
argument_list|<
name|T
argument_list|>
name|wrap
parameter_list|(
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|,
name|Priority
name|priority
parameter_list|)
block|{
return|return
operator|new
name|Wrapped
argument_list|<
name|T
argument_list|>
argument_list|(
name|callable
argument_list|,
name|priority
argument_list|)
return|;
block|}
DECL|method|PrioritizedCallable
specifier|protected
name|PrioritizedCallable
parameter_list|(
name|Priority
name|priority
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|PrioritizedCallable
name|pc
parameter_list|)
block|{
return|return
name|priority
operator|.
name|compareTo
argument_list|(
name|pc
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
parameter_list|<
name|T
parameter_list|>
extends|extends
name|PrioritizedCallable
argument_list|<
name|T
argument_list|>
block|{
DECL|field|callable
specifier|private
specifier|final
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
decl_stmt|;
DECL|method|Wrapped
specifier|private
name|Wrapped
parameter_list|(
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
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
name|callable
operator|=
name|callable
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|call
specifier|public
name|T
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|callable
operator|.
name|call
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

