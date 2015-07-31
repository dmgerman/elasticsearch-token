begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
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
name|impl
operator|.
name|client
operator|.
name|CloseableHttpClient
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
name|impl
operator|.
name|client
operator|.
name|HttpClients
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
name|http
operator|.
name|HttpServerTransport
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
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|rest
operator|.
name|client
operator|.
name|http
operator|.
name|HttpRequestBuilder
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
name|rest
operator|.
name|client
operator|.
name|http
operator|.
name|HttpResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Constants
operator|.
name|WINDOWS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|OK
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
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|SUITE
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
name|hasStatus
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|SitePluginRelativePathConfigTests
specifier|public
class|class
name|SitePluginRelativePathConfigTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|root
specifier|private
specifier|final
name|Path
name|root
init|=
name|PathUtils
operator|.
name|get
argument_list|(
literal|"."
argument_list|)
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|getRoot
argument_list|()
decl_stmt|;
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
name|String
name|cwdToRoot
init|=
name|getRelativePath
argument_list|(
name|PathUtils
operator|.
name|get
argument_list|(
literal|"."
argument_list|)
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|pluginDir
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|cwdToRoot
argument_list|,
name|relativizeToRootIfNecessary
argument_list|(
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/test_plugins"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tempDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|boolean
name|useRelativeInMiddleOfPath
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|useRelativeInMiddleOfPath
condition|)
block|{
name|pluginDir
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|tempDir
operator|.
name|toString
argument_list|()
argument_list|,
name|getRelativePath
argument_list|(
name|tempDir
argument_list|)
argument_list|,
name|pluginDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|settingsBuilder
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
literal|"path.plugins"
argument_list|,
name|pluginDir
argument_list|)
operator|.
name|put
argument_list|(
literal|"force.http.enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testThatRelativePathsDontAffectPlugins
specifier|public
name|void
name|testThatRelativePathsDontAffectPlugins
parameter_list|()
throws|throws
name|Exception
block|{
name|HttpResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"GET"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/_plugin/dummy/"
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
argument_list|,
name|hasStatus
argument_list|(
name|OK
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|relativizeToRootIfNecessary
specifier|private
name|Path
name|relativizeToRootIfNecessary
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
if|if
condition|(
name|WINDOWS
condition|)
block|{
return|return
name|root
operator|.
name|relativize
argument_list|(
name|path
argument_list|)
return|;
block|}
return|return
name|path
return|;
block|}
DECL|method|getRelativePath
specifier|private
name|String
name|getRelativePath
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|path
operator|.
name|getNameCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|".."
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|path
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|httpClient
specifier|public
name|HttpRequestBuilder
name|httpClient
parameter_list|()
block|{
name|CloseableHttpClient
name|httpClient
init|=
name|HttpClients
operator|.
name|createDefault
argument_list|()
decl_stmt|;
return|return
operator|new
name|HttpRequestBuilder
argument_list|(
name|httpClient
argument_list|)
operator|.
name|httpTransport
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|getDataNodeInstance
argument_list|(
name|HttpServerTransport
operator|.
name|class
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

