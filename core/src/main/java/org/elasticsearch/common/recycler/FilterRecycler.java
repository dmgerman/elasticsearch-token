begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.recycler
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|recycler
package|;
end_package

begin_class
DECL|class|FilterRecycler
specifier|abstract
class|class
name|FilterRecycler
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Recycler
argument_list|<
name|T
argument_list|>
block|{
comment|/** Get the delegate instance to forward calls to. */
DECL|method|getDelegate
specifier|protected
specifier|abstract
name|Recycler
argument_list|<
name|T
argument_list|>
name|getDelegate
parameter_list|()
function_decl|;
comment|/** Wrap a recycled reference. */
DECL|method|wrap
specifier|protected
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|wrap
parameter_list|(
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|delegate
parameter_list|)
block|{
return|return
name|delegate
return|;
block|}
annotation|@
name|Override
DECL|method|obtain
specifier|public
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|obtain
parameter_list|(
name|int
name|sizing
parameter_list|)
block|{
return|return
name|wrap
argument_list|(
name|getDelegate
argument_list|()
operator|.
name|obtain
argument_list|(
name|sizing
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|obtain
specifier|public
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|obtain
parameter_list|()
block|{
return|return
name|wrap
argument_list|(
name|getDelegate
argument_list|()
operator|.
name|obtain
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|getDelegate
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

