begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
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
name|cluster
operator|.
name|ClusterService
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
name|Inject
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
name|Settings
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
name|indices
operator|.
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesWarmer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cache
operator|.
name|request
operator|.
name|IndicesRequestCache
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
name|ClusterSettingsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|dfs
operator|.
name|DfsPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|FetchPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|query
operator|.
name|QueryPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|ConcurrentHashMap
import|;
end_import

begin_class
DECL|class|MockSearchService
specifier|public
class|class
name|MockSearchService
extends|extends
name|SearchService
block|{
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"mock-search-service"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"a mock search service for testing"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|SearchModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|searchServiceImpl
operator|=
name|MockSearchService
operator|.
name|class
expr_stmt|;
block|}
block|}
DECL|field|ACTIVE_SEARCH_CONTEXTS
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|SearchContext
argument_list|,
name|Throwable
argument_list|>
name|ACTIVE_SEARCH_CONTEXTS
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/** Throw an {@link AssertionError} if there are still in-flight contexts. */
DECL|method|assertNoInFLightContext
specifier|public
specifier|static
name|void
name|assertNoInFLightContext
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|SearchContext
argument_list|,
name|Throwable
argument_list|>
name|copy
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|ACTIVE_SEARCH_CONTEXTS
argument_list|)
decl_stmt|;
if|if
condition|(
name|copy
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"There are still "
operator|+
name|copy
operator|.
name|size
argument_list|()
operator|+
literal|" in-flight contexts"
argument_list|,
name|copy
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Inject
DECL|method|MockSearchService
specifier|public
name|MockSearchService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettingsService
name|clusterSettingsService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|IndicesWarmer
name|indicesWarmer
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|PageCacheRecycler
name|pageCacheRecycler
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|DfsPhase
name|dfsPhase
parameter_list|,
name|QueryPhase
name|queryPhase
parameter_list|,
name|FetchPhase
name|fetchPhase
parameter_list|,
name|IndicesRequestCache
name|indicesQueryCache
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|clusterSettingsService
argument_list|,
name|clusterService
argument_list|,
name|indicesService
argument_list|,
name|indicesWarmer
argument_list|,
name|threadPool
argument_list|,
name|scriptService
argument_list|,
name|pageCacheRecycler
argument_list|,
name|bigArrays
argument_list|,
name|dfsPhase
argument_list|,
name|queryPhase
argument_list|,
name|fetchPhase
argument_list|,
name|indicesQueryCache
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|putContext
specifier|protected
name|void
name|putContext
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
name|super
operator|.
name|putContext
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|ACTIVE_SEARCH_CONTEXTS
operator|.
name|put
argument_list|(
name|context
argument_list|,
operator|new
name|RuntimeException
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|removeContext
specifier|protected
name|SearchContext
name|removeContext
parameter_list|(
name|long
name|id
parameter_list|)
block|{
specifier|final
name|SearchContext
name|removed
init|=
name|super
operator|.
name|removeContext
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|removed
operator|!=
literal|null
condition|)
block|{
name|ACTIVE_SEARCH_CONTEXTS
operator|.
name|remove
argument_list|(
name|removed
argument_list|)
expr_stmt|;
block|}
return|return
name|removed
return|;
block|}
block|}
end_class

end_unit

