begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|metadata
operator|.
name|MetaData
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
name|env
operator|.
name|NodeEnvironment
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
name|test
operator|.
name|ESTestCase
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
name|nullValue
import|;
end_import

begin_class
DECL|class|MetaStateServiceTests
specifier|public
class|class
name|MetaStateServiceTests
extends|extends
name|ESTestCase
block|{
DECL|field|indexSettings
specifier|private
specifier|static
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|method|testWriteLoadIndex
specifier|public
name|void
name|testWriteLoadIndex
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
init|)
block|{
name|MetaStateService
name|metaStateService
init|=
operator|new
name|MetaStateService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|env
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|IndexMetaData
name|index
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|settings
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|metaStateService
operator|.
name|writeIndex
argument_list|(
literal|"test_write"
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|metaStateService
operator|.
name|loadIndexState
argument_list|(
name|index
operator|.
name|getIndex
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLoadMissingIndex
specifier|public
name|void
name|testLoadMissingIndex
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
init|)
block|{
name|MetaStateService
name|metaStateService
init|=
operator|new
name|MetaStateService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|env
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaStateService
operator|.
name|loadIndexState
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test1"
argument_list|,
literal|"test1UUID"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testWriteLoadGlobal
specifier|public
name|void
name|testWriteLoadGlobal
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
init|)
block|{
name|MetaStateService
name|metaStateService
init|=
operator|new
name|MetaStateService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|env
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|persistentSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"test1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|metaStateService
operator|.
name|writeGlobalState
argument_list|(
literal|"test_write"
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|metaStateService
operator|.
name|loadGlobalState
argument_list|()
operator|.
name|persistentSettings
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|metaData
operator|.
name|persistentSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testWriteGlobalStateWithIndexAndNoIndexIsLoaded
specifier|public
name|void
name|testWriteGlobalStateWithIndexAndNoIndexIsLoaded
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
init|)
block|{
name|MetaStateService
name|metaStateService
init|=
operator|new
name|MetaStateService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|env
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|persistentSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"test1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexMetaData
name|index
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|settings
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MetaData
name|metaDataWithIndex
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|metaData
argument_list|)
operator|.
name|put
argument_list|(
name|index
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|metaStateService
operator|.
name|writeGlobalState
argument_list|(
literal|"test_write"
argument_list|,
name|metaDataWithIndex
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|metaStateService
operator|.
name|loadGlobalState
argument_list|()
operator|.
name|persistentSettings
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|metaData
operator|.
name|persistentSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|metaStateService
operator|.
name|loadGlobalState
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"test1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLoadGlobal
specifier|public
name|void
name|testLoadGlobal
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
init|)
block|{
name|MetaStateService
name|metaStateService
init|=
operator|new
name|MetaStateService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|env
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|IndexMetaData
name|index
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|settings
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|persistentSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"test1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|index
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|metaStateService
operator|.
name|writeGlobalState
argument_list|(
literal|"test_write"
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|metaStateService
operator|.
name|writeIndex
argument_list|(
literal|"test_write"
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|MetaData
name|loadedState
init|=
name|metaStateService
operator|.
name|loadFullState
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|loadedState
operator|.
name|persistentSettings
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|metaData
operator|.
name|persistentSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|loadedState
operator|.
name|hasIndex
argument_list|(
literal|"test1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|loadedState
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

