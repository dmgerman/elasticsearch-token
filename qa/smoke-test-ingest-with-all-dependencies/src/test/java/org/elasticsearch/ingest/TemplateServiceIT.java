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
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|TemplateService
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
name|Map
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
DECL|class|TemplateServiceIT
specifier|public
class|class
name|TemplateServiceIT
extends|extends
name|AbstractMustacheTestCase
block|{
DECL|method|testTemplates
specifier|public
name|void
name|testTemplates
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|model
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|model
operator|.
name|put
argument_list|(
literal|"fielda"
argument_list|,
literal|"value1"
argument_list|)
expr_stmt|;
name|model
operator|.
name|put
argument_list|(
literal|"fieldb"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"fieldc"
argument_list|,
literal|"value3"
argument_list|)
argument_list|)
expr_stmt|;
name|TemplateService
operator|.
name|Template
name|template
init|=
name|templateService
operator|.
name|compile
argument_list|(
literal|"{{fielda}}/{{fieldb}}/{{fieldb.fieldc}}"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|execute
argument_list|(
name|model
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1/{fieldc=value3}/value3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testWrongTemplateUsage
specifier|public
name|void
name|testWrongTemplateUsage
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|model
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
name|TemplateService
operator|.
name|Template
name|template
init|=
name|templateService
operator|.
name|compile
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|execute
argument_list|(
name|model
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|template
operator|=
name|templateService
operator|.
name|compile
argument_list|(
literal|"value {{"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|execute
argument_list|(
name|model
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value {{"
argument_list|)
argument_list|)
expr_stmt|;
name|template
operator|=
name|templateService
operator|.
name|compile
argument_list|(
literal|"value {{abc"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|execute
argument_list|(
name|model
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value {{abc"
argument_list|)
argument_list|)
expr_stmt|;
name|template
operator|=
name|templateService
operator|.
name|compile
argument_list|(
literal|"value }}"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|execute
argument_list|(
name|model
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value }}"
argument_list|)
argument_list|)
expr_stmt|;
name|template
operator|=
name|templateService
operator|.
name|compile
argument_list|(
literal|"value }} {{"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|execute
argument_list|(
name|model
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value }} {{"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
