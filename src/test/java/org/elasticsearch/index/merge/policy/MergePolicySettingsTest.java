begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.merge.policy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
operator|.
name|policy
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|settings
operator|.
name|IndexSettingsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|DirectoryService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|Store
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|distributor
operator|.
name|LeastUsedDistributor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|ram
operator|.
name|RamDirectoryService
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
name|IOException
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
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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

begin_class
DECL|class|MergePolicySettingsTest
specifier|public
class|class
name|MergePolicySettingsTest
block|{
DECL|field|shardId
specifier|protected
specifier|final
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
annotation|@
name|Test
DECL|method|testCompoundFileSettings
specifier|public
name|void
name|testCompoundFileSettings
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexSettingsService
name|service
init|=
operator|new
name|IndexSettingsService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|true
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0.5
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"true"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"True"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"False"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"false"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|false
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|true
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0.5
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"true"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"True"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"False"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"false"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|false
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|true
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0.5
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"true"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"True"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"False"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"false"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|false
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testInvalidValue
specifier|public
name|void
name|testInvalidValue
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexSettingsService
name|service
init|=
operator|new
name|IndexSettingsService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
try|try
block|{
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
operator|-
literal|0.1
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"exception expected"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchIllegalArgumentException
name|ex
parameter_list|)
block|{          }
try|try
block|{
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|1.1
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"exception expected"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchIllegalArgumentException
name|ex
parameter_list|)
block|{          }
try|try
block|{
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|build
argument_list|(
literal|"Falsch"
argument_list|)
argument_list|)
argument_list|,
name|service
argument_list|)
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"exception expected"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchIllegalArgumentException
name|ex
parameter_list|)
block|{          }
block|}
annotation|@
name|Test
DECL|method|testUpdateSettings
specifier|public
name|void
name|testUpdateSettings
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|IndexSettingsService
name|service
init|=
operator|new
name|IndexSettingsService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
name|TieredMergePolicyProvider
name|mp
init|=
operator|new
name|TieredMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|service
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|{
name|IndexSettingsService
name|service
init|=
operator|new
name|IndexSettingsService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
name|LogByteSizeMergePolicyProvider
name|mp
init|=
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|service
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|{
name|IndexSettingsService
name|service
init|=
operator|new
name|IndexSettingsService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
name|LogDocMergePolicyProvider
name|mp
init|=
operator|new
name|LogDocMergePolicyProvider
argument_list|(
name|createStore
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|service
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|refreshSettings
argument_list|(
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|newMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AbstractMergePolicyProvider
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AbstractMergePolicyProvider
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|int
name|value
parameter_list|)
block|{
return|return
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AbstractMergePolicyProvider
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
return|return
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AbstractMergePolicyProvider
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|createStore
specifier|protected
name|Store
name|createStore
parameter_list|(
name|Settings
name|settings
parameter_list|)
throws|throws
name|IOException
block|{
name|DirectoryService
name|directoryService
init|=
operator|new
name|RamDirectoryService
argument_list|(
name|shardId
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
return|return
operator|new
name|Store
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|directoryService
argument_list|,
operator|new
name|LeastUsedDistributor
argument_list|(
name|directoryService
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

