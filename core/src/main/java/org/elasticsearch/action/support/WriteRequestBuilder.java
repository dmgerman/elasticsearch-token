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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|WriteRequest
operator|.
name|RefreshPolicy
import|;
end_import

begin_interface
DECL|interface|WriteRequestBuilder
specifier|public
interface|interface
name|WriteRequestBuilder
parameter_list|<
name|B
extends|extends
name|WriteRequestBuilder
parameter_list|<
name|B
parameter_list|>
parameter_list|>
block|{
DECL|method|request
name|WriteRequest
argument_list|<
name|?
argument_list|>
name|request
parameter_list|()
function_decl|;
comment|/**      * Should this request trigger a refresh ({@linkplain RefreshPolicy#IMMEDIATE}), wait for a refresh (      * {@linkplain RefreshPolicy#WAIT_UNTIL}), or proceed ignore refreshes entirely ({@linkplain RefreshPolicy#NONE}, the default).      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|setRefreshPolicy
specifier|default
name|B
name|setRefreshPolicy
parameter_list|(
name|RefreshPolicy
name|refreshPolicy
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|refreshPolicy
argument_list|)
expr_stmt|;
return|return
operator|(
name|B
operator|)
name|this
return|;
block|}
comment|/**      * If set to true then this request will force an immediate refresh. Backwards compatibility layer for Elasticsearch's old      * {@code setRefresh} calls.      *      * @deprecated use {@link #setRefreshPolicy(RefreshPolicy)} with {@link RefreshPolicy#IMMEDIATE} or {@link RefreshPolicy#NONE} instead.      *             Will be removed in 6.0.      */
annotation|@
name|Deprecated
DECL|method|setRefresh
specifier|default
name|B
name|setRefresh
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
assert|assert
name|Version
operator|.
name|CURRENT
operator|.
name|major
operator|<
literal|6
operator|:
literal|"Remove setRefresh(boolean) in 6.0"
assert|;
return|return
name|setRefreshPolicy
argument_list|(
name|refresh
condition|?
name|RefreshPolicy
operator|.
name|IMMEDIATE
else|:
name|RefreshPolicy
operator|.
name|NONE
argument_list|)
return|;
block|}
block|}
end_interface

end_unit

