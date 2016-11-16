begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
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
name|IngestDocument
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
name|RandomDocumentPicks
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestDocumentMatcher
operator|.
name|assertIngestDocument
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
DECL|class|GrokProcessorTests
specifier|public
class|class
name|GrokProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testMatch
specifier|public
name|void
name|testMatch
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFieldValue
argument_list|(
literal|"one"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoMatch
specifier|public
name|void
name|testNoMatch
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|"23"
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|Exception
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|processor
operator|.
name|execute
argument_list|(
name|doc
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
literal|"Provided Grok expressions do not match field value: [23]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMatchWithoutCaptures
specifier|public
name|void
name|testMatchWithoutCaptures
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
literal|"value"
decl_stmt|;
name|IngestDocument
name|originalDoc
init|=
operator|new
name|IngestDocument
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|originalDoc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|IngestDocument
name|doc
init|=
operator|new
name|IngestDocument
argument_list|(
name|originalDoc
argument_list|)
decl_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|fieldName
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
argument_list|,
name|equalTo
argument_list|(
name|originalDoc
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullField
specifier|public
name|void
name|testNullField
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|Exception
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|processor
operator|.
name|execute
argument_list|(
name|doc
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
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] is null, cannot process it."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullFieldWithIgnoreMissing
specifier|public
name|void
name|testNullFieldWithIgnoreMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|originalIngestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|originalIngestDocument
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|originalIngestDocument
argument_list|)
decl_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertIngestDocument
argument_list|(
name|originalIngestDocument
argument_list|,
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
DECL|method|testNotStringField
specifier|public
name|void
name|testNotStringField
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|Exception
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|processor
operator|.
name|execute
argument_list|(
name|doc
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
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] of type [java.lang.Integer] cannot be cast to [java.lang.String]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNotStringFieldWithIgnoreMissing
specifier|public
name|void
name|testNotStringFieldWithIgnoreMissing
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|Exception
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|processor
operator|.
name|execute
argument_list|(
name|doc
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
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] of type [java.lang.Integer] cannot be cast to [java.lang.String]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingField
specifier|public
name|void
name|testMissingField
parameter_list|()
block|{
name|String
name|fieldName
init|=
literal|"foo.bar"
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|Exception
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|processor
operator|.
name|execute
argument_list|(
name|doc
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
literal|"field [foo] not present as part of path [foo.bar]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingFieldWithIgnoreMissing
specifier|public
name|void
name|testMissingFieldWithIgnoreMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
literal|"foo.bar"
decl_stmt|;
name|IngestDocument
name|originalIngestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertIngestDocument
argument_list|(
name|originalIngestDocument
argument_list|,
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiplePatternsWithMatchReturn
specifier|public
name|void
name|testMultiplePatternsWithMatchReturn
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|patternBank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|patternBank
operator|.
name|put
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|patternBank
operator|.
name|put
argument_list|(
literal|"TWO"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|patternBank
operator|.
name|put
argument_list|(
literal|"THREE"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|patternBank
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"%{ONE:one}"
argument_list|,
literal|"%{TWO:two}"
argument_list|,
literal|"%{THREE:three}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|hasField
argument_list|(
literal|"one"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFieldValue
argument_list|(
literal|"two"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|hasField
argument_list|(
literal|"three"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSetMetadata
specifier|public
name|void
name|testSetMetadata
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|"abc23"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|patternBank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|patternBank
operator|.
name|put
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|patternBank
operator|.
name|put
argument_list|(
literal|"TWO"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|patternBank
operator|.
name|put
argument_list|(
literal|"THREE"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|patternBank
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"%{ONE:one}"
argument_list|,
literal|"%{TWO:two}"
argument_list|,
literal|"%{THREE:three}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|hasField
argument_list|(
literal|"one"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFieldValue
argument_list|(
literal|"two"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|hasField
argument_list|(
literal|"three"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFieldValue
argument_list|(
literal|"_ingest._grok_match_index"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testTraceWithOnePattern
specifier|public
name|void
name|testTraceWithOnePattern
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IngestDocument
name|doc
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|"first1"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|patternBank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|patternBank
operator|.
name|put
argument_list|(
literal|"ONE"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|GrokProcessor
name|processor
init|=
operator|new
name|GrokProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|patternBank
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"%{ONE:one}"
argument_list|)
argument_list|,
name|fieldName
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|hasField
argument_list|(
literal|"one"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFieldValue
argument_list|(
literal|"_ingest._grok_match_index"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCombinedPatterns
specifier|public
name|void
name|testCombinedPatterns
parameter_list|()
block|{
name|String
name|combined
decl_stmt|;
name|combined
operator|=
name|GrokProcessor
operator|.
name|combinePatterns
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|combined
argument_list|,
name|equalTo
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|combined
operator|=
name|GrokProcessor
operator|.
name|combinePatterns
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|combined
argument_list|,
name|equalTo
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|combined
operator|=
name|GrokProcessor
operator|.
name|combinePatterns
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|combined
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|combined
operator|=
name|GrokProcessor
operator|.
name|combinePatterns
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|combined
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|combined
operator|=
name|GrokProcessor
operator|.
name|combinePatterns
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|combined
argument_list|,
name|equalTo
argument_list|(
literal|"(?:foo)|(?:bar)"
argument_list|)
argument_list|)
expr_stmt|;
name|combined
operator|=
name|GrokProcessor
operator|.
name|combinePatterns
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|combined
argument_list|,
name|equalTo
argument_list|(
literal|"(?<_ingest._grok_match_index.0>foo)|(?<_ingest._grok_match_index.1>bar)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

