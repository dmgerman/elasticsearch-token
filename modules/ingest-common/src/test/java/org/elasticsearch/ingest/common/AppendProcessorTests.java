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
name|Processor
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
name|TestTemplateService
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
name|ValueSource
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
name|ArrayList
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
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
name|CoreMatchers
operator|.
name|not
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|sameInstance
import|;
end_import

begin_class
DECL|class|AppendProcessorTests
specifier|public
class|class
name|AppendProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testAppendValuesToExistingList
specifier|public
name|void
name|testAppendValuesToExistingList
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
name|Scalar
name|scalar
init|=
name|randomFrom
argument_list|(
name|Scalar
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|scalar
operator|.
name|randomValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|checkList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|String
name|field
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
name|list
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Processor
name|appendProcessor
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Object
name|value
init|=
name|scalar
operator|.
name|randomValue
argument_list|()
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|field
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|valuesSize
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valuesSize
condition|;
name|i
operator|++
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|scalar
operator|.
name|randomValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|field
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|appendProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|Object
name|fieldValue
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|field
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldValue
argument_list|,
name|sameInstance
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|size
operator|+
name|values
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|checkList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|size
init|;
name|i
operator|<
name|size
operator|+
name|values
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|i
operator|-
name|size
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testAppendValuesToNonExistingList
specifier|public
name|void
name|testAppendValuesToNonExistingList
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
name|field
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Scalar
name|scalar
init|=
name|randomFrom
argument_list|(
name|Scalar
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Processor
name|appendProcessor
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Object
name|value
init|=
name|scalar
operator|.
name|randomValue
argument_list|()
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|field
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|valuesSize
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valuesSize
condition|;
name|i
operator|++
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|scalar
operator|.
name|randomValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|field
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|appendProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
name|list
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|field
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|list
argument_list|,
name|not
argument_list|(
name|sameInstance
argument_list|(
name|values
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
argument_list|,
name|equalTo
argument_list|(
name|values
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertScalarToList
specifier|public
name|void
name|testConvertScalarToList
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
name|Scalar
name|scalar
init|=
name|randomFrom
argument_list|(
name|Scalar
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|initialValue
init|=
name|scalar
operator|.
name|randomValue
argument_list|()
decl_stmt|;
name|String
name|field
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
name|initialValue
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Processor
name|appendProcessor
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Object
name|value
init|=
name|scalar
operator|.
name|randomValue
argument_list|()
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|field
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|valuesSize
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valuesSize
condition|;
name|i
operator|++
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|scalar
operator|.
name|randomValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|field
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|appendProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
name|fieldValue
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|field
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldValue
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|values
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldValue
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|initialValue
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|values
operator|.
name|size
argument_list|()
operator|+
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|fieldValue
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testAppendMetadata
specifier|public
name|void
name|testAppendMetadata
parameter_list|()
throws|throws
name|Exception
block|{
comment|//here any metadata field value becomes a list, which won't make sense in most of the cases,
comment|// but support for append is streamlined like for set so we test it
name|IngestDocument
operator|.
name|MetaData
name|randomMetaData
init|=
name|randomFrom
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Processor
name|appendProcessor
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|String
name|value
init|=
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|randomMetaData
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|valuesSize
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valuesSize
condition|;
name|i
operator|++
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|appendProcessor
operator|=
name|createAppendProcessor
argument_list|(
name|randomMetaData
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
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
name|initialValue
init|=
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
name|randomMetaData
operator|.
name|getFieldName
argument_list|()
argument_list|)
decl_stmt|;
name|appendProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
name|list
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|randomMetaData
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|initialValue
operator|==
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|list
argument_list|,
name|equalTo
argument_list|(
name|values
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|values
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|initialValue
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|createAppendProcessor
specifier|private
specifier|static
name|Processor
name|createAppendProcessor
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|fieldValue
parameter_list|)
block|{
return|return
operator|new
name|AppendProcessor
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
operator|new
name|TestTemplateService
operator|.
name|MockTemplateScript
operator|.
name|Factory
argument_list|(
name|fieldName
argument_list|)
argument_list|,
name|ValueSource
operator|.
name|wrap
argument_list|(
name|fieldValue
argument_list|,
name|TestTemplateService
operator|.
name|instance
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
DECL|enum|Scalar
specifier|private
enum|enum
name|Scalar
block|{
DECL|enum constant|INTEGER
name|INTEGER
block|{
annotation|@
name|Override
name|Object
name|randomValue
parameter_list|()
block|{
return|return
name|randomInt
argument_list|()
return|;
block|}
DECL|enum constant|DOUBLE
block|}
block|,
name|DOUBLE
block|{
annotation|@
name|Override
name|Object
name|randomValue
parameter_list|()
block|{
return|return
name|randomDouble
argument_list|()
return|;
block|}
DECL|enum constant|FLOAT
block|}
block|,
name|FLOAT
block|{
annotation|@
name|Override
name|Object
name|randomValue
parameter_list|()
block|{
return|return
name|randomFloat
argument_list|()
return|;
block|}
DECL|enum constant|BOOLEAN
block|}
block|,
name|BOOLEAN
block|{
annotation|@
name|Override
name|Object
name|randomValue
parameter_list|()
block|{
return|return
name|randomBoolean
argument_list|()
return|;
block|}
DECL|enum constant|STRING
block|}
block|,
name|STRING
block|{
annotation|@
name|Override
name|Object
name|randomValue
parameter_list|()
block|{
return|return
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
return|;
block|}
DECL|enum constant|MAP
block|}
block|,
name|MAP
block|{
annotation|@
name|Override
name|Object
name|randomValue
parameter_list|()
block|{
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
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|numItems
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numItems
condition|;
name|i
operator|++
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|Scalar
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|randomValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
DECL|enum constant|NULL
block|}
block|,
name|NULL
block|{
annotation|@
name|Override
name|Object
name|randomValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
block|;
DECL|method|randomValue
specifier|abstract
name|Object
name|randomValue
parameter_list|()
function_decl|;
block|}
block|}
end_class

end_unit

