begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|node
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
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
name|inject
operator|.
name|AbstractModule
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
name|BigArrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|service
operator|.
name|NodeService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NodeModule
specifier|public
class|class
name|NodeModule
extends|extends
name|AbstractModule
block|{
DECL|field|node
specifier|private
specifier|final
name|Node
name|node
decl_stmt|;
comment|// pkg private so tests can mock
DECL|field|pageCacheRecyclerImpl
name|Class
argument_list|<
name|?
extends|extends
name|PageCacheRecycler
argument_list|>
name|pageCacheRecyclerImpl
init|=
name|PageCacheRecycler
operator|.
name|class
decl_stmt|;
DECL|field|bigArraysImpl
name|Class
argument_list|<
name|?
extends|extends
name|BigArrays
argument_list|>
name|bigArraysImpl
init|=
name|BigArrays
operator|.
name|class
decl_stmt|;
DECL|method|NodeModule
specifier|public
name|NodeModule
parameter_list|(
name|Node
name|node
parameter_list|)
block|{
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
if|if
condition|(
name|pageCacheRecyclerImpl
operator|==
name|PageCacheRecycler
operator|.
name|class
condition|)
block|{
name|bind
argument_list|(
name|PageCacheRecycler
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|bind
argument_list|(
name|PageCacheRecycler
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|pageCacheRecyclerImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|bigArraysImpl
operator|==
name|BigArrays
operator|.
name|class
condition|)
block|{
name|bind
argument_list|(
name|BigArrays
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|bind
argument_list|(
name|BigArrays
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|bigArraysImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
name|bind
argument_list|(
name|Node
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|NodeSettingsService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|NodeService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
