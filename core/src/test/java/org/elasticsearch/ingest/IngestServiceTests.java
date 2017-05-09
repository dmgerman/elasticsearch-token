begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

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
name|Collections
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
name|plugins
operator|.
name|IngestPlugin
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
DECL|class|IngestServiceTests
specifier|public
class|class
name|IngestServiceTests
extends|extends
name|ESTestCase
block|{
DECL|field|DUMMY_PLUGIN
specifier|private
specifier|final
name|IngestPlugin
name|DUMMY_PLUGIN
init|=
operator|new
name|IngestPlugin
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|getProcessors
parameter_list|(
name|Processor
operator|.
name|Parameters
name|parameters
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"foo"
argument_list|,
parameter_list|(
name|factories
parameter_list|,
name|tag
parameter_list|,
name|config
parameter_list|)
lambda|->
literal|null
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|testIngestPlugin
specifier|public
name|void
name|testIngestPlugin
parameter_list|()
block|{
name|ThreadPool
name|tp
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
decl_stmt|;
name|IngestService
name|ingestService
init|=
operator|new
name|IngestService
argument_list|(
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
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|tp
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|DUMMY_PLUGIN
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|factories
init|=
name|ingestService
operator|.
name|getPipelineStore
argument_list|()
operator|.
name|getProcessorFactories
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|factories
operator|.
name|containsKey
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|factories
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testIngestPluginDuplicate
specifier|public
name|void
name|testIngestPluginDuplicate
parameter_list|()
block|{
name|ThreadPool
name|tp
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
decl_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|IngestService
argument_list|(
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
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|tp
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|DUMMY_PLUGIN
argument_list|,
name|DUMMY_PLUGIN
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"already registered"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

