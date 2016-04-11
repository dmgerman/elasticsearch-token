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
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
DECL|class|YamlSettingsLoaderTests
specifier|public
class|class
name|YamlSettingsLoaderTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSimpleYamlSettings
specifier|public
name|void
name|testSimpleYamlSettings
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|yaml
init|=
literal|"/org/elasticsearch/common/settings/loader/test-settings.yml"
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
name|yaml
argument_list|,
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|yaml
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
DECL|method|testIndentation
specifier|public
name|void
name|testIndentation
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|yaml
init|=
literal|"/org/elasticsearch/common/settings/loader/indentation-settings.yml"
decl_stmt|;
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
block|{
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|loadFromStream
argument_list|(
name|yaml
argument_list|,
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|yaml
argument_list|)
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
literal|"malformed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndentationWithExplicitDocumentStart
specifier|public
name|void
name|testIndentationWithExplicitDocumentStart
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|yaml
init|=
literal|"/org/elasticsearch/common/settings/loader/indentation-with-explicit-document-start-settings.yml"
decl_stmt|;
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
block|{
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|loadFromStream
argument_list|(
name|yaml
argument_list|,
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|yaml
argument_list|)
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
literal|"malformed"
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
name|String
name|yaml
init|=
literal|"foo: bar\nfoo: baz"
decl_stmt|;
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
block|{
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|loadFromSource
argument_list|(
name|yaml
argument_list|)
expr_stmt|;
block|}
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
name|String
name|msg
init|=
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|msg
argument_list|,
name|msg
operator|.
name|contains
argument_list|(
literal|"duplicate settings key [foo] found"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|msg
argument_list|,
name|msg
operator|.
name|contains
argument_list|(
literal|"previous value [bar], current value [baz]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingValue
specifier|public
name|void
name|testMissingValue
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmp
init|=
name|createTempFile
argument_list|(
literal|"test"
argument_list|,
literal|".yaml"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|tmp
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"foo: # missing value\n"
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
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
block|{
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|loadFromPath
argument_list|(
name|tmp
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
literal|"null-valued setting found for key [foo] found at line"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

