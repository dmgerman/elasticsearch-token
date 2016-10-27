begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|analyze
operator|.
name|AnalyzeRequest
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
name|ParseFieldMatcher
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
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|xcontent
operator|.
name|XContentFactory
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|startsWith
import|;
end_import

begin_class
DECL|class|RestAnalyzeActionTests
specifier|public
class|class
name|RestAnalyzeActionTests
extends|extends
name|ESTestCase
block|{
DECL|method|testParseXContentForAnalyzeRequest
specifier|public
name|void
name|testParseXContentForAnalyzeRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesReference
name|content
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"THIS IS A TEST"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tokenizer"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|array
argument_list|(
literal|"filter"
argument_list|,
literal|"lowercase"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|AnalyzeRequest
name|analyzeRequest
init|=
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
decl_stmt|;
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|content
argument_list|,
name|analyzeRequest
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|text
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|text
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"THIS IS A TEST"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|tokenizer
argument_list|()
operator|.
name|name
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|tokenFilters
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
for|for
control|(
name|AnalyzeRequest
operator|.
name|NameOrDefinition
name|filter
range|:
name|analyzeRequest
operator|.
name|tokenFilters
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|filter
operator|.
name|name
argument_list|,
name|equalTo
argument_list|(
literal|"lowercase"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParseXContentForAnalyzeRequestWithCustomFilters
specifier|public
name|void
name|testParseXContentForAnalyzeRequestWithCustomFilters
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesReference
name|content
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"THIS IS A TEST"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tokenizer"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"filter"
argument_list|)
operator|.
name|value
argument_list|(
literal|"lowercase"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"stop"
argument_list|)
operator|.
name|array
argument_list|(
literal|"stopwords"
argument_list|,
literal|"foo"
argument_list|,
literal|"buzz"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"char_filter"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"mapping"
argument_list|)
operator|.
name|array
argument_list|(
literal|"mappings"
argument_list|,
literal|"ph => f"
argument_list|,
literal|"qu => q"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|AnalyzeRequest
name|analyzeRequest
init|=
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
decl_stmt|;
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|content
argument_list|,
name|analyzeRequest
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|text
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|text
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"THIS IS A TEST"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|tokenizer
argument_list|()
operator|.
name|name
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|tokenFilters
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
name|analyzeRequest
operator|.
name|tokenFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|name
argument_list|,
name|equalTo
argument_list|(
literal|"lowercase"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|tokenFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|definition
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analyzeRequest
operator|.
name|charFilters
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
name|analyzeRequest
operator|.
name|charFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|definition
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseXContentForAnalyzeRequestWithInvalidJsonThrowsException
specifier|public
name|void
name|testParseXContentForAnalyzeRequestWithInvalidJsonThrowsException
parameter_list|()
throws|throws
name|Exception
block|{
name|AnalyzeRequest
name|analyzeRequest
init|=
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
decl_stmt|;
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
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
operator|new
name|BytesArray
argument_list|(
literal|"{invalid_json}"
argument_list|)
argument_list|,
name|analyzeRequest
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Failed to parse request body"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseXContentForAnalyzeRequestWithUnknownParamThrowsException
specifier|public
name|void
name|testParseXContentForAnalyzeRequestWithUnknownParamThrowsException
parameter_list|()
throws|throws
name|Exception
block|{
name|AnalyzeRequest
name|analyzeRequest
init|=
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
decl_stmt|;
name|BytesReference
name|invalidContent
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"THIS IS A TEST"
argument_list|)
operator|.
name|field
argument_list|(
literal|"unknown"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
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
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|invalidContent
argument_list|,
name|analyzeRequest
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"Unknown parameter [unknown]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseXContentForAnalyzeRequestWithInvalidStringExplainParamThrowsException
specifier|public
name|void
name|testParseXContentForAnalyzeRequestWithInvalidStringExplainParamThrowsException
parameter_list|()
throws|throws
name|Exception
block|{
name|AnalyzeRequest
name|analyzeRequest
init|=
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
decl_stmt|;
name|BytesReference
name|invalidExplain
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"explain"
argument_list|,
literal|"fals"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
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
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|invalidExplain
argument_list|,
name|analyzeRequest
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"explain must be either 'true' or 'false'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeprecatedParamIn2xException
specifier|public
name|void
name|testDeprecatedParamIn2xException
parameter_list|()
throws|throws
name|Exception
block|{
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
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"THIS IS A TEST"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tokenizer"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|array
argument_list|(
literal|"filters"
argument_list|,
literal|"lowercase"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"Unknown parameter [filters]"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"THIS IS A TEST"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tokenizer"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|array
argument_list|(
literal|"token_filters"
argument_list|,
literal|"lowercase"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"Unknown parameter [token_filters]"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"THIS IS A TEST"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tokenizer"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|array
argument_list|(
literal|"char_filters"
argument_list|,
literal|"lowercase"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"Unknown parameter [char_filters]"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|RestAnalyzeAction
operator|.
name|buildFromContent
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"THIS IS A TEST"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tokenizer"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|array
argument_list|(
literal|"token_filter"
argument_list|,
literal|"lowercase"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
operator|new
name|AnalyzeRequest
argument_list|(
literal|"for test"
argument_list|)
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"Unknown parameter [token_filter]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

