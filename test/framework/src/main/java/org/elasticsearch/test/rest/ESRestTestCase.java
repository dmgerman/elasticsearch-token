begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|Header
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|message
operator|.
name|BasicHeader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|nio
operator|.
name|conn
operator|.
name|ssl
operator|.
name|SSLIOSessionStrategy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|ssl
operator|.
name|SSLContexts
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
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|tasks
operator|.
name|list
operator|.
name|ListTasksAction
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
name|Response
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
name|ResponseException
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
name|RestClient
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
name|RestClientBuilder
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
name|PathUtils
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
name|concurrent
operator|.
name|ThreadContext
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
name|xcontent
operator|.
name|XContentParser
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
name|xcontent
operator|.
name|XContentType
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
name|KeyManagementException
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
name|security
operator|.
name|KeyStoreException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|cert
operator|.
name|CertificateException
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
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|sort
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
name|unmodifiableList
import|;
end_import

begin_comment
comment|/**  * Superclass for tests that interact with an external test cluster using Elasticsearch's {@link RestClient}.  */
end_comment

begin_class
DECL|class|ESRestTestCase
specifier|public
specifier|abstract
class|class
name|ESRestTestCase
extends|extends
name|ESTestCase
block|{
DECL|field|TRUSTSTORE_PATH
specifier|public
specifier|static
specifier|final
name|String
name|TRUSTSTORE_PATH
init|=
literal|"truststore.path"
decl_stmt|;
DECL|field|TRUSTSTORE_PASSWORD
specifier|public
specifier|static
specifier|final
name|String
name|TRUSTSTORE_PASSWORD
init|=
literal|"truststore.password"
decl_stmt|;
comment|/**      * Convert the entity from a {@link Response} into a map of maps.      */
DECL|method|entityAsMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entityAsMap
parameter_list|(
name|Response
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentType
name|xContentType
init|=
name|XContentType
operator|.
name|fromMediaTypeOrFormat
argument_list|(
name|response
operator|.
name|getEntity
argument_list|()
operator|.
name|getContentType
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|xContentType
operator|.
name|xContent
argument_list|()
argument_list|,
name|response
operator|.
name|getEntity
argument_list|()
operator|.
name|getContent
argument_list|()
argument_list|)
init|)
block|{
return|return
name|parser
operator|.
name|map
argument_list|()
return|;
block|}
block|}
DECL|field|clusterHosts
specifier|private
specifier|static
name|List
argument_list|<
name|HttpHost
argument_list|>
name|clusterHosts
decl_stmt|;
comment|/**      * A client for the running Elasticsearch cluster      */
DECL|field|client
specifier|private
specifier|static
name|RestClient
name|client
decl_stmt|;
comment|/**      * A client for the running Elasticsearch cluster configured to take test administrative actions like remove all indexes after the test      * completes      */
DECL|field|adminClient
specifier|private
specifier|static
name|RestClient
name|adminClient
decl_stmt|;
annotation|@
name|Before
DECL|method|initClient
specifier|public
name|void
name|initClient
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
assert|assert
name|adminClient
operator|==
literal|null
assert|;
assert|assert
name|clusterHosts
operator|==
literal|null
assert|;
name|String
name|cluster
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.rest.cluster"
argument_list|)
decl_stmt|;
if|if
condition|(
name|cluster
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Must specify [tests.rest.cluster] system property with a comma delimited list of [host:port] "
operator|+
literal|"to which to send REST requests"
argument_list|)
throw|;
block|}
name|String
index|[]
name|stringUrls
init|=
name|cluster
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HttpHost
argument_list|>
name|hosts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|stringUrls
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|stringUrl
range|:
name|stringUrls
control|)
block|{
name|int
name|portSeparator
init|=
name|stringUrl
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
if|if
condition|(
name|portSeparator
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal cluster url ["
operator|+
name|stringUrl
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|String
name|host
init|=
name|stringUrl
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|portSeparator
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|stringUrl
operator|.
name|substring
argument_list|(
name|portSeparator
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|hosts
operator|.
name|add
argument_list|(
operator|new
name|HttpHost
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|getProtocol
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|clusterHosts
operator|=
name|unmodifiableList
argument_list|(
name|hosts
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"initializing REST clients against {}"
argument_list|,
name|clusterHosts
argument_list|)
expr_stmt|;
name|client
operator|=
name|buildClient
argument_list|(
name|restClientSettings
argument_list|()
argument_list|,
name|clusterHosts
operator|.
name|toArray
argument_list|(
operator|new
name|HttpHost
index|[
name|clusterHosts
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|adminClient
operator|=
name|buildClient
argument_list|(
name|restAdminSettings
argument_list|()
argument_list|,
name|clusterHosts
operator|.
name|toArray
argument_list|(
operator|new
name|HttpHost
index|[
name|clusterHosts
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
assert|assert
name|client
operator|!=
literal|null
assert|;
assert|assert
name|adminClient
operator|!=
literal|null
assert|;
assert|assert
name|clusterHosts
operator|!=
literal|null
assert|;
block|}
comment|/**      * Clean up after the test case.      */
annotation|@
name|After
DECL|method|cleanUpCluster
specifier|public
specifier|final
name|void
name|cleanUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|wipeCluster
argument_list|()
expr_stmt|;
name|logIfThereAreRunningTasks
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|closeClients
specifier|public
specifier|static
name|void
name|closeClients
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|client
argument_list|,
name|adminClient
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|clusterHosts
operator|=
literal|null
expr_stmt|;
name|client
operator|=
literal|null
expr_stmt|;
name|adminClient
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**      * Get the client used for ordinary api calls while writing a test      */
DECL|method|client
specifier|protected
specifier|static
name|RestClient
name|client
parameter_list|()
block|{
return|return
name|client
return|;
block|}
comment|/**      * Get the client used for test administrative actions. Do not use this while writing a test. Only use it for cleaning up after tests.      */
DECL|method|adminClient
specifier|protected
specifier|static
name|RestClient
name|adminClient
parameter_list|()
block|{
return|return
name|adminClient
return|;
block|}
comment|/**      * Returns whether to preserve the indices created during this test on completion of this test.      * Defaults to {@code false}. Override this method if indices should be preserved after the test,      * with the assumption that some other process or test will clean up the indices afterward.      * This is useful if the data directory and indices need to be preserved between test runs      * (for example, when testing rolling upgrades).      */
DECL|method|preserveIndicesUponCompletion
specifier|protected
name|boolean
name|preserveIndicesUponCompletion
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**      * Controls whether or not to preserve templates upon completion of this test. The default implementation is to delete not preserve      * templates.      *      * @return whether or not to preserve templates      */
DECL|method|preserveTemplatesUponCompletion
specifier|protected
name|boolean
name|preserveTemplatesUponCompletion
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|wipeCluster
specifier|private
name|void
name|wipeCluster
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|preserveIndicesUponCompletion
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// wipe indices
try|try
block|{
name|adminClient
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"DELETE"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
comment|// 404 here just means we had no indexes
if|if
condition|(
name|e
operator|.
name|getResponse
argument_list|()
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
operator|!=
literal|404
condition|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
comment|// wipe index templates
if|if
condition|(
name|preserveTemplatesUponCompletion
argument_list|()
operator|==
literal|false
condition|)
block|{
name|adminClient
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"DELETE"
argument_list|,
literal|"_template/*"
argument_list|)
expr_stmt|;
block|}
name|wipeSnapshots
argument_list|()
expr_stmt|;
block|}
comment|/**      * Wipe fs snapshots we created one by one and all repositories so that the next test can create the repositories fresh and they'll      * start empty. There isn't an API to delete all snapshots. There is an API to delete all snapshot repositories but that leaves all of      * the snapshots intact in the repository.      */
DECL|method|wipeSnapshots
specifier|private
name|void
name|wipeSnapshots
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|repo
range|:
name|entityAsMap
argument_list|(
name|adminClient
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"_snapshot/_all"
argument_list|)
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|repoName
init|=
name|repo
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|repoSpec
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|repo
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|repoType
init|=
operator|(
name|String
operator|)
name|repoSpec
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
if|if
condition|(
name|repoType
operator|.
name|equals
argument_list|(
literal|"fs"
argument_list|)
condition|)
block|{
comment|// All other repo types we really don't have a chance of being able to iterate properly, sadly.
name|String
name|url
init|=
literal|"_snapshot/"
operator|+
name|repoName
operator|+
literal|"/_all"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|singletonMap
argument_list|(
literal|"ignore_unavailable"
argument_list|,
literal|"true"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|snapshots
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|entityAsMap
argument_list|(
name|adminClient
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
name|url
argument_list|,
name|params
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|"snapshots"
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|snapshot
range|:
name|snapshots
control|)
block|{
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|snapshotInfo
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|snapshot
decl_stmt|;
name|String
name|name
init|=
operator|(
name|String
operator|)
name|snapshotInfo
operator|.
name|get
argument_list|(
literal|"snapshot"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"wiping snapshot [{}/{}]"
argument_list|,
name|repoName
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|adminClient
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"DELETE"
argument_list|,
literal|"_snapshot/"
operator|+
name|repoName
operator|+
literal|"/"
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"wiping snapshot repository [{}]"
argument_list|,
name|repoName
argument_list|)
expr_stmt|;
name|adminClient
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"DELETE"
argument_list|,
literal|"_snapshot/"
operator|+
name|repoName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Logs a message if there are still running tasks. The reasoning is that any tasks still running are state the is trying to bleed into      * other tests.      */
DECL|method|logIfThereAreRunningTasks
specifier|private
name|void
name|logIfThereAreRunningTasks
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|runningTasks
init|=
name|runningTasks
argument_list|(
name|adminClient
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"_tasks"
argument_list|)
argument_list|)
decl_stmt|;
comment|// Ignore the task list API - it doens't count against us
name|runningTasks
operator|.
name|remove
argument_list|(
name|ListTasksAction
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|runningTasks
operator|.
name|remove
argument_list|(
name|ListTasksAction
operator|.
name|NAME
operator|+
literal|"[n]"
argument_list|)
expr_stmt|;
if|if
condition|(
name|runningTasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|stillRunning
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|runningTasks
argument_list|)
decl_stmt|;
name|sort
argument_list|(
name|stillRunning
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"There are still tasks running after this test that might break subsequent tests {}."
argument_list|,
name|stillRunning
argument_list|)
expr_stmt|;
comment|/*          * This isn't a higher level log or outright failure because some of these tasks are run by the cluster in the background. If we          * could determine that some tasks are run by the user we'd fail the tests if those tasks were running and ignore any background          * tasks.          */
block|}
comment|/**      * Used to obtain settings for the REST client that is used to send REST requests.      */
DECL|method|restClientSettings
specifier|protected
name|Settings
name|restClientSettings
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|EMPTY
return|;
block|}
comment|/**      * Returns the REST client settings used for admin actions like cleaning up after the test has completed.      */
DECL|method|restAdminSettings
specifier|protected
name|Settings
name|restAdminSettings
parameter_list|()
block|{
return|return
name|restClientSettings
argument_list|()
return|;
comment|// default to the same client settings
block|}
comment|/**      * Get the list of hosts in the cluster.      */
DECL|method|getClusterHosts
specifier|protected
specifier|final
name|List
argument_list|<
name|HttpHost
argument_list|>
name|getClusterHosts
parameter_list|()
block|{
return|return
name|clusterHosts
return|;
block|}
comment|/**      * Override this to switch to testing https.      */
DECL|method|getProtocol
specifier|protected
name|String
name|getProtocol
parameter_list|()
block|{
return|return
literal|"http"
return|;
block|}
DECL|method|buildClient
specifier|protected
name|RestClient
name|buildClient
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|HttpHost
index|[]
name|hosts
parameter_list|)
throws|throws
name|IOException
block|{
name|RestClientBuilder
name|builder
init|=
name|RestClient
operator|.
name|builder
argument_list|(
name|hosts
argument_list|)
decl_stmt|;
name|String
name|keystorePath
init|=
name|settings
operator|.
name|get
argument_list|(
name|TRUSTSTORE_PATH
argument_list|)
decl_stmt|;
if|if
condition|(
name|keystorePath
operator|!=
literal|null
condition|)
block|{
specifier|final
name|String
name|keystorePass
init|=
name|settings
operator|.
name|get
argument_list|(
name|TRUSTSTORE_PASSWORD
argument_list|)
decl_stmt|;
if|if
condition|(
name|keystorePass
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|TRUSTSTORE_PATH
operator|+
literal|" is provided but not "
operator|+
name|TRUSTSTORE_PASSWORD
argument_list|)
throw|;
block|}
name|Path
name|path
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|keystorePath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|TRUSTSTORE_PATH
operator|+
literal|" is set but points to a non-existing file"
argument_list|)
throw|;
block|}
try|try
block|{
name|KeyStore
name|keyStore
init|=
name|KeyStore
operator|.
name|getInstance
argument_list|(
literal|"jks"
argument_list|)
decl_stmt|;
try|try
init|(
name|InputStream
name|is
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|path
argument_list|)
init|)
block|{
name|keyStore
operator|.
name|load
argument_list|(
name|is
argument_list|,
name|keystorePass
operator|.
name|toCharArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SSLContext
name|sslcontext
init|=
name|SSLContexts
operator|.
name|custom
argument_list|()
operator|.
name|loadTrustMaterial
argument_list|(
name|keyStore
argument_list|,
literal|null
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|SSLIOSessionStrategy
name|sessionStrategy
init|=
operator|new
name|SSLIOSessionStrategy
argument_list|(
name|sslcontext
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setHttpClientConfigCallback
argument_list|(
name|httpClientBuilder
lambda|->
name|httpClientBuilder
operator|.
name|setSSLStrategy
argument_list|(
name|sessionStrategy
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyStoreException
decl||
name|NoSuchAlgorithmException
decl||
name|KeyManagementException
decl||
name|CertificateException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error setting up ssl"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
try|try
init|(
name|ThreadContext
name|threadContext
init|=
operator|new
name|ThreadContext
argument_list|(
name|settings
argument_list|)
init|)
block|{
name|Header
index|[]
name|defaultHeaders
init|=
operator|new
name|Header
index|[
name|threadContext
operator|.
name|getHeaders
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
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
name|threadContext
operator|.
name|getHeaders
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|defaultHeaders
index|[
name|i
operator|++
index|]
operator|=
operator|new
name|BasicHeader
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
expr_stmt|;
block|}
name|builder
operator|.
name|setDefaultHeaders
argument_list|(
name|defaultHeaders
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|runningTasks
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|runningTasks
parameter_list|(
name|Response
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|runningTasks
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|nodes
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|entityAsMap
argument_list|(
name|response
argument_list|)
operator|.
name|get
argument_list|(
literal|"nodes"
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
range|:
name|nodes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|nodeInfo
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|node
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|nodeTasks
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|nodeInfo
operator|.
name|get
argument_list|(
literal|"tasks"
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|taskAndName
range|:
name|nodeTasks
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|task
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|taskAndName
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|runningTasks
operator|.
name|add
argument_list|(
name|task
operator|.
name|get
argument_list|(
literal|"action"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|runningTasks
return|;
block|}
block|}
end_class

end_unit

