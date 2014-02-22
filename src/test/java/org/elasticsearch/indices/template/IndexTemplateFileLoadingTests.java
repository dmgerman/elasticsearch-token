begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.template
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|template
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|LifecycleScope
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
name|base
operator|.
name|Charsets
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
name|io
operator|.
name|Files
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
name|state
operator|.
name|ClusterStateResponse
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
name|Streams
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
name|ImmutableSettings
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
name|ElasticsearchIntegrationTest
operator|.
name|Scope
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
name|HashSet
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
name|is
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numNodes
operator|=
literal|1
argument_list|)
DECL|class|IndexTemplateFileLoadingTests
specifier|public
class|class
name|IndexTemplateFileLoadingTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
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
name|ImmutableSettings
operator|.
name|Builder
name|settingsBuilder
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
decl_stmt|;
name|settingsBuilder
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
expr_stmt|;
try|try
block|{
name|File
name|directory
init|=
name|newTempDir
argument_list|(
name|LifecycleScope
operator|.
name|SUITE
argument_list|)
decl_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"path.conf"
argument_list|,
name|directory
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|File
name|templatesDir
init|=
operator|new
name|File
argument_list|(
name|directory
operator|+
name|File
operator|.
name|separator
operator|+
literal|"templates"
argument_list|)
decl_stmt|;
name|templatesDir
operator|.
name|mkdir
argument_list|()
expr_stmt|;
name|File
name|dst
init|=
operator|new
name|File
argument_list|(
name|templatesDir
argument_list|,
literal|"template.json"
argument_list|)
decl_stmt|;
name|String
name|templatePath
init|=
literal|"/org/elasticsearch/indices/template/template"
operator|+
name|randomInt
argument_list|(
literal|5
argument_list|)
operator|+
literal|".json"
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Picking template path [{}]"
argument_list|,
name|templatePath
argument_list|)
expr_stmt|;
comment|// random template, one uses the 'setting.index.number_of_shards', the other 'settings.number_of_shards'
name|String
name|template
init|=
name|Streams
operator|.
name|copyToStringFromClasspath
argument_list|(
name|templatePath
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|template
argument_list|,
name|dst
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
name|settingsBuilder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|numberOfShards
specifier|protected
name|int
name|numberOfShards
parameter_list|()
block|{
comment|//number of shards won't be set through index settings, the one from the index templates needs to be used
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Test
DECL|method|testThatLoadingTemplateFromFileWorks
specifier|public
name|void
name|testThatLoadingTemplateFromFileWorks
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|iters
init|=
name|atLeast
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
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
name|iters
condition|;
name|i
operator|++
control|)
block|{
name|String
name|indexName
init|=
literal|"foo"
operator|+
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
if|if
condition|(
name|indices
operator|.
name|contains
argument_list|(
name|indexName
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|indices
operator|.
name|add
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
comment|// ensuring yellow so the test fails faster if the template cannot be loaded
name|ClusterStateResponse
name|stateResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|setIndices
argument_list|(
name|indexName
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|stateResponse
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|indexName
argument_list|)
operator|.
name|getNumberOfShards
argument_list|()
argument_list|,
name|is
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stateResponse
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|indexName
argument_list|)
operator|.
name|getNumberOfReplicas
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stateResponse
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|indexName
argument_list|)
operator|.
name|aliases
argument_list|()
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
name|String
name|aliasName
init|=
name|indexName
operator|+
literal|"-alias"
decl_stmt|;
name|assertThat
argument_list|(
name|stateResponse
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|indexName
argument_list|)
operator|.
name|aliases
argument_list|()
operator|.
name|get
argument_list|(
name|aliasName
argument_list|)
operator|.
name|alias
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|aliasName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

