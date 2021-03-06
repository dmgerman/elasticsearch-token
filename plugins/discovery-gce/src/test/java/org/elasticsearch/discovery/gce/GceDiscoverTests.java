begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.gce
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|gce
package|;
end_package

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|Headers
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|HttpServer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|HttpsConfigurator
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|HttpsServer
import|;
end_import

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
name|cloud
operator|.
name|gce
operator|.
name|GceInstancesServiceImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|gce
operator|.
name|GceMetadataService
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
name|SuppressForbidden
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
name|io
operator|.
name|FileSystemUtils
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
name|Loggers
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
name|Setting
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
name|mocksocket
operator|.
name|MockHttpServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|discovery
operator|.
name|gce
operator|.
name|GceDiscoveryPlugin
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
name|test
operator|.
name|ESIntegTestCase
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
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|KeyManagerFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|SSLContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|TrustManagerFactory
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|Files
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
name|security
operator|.
name|KeyStore
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
name|List
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
name|ExecutionException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertNoTimeout
import|;
end_import

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|supportsDedicatedMasters
operator|=
literal|false
argument_list|,
name|numDataNodes
operator|=
literal|2
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|)
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"use http server"
argument_list|)
comment|// TODO this should be a IT but currently all ITs in this project run against a real cluster
DECL|class|GceDiscoverTests
specifier|public
class|class
name|GceDiscoverTests
extends|extends
name|ESIntegTestCase
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
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|GceMetadataService
operator|.
name|GCE_HOST
argument_list|,
name|GceInstancesServiceImpl
operator|.
name|GCE_ROOT_URL
argument_list|,
name|GceInstancesServiceImpl
operator|.
name|GCE_VALIDATE_CERTIFICATES
argument_list|)
return|;
block|}
block|}
DECL|field|httpsServer
specifier|private
specifier|static
name|HttpsServer
name|httpsServer
decl_stmt|;
DECL|field|httpServer
specifier|private
specifier|static
name|HttpServer
name|httpServer
decl_stmt|;
DECL|field|logDir
specifier|private
specifier|static
name|Path
name|logDir
decl_stmt|;
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|GceDiscoveryPlugin
operator|.
name|class
argument_list|,
name|TestPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
name|Path
name|resolve
init|=
name|logDir
operator|.
name|resolve
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|createDirectory
argument_list|(
name|resolve
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.zen.hosts_provider"
argument_list|,
literal|"gce"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.logs"
argument_list|,
name|resolve
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.portsfile"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.gce.project_id"
argument_list|,
literal|"testproject"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.gce.zone"
argument_list|,
literal|"primaryzone"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.gce.host"
argument_list|,
literal|"http://"
operator|+
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostName
argument_list|()
operator|+
literal|":"
operator|+
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.gce.root_url"
argument_list|,
literal|"https://"
operator|+
name|httpsServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostName
argument_list|()
operator|+
literal|":"
operator|+
name|httpsServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
comment|// this is annoying but by default the client pulls a static list of trusted CAs
operator|.
name|put
argument_list|(
literal|"cloud.gce.validate_certificates"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|BeforeClass
DECL|method|startHttpd
specifier|public
specifier|static
name|void
name|startHttpd
parameter_list|()
throws|throws
name|Exception
block|{
name|logDir
operator|=
name|createTempDir
argument_list|()
expr_stmt|;
name|SSLContext
name|sslContext
init|=
name|getSSLContext
argument_list|()
decl_stmt|;
name|httpsServer
operator|=
name|MockHttpServer
operator|.
name|createHttps
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|InetAddress
operator|.
name|getLoopbackAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|httpServer
operator|=
name|MockHttpServer
operator|.
name|createHttp
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|InetAddress
operator|.
name|getLoopbackAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|httpsServer
operator|.
name|setHttpsConfigurator
argument_list|(
operator|new
name|HttpsConfigurator
argument_list|(
name|sslContext
argument_list|)
argument_list|)
expr_stmt|;
name|httpServer
operator|.
name|createContext
argument_list|(
literal|"/computeMetadata/v1/instance/service-accounts/default/token"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
block|{
name|String
name|response
init|=
name|GceMockUtils
operator|.
name|readGoogleInternalJsonResponse
argument_list|(
literal|"http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|responseAsBytes
init|=
name|response
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|s
operator|.
name|sendResponseHeaders
argument_list|(
literal|200
argument_list|,
name|responseAsBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|OutputStream
name|responseBody
init|=
name|s
operator|.
name|getResponseBody
argument_list|()
decl_stmt|;
name|responseBody
operator|.
name|write
argument_list|(
name|responseAsBytes
argument_list|)
expr_stmt|;
name|responseBody
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|httpsServer
operator|.
name|createContext
argument_list|(
literal|"/compute/v1/projects/testproject/zones/primaryzone/instances"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
block|{
name|Headers
name|headers
init|=
name|s
operator|.
name|getResponseHeaders
argument_list|()
decl_stmt|;
name|headers
operator|.
name|add
argument_list|(
literal|"Content-Type"
argument_list|,
literal|"application/json; charset=UTF-8"
argument_list|)
expr_stmt|;
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|GceDiscoverTests
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|Path
index|[]
name|files
init|=
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|logDir
argument_list|)
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"{\"id\": \"dummy\",\"items\":["
argument_list|)
decl_stmt|;
name|int
name|foundFiles
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|files
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|resolve
init|=
name|files
index|[
name|i
index|]
operator|.
name|resolve
argument_list|(
literal|"transport.ports"
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|resolve
argument_list|)
condition|)
block|{
if|if
condition|(
name|foundFiles
operator|++
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|addressses
init|=
name|Files
operator|.
name|readAllLines
argument_list|(
name|resolve
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|addressses
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"addresses for node: [{}] published addresses [{}]"
argument_list|,
name|files
index|[
name|i
index|]
operator|.
name|getFileName
argument_list|()
argument_list|,
name|addressses
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"{\"description\": \"ES Node "
argument_list|)
operator|.
name|append
argument_list|(
name|files
index|[
name|i
index|]
operator|.
name|getFileName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"\",\"networkInterfaces\": [ {"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"\"networkIP\": \""
argument_list|)
operator|.
name|append
argument_list|(
name|addressses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"\"}],"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"\"status\" : \"RUNNING\"}"
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|append
argument_list|(
literal|"]}"
argument_list|)
expr_stmt|;
name|String
name|responseString
init|=
name|builder
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|responseAsBytes
init|=
name|responseString
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|s
operator|.
name|sendResponseHeaders
argument_list|(
literal|200
argument_list|,
name|responseAsBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|OutputStream
name|responseBody
init|=
name|s
operator|.
name|getResponseBody
argument_list|()
decl_stmt|;
name|responseBody
operator|.
name|write
argument_list|(
name|responseAsBytes
argument_list|)
expr_stmt|;
name|responseBody
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|//
name|byte
index|[]
name|responseAsBytes
init|=
operator|(
literal|"{ \"error\" : {\"message\" : \""
operator|+
name|e
operator|.
name|toString
argument_list|()
operator|+
literal|"\" } }"
operator|)
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|s
operator|.
name|sendResponseHeaders
argument_list|(
literal|500
argument_list|,
name|responseAsBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|OutputStream
name|responseBody
init|=
name|s
operator|.
name|getResponseBody
argument_list|()
decl_stmt|;
name|responseBody
operator|.
name|write
argument_list|(
name|responseAsBytes
argument_list|)
expr_stmt|;
name|responseBody
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|httpsServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|httpServer
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|getSSLContext
specifier|private
specifier|static
name|SSLContext
name|getSSLContext
parameter_list|()
throws|throws
name|Exception
block|{
name|char
index|[]
name|passphrase
init|=
literal|"keypass"
operator|.
name|toCharArray
argument_list|()
decl_stmt|;
name|KeyStore
name|ks
init|=
name|KeyStore
operator|.
name|getInstance
argument_list|(
literal|"JKS"
argument_list|)
decl_stmt|;
try|try
init|(
name|InputStream
name|stream
init|=
name|GceDiscoverTests
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/test-node.jks"
argument_list|)
init|)
block|{
name|assertNotNull
argument_list|(
literal|"can't find keystore file"
argument_list|,
name|stream
argument_list|)
expr_stmt|;
name|ks
operator|.
name|load
argument_list|(
name|stream
argument_list|,
name|passphrase
argument_list|)
expr_stmt|;
block|}
name|KeyManagerFactory
name|kmf
init|=
name|KeyManagerFactory
operator|.
name|getInstance
argument_list|(
literal|"SunX509"
argument_list|)
decl_stmt|;
name|kmf
operator|.
name|init
argument_list|(
name|ks
argument_list|,
name|passphrase
argument_list|)
expr_stmt|;
name|TrustManagerFactory
name|tmf
init|=
name|TrustManagerFactory
operator|.
name|getInstance
argument_list|(
literal|"SunX509"
argument_list|)
decl_stmt|;
name|tmf
operator|.
name|init
argument_list|(
name|ks
argument_list|)
expr_stmt|;
name|SSLContext
name|ssl
init|=
name|SSLContext
operator|.
name|getInstance
argument_list|(
literal|"TLS"
argument_list|)
decl_stmt|;
name|ssl
operator|.
name|init
argument_list|(
name|kmf
operator|.
name|getKeyManagers
argument_list|()
argument_list|,
name|tmf
operator|.
name|getTrustManagers
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|ssl
return|;
block|}
annotation|@
name|AfterClass
DECL|method|stopHttpd
specifier|public
specifier|static
name|void
name|stopHttpd
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|internalCluster
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
comment|// shut them all down otherwise we get spammed with connection refused exceptions
name|internalCluster
argument_list|()
operator|.
name|stopRandomDataNode
argument_list|()
expr_stmt|;
block|}
name|httpsServer
operator|.
name|stop
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|httpServer
operator|.
name|stop
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|httpsServer
operator|=
literal|null
expr_stmt|;
name|httpServer
operator|=
literal|null
expr_stmt|;
name|logDir
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testJoin
specifier|public
name|void
name|testJoin
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
comment|// only wait for the cluster to form
name|assertNoTimeout
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
name|prepareHealth
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// add one more node and wait for it to join
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNode
argument_list|()
expr_stmt|;
name|assertNoTimeout
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
name|prepareHealth
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

