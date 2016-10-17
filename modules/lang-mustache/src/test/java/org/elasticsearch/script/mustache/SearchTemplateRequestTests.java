begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.mustache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|mustache
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
name|ParsingException
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
name|script
operator|.
name|ScriptService
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
name|java
operator|.
name|util
operator|.
name|List
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
name|hasEntry
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
name|hasItems
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
name|hasKey
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
DECL|class|SearchTemplateRequestTests
specifier|public
class|class
name|SearchTemplateRequestTests
extends|extends
name|ESTestCase
block|{
DECL|method|testParseInlineTemplate
specifier|public
name|void
name|testParseInlineTemplate
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{"
operator|+
literal|"    'inline' : {\n"
operator|+
literal|"    'query': {\n"
operator|+
literal|"      'terms': {\n"
operator|+
literal|"        'status': [\n"
operator|+
literal|"          '{{#status}}',\n"
operator|+
literal|"          '{{.}}',\n"
operator|+
literal|"          '{{/status}}'\n"
operator|+
literal|"        ]\n"
operator|+
literal|"      }\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }"
operator|+
literal|"}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{\"query\":{\"terms\":{\"status\":[\"{{#status}}\",\"{{.}}\",\"{{/status}}\"]}}}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseInlineTemplateWithParams
specifier|public
name|void
name|testParseInlineTemplateWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{"
operator|+
literal|"    'inline' : {"
operator|+
literal|"      'query': { 'match' : { '{{my_field}}' : '{{my_value}}' } },"
operator|+
literal|"      'size' : '{{my_size}}'"
operator|+
literal|"    },"
operator|+
literal|"    'params' : {"
operator|+
literal|"        'my_field' : 'foo',"
operator|+
literal|"        'my_value' : 'bar',"
operator|+
literal|"        'my_size' : 5"
operator|+
literal|"    }"
operator|+
literal|"}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{\"query\":{\"match\":{\"{{my_field}}\":\"{{my_value}}\"}},\"size\":\"{{my_size}}\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|hasEntry
argument_list|(
literal|"my_field"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|hasEntry
argument_list|(
literal|"my_value"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|hasEntry
argument_list|(
literal|"my_size"
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseInlineTemplateAsString
specifier|public
name|void
name|testParseInlineTemplateAsString
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{'inline' : '{\\\"query\\\":{\\\"bool\\\":{\\\"must\\\":{\\\"match\\\":{\\\"foo\\\":\\\"{{text}}\\\"}}}}}'}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{\"query\":{\"bool\":{\"must\":{\"match\":{\"foo\":\"{{text}}\"}}}}}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testParseInlineTemplateAsStringWithParams
specifier|public
name|void
name|testParseInlineTemplateAsStringWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{'inline' : '{\\\"query\\\":{\\\"match\\\":{\\\"{{field}}\\\":\\\"{{value}}\\\"}}}', "
operator|+
literal|"'params': {'status': ['pending', 'published']}}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{\"query\":{\"match\":{\"{{field}}\":\"{{value}}\"}}}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
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
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|hasKey
argument_list|(
literal|"status"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|request
operator|.
name|getScriptParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"status"
argument_list|)
argument_list|,
name|hasItems
argument_list|(
literal|"pending"
argument_list|,
literal|"published"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseFileTemplate
specifier|public
name|void
name|testParseFileTemplate
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{'file' : 'fileTemplate'}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"fileTemplate"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|FILE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseFileTemplateWithParams
specifier|public
name|void
name|testParseFileTemplateWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{'file' : 'template_foo', 'params' : {'foo': 'bar', 'size': 500}}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"template_foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|FILE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
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
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|hasEntry
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|hasEntry
argument_list|(
literal|"size"
argument_list|,
literal|500
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseStoredTemplate
specifier|public
name|void
name|testParseStoredTemplate
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{'id' : 'storedTemplate'}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"storedTemplate"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|STORED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseStoredTemplateWithParams
specifier|public
name|void
name|testParseStoredTemplateWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{'id' : 'another_template', 'params' : {'bar': 'foo'}}"
decl_stmt|;
name|SearchTemplateRequest
name|request
init|=
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"another_template"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|STORED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getScriptParams
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
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|,
name|hasEntry
argument_list|(
literal|"bar"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseWrongTemplate
specifier|public
name|void
name|testParseWrongTemplate
parameter_list|()
block|{
comment|// Unclosed template id
name|expectThrows
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|RestSearchTemplateAction
operator|.
name|parse
argument_list|(
name|newBytesReference
argument_list|(
literal|"{'id' : 'another_temp }"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a {@link BytesReference} with the given string while replacing single quote to double quotes.      */
DECL|method|newBytesReference
specifier|private
specifier|static
name|BytesReference
name|newBytesReference
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|s
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesArray
argument_list|(
name|s
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit
