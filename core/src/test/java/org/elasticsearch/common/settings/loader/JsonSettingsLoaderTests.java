begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings.loader
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|loader
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|common
operator|.
name|xcontent
operator|.
name|XContent
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
name|CoreMatchers
operator|.
name|containsString
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
DECL|class|JsonSettingsLoaderTests
specifier|public
class|class
name|JsonSettingsLoaderTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSimpleJsonSettings
specifier|public
name|void
name|testSimpleJsonSettings
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|json
init|=
literal|"/org/elasticsearch/common/settings/loader/test-settings.json"
decl_stmt|;
specifier|final
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|loadFromStream
argument_list|(
name|json
argument_list|,
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|json
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"test1.value1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"test1.test2.value2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"test1.test2.value3"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// check array
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"test1.test3.0"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"test3-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"test1.test3.1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"test3-2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"test1.test3"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"test1.test3"
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test3-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"test1.test3"
argument_list|)
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test3-2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDuplicateKeysThrowsException
specifier|public
name|void
name|testDuplicateKeysThrowsException
parameter_list|()
block|{
name|assumeFalse
argument_list|(
literal|"Test only makes sense if XContent parser doesn't have strict duplicate checks enabled"
argument_list|,
name|XContent
operator|.
name|isStrictDuplicateDetectionEnabled
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|json
init|=
literal|"{\"foo\":\"bar\",\"foo\":\"baz\"}"
decl_stmt|;
specifier|final
name|SettingsException
name|e
init|=
name|expectThrows
argument_list|(
name|SettingsException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|loadFromSource
argument_list|(
name|json
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|ElasticsearchParseException
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"duplicate settings key [foo] "
operator|+
literal|"found at line number [1], "
operator|+
literal|"column number [20], "
operator|+
literal|"previous value [bar], "
operator|+
literal|"current value [baz]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullValuedSettingThrowsException
specifier|public
name|void
name|testNullValuedSettingThrowsException
parameter_list|()
block|{
specifier|final
name|String
name|json
init|=
literal|"{\"foo\":null}"
decl_stmt|;
specifier|final
name|ElasticsearchParseException
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|JsonSettingsLoader
argument_list|(
literal|false
argument_list|)
operator|.
name|load
argument_list|(
name|json
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"null-valued setting found for key [foo] found at line number [1], column number [8]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

