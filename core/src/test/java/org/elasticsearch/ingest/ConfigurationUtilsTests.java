begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|test
operator|.
name|ESTestCase
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
name|nullValue
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
name|sameInstance
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_class
DECL|class|ConfigurationUtilsTests
specifier|public
class|class
name|ConfigurationUtilsTests
extends|extends
name|ESTestCase
block|{
DECL|field|config
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
decl_stmt|;
annotation|@
name|Before
DECL|method|setConfig
specifier|public
name|void
name|setConfig
parameter_list|()
block|{
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"boolVal"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"null"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"arr"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"int"
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"ip"
argument_list|,
literal|"127.0.0.1"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|fizz
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|fizz
operator|.
name|put
argument_list|(
literal|"buzz"
argument_list|,
literal|"hello world"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"fizz"
argument_list|,
name|fizz
argument_list|)
expr_stmt|;
block|}
DECL|method|testReadStringProperty
specifier|public
name|void
name|testReadStringProperty
parameter_list|()
block|{
name|String
name|val
init|=
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|val
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReadStringPropertyInvalidType
specifier|public
name|void
name|testReadStringPropertyInvalidType
parameter_list|()
block|{
try|try
block|{
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
literal|"arr"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[arr] property isn't a string, but of type [java.util.Arrays$ArrayList]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testReadBooleanProperty
specifier|public
name|void
name|testReadBooleanProperty
parameter_list|()
block|{
name|Boolean
name|val
init|=
name|ConfigurationUtils
operator|.
name|readBooleanProperty
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
literal|"boolVal"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|val
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReadNullBooleanProperty
specifier|public
name|void
name|testReadNullBooleanProperty
parameter_list|()
block|{
name|Boolean
name|val
init|=
name|ConfigurationUtils
operator|.
name|readBooleanProperty
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
literal|"null"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|val
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReadBooleanPropertyInvalidType
specifier|public
name|void
name|testReadBooleanPropertyInvalidType
parameter_list|()
block|{
try|try
block|{
name|ConfigurationUtils
operator|.
name|readBooleanProperty
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
literal|"arr"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[arr] property isn't a boolean, but of type [java.util.Arrays$ArrayList]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// TODO(talevy): Issue with generics. This test should fail, "int" is of type List<Integer>
DECL|method|testOptional_InvalidType
specifier|public
name|void
name|testOptional_InvalidType
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|val
init|=
name|ConfigurationUtils
operator|.
name|readList
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
literal|"int"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|val
argument_list|,
name|equalTo
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReadProcessors
specifier|public
name|void
name|testReadProcessors
parameter_list|()
throws|throws
name|Exception
block|{
name|Processor
name|processor
init|=
name|mock
argument_list|(
name|Processor
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|registry
init|=
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test_processor"
argument_list|,
parameter_list|(
name|factories
parameter_list|,
name|tag
parameter_list|,
name|config
parameter_list|)
lambda|->
name|processor
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|>
name|config
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|emptyConfig
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
name|config
operator|.
name|add
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test_processor"
argument_list|,
name|emptyConfig
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|add
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test_processor"
argument_list|,
name|emptyConfig
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Processor
argument_list|>
name|result
init|=
name|ConfigurationUtils
operator|.
name|readProcessorConfigs
argument_list|(
name|config
argument_list|,
name|registry
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
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
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|processor
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|processor
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|unknownTaggedConfig
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|unknownTaggedConfig
operator|.
name|put
argument_list|(
literal|"tag"
argument_list|,
literal|"my_unknown"
argument_list|)
expr_stmt|;
name|config
operator|.
name|add
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"unknown_processor"
argument_list|,
name|unknownTaggedConfig
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|ConfigurationUtils
operator|.
name|readProcessorConfigs
argument_list|(
name|config
argument_list|,
name|registry
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"No processor type exists with name [unknown_processor]"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getHeader
argument_list|(
literal|"processor_tag"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"my_unknown"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getHeader
argument_list|(
literal|"processor_type"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"unknown_processor"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getHeader
argument_list|(
literal|"property_name"
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
block|}
block|}
end_class

end_unit

