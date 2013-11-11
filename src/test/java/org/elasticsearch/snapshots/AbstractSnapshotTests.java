begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
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
name|base
operator|.
name|Predicate
import|;
end_import

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
name|unit
operator|.
name|TimeValue
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
name|RepositoriesService
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
name|snapshots
operator|.
name|mockstore
operator|.
name|MockRepository
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
annotation|@
name|Ignore
DECL|class|AbstractSnapshotTests
specifier|public
specifier|abstract
class|class
name|AbstractSnapshotTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|After
DECL|method|wipeAfter
specifier|public
specifier|final
name|void
name|wipeAfter
parameter_list|()
block|{
name|wipeRepositories
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
DECL|method|wipeBefore
specifier|public
specifier|final
name|void
name|wipeBefore
parameter_list|()
block|{
name|wipeRepositories
argument_list|()
expr_stmt|;
block|}
comment|/**      * Deletes repositories, supports wildcard notation.      */
DECL|method|wipeRepositories
specifier|public
specifier|static
name|void
name|wipeRepositories
parameter_list|(
name|String
modifier|...
name|repositories
parameter_list|)
block|{
comment|// if nothing is provided, delete all
if|if
condition|(
name|repositories
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|repositories
operator|=
operator|new
name|String
index|[]
block|{
literal|"*"
block|}
expr_stmt|;
block|}
for|for
control|(
name|String
name|repository
range|:
name|repositories
control|)
block|{
try|try
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareDeleteRepository
argument_list|(
name|repository
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryMissingException
name|ex
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
DECL|method|getFailureCount
specifier|public
specifier|static
name|long
name|getFailureCount
parameter_list|(
name|String
name|repository
parameter_list|)
block|{
name|long
name|failureCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RepositoriesService
name|repositoriesService
range|:
name|cluster
argument_list|()
operator|.
name|getInstances
argument_list|(
name|RepositoriesService
operator|.
name|class
argument_list|)
control|)
block|{
name|MockRepository
name|mockRepository
init|=
operator|(
name|MockRepository
operator|)
name|repositoriesService
operator|.
name|repository
argument_list|(
name|repository
argument_list|)
decl_stmt|;
name|failureCount
operator|+=
name|mockRepository
operator|.
name|getFailureCount
argument_list|()
expr_stmt|;
block|}
return|return
name|failureCount
return|;
block|}
DECL|method|numberOfFiles
specifier|public
specifier|static
name|int
name|numberOfFiles
parameter_list|(
name|File
name|dir
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|File
index|[]
name|files
init|=
name|dir
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|File
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|count
operator|+=
name|numberOfFiles
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
name|count
return|;
block|}
DECL|method|stopNode
specifier|public
specifier|static
name|void
name|stopNode
parameter_list|(
specifier|final
name|String
name|node
parameter_list|)
block|{
name|cluster
argument_list|()
operator|.
name|stopRandomNode
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Settings
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|.
name|equals
argument_list|(
name|node
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|waitForCompletionOrBlock
specifier|public
name|String
name|waitForCompletionOrBlock
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|nodes
parameter_list|,
name|String
name|repository
parameter_list|,
name|String
name|snapshot
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|<
name|timeout
operator|.
name|millis
argument_list|()
condition|)
block|{
name|ImmutableList
argument_list|<
name|SnapshotInfo
argument_list|>
name|snapshotInfos
init|=
name|run
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetSnapshots
argument_list|(
name|repository
argument_list|)
operator|.
name|setSnapshots
argument_list|(
name|snapshot
argument_list|)
argument_list|)
operator|.
name|getSnapshots
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshotInfos
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|snapshotInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
operator|.
name|completed
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|String
name|node
range|:
name|nodes
control|)
block|{
name|RepositoriesService
name|repositoriesService
init|=
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|RepositoriesService
operator|.
name|class
argument_list|,
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
operator|(
name|MockRepository
operator|)
name|repositoriesService
operator|.
name|repository
argument_list|(
name|repository
argument_list|)
operator|)
operator|.
name|blocked
argument_list|()
condition|)
block|{
return|return
name|node
return|;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"Timeout!!!"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
DECL|method|waitForCompletion
specifier|public
name|SnapshotInfo
name|waitForCompletion
parameter_list|(
name|String
name|repository
parameter_list|,
name|String
name|snapshot
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|<
name|timeout
operator|.
name|millis
argument_list|()
condition|)
block|{
name|ImmutableList
argument_list|<
name|SnapshotInfo
argument_list|>
name|snapshotInfos
init|=
name|run
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetSnapshots
argument_list|(
name|repository
argument_list|)
operator|.
name|setSnapshots
argument_list|(
name|snapshot
argument_list|)
argument_list|)
operator|.
name|getSnapshots
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshotInfos
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|snapshotInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
operator|.
name|completed
argument_list|()
condition|)
block|{
return|return
name|snapshotInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"Timeout!!!"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
DECL|method|unblock
specifier|public
specifier|static
name|void
name|unblock
parameter_list|(
name|String
name|repository
parameter_list|)
block|{
for|for
control|(
name|RepositoriesService
name|repositoriesService
range|:
name|cluster
argument_list|()
operator|.
name|getInstances
argument_list|(
name|RepositoriesService
operator|.
name|class
argument_list|)
control|)
block|{
operator|(
operator|(
name|MockRepository
operator|)
name|repositoriesService
operator|.
name|repository
argument_list|(
name|repository
argument_list|)
operator|)
operator|.
name|unblock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

