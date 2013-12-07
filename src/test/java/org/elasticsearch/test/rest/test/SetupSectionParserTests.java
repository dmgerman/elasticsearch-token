begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|test
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
name|xcontent
operator|.
name|yaml
operator|.
name|YamlXContent
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
name|rest
operator|.
name|parser
operator|.
name|RestTestSuiteParseContext
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
name|rest
operator|.
name|parser
operator|.
name|SetupSectionParser
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
name|rest
operator|.
name|section
operator|.
name|SetupSection
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
name|notNullValue
import|;
end_import

begin_class
DECL|class|SetupSectionParserTests
specifier|public
class|class
name|SetupSectionParserTests
extends|extends
name|AbstractParserTests
block|{
annotation|@
name|Test
DECL|method|testParseSetupSection
specifier|public
name|void
name|testParseSetupSection
parameter_list|()
throws|throws
name|Exception
block|{
name|parser
operator|=
name|YamlXContent
operator|.
name|yamlXContent
operator|.
name|createParser
argument_list|(
literal|"  - do:\n"
operator|+
literal|"      index1:\n"
operator|+
literal|"        index:  test_1\n"
operator|+
literal|"        type:   test\n"
operator|+
literal|"        id:     1\n"
operator|+
literal|"        body:   { \"include\": { \"field1\": \"v1\", \"field2\": \"v2\" }, \"count\": 1 }\n"
operator|+
literal|"  - do:\n"
operator|+
literal|"      index2:\n"
operator|+
literal|"        index:  test_1\n"
operator|+
literal|"        type:   test\n"
operator|+
literal|"        id:     2\n"
operator|+
literal|"        body:   { \"include\": { \"field1\": \"v1\", \"field2\": \"v2\" }, \"count\": 1 }\n"
argument_list|)
expr_stmt|;
name|SetupSectionParser
name|setupSectionParser
init|=
operator|new
name|SetupSectionParser
argument_list|()
decl_stmt|;
name|SetupSection
name|setupSection
init|=
name|setupSectionParser
operator|.
name|parse
argument_list|(
operator|new
name|RestTestSuiteParseContext
argument_list|(
literal|"api"
argument_list|,
literal|"suite"
argument_list|,
name|parser
argument_list|,
literal|"0.90.7"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|setupSection
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getDoSections
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
name|setupSection
operator|.
name|getDoSections
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getApiCallSection
argument_list|()
operator|.
name|getApi
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getDoSections
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getApiCallSection
argument_list|()
operator|.
name|getApi
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"index2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParseSetupAndSkipSectionSkip
specifier|public
name|void
name|testParseSetupAndSkipSectionSkip
parameter_list|()
throws|throws
name|Exception
block|{
name|parser
operator|=
name|YamlXContent
operator|.
name|yamlXContent
operator|.
name|createParser
argument_list|(
literal|"  - skip:\n"
operator|+
literal|"      version:  \"0.90.0 - 0.90.7\"\n"
operator|+
literal|"      reason:   \"Update doesn't return metadata fields, waiting for #3259\"\n"
operator|+
literal|"  - do:\n"
operator|+
literal|"      index1:\n"
operator|+
literal|"        index:  test_1\n"
operator|+
literal|"        type:   test\n"
operator|+
literal|"        id:     1\n"
operator|+
literal|"        body:   { \"include\": { \"field1\": \"v1\", \"field2\": \"v2\" }, \"count\": 1 }\n"
operator|+
literal|"  - do:\n"
operator|+
literal|"      index2:\n"
operator|+
literal|"        index:  test_1\n"
operator|+
literal|"        type:   test\n"
operator|+
literal|"        id:     2\n"
operator|+
literal|"        body:   { \"include\": { \"field1\": \"v1\", \"field2\": \"v2\" }, \"count\": 1 }\n"
argument_list|)
expr_stmt|;
name|SetupSectionParser
name|setupSectionParser
init|=
operator|new
name|SetupSectionParser
argument_list|()
decl_stmt|;
name|SetupSection
name|setupSection
init|=
name|setupSectionParser
operator|.
name|parse
argument_list|(
operator|new
name|RestTestSuiteParseContext
argument_list|(
literal|"api"
argument_list|,
literal|"suite"
argument_list|,
name|parser
argument_list|,
literal|"0.90.5"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|setupSection
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"0.90.0 - 0.90.7"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
operator|.
name|getReason
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Update doesn't return metadata fields, waiting for #3259"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getDoSections
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
block|}
annotation|@
name|Test
DECL|method|testParseSetupAndSkipSectionNoSkip
specifier|public
name|void
name|testParseSetupAndSkipSectionNoSkip
parameter_list|()
throws|throws
name|Exception
block|{
name|parser
operator|=
name|YamlXContent
operator|.
name|yamlXContent
operator|.
name|createParser
argument_list|(
literal|"  - skip:\n"
operator|+
literal|"      version:  \"0.90.0 - 0.90.7\"\n"
operator|+
literal|"      reason:   \"Update doesn't return metadata fields, waiting for #3259\"\n"
operator|+
literal|"  - do:\n"
operator|+
literal|"      index1:\n"
operator|+
literal|"        index:  test_1\n"
operator|+
literal|"        type:   test\n"
operator|+
literal|"        id:     1\n"
operator|+
literal|"        body:   { \"include\": { \"field1\": \"v1\", \"field2\": \"v2\" }, \"count\": 1 }\n"
operator|+
literal|"  - do:\n"
operator|+
literal|"      index2:\n"
operator|+
literal|"        index:  test_1\n"
operator|+
literal|"        type:   test\n"
operator|+
literal|"        id:     2\n"
operator|+
literal|"        body:   { \"include\": { \"field1\": \"v1\", \"field2\": \"v2\" }, \"count\": 1 }\n"
argument_list|)
expr_stmt|;
name|SetupSectionParser
name|setupSectionParser
init|=
operator|new
name|SetupSectionParser
argument_list|()
decl_stmt|;
name|SetupSection
name|setupSection
init|=
name|setupSectionParser
operator|.
name|parse
argument_list|(
operator|new
name|RestTestSuiteParseContext
argument_list|(
literal|"api"
argument_list|,
literal|"suite"
argument_list|,
name|parser
argument_list|,
literal|"0.90.8"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|setupSection
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"0.90.0 - 0.90.7"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getSkipSection
argument_list|()
operator|.
name|getReason
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Update doesn't return metadata fields, waiting for #3259"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getDoSections
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
name|setupSection
operator|.
name|getDoSections
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getApiCallSection
argument_list|()
operator|.
name|getApi
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setupSection
operator|.
name|getDoSections
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getApiCallSection
argument_list|()
operator|.
name|getApi
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"index2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

