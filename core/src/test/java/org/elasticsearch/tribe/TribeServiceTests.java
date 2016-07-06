begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.tribe
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|tribe
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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_class
DECL|class|TribeServiceTests
specifier|public
class|class
name|TribeServiceTests
extends|extends
name|ESTestCase
block|{
DECL|method|testMinimalSettings
specifier|public
name|void
name|testMinimalSettings
parameter_list|()
block|{
name|Settings
name|globalSettings
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
literal|"nodename"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
literal|"some/path"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|clientSettings
init|=
name|TribeService
operator|.
name|buildClientSettings
argument_list|(
literal|"tribe1"
argument_list|,
literal|"parent_id"
argument_list|,
name|globalSettings
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"some/path"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"path.home"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"nodename/tribe1"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"node.name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tribe1"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"tribe.name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"http.enabled"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"node.master"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"node.data"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"node.ingest"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"node.local_storage"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"3707202549613653169"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"node.id.seed"
argument_list|)
argument_list|)
expr_stmt|;
comment|// should be fixed by the parent id and tribe name
name|assertEquals
argument_list|(
literal|9
argument_list|,
name|clientSettings
operator|.
name|getAsMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnvironmentSettings
specifier|public
name|void
name|testEnvironmentSettings
parameter_list|()
block|{
name|Settings
name|globalSettings
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
literal|"nodename"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
literal|"some/path"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.conf"
argument_list|,
literal|"conf/path"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.scripts"
argument_list|,
literal|"scripts/path"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.logs"
argument_list|,
literal|"logs/path"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|clientSettings
init|=
name|TribeService
operator|.
name|buildClientSettings
argument_list|(
literal|"tribe1"
argument_list|,
literal|"parent_id"
argument_list|,
name|globalSettings
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"some/path"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"path.home"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"conf/path"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"path.conf"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"scripts/path"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"path.scripts"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"logs/path"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"path.logs"
argument_list|)
argument_list|)
expr_stmt|;
name|Settings
name|tribeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
literal|"alternate/path"
argument_list|)
operator|.
name|build
argument_list|()
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
block|{
name|TribeService
operator|.
name|buildClientSettings
argument_list|(
literal|"tribe1"
argument_list|,
literal|"parent_id"
argument_list|,
name|globalSettings
argument_list|,
name|tribeSettings
argument_list|)
expr_stmt|;
block|}
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
literal|"Setting [path.home] not allowed in tribe client"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPassthroughSettings
specifier|public
name|void
name|testPassthroughSettings
parameter_list|()
block|{
name|Settings
name|globalSettings
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
literal|"nodename"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
literal|"some/path"
argument_list|)
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
literal|"0.0.0.0"
argument_list|)
operator|.
name|put
argument_list|(
literal|"network.bind_host"
argument_list|,
literal|"1.1.1.1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"network.publish_host"
argument_list|,
literal|"2.2.2.2"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.host"
argument_list|,
literal|"3.3.3.3"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.bind_host"
argument_list|,
literal|"4.4.4.4"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.publish_host"
argument_list|,
literal|"5.5.5.5"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|clientSettings
init|=
name|TribeService
operator|.
name|buildClientSettings
argument_list|(
literal|"tribe1"
argument_list|,
literal|"parent_id"
argument_list|,
name|globalSettings
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"0.0.0.0"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"network.host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1.1.1.1"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"network.bind_host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2.2.2.2"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"network.publish_host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"3.3.3.3"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"transport.host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4.4.4.4"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"transport.bind_host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"5.5.5.5"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"transport.publish_host"
argument_list|)
argument_list|)
expr_stmt|;
comment|// per tribe client overrides still work
name|Settings
name|tribeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
literal|"3.3.3.3"
argument_list|)
operator|.
name|put
argument_list|(
literal|"network.bind_host"
argument_list|,
literal|"4.4.4.4"
argument_list|)
operator|.
name|put
argument_list|(
literal|"network.publish_host"
argument_list|,
literal|"5.5.5.5"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.host"
argument_list|,
literal|"6.6.6.6"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.bind_host"
argument_list|,
literal|"7.7.7.7"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.publish_host"
argument_list|,
literal|"8.8.8.8"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|clientSettings
operator|=
name|TribeService
operator|.
name|buildClientSettings
argument_list|(
literal|"tribe1"
argument_list|,
literal|"parent_id"
argument_list|,
name|globalSettings
argument_list|,
name|tribeSettings
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"3.3.3.3"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"network.host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4.4.4.4"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"network.bind_host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"5.5.5.5"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"network.publish_host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"6.6.6.6"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"transport.host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"7.7.7.7"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"transport.bind_host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"8.8.8.8"
argument_list|,
name|clientSettings
operator|.
name|get
argument_list|(
literal|"transport.publish_host"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

