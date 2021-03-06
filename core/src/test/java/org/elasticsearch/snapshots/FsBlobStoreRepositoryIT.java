begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
package|;
end_package

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
name|unit
operator|.
name|ByteSizeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|blobstore
operator|.
name|ESBlobStoreRepositoryIntegTestCase
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
name|assertAcked
import|;
end_import

begin_class
DECL|class|FsBlobStoreRepositoryIT
specifier|public
class|class
name|FsBlobStoreRepositoryIT
extends|extends
name|ESBlobStoreRepositoryIntegTestCase
block|{
annotation|@
name|Override
DECL|method|createTestRepository
specifier|protected
name|void
name|createTestRepository
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|assertAcked
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
name|preparePutRepository
argument_list|(
name|name
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|randomRepoPath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"compress"
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

