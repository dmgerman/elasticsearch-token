begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.aws
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
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
name|Strings
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
name|common
operator|.
name|settings
operator|.
name|SettingsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|discovery
operator|.
name|ec2
operator|.
name|Ec2DiscoveryPlugin
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
name|ESIntegTestCase
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
name|ESIntegTestCase
operator|.
name|ThirdParty
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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

begin_comment
comment|/**  * Base class for AWS tests that require credentials.  *<p>  * You must specify {@code -Dtests.thirdparty=true -Dtests.config=/path/to/config}  * in order to run these tests.  */
end_comment

begin_class
annotation|@
name|ThirdParty
DECL|class|AbstractAwsTestCase
specifier|public
specifier|abstract
class|class
name|AbstractAwsTestCase
extends|extends
name|ESIntegTestCase
block|{
comment|/**      * Those properties are set by the AWS SDK v1.9.4 and if not ignored,      * lead to tests failure (see AbstractRandomizedTest#IGNORED_INVARIANT_PROPERTIES)      */
DECL|field|AWS_INVARIANT_PROPERTIES
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|AWS_INVARIANT_PROPERTIES
init|=
block|{
literal|"com.sun.org.apache.xml.internal.dtm.DTMManager"
block|,
literal|"javax.xml.parsers.DocumentBuilderFactory"
block|}
decl_stmt|;
DECL|field|properties
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Before
DECL|method|saveProperties
specifier|public
name|void
name|saveProperties
parameter_list|()
block|{
for|for
control|(
name|String
name|p
range|:
name|AWS_INVARIANT_PROPERTIES
control|)
block|{
name|properties
operator|.
name|put
argument_list|(
name|p
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
DECL|method|restoreProperties
specifier|public
name|void
name|restoreProperties
parameter_list|()
block|{
for|for
control|(
name|String
name|p
range|:
name|AWS_INVARIANT_PROPERTIES
control|)
block|{
if|if
condition|(
name|properties
operator|.
name|get
argument_list|(
name|p
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|p
argument_list|,
name|properties
operator|.
name|get
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|clearProperty
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|Settings
operator|.
name|Builder
name|settings
init|=
name|Settings
operator|.
name|builder
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
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
argument_list|)
operator|.
name|extendArray
argument_list|(
literal|"plugin.types"
argument_list|,
name|Ec2DiscoveryPlugin
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.aws.test.random"
argument_list|,
name|randomInt
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.aws.test.write_failures"
argument_list|,
literal|0.1
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.aws.test.read_failures"
argument_list|,
literal|0.1
argument_list|)
decl_stmt|;
comment|// if explicit, just load it and don't load from env
try|try
block|{
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.config"
argument_list|)
argument_list|)
condition|)
block|{
name|settings
operator|.
name|loadFromPath
argument_list|(
name|PathUtils
operator|.
name|get
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.config"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"to run integration tests, you need to set -Dtest.thirdparty=true and -Dtests.config=/path/to/elasticsearch.yml"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|SettingsException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"your test configuration file is incorrect: "
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.config"
argument_list|)
argument_list|,
name|exception
argument_list|)
throw|;
block|}
return|return
name|settings
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit
