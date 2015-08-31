begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|test
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
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|InternalTestCluster
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
name|NodeConfigurationSource
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
name|*
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasEntry
import|;
end_import

begin_comment
comment|/**  * Basic test that ensure that the internal cluster reproduces the same  * configuration given the same seed / input.  */
end_comment

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|SuppressFileSystems
argument_list|(
literal|"ExtrasFS"
argument_list|)
comment|// doesn't work with potential multi data path from test cluster yet
DECL|class|InternalTestClusterTests
specifier|public
class|class
name|InternalTestClusterTests
extends|extends
name|ESTestCase
block|{
DECL|method|testInitializiationIsConsistent
specifier|public
name|void
name|testInitializiationIsConsistent
parameter_list|()
block|{
name|long
name|clusterSeed
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|int
name|minNumDataNodes
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|int
name|maxNumDataNodes
init|=
name|randomIntBetween
argument_list|(
name|minNumDataNodes
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|clusterName
init|=
name|randomRealisticUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|NodeConfigurationSource
name|nodeConfigurationSource
init|=
name|NodeConfigurationSource
operator|.
name|EMPTY
decl_stmt|;
name|int
name|numClientNodes
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|boolean
name|enableHttpPipelining
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|String
name|nodePrefix
init|=
name|randomRealisticUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Path
name|baseDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|InternalTestCluster
name|cluster0
init|=
operator|new
name|InternalTestCluster
argument_list|(
literal|"local"
argument_list|,
name|clusterSeed
argument_list|,
name|baseDir
argument_list|,
name|minNumDataNodes
argument_list|,
name|maxNumDataNodes
argument_list|,
name|clusterName
argument_list|,
name|nodeConfigurationSource
argument_list|,
name|numClientNodes
argument_list|,
name|enableHttpPipelining
argument_list|,
name|nodePrefix
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|InternalTestCluster
name|cluster1
init|=
operator|new
name|InternalTestCluster
argument_list|(
literal|"local"
argument_list|,
name|clusterSeed
argument_list|,
name|baseDir
argument_list|,
name|minNumDataNodes
argument_list|,
name|maxNumDataNodes
argument_list|,
name|clusterName
argument_list|,
name|nodeConfigurationSource
argument_list|,
name|numClientNodes
argument_list|,
name|enableHttpPipelining
argument_list|,
name|nodePrefix
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// TODO: this is not ideal - we should have a way to make sure ports are initialized in the same way
name|assertClusters
argument_list|(
name|cluster0
argument_list|,
name|cluster1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * a set of settings that are expected to have different values betweem clusters, even they have been initialized with the same      * base settins.      */
DECL|field|clusterUniqueSettings
specifier|final
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|clusterUniqueSettings
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|clusterUniqueSettings
operator|.
name|add
argument_list|(
name|ClusterName
operator|.
name|SETTING
argument_list|)
expr_stmt|;
name|clusterUniqueSettings
operator|.
name|add
argument_list|(
literal|"transport.tcp.port"
argument_list|)
expr_stmt|;
name|clusterUniqueSettings
operator|.
name|add
argument_list|(
literal|"http.port"
argument_list|)
expr_stmt|;
name|clusterUniqueSettings
operator|.
name|add
argument_list|(
literal|"http.port"
argument_list|)
expr_stmt|;
block|}
DECL|method|assertClusters
specifier|public
specifier|static
name|void
name|assertClusters
parameter_list|(
name|InternalTestCluster
name|cluster0
parameter_list|,
name|InternalTestCluster
name|cluster1
parameter_list|,
name|boolean
name|checkClusterUniqueSettings
parameter_list|)
block|{
name|Settings
name|defaultSettings0
init|=
name|cluster0
operator|.
name|getDefaultSettings
argument_list|()
decl_stmt|;
name|Settings
name|defaultSettings1
init|=
name|cluster1
operator|.
name|getDefaultSettings
argument_list|()
decl_stmt|;
name|assertSettings
argument_list|(
name|defaultSettings0
argument_list|,
name|defaultSettings1
argument_list|,
name|checkClusterUniqueSettings
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cluster0
operator|.
name|numDataNodes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|cluster1
operator|.
name|numDataNodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|checkClusterUniqueSettings
condition|)
block|{
name|assertThat
argument_list|(
name|cluster0
operator|.
name|getClusterName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|cluster1
operator|.
name|getClusterName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertSettings
specifier|public
specifier|static
name|void
name|assertSettings
parameter_list|(
name|Settings
name|left
parameter_list|,
name|Settings
name|right
parameter_list|,
name|boolean
name|checkClusterUniqueSettings
parameter_list|)
block|{
name|ImmutableSet
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|entries0
init|=
name|left
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entries1
init|=
name|right
operator|.
name|getAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|entries0
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|entries1
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|entries0
control|)
block|{
if|if
condition|(
name|clusterUniqueSettings
operator|.
name|contains
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|&&
name|checkClusterUniqueSettings
operator|==
literal|false
condition|)
block|{
continue|continue;
block|}
name|assertThat
argument_list|(
name|entries1
argument_list|,
name|hasEntry
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testBeforeTest
specifier|public
name|void
name|testBeforeTest
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|clusterSeed
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|int
name|minNumDataNodes
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|int
name|maxNumDataNodes
init|=
name|randomIntBetween
argument_list|(
name|minNumDataNodes
argument_list|,
literal|4
argument_list|)
decl_stmt|;
specifier|final
name|String
name|clusterName1
init|=
literal|"shared1"
decl_stmt|;
comment|//clusterName("shared1", clusterSeed);
specifier|final
name|String
name|clusterName2
init|=
literal|"shared2"
decl_stmt|;
comment|//clusterName("shared", Integer.toString(CHILD_JVM_ID), clusterSeed);
comment|/*while (clusterName.equals(clusterName1)) {             clusterName1 = clusterName("shared", Integer.toString(CHILD_JVM_ID), clusterSeed);   // spin until the time changes         }*/
name|NodeConfigurationSource
name|nodeConfigurationSource
init|=
name|NodeConfigurationSource
operator|.
name|EMPTY
decl_stmt|;
name|int
name|numClientNodes
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|boolean
name|enableHttpPipelining
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|int
name|jvmOrdinal
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|nodePrefix
init|=
literal|"foobar"
decl_stmt|;
name|Path
name|baseDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|InternalTestCluster
name|cluster0
init|=
operator|new
name|InternalTestCluster
argument_list|(
literal|"local"
argument_list|,
name|clusterSeed
argument_list|,
name|baseDir
argument_list|,
name|minNumDataNodes
argument_list|,
name|maxNumDataNodes
argument_list|,
name|clusterName1
argument_list|,
name|nodeConfigurationSource
argument_list|,
name|numClientNodes
argument_list|,
name|enableHttpPipelining
argument_list|,
name|nodePrefix
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|InternalTestCluster
name|cluster1
init|=
operator|new
name|InternalTestCluster
argument_list|(
literal|"local"
argument_list|,
name|clusterSeed
argument_list|,
name|baseDir
argument_list|,
name|minNumDataNodes
argument_list|,
name|maxNumDataNodes
argument_list|,
name|clusterName2
argument_list|,
name|nodeConfigurationSource
argument_list|,
name|numClientNodes
argument_list|,
name|enableHttpPipelining
argument_list|,
name|nodePrefix
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertClusters
argument_list|(
name|cluster0
argument_list|,
name|cluster1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|long
name|seed
init|=
name|randomLong
argument_list|()
decl_stmt|;
try|try
block|{
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|cluster0
operator|.
name|beforeTest
argument_list|(
name|random
argument_list|,
name|random
operator|.
name|nextDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|cluster1
operator|.
name|beforeTest
argument_list|(
name|random
argument_list|,
name|random
operator|.
name|nextDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertArrayEquals
argument_list|(
name|cluster0
operator|.
name|getNodeNames
argument_list|()
argument_list|,
name|cluster1
operator|.
name|getNodeNames
argument_list|()
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Client
argument_list|>
name|iterator1
init|=
name|cluster1
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|Client
name|client
range|:
name|cluster0
control|)
block|{
name|assertTrue
argument_list|(
name|iterator1
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|Client
name|other
init|=
name|iterator1
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertSettings
argument_list|(
name|client
operator|.
name|settings
argument_list|()
argument_list|,
name|other
operator|.
name|settings
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|assertArrayEquals
argument_list|(
name|cluster0
operator|.
name|getNodeNames
argument_list|()
argument_list|,
name|cluster1
operator|.
name|getNodeNames
argument_list|()
argument_list|)
expr_stmt|;
name|cluster0
operator|.
name|afterTest
argument_list|()
expr_stmt|;
name|cluster1
operator|.
name|afterTest
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|cluster0
argument_list|,
name|cluster1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

