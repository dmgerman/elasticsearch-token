begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
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
name|AbstractProcessorFactory
import|;
end_import

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
name|Processor
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
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

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
name|Locale
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
DECL|class|DateProcessorFactoryTests
specifier|public
class|class
name|DateProcessorFactoryTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBuildDefaults
specifier|public
name|void
name|testBuildDefaults
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"dd/MM/yyyyy"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|processorTag
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
name|AbstractProcessorFactory
operator|.
name|TAG_KEY
argument_list|,
name|processorTag
argument_list|)
expr_stmt|;
name|DateProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTag
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|processorTag
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getMatchField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|sourceField
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DateProcessor
operator|.
name|DEFAULT_TARGET_FIELD
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getMatchFormats
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"dd/MM/yyyyy"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getLocale
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTimezone
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMatchFieldIsMandatory
specifier|public
name|void
name|testMatchFieldIsMandatory
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|targetField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
name|targetField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"dd/MM/yyyyy"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor creation should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfigurationPropertyException
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
name|containsString
argument_list|(
literal|"[match_field] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMatchFormatsIsMandatory
specifier|public
name|void
name|testMatchFormatsIsMandatory
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|targetField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
name|targetField
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor creation should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfigurationPropertyException
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
name|containsString
argument_list|(
literal|"[match_formats] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParseLocale
specifier|public
name|void
name|testParseLocale
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"dd/MM/yyyyy"
argument_list|)
argument_list|)
expr_stmt|;
name|Locale
name|locale
init|=
name|randomLocale
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"locale"
argument_list|,
name|locale
operator|.
name|toLanguageTag
argument_list|()
argument_list|)
expr_stmt|;
name|DateProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getLocale
argument_list|()
operator|.
name|toLanguageTag
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|locale
operator|.
name|toLanguageTag
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseInvalidLocale
specifier|public
name|void
name|testParseInvalidLocale
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"dd/MM/yyyyy"
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"locale"
argument_list|,
literal|"invalid_locale"
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail with invalid locale"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
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
literal|"Invalid language tag specified: invalid_locale"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParseTimezone
specifier|public
name|void
name|testParseTimezone
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"dd/MM/yyyyy"
argument_list|)
argument_list|)
expr_stmt|;
name|DateTimeZone
name|timezone
init|=
name|randomTimezone
argument_list|()
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"timezone"
argument_list|,
name|timezone
operator|.
name|getID
argument_list|()
argument_list|)
expr_stmt|;
name|DateProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTimezone
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timezone
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseInvalidTimezone
specifier|public
name|void
name|testParseInvalidTimezone
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"dd/MM/yyyyy"
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"timezone"
argument_list|,
literal|"invalid_timezone"
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"invalid timezone should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
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
literal|"The datetime zone id 'invalid_timezone' is not recognised"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|//we generate a timezone out of the available ones in joda, some available in the jdk are not available in joda by default
DECL|method|randomTimezone
specifier|private
specifier|static
name|DateTimeZone
name|randomTimezone
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|ids
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|DateTimeZone
operator|.
name|getAvailableIDs
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|ids
argument_list|)
expr_stmt|;
return|return
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|randomFrom
argument_list|(
name|ids
argument_list|)
argument_list|)
return|;
block|}
DECL|method|testParseMatchFormats
specifier|public
name|void
name|testParseMatchFormats
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"dd/MM/yyyy"
argument_list|,
literal|"dd-MM-yyyy"
argument_list|)
argument_list|)
expr_stmt|;
name|DateProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getMatchFormats
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"dd/MM/yyyy"
argument_list|,
literal|"dd-MM-yyyy"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseMatchFormatsFailure
specifier|public
name|void
name|testParseMatchFormatsFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
literal|"dd/MM/yyyy"
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor creation should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfigurationPropertyException
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
name|containsString
argument_list|(
literal|"[match_formats] property isn't a list, but of type [java.lang.String]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParseTargetField
specifier|public
name|void
name|testParseTargetField
parameter_list|()
throws|throws
name|Exception
block|{
name|DateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|targetField
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
name|sourceField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
name|targetField
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"dd/MM/yyyy"
argument_list|,
literal|"dd-MM-yyyy"
argument_list|)
argument_list|)
expr_stmt|;
name|DateProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|targetField
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

