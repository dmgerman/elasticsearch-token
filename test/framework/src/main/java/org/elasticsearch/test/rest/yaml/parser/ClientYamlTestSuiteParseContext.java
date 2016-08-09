begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.yaml.parser
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|yaml
operator|.
name|parser
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
name|ParseFieldMatcherSupplier
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
name|collect
operator|.
name|Tuple
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
name|XContentLocation
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
name|XContentParser
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
name|yaml
operator|.
name|section
operator|.
name|DoSection
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
name|yaml
operator|.
name|section
operator|.
name|ExecutableSection
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
name|yaml
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
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|yaml
operator|.
name|section
operator|.
name|SkipSection
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
name|yaml
operator|.
name|section
operator|.
name|TeardownSection
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
name|yaml
operator|.
name|section
operator|.
name|ClientYamlTestSection
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

begin_comment
comment|/**  * Context shared across the whole tests parse phase.  * Provides shared parse methods and holds information needed to parse the test sections (e.g. es version)  */
end_comment

begin_class
DECL|class|ClientYamlTestSuiteParseContext
specifier|public
class|class
name|ClientYamlTestSuiteParseContext
implements|implements
name|ParseFieldMatcherSupplier
block|{
DECL|field|SETUP_SECTION_PARSER
specifier|private
specifier|static
specifier|final
name|SetupSectionParser
name|SETUP_SECTION_PARSER
init|=
operator|new
name|SetupSectionParser
argument_list|()
decl_stmt|;
DECL|field|TEARDOWN_SECTION_PARSER
specifier|private
specifier|static
specifier|final
name|TeardownSectionParser
name|TEARDOWN_SECTION_PARSER
init|=
operator|new
name|TeardownSectionParser
argument_list|()
decl_stmt|;
DECL|field|TEST_SECTION_PARSER
specifier|private
specifier|static
specifier|final
name|ClientYamlTestSectionParser
name|TEST_SECTION_PARSER
init|=
operator|new
name|ClientYamlTestSectionParser
argument_list|()
decl_stmt|;
DECL|field|SKIP_SECTION_PARSER
specifier|private
specifier|static
specifier|final
name|SkipSectionParser
name|SKIP_SECTION_PARSER
init|=
operator|new
name|SkipSectionParser
argument_list|()
decl_stmt|;
DECL|field|DO_SECTION_PARSER
specifier|private
specifier|static
specifier|final
name|DoSectionParser
name|DO_SECTION_PARSER
init|=
operator|new
name|DoSectionParser
argument_list|()
decl_stmt|;
DECL|field|EXECUTABLE_SECTIONS_PARSERS
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ClientYamlTestFragmentParser
argument_list|<
name|?
extends|extends
name|ExecutableSection
argument_list|>
argument_list|>
name|EXECUTABLE_SECTIONS_PARSERS
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"do"
argument_list|,
name|DO_SECTION_PARSER
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"set"
argument_list|,
operator|new
name|SetSectionParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"match"
argument_list|,
operator|new
name|MatchParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"is_true"
argument_list|,
operator|new
name|IsTrueParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"is_false"
argument_list|,
operator|new
name|IsFalseParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"gt"
argument_list|,
operator|new
name|GreaterThanParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"gte"
argument_list|,
operator|new
name|GreaterThanEqualToParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"lt"
argument_list|,
operator|new
name|LessThanParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"lte"
argument_list|,
operator|new
name|LessThanOrEqualToParser
argument_list|()
argument_list|)
expr_stmt|;
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|put
argument_list|(
literal|"length"
argument_list|,
operator|new
name|LengthParser
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|field|api
specifier|private
specifier|final
name|String
name|api
decl_stmt|;
DECL|field|suiteName
specifier|private
specifier|final
name|String
name|suiteName
decl_stmt|;
DECL|field|parser
specifier|private
specifier|final
name|XContentParser
name|parser
decl_stmt|;
DECL|method|ClientYamlTestSuiteParseContext
specifier|public
name|ClientYamlTestSuiteParseContext
parameter_list|(
name|String
name|api
parameter_list|,
name|String
name|suiteName
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
block|{
name|this
operator|.
name|api
operator|=
name|api
expr_stmt|;
name|this
operator|.
name|suiteName
operator|=
name|suiteName
expr_stmt|;
name|this
operator|.
name|parser
operator|=
name|parser
expr_stmt|;
block|}
DECL|method|getApi
specifier|public
name|String
name|getApi
parameter_list|()
block|{
return|return
name|api
return|;
block|}
DECL|method|getSuiteName
specifier|public
name|String
name|getSuiteName
parameter_list|()
block|{
return|return
name|suiteName
return|;
block|}
DECL|method|parser
specifier|public
name|XContentParser
name|parser
parameter_list|()
block|{
return|return
name|parser
return|;
block|}
DECL|method|parseSetupSection
specifier|public
name|SetupSection
name|parseSetupSection
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
name|advanceToFieldName
argument_list|()
expr_stmt|;
if|if
condition|(
literal|"setup"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|SetupSection
name|setupSection
init|=
name|SETUP_SECTION_PARSER
operator|.
name|parse
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|setupSection
return|;
block|}
return|return
name|SetupSection
operator|.
name|EMPTY
return|;
block|}
DECL|method|parseTeardownSection
specifier|public
name|TeardownSection
name|parseTeardownSection
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
name|advanceToFieldName
argument_list|()
expr_stmt|;
if|if
condition|(
literal|"teardown"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|TeardownSection
name|teardownSection
init|=
name|TEARDOWN_SECTION_PARSER
operator|.
name|parse
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|teardownSection
return|;
block|}
return|return
name|TeardownSection
operator|.
name|EMPTY
return|;
block|}
DECL|method|parseTestSection
specifier|public
name|ClientYamlTestSection
name|parseTestSection
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
return|return
name|TEST_SECTION_PARSER
operator|.
name|parse
argument_list|(
name|this
argument_list|)
return|;
block|}
DECL|method|parseSkipSection
specifier|public
name|SkipSection
name|parseSkipSection
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
name|advanceToFieldName
argument_list|()
expr_stmt|;
if|if
condition|(
literal|"skip"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|SkipSection
name|skipSection
init|=
name|SKIP_SECTION_PARSER
operator|.
name|parse
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|skipSection
return|;
block|}
return|return
name|SkipSection
operator|.
name|EMPTY
return|;
block|}
DECL|method|parseExecutableSection
specifier|public
name|ExecutableSection
name|parseExecutableSection
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
name|advanceToFieldName
argument_list|()
expr_stmt|;
name|String
name|section
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|ClientYamlTestFragmentParser
argument_list|<
name|?
extends|extends
name|ExecutableSection
argument_list|>
name|execSectionParser
init|=
name|EXECUTABLE_SECTIONS_PARSERS
operator|.
name|get
argument_list|(
name|section
argument_list|)
decl_stmt|;
if|if
condition|(
name|execSectionParser
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ClientYamlTestParseException
argument_list|(
literal|"no parser found for executable section ["
operator|+
name|section
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|XContentLocation
name|location
init|=
name|parser
operator|.
name|getTokenLocation
argument_list|()
decl_stmt|;
try|try
block|{
name|ExecutableSection
name|executableSection
init|=
name|execSectionParser
operator|.
name|parse
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|executableSection
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error parsing section starting at ["
operator|+
name|location
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|parseDoSection
specifier|public
name|DoSection
name|parseDoSection
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
return|return
name|DO_SECTION_PARSER
operator|.
name|parse
argument_list|(
name|this
argument_list|)
return|;
block|}
DECL|method|advanceToFieldName
specifier|public
name|void
name|advanceToFieldName
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
comment|//we are in the beginning, haven't called nextToken yet
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
throw|throw
operator|new
name|ClientYamlTestParseException
argument_list|(
literal|"malformed test section: field name expected but found "
operator|+
name|token
operator|+
literal|" at "
operator|+
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
DECL|method|parseField
specifier|public
name|String
name|parseField
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
assert|assert
name|parser
operator|.
name|currentToken
argument_list|()
operator|.
name|isValue
argument_list|()
assert|;
name|String
name|field
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|field
return|;
block|}
DECL|method|parseTuple
specifier|public
name|Tuple
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|parseTuple
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClientYamlTestParseException
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|advanceToFieldName
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
assert|assert
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
assert|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|map
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|ClientYamlTestParseException
argument_list|(
literal|"expected key value pair but found "
operator|+
name|map
operator|.
name|size
argument_list|()
operator|+
literal|" "
argument_list|)
throw|;
block|}
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|map
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getParseFieldMatcher
specifier|public
name|ParseFieldMatcher
name|getParseFieldMatcher
parameter_list|()
block|{
return|return
name|ParseFieldMatcher
operator|.
name|STRICT
return|;
block|}
block|}
end_class

end_unit

