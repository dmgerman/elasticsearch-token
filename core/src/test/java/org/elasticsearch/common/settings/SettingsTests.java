begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
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
name|loader
operator|.
name|YamlSettingsLoader
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
name|hamcrest
operator|.
name|Matchers
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
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|allOf
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
name|arrayContaining
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
name|contains
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasToString
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
name|is
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
name|notNullValue
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
DECL|class|SettingsTests
specifier|public
class|class
name|SettingsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testReplacePropertiesPlaceholderSystemProperty
specifier|public
name|void
name|testReplacePropertiesPlaceholderSystemProperty
parameter_list|()
block|{
name|String
name|value
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.home"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|value
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"property.placeholder"
argument_list|,
name|value
argument_list|)
operator|.
name|put
argument_list|(
literal|"setting1"
argument_list|,
literal|"${property.placeholder}"
argument_list|)
operator|.
name|replacePropertyPlaceholders
argument_list|()
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
literal|"setting1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplacePropertiesPlaceholderSystemVariablesHaveNoEffect
specifier|public
name|void
name|testReplacePropertiesPlaceholderSystemVariablesHaveNoEffect
parameter_list|()
block|{
specifier|final
name|String
name|value
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.home"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
specifier|final
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
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"setting1"
argument_list|,
literal|"${java.home}"
argument_list|)
operator|.
name|replacePropertyPlaceholders
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
argument_list|,
name|hasToString
argument_list|(
name|containsString
argument_list|(
literal|"Could not resolve placeholder 'java.home'"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplacePropertiesPlaceholderByEnvironmentVariables
specifier|public
name|void
name|testReplacePropertiesPlaceholderByEnvironmentVariables
parameter_list|()
block|{
specifier|final
name|String
name|hostname
init|=
name|randomAsciiOfLength
argument_list|(
literal|16
argument_list|)
decl_stmt|;
specifier|final
name|Settings
name|implicitEnvSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"setting1"
argument_list|,
literal|"${HOSTNAME}"
argument_list|)
operator|.
name|replacePropertyPlaceholders
argument_list|(
name|name
lambda|->
literal|"HOSTNAME"
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|?
name|hostname
else|:
literal|null
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|implicitEnvSettings
operator|.
name|get
argument_list|(
literal|"setting1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|hostname
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplacePropertiesPlaceholderIgnoresPrompt
specifier|public
name|void
name|testReplacePropertiesPlaceholderIgnoresPrompt
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"setting1"
argument_list|,
literal|"${prompt.text}"
argument_list|)
operator|.
name|put
argument_list|(
literal|"setting2"
argument_list|,
literal|"${prompt.secret}"
argument_list|)
operator|.
name|replacePropertyPlaceholders
argument_list|()
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
literal|"setting1"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"${prompt.text}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"setting2"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"${prompt.secret}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnFlattenedSettings
specifier|public
name|void
name|testUnFlattenedSettings
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
literal|"def"
argument_list|)
operator|.
name|put
argument_list|(
literal|"baz.foo"
argument_list|,
literal|"ghi"
argument_list|)
operator|.
name|put
argument_list|(
literal|"baz.bar"
argument_list|,
literal|"jkl"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"baz.arr"
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|settings
operator|.
name|getAsStructuredMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|keySet
argument_list|()
argument_list|,
name|Matchers
operator|.
expr|<
name|String
operator|>
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
argument_list|,
name|allOf
argument_list|(
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
argument_list|,
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"bar"
argument_list|,
literal|"def"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|bazMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"baz"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|bazMap
operator|.
name|keySet
argument_list|()
argument_list|,
name|Matchers
operator|.
expr|<
name|String
operator|>
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bazMap
argument_list|,
name|allOf
argument_list|(
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo"
argument_list|,
literal|"ghi"
argument_list|)
argument_list|,
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"bar"
argument_list|,
literal|"jkl"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|String
argument_list|>
name|bazArr
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|bazMap
operator|.
name|get
argument_list|(
literal|"arr"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|bazArr
argument_list|,
name|contains
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFallbackToFlattenedSettings
specifier|public
name|void
name|testFallbackToFlattenedSettings
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"def"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.baz"
argument_list|,
literal|"ghi"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|settings
operator|.
name|getAsStructuredMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|keySet
argument_list|()
argument_list|,
name|Matchers
operator|.
expr|<
name|String
operator|>
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
argument_list|,
name|allOf
argument_list|(
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
argument_list|,
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"def"
argument_list|)
argument_list|,
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo.baz"
argument_list|,
literal|"ghi"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"def"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.baz"
argument_list|,
literal|"ghi"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|map
operator|=
name|settings
operator|.
name|getAsStructuredMap
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|keySet
argument_list|()
argument_list|,
name|Matchers
operator|.
expr|<
name|String
operator|>
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
argument_list|,
name|allOf
argument_list|(
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
argument_list|,
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"def"
argument_list|)
argument_list|,
name|Matchers
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|hasEntry
argument_list|(
literal|"foo.baz"
argument_list|,
literal|"ghi"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetAsSettings
specifier|public
name|void
name|testGetAsSettings
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"def"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.baz"
argument_list|,
literal|"ghi"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|fooSettings
init|=
name|settings
operator|.
name|getAsSettings
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fooSettings
operator|.
name|get
argument_list|(
literal|"bar"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"def"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fooSettings
operator|.
name|get
argument_list|(
literal|"baz"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ghi"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNames
specifier|public
name|void
name|testNames
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"abc"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"def"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.baz"
argument_list|,
literal|"ghi"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|names
init|=
name|settings
operator|.
name|names
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|names
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|Settings
name|fooSettings
init|=
name|settings
operator|.
name|getAsSettings
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|names
operator|=
name|fooSettings
operator|.
name|names
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|names
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatArraysAreOverriddenCorrectly
specifier|public
name|void
name|testThatArraysAreOverriddenCorrectly
parameter_list|()
throws|throws
name|IOException
block|{
comment|// overriding a single value with an array
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"2"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"value"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"2"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
operator|new
name|YamlSettingsLoader
argument_list|(
literal|false
argument_list|)
operator|.
name|load
argument_list|(
literal|"value: 1"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
operator|new
name|YamlSettingsLoader
argument_list|(
literal|false
argument_list|)
operator|.
name|load
argument_list|(
literal|"value: [ 2, 3 ]"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"2"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"value.with.deep.key"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.with.deep.key"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value.with.deep.key"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"2"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
comment|// overriding an array with a shorter array
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"4"
argument_list|,
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value.deep.key"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"4"
argument_list|,
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
comment|// overriding an array with a longer array
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"3"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"3"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value.deep.key"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"4"
argument_list|,
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
comment|// overriding an array with a single value
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"value"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value.deep.key"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
comment|// test that other arrays are not overridden
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"d"
argument_list|,
literal|"e"
argument_list|,
literal|"f"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"4"
argument_list|,
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"b"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"d"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"e"
argument_list|,
literal|"f"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value.deep.key"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"d"
argument_list|,
literal|"e"
argument_list|,
literal|"f"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value.deep.key"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"4"
argument_list|,
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"d"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// overriding a deeper structure with an array
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"value.data"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"4"
argument_list|,
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
comment|// overriding an array with a deeper structure
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"value"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"value.data"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"value.data"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|is
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPrefixNormalization
specifier|public
name|void
name|testPrefixNormalization
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|normalizePrefix
argument_list|(
literal|"foo."
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|names
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|normalizePrefix
argument_list|(
literal|"foo."
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"bar"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"foo.bar"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|put
argument_list|(
literal|"foo.test"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|normalizePrefix
argument_list|(
literal|"foo."
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|size
argument_list|()
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
name|get
argument_list|(
literal|"bar"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"foo.bar"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"foo.test"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"foo.test"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|normalizePrefix
argument_list|(
literal|"foo."
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"foo.test"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

