begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
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
name|cluster
operator|.
name|ClusterName
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
name|metadata
operator|.
name|IndexMetaData
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
name|common
operator|.
name|settings
operator|.
name|ClusterSettings
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
name|test
operator|.
name|ESTestCase
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
name|VersionUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_class
DECL|class|RoutingBackwardCompatibilityTests
specifier|public
class|class
name|RoutingBackwardCompatibilityTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBackwardCompatibility
specifier|public
name|void
name|testBackwardCompatibility
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|RoutingBackwardCompatibilityTests
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/org/elasticsearch/cluster/routing/shard_routes.txt"
argument_list|)
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
name|String
name|line
init|=
name|reader
operator|.
name|readLine
argument_list|()
init|;
name|line
operator|!=
literal|null
condition|;
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
control|)
block|{
if|if
condition|(
name|line
operator|.
name|startsWith
argument_list|(
literal|"#"
argument_list|)
condition|)
block|{
comment|// comment
continue|continue;
block|}
name|String
index|[]
name|parts
init|=
name|line
operator|.
name|split
argument_list|(
literal|"\t"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|parts
argument_list|)
argument_list|,
literal|7
argument_list|,
name|parts
operator|.
name|length
argument_list|)
expr_stmt|;
specifier|final
name|String
name|index
init|=
name|parts
index|[
literal|0
index|]
decl_stmt|;
specifier|final
name|int
name|numberOfShards
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
specifier|final
name|String
name|type
init|=
name|parts
index|[
literal|2
index|]
decl_stmt|;
specifier|final
name|String
name|id
init|=
name|parts
index|[
literal|3
index|]
decl_stmt|;
specifier|final
name|String
name|routing
init|=
literal|"null"
operator|.
name|equals
argument_list|(
name|parts
index|[
literal|4
index|]
argument_list|)
condition|?
literal|null
else|:
name|parts
index|[
literal|4
index|]
decl_stmt|;
specifier|final
name|int
name|pre20ExpectedShardId
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|5
index|]
argument_list|)
decl_stmt|;
comment|// not needed anymore - old hashing is gone
specifier|final
name|int
name|currentExpectedShard
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|6
index|]
argument_list|)
decl_stmt|;
name|OperationRouting
name|operationRouting
init|=
operator|new
name|OperationRouting
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Version
name|version
range|:
name|VersionUtils
operator|.
name|allVersions
argument_list|()
control|)
block|{
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// unsupported version, no need to test
continue|continue;
block|}
specifier|final
name|Settings
name|settings
init|=
name|settings
argument_list|(
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|index
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|numberOfShards
argument_list|(
name|numberOfShards
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
name|randomInt
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|addAsNew
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|int
name|shardId
init|=
name|operationRouting
operator|.
name|indexShards
argument_list|(
name|clusterState
argument_list|,
name|index
argument_list|,
name|id
argument_list|,
name|routing
argument_list|)
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|currentExpectedShard
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

