begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bwcompat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bwcompat
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
name|network
operator|.
name|NetworkModule
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
name|node
operator|.
name|Node
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
name|containsString
import|;
end_import

begin_class
DECL|class|RecoveryWithUnsupportedIndicesIT
specifier|public
class|class
name|RecoveryWithUnsupportedIndicesIT
extends|extends
name|StaticIndexBackwardCompatibilityIT
block|{
DECL|method|testUpgradeStartClusterOn_0_20_6
specifier|public
name|void
name|testUpgradeStartClusterOn_0_20_6
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|indexName
init|=
literal|"unsupported-0.20.6"
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Checking static index {}"
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|Settings
name|nodeSettings
init|=
name|prepareBackwardsDataDir
argument_list|(
name|getBwcIndicesPath
argument_list|()
operator|.
name|resolve
argument_list|(
name|indexName
operator|+
literal|".zip"
argument_list|)
argument_list|,
name|NetworkModule
operator|.
name|HTTP_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|" was created before v2.0.0.beta1 and wasn't upgraded"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

