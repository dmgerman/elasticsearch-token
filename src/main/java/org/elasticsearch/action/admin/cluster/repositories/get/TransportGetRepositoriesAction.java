begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.repositories.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|repositories
operator|.
name|get
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|ActionListener
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
name|ActionFilters
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
name|master
operator|.
name|TransportMasterNodeReadOperationAction
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
name|cluster
operator|.
name|ClusterState
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
name|block
operator|.
name|ClusterBlockException
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
name|block
operator|.
name|ClusterBlockLevel
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
name|metadata
operator|.
name|MetaData
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
name|metadata
operator|.
name|RepositoriesMetaData
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
name|metadata
operator|.
name|RepositoryMetaData
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
name|repositories
operator|.
name|RepositoryMissingException
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_comment
comment|/**  * Transport action for get repositories operation  */
end_comment

begin_class
DECL|class|TransportGetRepositoriesAction
specifier|public
class|class
name|TransportGetRepositoriesAction
extends|extends
name|TransportMasterNodeReadOperationAction
argument_list|<
name|GetRepositoriesRequest
argument_list|,
name|GetRepositoriesResponse
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportGetRepositoriesAction
specifier|public
name|TransportGetRepositoriesAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|GetRepositoriesAction
operator|.
name|NAME
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|MANAGEMENT
return|;
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|GetRepositoriesRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|GetRepositoriesRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|GetRepositoriesResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|GetRepositoriesResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|GetRepositoriesRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indexBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA
argument_list|,
literal|""
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|GetRepositoriesRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|GetRepositoriesResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|MetaData
name|metaData
init|=
name|state
operator|.
name|metaData
argument_list|()
decl_stmt|;
name|RepositoriesMetaData
name|repositories
init|=
name|metaData
operator|.
name|custom
argument_list|(
name|RepositoriesMetaData
operator|.
name|TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|repositories
argument_list|()
operator|.
name|length
operator|==
literal|0
operator|||
operator|(
name|request
operator|.
name|repositories
argument_list|()
operator|.
name|length
operator|==
literal|1
operator|&&
literal|"_all"
operator|.
name|equals
argument_list|(
name|request
operator|.
name|repositories
argument_list|()
index|[
literal|0
index|]
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
name|repositories
operator|!=
literal|null
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetRepositoriesResponse
argument_list|(
name|repositories
operator|.
name|repositories
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetRepositoriesResponse
argument_list|(
name|ImmutableList
operator|.
expr|<
name|RepositoryMetaData
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|repositories
operator|!=
literal|null
condition|)
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RepositoryMetaData
argument_list|>
name|repositoryListBuilder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|repository
range|:
name|request
operator|.
name|repositories
argument_list|()
control|)
block|{
name|RepositoryMetaData
name|repositoryMetaData
init|=
name|repositories
operator|.
name|repository
argument_list|(
name|repository
argument_list|)
decl_stmt|;
if|if
condition|(
name|repositoryMetaData
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|RepositoryMissingException
argument_list|(
name|repository
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|repositoryListBuilder
operator|.
name|add
argument_list|(
name|repositoryMetaData
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetRepositoriesResponse
argument_list|(
name|repositoryListBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|RepositoryMissingException
argument_list|(
name|request
operator|.
name|repositories
argument_list|()
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

