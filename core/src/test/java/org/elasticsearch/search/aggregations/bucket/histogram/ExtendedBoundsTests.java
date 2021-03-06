begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|histogram
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|joda
operator|.
name|FormatDateTimeFormatter
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
name|joda
operator|.
name|Joda
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
name|ToXContent
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
name|XContentBuilder
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
name|common
operator|.
name|xcontent
operator|.
name|json
operator|.
name|JsonXContent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryShardContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|DocValueFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Instant
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
import|import static
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|max
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|min
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
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|when
import|;
end_import

begin_class
DECL|class|ExtendedBoundsTests
specifier|public
class|class
name|ExtendedBoundsTests
extends|extends
name|ESTestCase
block|{
comment|/**      * Construct a random {@link ExtendedBounds}.      */
DECL|method|randomExtendedBounds
specifier|public
specifier|static
name|ExtendedBounds
name|randomExtendedBounds
parameter_list|()
block|{
name|ExtendedBounds
name|bounds
init|=
name|randomParsedExtendedBounds
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|bounds
operator|=
name|unparsed
argument_list|(
name|bounds
argument_list|)
expr_stmt|;
block|}
return|return
name|bounds
return|;
block|}
comment|/**      * Construct a random {@link ExtendedBounds} in pre-parsed form.      */
DECL|method|randomParsedExtendedBounds
specifier|public
specifier|static
name|ExtendedBounds
name|randomParsedExtendedBounds
parameter_list|()
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// Construct with one missing bound
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
return|return
operator|new
name|ExtendedBounds
argument_list|(
literal|null
argument_list|,
name|randomLong
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|ExtendedBounds
argument_list|(
name|randomLong
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
name|long
name|a
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|long
name|b
decl_stmt|;
do|do
block|{
name|b
operator|=
name|randomLong
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|a
operator|==
name|b
condition|)
do|;
name|long
name|min
init|=
name|min
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
decl_stmt|;
name|long
name|max
init|=
name|max
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
decl_stmt|;
return|return
operator|new
name|ExtendedBounds
argument_list|(
name|min
argument_list|,
name|max
argument_list|)
return|;
block|}
comment|/**      * Convert an extended bounds in parsed for into one in unparsed form.      */
DECL|method|unparsed
specifier|public
specifier|static
name|ExtendedBounds
name|unparsed
parameter_list|(
name|ExtendedBounds
name|template
parameter_list|)
block|{
comment|// It'd probably be better to randomize the formatter
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateOptionalTime"
argument_list|)
decl_stmt|;
name|String
name|minAsStr
init|=
name|template
operator|.
name|getMin
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
operator|new
name|Instant
argument_list|(
name|template
operator|.
name|getMin
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|maxAsStr
init|=
name|template
operator|.
name|getMax
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
operator|new
name|Instant
argument_list|(
name|template
operator|.
name|getMax
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|ExtendedBounds
argument_list|(
name|minAsStr
argument_list|,
name|maxAsStr
argument_list|)
return|;
block|}
DECL|method|testParseAndValidate
specifier|public
name|void
name|testParseAndValidate
parameter_list|()
block|{
name|long
name|now
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|SearchContext
name|context
init|=
name|mock
argument_list|(
name|SearchContext
operator|.
name|class
argument_list|)
decl_stmt|;
name|QueryShardContext
name|qsc
init|=
operator|new
name|QueryShardContext
argument_list|(
literal|0
argument_list|,
operator|new
name|IndexSettings
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|settings
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
name|now
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|qsc
argument_list|)
expr_stmt|;
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateOptionalTime"
argument_list|)
decl_stmt|;
name|DocValueFormat
name|format
init|=
operator|new
name|DocValueFormat
operator|.
name|DateTime
argument_list|(
name|formatter
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|ExtendedBounds
name|expected
init|=
name|randomParsedExtendedBounds
argument_list|()
decl_stmt|;
name|ExtendedBounds
name|parsed
init|=
name|unparsed
argument_list|(
name|expected
argument_list|)
operator|.
name|parseAndValidate
argument_list|(
literal|"test"
argument_list|,
name|context
argument_list|,
name|format
argument_list|)
decl_stmt|;
comment|// parsed won't *equal* expected because equal includes the String parts
name|assertEquals
argument_list|(
name|expected
operator|.
name|getMin
argument_list|()
argument_list|,
name|parsed
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getMax
argument_list|()
argument_list|,
name|parsed
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|parsed
operator|=
operator|new
name|ExtendedBounds
argument_list|(
literal|"now"
argument_list|,
literal|null
argument_list|)
operator|.
name|parseAndValidate
argument_list|(
literal|"test"
argument_list|,
name|context
argument_list|,
name|format
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|now
argument_list|,
operator|(
name|long
operator|)
name|parsed
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parsed
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|parsed
operator|=
operator|new
name|ExtendedBounds
argument_list|(
literal|null
argument_list|,
literal|"now"
argument_list|)
operator|.
name|parseAndValidate
argument_list|(
literal|"test"
argument_list|,
name|context
argument_list|,
name|format
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parsed
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|now
argument_list|,
operator|(
name|long
operator|)
name|parsed
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|SearchParseException
name|e
init|=
name|expectThrows
argument_list|(
name|SearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|ExtendedBounds
argument_list|(
literal|100L
argument_list|,
literal|90L
argument_list|)
operator|.
name|parseAndValidate
argument_list|(
literal|"test"
argument_list|,
name|context
argument_list|,
name|format
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"[extended_bounds.min][100] cannot be greater than [extended_bounds.max][90] for histogram aggregation [test]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|SearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|unparsed
argument_list|(
operator|new
name|ExtendedBounds
argument_list|(
literal|100L
argument_list|,
literal|90L
argument_list|)
argument_list|)
operator|.
name|parseAndValidate
argument_list|(
literal|"test"
argument_list|,
name|context
argument_list|,
name|format
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"[extended_bounds.min][100] cannot be greater than [extended_bounds.max][90] for histogram aggregation [test]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testTransportRoundTrip
specifier|public
name|void
name|testTransportRoundTrip
parameter_list|()
throws|throws
name|IOException
block|{
name|ExtendedBounds
name|orig
init|=
name|randomExtendedBounds
argument_list|()
decl_stmt|;
name|BytesReference
name|origBytes
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|orig
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|origBytes
operator|=
name|out
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
name|ExtendedBounds
name|read
decl_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
name|origBytes
operator|.
name|streamInput
argument_list|()
init|)
block|{
name|read
operator|=
operator|new
name|ExtendedBounds
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"read fully"
argument_list|,
literal|0
argument_list|,
name|in
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|orig
argument_list|,
name|read
argument_list|)
expr_stmt|;
name|BytesReference
name|readBytes
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|read
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|readBytes
operator|=
name|out
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|origBytes
argument_list|,
name|readBytes
argument_list|)
expr_stmt|;
block|}
DECL|method|testXContentRoundTrip
specifier|public
name|void
name|testXContentRoundTrip
parameter_list|()
throws|throws
name|Exception
block|{
name|ExtendedBounds
name|orig
init|=
name|randomExtendedBounds
argument_list|()
decl_stmt|;
try|try
init|(
name|XContentBuilder
name|out
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
init|)
block|{
name|out
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|orig
operator|.
name|toXContent
argument_list|(
name|out
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|out
operator|.
name|endObject
argument_list|()
expr_stmt|;
try|try
init|(
name|XContentParser
name|in
init|=
name|createParser
argument_list|(
name|JsonXContent
operator|.
name|jsonXContent
argument_list|,
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|in
operator|.
name|currentToken
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|token
argument_list|)
expr_stmt|;
name|token
operator|=
name|in
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|token
argument_list|,
name|equalTo
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|)
argument_list|)
expr_stmt|;
name|token
operator|=
name|in
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|token
argument_list|,
name|equalTo
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|currentName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ExtendedBounds
operator|.
name|EXTENDED_BOUNDS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ExtendedBounds
name|read
init|=
name|ExtendedBounds
operator|.
name|PARSER
operator|.
name|apply
argument_list|(
name|in
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|orig
argument_list|,
name|read
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Error parsing ["
operator|+
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|utf8ToString
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

