begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.convert
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|convert
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
name|ingest
operator|.
name|processor
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
name|java
operator|.
name|util
operator|.
name|*
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
name|processor
operator|.
name|convert
operator|.
name|ConvertProcessor
operator|.
name|Type
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
DECL|class|ConvertProcessorTests
specifier|public
class|class
name|ConvertProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testConvertInt
specifier|public
name|void
name|testConvertInt
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|randomInt
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|randomInt
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|INTEGER
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|randomInt
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertIntList
specifier|public
name|void
name|testConvertIntList
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numItems
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldValue
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|expectedList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numItems
condition|;
name|j
operator|++
control|)
block|{
name|int
name|randomInt
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|fieldValue
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|randomInt
argument_list|)
argument_list|)
expr_stmt|;
name|expectedList
operator|.
name|add
argument_list|(
name|randomInt
argument_list|)
expr_stmt|;
block|}
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|fieldValue
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|INTEGER
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedList
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertIntError
specifier|public
name|void
name|testConvertIntError
parameter_list|()
throws|throws
name|Exception
block|{
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
name|String
name|value
init|=
literal|"string-"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|INTEGER
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor execute should have failed"
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
literal|"unable to convert ["
operator|+
name|value
operator|+
literal|"] to integer"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testConvertFloat
specifier|public
name|void
name|testConvertFloat
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|expectedResult
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|float
name|randomFloat
init|=
name|randomFloat
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|randomFloat
argument_list|)
decl_stmt|;
name|expectedResult
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|randomFloat
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|FLOAT
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|Float
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|randomFloat
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertFloatList
specifier|public
name|void
name|testConvertFloatList
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numItems
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldValue
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Float
argument_list|>
name|expectedList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numItems
condition|;
name|j
operator|++
control|)
block|{
name|float
name|randomFloat
init|=
name|randomFloat
argument_list|()
decl_stmt|;
name|fieldValue
operator|.
name|add
argument_list|(
name|Float
operator|.
name|toString
argument_list|(
name|randomFloat
argument_list|)
argument_list|)
expr_stmt|;
name|expectedList
operator|.
name|add
argument_list|(
name|randomFloat
argument_list|)
expr_stmt|;
block|}
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|fieldValue
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|FLOAT
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedList
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertFloatError
specifier|public
name|void
name|testConvertFloatError
parameter_list|()
throws|throws
name|Exception
block|{
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
name|String
name|value
init|=
literal|"string-"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|FLOAT
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor execute should have failed"
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
literal|"unable to convert ["
operator|+
name|value
operator|+
literal|"] to float"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testConvertBoolean
specifier|public
name|void
name|testConvertBoolean
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Type
argument_list|>
name|fields
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|expectedResult
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|randomBoolean
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|String
name|booleanString
init|=
name|Boolean
operator|.
name|toString
argument_list|(
name|randomBoolean
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
condition|)
block|{
name|booleanString
operator|=
name|booleanString
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
block|}
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|booleanString
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|BOOLEAN
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|Boolean
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|randomBoolean
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertBooleanList
specifier|public
name|void
name|testConvertBooleanList
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numItems
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldValue
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Boolean
argument_list|>
name|expectedList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numItems
condition|;
name|j
operator|++
control|)
block|{
name|boolean
name|randomBoolean
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|String
name|booleanString
init|=
name|Boolean
operator|.
name|toString
argument_list|(
name|randomBoolean
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
condition|)
block|{
name|booleanString
operator|=
name|booleanString
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
block|}
name|fieldValue
operator|.
name|add
argument_list|(
name|booleanString
argument_list|)
expr_stmt|;
name|expectedList
operator|.
name|add
argument_list|(
name|randomBoolean
argument_list|)
expr_stmt|;
block|}
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|fieldValue
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|BOOLEAN
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedList
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertBooleanError
specifier|public
name|void
name|testConvertBooleanError
parameter_list|()
throws|throws
name|Exception
block|{
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
name|String
name|fieldValue
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|fieldValue
operator|=
literal|"string-"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//verify that only proper boolean values are supported and we are strict about it
name|fieldValue
operator|=
name|randomFrom
argument_list|(
literal|"on"
argument_list|,
literal|"off"
argument_list|,
literal|"yes"
argument_list|,
literal|"no"
argument_list|,
literal|"0"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
block|}
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|BOOLEAN
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor execute should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
literal|"["
operator|+
name|fieldValue
operator|+
literal|"] is not a boolean value, cannot convert to boolean"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testConvertString
specifier|public
name|void
name|testConvertString
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|fieldValue
decl_stmt|;
name|String
name|expectedFieldValue
decl_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|float
name|randomFloat
init|=
name|randomFloat
argument_list|()
decl_stmt|;
name|fieldValue
operator|=
name|randomFloat
expr_stmt|;
name|expectedFieldValue
operator|=
name|Float
operator|.
name|toString
argument_list|(
name|randomFloat
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|int
name|randomInt
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|fieldValue
operator|=
name|randomInt
expr_stmt|;
name|expectedFieldValue
operator|=
name|Integer
operator|.
name|toString
argument_list|(
name|randomInt
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|boolean
name|randomBoolean
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|fieldValue
operator|=
name|randomBoolean
expr_stmt|;
name|expectedFieldValue
operator|=
name|Boolean
operator|.
name|toString
argument_list|(
name|randomBoolean
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|fieldValue
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|STRING
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedFieldValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertStringList
specifier|public
name|void
name|testConvertStringList
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numItems
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|fieldValue
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expectedList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numItems
condition|;
name|j
operator|++
control|)
block|{
name|Object
name|randomValue
decl_stmt|;
name|String
name|randomValueString
decl_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|float
name|randomFloat
init|=
name|randomFloat
argument_list|()
decl_stmt|;
name|randomValue
operator|=
name|randomFloat
expr_stmt|;
name|randomValueString
operator|=
name|Float
operator|.
name|toString
argument_list|(
name|randomFloat
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|int
name|randomInt
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|randomValue
operator|=
name|randomInt
expr_stmt|;
name|randomValueString
operator|=
name|Integer
operator|.
name|toString
argument_list|(
name|randomInt
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|boolean
name|randomBoolean
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|randomValue
operator|=
name|randomBoolean
expr_stmt|;
name|randomValueString
operator|=
name|Boolean
operator|.
name|toString
argument_list|(
name|randomBoolean
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
name|fieldValue
operator|.
name|add
argument_list|(
name|randomValue
argument_list|)
expr_stmt|;
name|expectedList
operator|.
name|add
argument_list|(
name|randomValueString
argument_list|)
expr_stmt|;
block|}
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
name|fieldValue
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|Type
operator|.
name|STRING
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedList
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertNonExistingField
specifier|public
name|void
name|testConvertNonExistingField
parameter_list|()
throws|throws
name|Exception
block|{
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
name|Type
name|type
init|=
name|randomFrom
argument_list|(
name|Type
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
name|fieldName
argument_list|,
name|type
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor execute should have failed"
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
name|containsString
argument_list|(
literal|"not present as part of path ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testConvertNullField
specifier|public
name|void
name|testConvertNullField
parameter_list|()
throws|throws
name|Exception
block|{
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
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"field"
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|Type
name|type
init|=
name|randomFrom
argument_list|(
name|Type
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|ConvertProcessor
argument_list|(
literal|"field"
argument_list|,
name|type
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor execute should have failed"
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
literal|"Field [field] is null, cannot be converted to type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

