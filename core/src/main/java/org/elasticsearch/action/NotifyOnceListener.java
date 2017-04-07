begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
package|;
end_package

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
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * A listener that ensures that only one of onResponse or onFailure is called. And the method  * the is called is only called once. Subclasses should implement notification logic with  * innerOnResponse and innerOnFailure.  */
end_comment

begin_class
DECL|class|NotifyOnceListener
specifier|public
specifier|abstract
class|class
name|NotifyOnceListener
parameter_list|<
name|Response
parameter_list|>
implements|implements
name|ActionListener
argument_list|<
name|Response
argument_list|>
block|{
DECL|field|hasBeenCalled
specifier|private
specifier|final
name|AtomicBoolean
name|hasBeenCalled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
DECL|method|innerOnResponse
specifier|protected
specifier|abstract
name|void
name|innerOnResponse
parameter_list|(
name|Response
name|response
parameter_list|)
function_decl|;
DECL|method|innerOnFailure
specifier|protected
specifier|abstract
name|void
name|innerOnFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
function_decl|;
annotation|@
name|Override
DECL|method|onResponse
specifier|public
specifier|final
name|void
name|onResponse
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
if|if
condition|(
name|hasBeenCalled
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|innerOnResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
specifier|final
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|hasBeenCalled
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|innerOnFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
