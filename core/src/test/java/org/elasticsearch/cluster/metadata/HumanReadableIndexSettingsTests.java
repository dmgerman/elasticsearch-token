begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
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
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
operator|.
name|newHashSet
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
name|VersionUtils
operator|.
name|randomVersion
import|;
end_import

begin_class
DECL|class|HumanReadableIndexSettingsTests
specifier|public
class|class
name|HumanReadableIndexSettingsTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testHumanReadableSettings
specifier|public
name|void
name|testHumanReadableSettings
parameter_list|()
block|{
name|Version
name|versionCreated
init|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Version
name|versionUpgraded
init|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|created
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Settings
name|testSettings
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
name|SETTING_VERSION_CREATED
argument_list|,
name|versionCreated
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED
argument_list|,
name|versionUpgraded
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_CREATION_DATE
argument_list|,
name|created
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|humanSettings
init|=
name|IndexMetaData
operator|.
name|addHumanReadableSettings
argument_list|(
name|testSettings
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|versionCreated
operator|.
name|toString
argument_list|()
argument_list|,
name|humanSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED_STRING
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|versionUpgraded
operator|.
name|toString
argument_list|()
argument_list|,
name|humanSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED_STRING
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|DateTime
argument_list|(
name|created
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|humanSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_CREATION_DATE_STRING
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

