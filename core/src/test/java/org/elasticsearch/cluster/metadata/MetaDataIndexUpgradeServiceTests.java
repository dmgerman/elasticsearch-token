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
name|IndexScopedSettings
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
name|indices
operator|.
name|mapper
operator|.
name|MapperRegistry
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
name|test
operator|.
name|VersionUtils
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

begin_class
DECL|class|MetaDataIndexUpgradeServiceTests
specifier|public
class|class
name|MetaDataIndexUpgradeServiceTests
extends|extends
name|ESTestCase
block|{
DECL|method|testArchiveBrokenIndexSettings
specifier|public
name|void
name|testArchiveBrokenIndexSettings
parameter_list|()
block|{
name|MetaDataIndexUpgradeService
name|service
init|=
operator|new
name|MetaDataIndexUpgradeService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|IndexScopedSettings
operator|.
name|DEFAULT_SCOPED_SETTINGS
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|IndexMetaData
name|src
init|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|service
operator|.
name|archiveBrokenIndexSettings
argument_list|(
name|src
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|indexMetaData
argument_list|,
name|src
argument_list|)
expr_stmt|;
name|src
operator|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"-200"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|service
operator|.
name|archiveBrokenIndexSettings
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|indexMetaData
argument_list|,
name|src
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"-200"
argument_list|,
name|indexMetaData
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"archived.index.refresh_interval"
argument_list|)
argument_list|)
expr_stmt|;
name|src
operator|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.codec"
argument_list|,
literal|"best_compression1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|service
operator|.
name|archiveBrokenIndexSettings
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|indexMetaData
argument_list|,
name|src
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"best_compression1"
argument_list|,
name|indexMetaData
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"archived.index.codec"
argument_list|)
argument_list|)
expr_stmt|;
name|src
operator|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh.interval"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|service
operator|.
name|archiveBrokenIndexSettings
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|indexMetaData
argument_list|,
name|src
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"-1"
argument_list|,
name|indexMetaData
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"archived.index.refresh.interval"
argument_list|)
argument_list|)
expr_stmt|;
name|src
operator|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|indexMetaData
operator|.
name|getSettings
argument_list|()
argument_list|)
expr_stmt|;
comment|// double archive?
name|indexMetaData
operator|=
name|service
operator|.
name|archiveBrokenIndexSettings
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|indexMetaData
argument_list|,
name|src
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpgrade
specifier|public
name|void
name|testUpgrade
parameter_list|()
block|{
name|MetaDataIndexUpgradeService
name|service
init|=
operator|new
name|MetaDataIndexUpgradeService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|IndexScopedSettings
operator|.
name|DEFAULT_SCOPED_SETTINGS
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|IndexMetaData
name|src
init|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"-200"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|service
operator|.
name|isUpgraded
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|src
operator|=
name|service
operator|.
name|upgradeIndexMetaData
argument_list|(
name|src
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|service
operator|.
name|isUpgraded
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"-200"
argument_list|,
name|src
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"archived.index.refresh_interval"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|src
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"index.refresh_interval"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|src
argument_list|,
name|service
operator|.
name|upgradeIndexMetaData
argument_list|(
name|src
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// no double upgrade
block|}
DECL|method|testIsUpgraded
specifier|public
name|void
name|testIsUpgraded
parameter_list|()
block|{
name|MetaDataIndexUpgradeService
name|service
init|=
operator|new
name|MetaDataIndexUpgradeService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|IndexScopedSettings
operator|.
name|DEFAULT_SCOPED_SETTINGS
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|IndexMetaData
name|src
init|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"-200"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|service
operator|.
name|isUpgraded
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getPreviousVersion
argument_list|()
argument_list|)
decl_stmt|;
name|src
operator|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|service
operator|.
name|isUpgraded
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|src
operator|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|service
operator|.
name|isUpgraded
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailUpgrade
specifier|public
name|void
name|testFailUpgrade
parameter_list|()
block|{
name|MetaDataIndexUpgradeService
name|service
init|=
operator|new
name|MetaDataIndexUpgradeService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|IndexScopedSettings
operator|.
name|DEFAULT_SCOPED_SETTINGS
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndexMetaData
name|metaData
init|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED
argument_list|,
name|Version
operator|.
name|V_5_0_0_beta1
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
name|fromString
argument_list|(
literal|"2.4.0"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|message
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|service
operator|.
name|upgradeIndexMetaData
argument_list|(
name|metaData
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
argument_list|)
argument_list|)
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|message
argument_list|,
literal|"The index [[foo/BOOM]] was created with version [2.4.0] but the minimum compatible version is [5.0.0]."
operator|+
literal|" It should be re-indexed in Elasticsearch 5.x before upgrading to "
operator|+
name|Version
operator|.
name|CURRENT
operator|.
name|toString
argument_list|()
operator|+
literal|"."
argument_list|)
expr_stmt|;
name|IndexMetaData
name|goodMeta
init|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED
argument_list|,
name|Version
operator|.
name|V_5_0_0_beta1
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
name|fromString
argument_list|(
literal|"5.1.0"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|service
operator|.
name|upgradeIndexMetaData
argument_list|(
name|goodMeta
argument_list|,
name|Version
operator|.
name|V_5_0_0
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testPluginUpgrade
specifier|public
name|void
name|testPluginUpgrade
parameter_list|()
block|{
name|MetaDataIndexUpgradeService
name|service
init|=
operator|new
name|MetaDataIndexUpgradeService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|IndexScopedSettings
operator|.
name|DEFAULT_SCOPED_SETTINGS
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|indexMetaData
lambda|->
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexMetaData
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"10s"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|IndexMetaData
name|src
init|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"200s"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|service
operator|.
name|isUpgraded
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|src
operator|=
name|service
operator|.
name|upgradeIndexMetaData
argument_list|(
name|src
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|service
operator|.
name|isUpgraded
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"10s"
argument_list|,
name|src
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"index.refresh_interval"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|src
argument_list|,
name|service
operator|.
name|upgradeIndexMetaData
argument_list|(
name|src
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// no double upgrade
block|}
DECL|method|testPluginUpgradeFailure
specifier|public
name|void
name|testPluginUpgradeFailure
parameter_list|()
block|{
name|MetaDataIndexUpgradeService
name|service
init|=
operator|new
name|MetaDataIndexUpgradeService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|IndexScopedSettings
operator|.
name|DEFAULT_SCOPED_SETTINGS
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|indexMetaData
lambda|->
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Cannot upgrade index "
operator|+
name|indexMetaData
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|IndexMetaData
name|src
init|=
name|newIndexMeta
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|String
name|message
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|service
operator|.
name|upgradeIndexMetaData
argument_list|(
name|src
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
argument_list|)
argument_list|)
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|message
argument_list|,
literal|"Cannot upgrade index foo"
argument_list|)
expr_stmt|;
block|}
DECL|method|newIndexMeta
specifier|public
specifier|static
name|IndexMetaData
name|newIndexMeta
parameter_list|(
name|String
name|name
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|Settings
name|build
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
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
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
name|SETTING_CREATION_DATE
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
literal|"BOOM"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED
argument_list|,
name|Version
operator|.
name|V_5_0_0_beta1
argument_list|)
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|name
argument_list|)
operator|.
name|settings
argument_list|(
name|build
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|metaData
return|;
block|}
block|}
end_class

end_unit

