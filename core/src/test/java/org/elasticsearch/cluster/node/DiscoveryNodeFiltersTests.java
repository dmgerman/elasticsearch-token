begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
name|Version
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
name|transport
operator|.
name|DummyTransportAddress
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
name|transport
operator|.
name|InetSocketTransportAddress
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
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodeFilters
operator|.
name|OpType
operator|.
name|AND
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodeFilters
operator|.
name|OpType
operator|.
name|OR
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
DECL|class|DiscoveryNodeFiltersTests
specifier|public
class|class
name|DiscoveryNodeFiltersTests
extends|extends
name|ESTestCase
block|{
DECL|field|localAddress
specifier|private
specifier|static
name|InetSocketTransportAddress
name|localAddress
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|createLocalAddress
specifier|public
specifier|static
name|void
name|createLocalAddress
parameter_list|()
throws|throws
name|UnknownHostException
block|{
name|localAddress
operator|=
operator|new
name|InetSocketTransportAddress
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
literal|"192.1.1.54"
argument_list|)
argument_list|,
literal|9999
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|releaseLocalAddress
specifier|public
specifier|static
name|void
name|releaseLocalAddress
parameter_list|()
block|{
name|localAddress
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testNameMatch
specifier|public
name|void
name|testNameMatch
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.name"
argument_list|,
literal|"name1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name1"
argument_list|,
literal|"id1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name2"
argument_list|,
literal|"id2"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIdMatch
specifier|public
name|void
name|testIdMatch
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx._id"
argument_list|,
literal|"id1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name1"
argument_list|,
literal|"id1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name2"
argument_list|,
literal|"id2"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIdOrNameMatch
specifier|public
name|void
name|testIdOrNameMatch
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx._id"
argument_list|,
literal|"id1,blah"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx.name"
argument_list|,
literal|"blah,name2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name1"
argument_list|,
literal|"id1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name2"
argument_list|,
literal|"id2"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name3"
argument_list|,
literal|"id3"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testTagAndGroupMatch
specifier|public
name|void
name|testTagAndGroupMatch
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx.group"
argument_list|,
literal|"B"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|AND
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
expr_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"group"
argument_list|,
literal|"B"
argument_list|)
expr_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name1"
argument_list|,
literal|"id1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|attributes
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|attributes
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
expr_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"group"
argument_list|,
literal|"B"
argument_list|)
expr_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"X"
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name2"
argument_list|,
literal|"id2"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|attributes
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|attributes
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
expr_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"group"
argument_list|,
literal|"F"
argument_list|)
expr_stmt|;
name|attributes
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"X"
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name3"
argument_list|,
literal|"id3"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|attributes
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name4"
argument_list|,
literal|"id4"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testStarMatch
specifier|public
name|void
name|testStarMatch
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.name"
argument_list|,
literal|"*"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"name1"
argument_list|,
literal|"id1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpBindFilteringMatchingAnd
specifier|public
name|void
name|testIpBindFilteringMatchingAnd
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx."
operator|+
name|randomFrom
argument_list|(
literal|"_ip"
argument_list|,
literal|"_host_ip"
argument_list|,
literal|"_publish_ip"
argument_list|)
argument_list|,
literal|"192.1.1.54"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|AND
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpBindFilteringNotMatching
specifier|public
name|void
name|testIpBindFilteringNotMatching
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"B"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx."
operator|+
name|randomFrom
argument_list|(
literal|"_ip"
argument_list|,
literal|"_host_ip"
argument_list|,
literal|"_publish_ip"
argument_list|)
argument_list|,
literal|"192.1.1.54"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|AND
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpBindFilteringNotMatchingAnd
specifier|public
name|void
name|testIpBindFilteringNotMatchingAnd
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx."
operator|+
name|randomFrom
argument_list|(
literal|"_ip"
argument_list|,
literal|"_host_ip"
argument_list|,
literal|"_publish_ip"
argument_list|)
argument_list|,
literal|"8.8.8.8"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|AND
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpBindFilteringMatchingOr
specifier|public
name|void
name|testIpBindFilteringMatchingOr
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx."
operator|+
name|randomFrom
argument_list|(
literal|"_ip"
argument_list|,
literal|"_host_ip"
argument_list|,
literal|"_publish_ip"
argument_list|)
argument_list|,
literal|"192.1.1.54"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpBindFilteringNotMatchingOr
specifier|public
name|void
name|testIpBindFilteringNotMatchingOr
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx."
operator|+
name|randomFrom
argument_list|(
literal|"_ip"
argument_list|,
literal|"_host_ip"
argument_list|,
literal|"_publish_ip"
argument_list|)
argument_list|,
literal|"8.8.8.8"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpPublishFilteringMatchingAnd
specifier|public
name|void
name|testIpPublishFilteringMatchingAnd
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx._publish_ip"
argument_list|,
literal|"192.1.1.54"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|AND
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpPublishFilteringNotMatchingAnd
specifier|public
name|void
name|testIpPublishFilteringNotMatchingAnd
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx._publish_ip"
argument_list|,
literal|"8.8.8.8"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|AND
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpPublishFilteringMatchingOr
specifier|public
name|void
name|testIpPublishFilteringMatchingOr
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx._publish_ip"
argument_list|,
literal|"192.1.1.54"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpPublishFilteringNotMatchingOr
specifier|public
name|void
name|testIpPublishFilteringNotMatchingOr
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|shuffleSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"xxx.tag"
argument_list|,
literal|"A"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xxx._publish_ip"
argument_list|,
literal|"8.8.8.8"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DiscoveryNodeFilters
name|filters
init|=
name|DiscoveryNodeFilters
operator|.
name|buildFromSettings
argument_list|(
name|OR
argument_list|,
literal|"xxx."
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|"192.1.1.54"
argument_list|,
name|localAddress
argument_list|,
name|singletonMap
argument_list|(
literal|"tag"
argument_list|,
literal|"A"
argument_list|)
argument_list|,
name|emptySet
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filters
operator|.
name|match
argument_list|(
name|node
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|shuffleSettings
specifier|private
name|Settings
name|shuffleSettings
parameter_list|(
name|Settings
name|source
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|source
operator|.
name|getAsMap
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|keys
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|o
range|:
name|keys
control|)
block|{
name|settings
operator|.
name|put
argument_list|(
name|o
argument_list|,
name|source
operator|.
name|getAsMap
argument_list|()
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|settings
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

