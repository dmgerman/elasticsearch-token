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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

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
name|common
operator|.
name|network
operator|.
name|NetworkModule
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
name|env
operator|.
name|Environment
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
name|InternalTestCluster
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|reset
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verifyNoMoreInteractions
import|;
end_import

begin_class
DECL|class|NodeTests
specifier|public
class|class
name|NodeTests
extends|extends
name|ESTestCase
block|{
DECL|method|testNodeName
specifier|public
name|void
name|testNodeName
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|tempDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
specifier|final
name|String
name|name
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
else|:
literal|null
decl_stmt|;
name|Settings
operator|.
name|Builder
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|InternalTestCluster
operator|.
name|clusterName
argument_list|(
literal|"single-node-cluster"
argument_list|,
name|randomLong
argument_list|()
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tempDir
argument_list|)
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|HTTP_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|settings
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Node
name|node
init|=
operator|new
name|MockNode
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
init|)
block|{
specifier|final
name|Settings
name|nodeSettings
init|=
name|randomBoolean
argument_list|()
condition|?
name|node
operator|.
name|settings
argument_list|()
else|:
name|node
operator|.
name|getEnvironment
argument_list|()
operator|.
name|settings
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|get
argument_list|(
name|nodeSettings
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|node
operator|.
name|getNodeEnvironment
argument_list|()
operator|.
name|nodeId
argument_list|()
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|7
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|get
argument_list|(
name|nodeSettings
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testWarnIfPreRelease
specifier|public
name|void
name|testWarnIfPreRelease
parameter_list|()
block|{
specifier|final
name|Logger
name|logger
init|=
name|mock
argument_list|(
name|Logger
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|int
name|id
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|9
argument_list|)
operator|*
literal|1000000
decl_stmt|;
specifier|final
name|Version
name|releaseVersion
init|=
name|Version
operator|.
name|fromId
argument_list|(
name|id
operator|+
literal|99
argument_list|)
decl_stmt|;
specifier|final
name|Version
name|preReleaseVersion
init|=
name|Version
operator|.
name|fromId
argument_list|(
name|id
operator|+
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|98
argument_list|)
argument_list|)
decl_stmt|;
name|Node
operator|.
name|warnIfPreRelease
argument_list|(
name|releaseVersion
argument_list|,
literal|false
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|logger
argument_list|)
expr_stmt|;
name|reset
argument_list|(
name|logger
argument_list|)
expr_stmt|;
name|Node
operator|.
name|warnIfPreRelease
argument_list|(
name|releaseVersion
argument_list|,
literal|true
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|logger
argument_list|)
operator|.
name|warn
argument_list|(
literal|"version [{}] is a pre-release version of Elasticsearch and is not suitable for production"
argument_list|,
name|releaseVersion
operator|+
literal|"-SNAPSHOT"
argument_list|)
expr_stmt|;
name|reset
argument_list|(
name|logger
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|isSnapshot
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|Node
operator|.
name|warnIfPreRelease
argument_list|(
name|preReleaseVersion
argument_list|,
name|isSnapshot
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|logger
argument_list|)
operator|.
name|warn
argument_list|(
literal|"version [{}] is a pre-release version of Elasticsearch and is not suitable for production"
argument_list|,
name|preReleaseVersion
operator|+
operator|(
name|isSnapshot
condition|?
literal|"-SNAPSHOT"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

