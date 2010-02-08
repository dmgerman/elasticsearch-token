begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.settings.loader
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
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
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|JsonSettingsLoaderTests
specifier|public
class|class
name|JsonSettingsLoaderTests
block|{
DECL|method|testSimpleJsonSettings
annotation|@
name|Test
specifier|public
name|void
name|testSimpleJsonSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
literal|"org/elasticsearch/util/settings/loader/test-settings.json"
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
block|}
end_class

end_unit

