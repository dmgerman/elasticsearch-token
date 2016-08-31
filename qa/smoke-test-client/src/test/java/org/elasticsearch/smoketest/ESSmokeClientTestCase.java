begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.smoketest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|smoketest
package|;
end_package

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
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|client
operator|.
name|transport
operator|.
name|TransportClient
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|ESLoggerFactory
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
name|common
operator|.
name|transport
operator|.
name|TransportAddress
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
name|transport
operator|.
name|MockTcpTransportPlugin
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
name|Netty3Plugin
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
name|Netty4Plugin
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
name|client
operator|.
name|PreBuiltTransportClient
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
name|AfterClass
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
name|BeforeClass
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
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|Collection
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
name|Locale
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedTest
operator|.
name|randomAsciiOfLength
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedTest
operator|.
name|randomIntBetween
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
name|notNullValue
import|;
end_import

begin_comment
comment|/**  * An abstract base class to run integration tests against an Elasticsearch cluster running outside of the test process.  *<p>  * You can define a list of transport addresses from where you can reach your cluster by setting "tests.cluster" system  * property. It defaults to "localhost:9300". If you run this from `gradle integTest` then it will start the clsuter for  * you and set up the property.  *<p>  * If you want to debug this module from your IDE, then start an external cluster by yourself, maybe with `gradle run`,  * then run JUnit. If you changed the default port, set "-Dtests.cluster=localhost:PORT" when running your test.  */
end_comment

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|SuppressSysoutChecks
argument_list|(
name|bugUrl
operator|=
literal|"we log a lot on purpose"
argument_list|)
DECL|class|ESSmokeClientTestCase
specifier|public
specifier|abstract
class|class
name|ESSmokeClientTestCase
extends|extends
name|LuceneTestCase
block|{
comment|/**      * Key used to eventually switch to using an external cluster and provide its transport addresses      */
DECL|field|TESTS_CLUSTER
specifier|public
specifier|static
specifier|final
name|String
name|TESTS_CLUSTER
init|=
literal|"tests.cluster"
decl_stmt|;
DECL|field|logger
specifier|protected
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|ESSmokeClientTestCase
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|counter
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|client
specifier|private
specifier|static
name|Client
name|client
decl_stmt|;
DECL|field|clusterAddresses
specifier|private
specifier|static
name|String
name|clusterAddresses
decl_stmt|;
DECL|field|index
specifier|protected
name|String
name|index
decl_stmt|;
DECL|method|startClient
specifier|private
specifier|static
name|Client
name|startClient
parameter_list|(
name|Path
name|tempDir
parameter_list|,
name|TransportAddress
modifier|...
name|transportAddresses
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
literal|"qa_smoke_client_"
operator|+
name|counter
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"client.transport.ignore_cluster_name"
argument_list|,
literal|true
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
decl_stmt|;
specifier|final
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|plugins
decl_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|builder
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
name|MockTcpTransportPlugin
operator|.
name|MOCK_TCP_TRANSPORT_NAME
argument_list|)
expr_stmt|;
name|plugins
operator|=
name|Collections
operator|.
name|singleton
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|plugins
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
name|Netty3Plugin
operator|.
name|NETTY_TRANSPORT_NAME
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|plugins
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
name|TransportClient
name|client
init|=
operator|new
name|PreBuiltTransportClient
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|plugins
argument_list|)
operator|.
name|addTransportAddresses
argument_list|(
name|transportAddresses
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Elasticsearch Java TransportClient started"
argument_list|)
expr_stmt|;
name|Exception
name|clientException
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ClusterHealthResponse
name|health
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> connected to [{}] cluster which is running [{}] node(s)."
argument_list|,
name|health
operator|.
name|getClusterName
argument_list|()
argument_list|,
name|health
operator|.
name|getNumberOfNodes
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|clientException
operator|=
name|e
expr_stmt|;
block|}
name|assumeNoException
argument_list|(
literal|"Sounds like your cluster is not running at "
operator|+
name|clusterAddresses
argument_list|,
name|clientException
argument_list|)
expr_stmt|;
return|return
name|client
return|;
block|}
DECL|method|startClient
specifier|private
specifier|static
name|Client
name|startClient
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|stringAddresses
init|=
name|clusterAddresses
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|TransportAddress
index|[]
name|transportAddresses
init|=
operator|new
name|TransportAddress
index|[
name|stringAddresses
operator|.
name|length
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|stringAddress
range|:
name|stringAddresses
control|)
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
literal|"http://"
operator|+
name|stringAddress
argument_list|)
decl_stmt|;
name|InetAddress
name|inetAddress
init|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|url
operator|.
name|getHost
argument_list|()
argument_list|)
decl_stmt|;
name|transportAddresses
index|[
name|i
operator|++
index|]
operator|=
operator|new
name|InetSocketTransportAddress
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|inetAddress
argument_list|,
name|url
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|startClient
argument_list|(
name|createTempDir
argument_list|()
argument_list|,
name|transportAddresses
argument_list|)
return|;
block|}
DECL|method|getClient
specifier|public
specifier|static
name|Client
name|getClient
parameter_list|()
block|{
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|client
operator|=
name|startClient
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"can not start the client"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|client
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|client
return|;
block|}
annotation|@
name|BeforeClass
DECL|method|initializeSettings
specifier|public
specifier|static
name|void
name|initializeSettings
parameter_list|()
block|{
name|clusterAddresses
operator|=
name|System
operator|.
name|getProperty
argument_list|(
name|TESTS_CLUSTER
argument_list|)
expr_stmt|;
if|if
condition|(
name|clusterAddresses
operator|==
literal|null
operator|||
name|clusterAddresses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|fail
argument_list|(
literal|"Must specify "
operator|+
name|TESTS_CLUSTER
operator|+
literal|" for smoke client test"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
DECL|method|stopTransportClient
specifier|public
specifier|static
name|void
name|stopTransportClient
parameter_list|()
block|{
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|client
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Before
DECL|method|defineIndexName
specifier|public
name|void
name|defineIndexName
parameter_list|()
block|{
name|doClean
argument_list|()
expr_stmt|;
name|index
operator|=
literal|"qa-smoke-test-client-"
operator|+
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|getDefault
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|cleanIndex
specifier|public
name|void
name|cleanIndex
parameter_list|()
block|{
name|doClean
argument_list|()
expr_stmt|;
block|}
DECL|method|doClean
specifier|private
name|void
name|doClean
parameter_list|()
block|{
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// We ignore this cleanup exception
block|}
block|}
block|}
block|}
end_class

end_unit

