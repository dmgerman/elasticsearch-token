begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
name|test
operator|.
name|ElasticsearchTestCase
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SettingsValidatorTests
specifier|public
class|class
name|SettingsValidatorTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testValidators
specifier|public
name|void
name|testValidators
parameter_list|()
throws|throws
name|Exception
block|{
name|assertThat
argument_list|(
name|Validator
operator|.
name|EMPTY
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"anything goes"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|TIME
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10m"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|TIME
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10g"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|TIME
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"bad timing"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10m"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10g"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"bad"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|FLOAT
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|FLOAT
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2.3"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_FLOAT
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_FLOAT
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"0.0"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_FLOAT
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"-1.0"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_FLOAT
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2.3"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|DOUBLE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|DOUBLE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2.3"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|DOUBLE_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|DOUBLE_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"2.0"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|DOUBLE_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"1.0"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|DOUBLE_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2.3"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_DOUBLE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_DOUBLE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"0.0"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_DOUBLE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"-1.0"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_DOUBLE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2.3"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|INTEGER_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|INTEGER_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|INTEGER_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"0"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|INTEGER_GTE_2
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2.3"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"0"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"-1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|POSITIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|POSITIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|POSITIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"0"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|POSITIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"-1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|POSITIVE_INTEGER
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"10.2"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"asdasd"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"-1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// nocommit require % too:
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"20"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"-1%"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"101%"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"100%"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"99%"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"0%"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"asdasd"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"20"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"20mb"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"-1%"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"101%"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"100%"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"99%"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
operator|.
name|validate
argument_list|(
literal|""
argument_list|,
literal|"0%"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDynamicValidators
specifier|public
name|void
name|testDynamicValidators
parameter_list|()
throws|throws
name|Exception
block|{
name|DynamicSettings
name|ds
init|=
operator|new
name|DynamicSettings
argument_list|()
decl_stmt|;
name|ds
operator|.
name|addDynamicSetting
argument_list|(
literal|"my.test.*"
argument_list|,
name|Validator
operator|.
name|POSITIVE_INTEGER
argument_list|)
expr_stmt|;
name|String
name|valid
init|=
name|ds
operator|.
name|validateDynamicSetting
argument_list|(
literal|"my.test.setting"
argument_list|,
literal|"-1"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valid
argument_list|,
name|equalTo
argument_list|(
literal|"the value of the setting my.test.setting must be a positive integer"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

