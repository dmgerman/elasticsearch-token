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
name|CompoundProcessor
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
name|TemplateService
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
name|TestProcessor
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
name|equalTo
import|;
end_import

begin_class
DECL|class|ForEachProcessorTests
specifier|public
class|class
name|ForEachProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testExecute
specifier|public
name|void
name|testExecute
parameter_list|()
throws|throws
name|Exception
block|{
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
name|values
operator|.
name|add
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
literal|"baz"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"values"
argument_list|,
name|values
argument_list|)
argument_list|)
decl_stmt|;
name|ForEachProcessor
name|processor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
operator|new
name|UppercaseProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"_ingest._value"
argument_list|)
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values"
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"FOO"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"BAR"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"BAZ"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExecuteWithFailure
specifier|public
name|void
name|testExecuteWithFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"values"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|TestProcessor
name|testProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
name|id
lambda|->
block|{
if|if
condition|(
literal|"c"
operator|.
name|equals
argument_list|(
name|id
operator|.
name|getFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"failure"
argument_list|)
throw|;
block|}
block|}
argument_list|)
decl_stmt|;
name|ForEachProcessor
name|processor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
name|testProcessor
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
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
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
literal|"failure"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|testProcessor
operator|.
name|getInvokedCounter
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
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values"
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|testProcessor
operator|=
operator|new
name|TestProcessor
argument_list|(
name|id
lambda|->
block|{
name|String
name|value
init|=
name|id
operator|.
name|getFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"c"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"failure"
argument_list|)
throw|;
block|}
else|else
block|{
name|id
operator|.
name|setFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|value
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Processor
name|onFailureProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
name|ingestDocument1
lambda|->
block|{}
argument_list|)
decl_stmt|;
name|processor
operator|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
operator|new
name|CompoundProcessor
argument_list|(
literal|false
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|testProcessor
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|onFailureProcessor
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testProcessor
operator|.
name|getInvokedCounter
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
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values"
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"A"
argument_list|,
literal|"B"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMetaDataAvailable
specifier|public
name|void
name|testMetaDataAvailable
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"values"
argument_list|,
name|values
argument_list|)
argument_list|)
decl_stmt|;
name|TestProcessor
name|innerProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
name|id
lambda|->
block|{
name|id
operator|.
name|setFieldValue
argument_list|(
literal|"_ingest._value.index"
argument_list|,
name|id
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"_index"
argument_list|)
argument_list|)
expr_stmt|;
name|id
operator|.
name|setFieldValue
argument_list|(
literal|"_ingest._value.type"
argument_list|,
name|id
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"_type"
argument_list|)
argument_list|)
expr_stmt|;
name|id
operator|.
name|setFieldValue
argument_list|(
literal|"_ingest._value.id"
argument_list|,
name|id
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|ForEachProcessor
name|processor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
name|innerProcessor
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
name|innerProcessor
operator|.
name|getInvokedCounter
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
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.0.index"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"_index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.0.type"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.0.id"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"_id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.1.index"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"_index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.1.type"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.1.id"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"_id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRestOfTheDocumentIsAvailable
specifier|public
name|void
name|testRestOfTheDocumentIsAvailable
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|object
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|object
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|object
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"values"
argument_list|,
name|values
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"flat_values"
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"other"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|TemplateService
name|ts
init|=
name|TestTemplateService
operator|.
name|instance
argument_list|()
decl_stmt|;
name|ForEachProcessor
name|processor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
operator|new
name|SetProcessor
argument_list|(
literal|"_tag"
argument_list|,
name|ts
operator|.
name|compile
argument_list|(
literal|"_ingest._value.new_field"
argument_list|)
argument_list|,
parameter_list|(
name|model
parameter_list|)
lambda|->
name|model
operator|.
name|get
argument_list|(
literal|"other"
argument_list|)
argument_list|)
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
literal|"values.0.new_field"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.1.new_field"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.2.new_field"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.3.new_field"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values.4.new_field"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRandom
specifier|public
name|void
name|testRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|Processor
name|innerProcessor
init|=
operator|new
name|Processor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|existingValue
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|existingValue
operator|+
literal|"."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTag
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|int
name|numValues
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|32
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
argument_list|(
name|numValues
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
name|numValues
condition|;
name|i
operator|++
control|)
block|{
name|values
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"values"
argument_list|,
name|values
argument_list|)
argument_list|)
decl_stmt|;
name|ForEachProcessor
name|processor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
name|innerProcessor
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values"
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numValues
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|r
range|:
name|result
control|)
block|{
name|assertThat
argument_list|(
name|r
argument_list|,
name|equalTo
argument_list|(
literal|"."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testModifyFieldsOutsideArray
specifier|public
name|void
name|testModifyFieldsOutsideArray
parameter_list|()
throws|throws
name|Exception
block|{
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
name|values
operator|.
name|add
argument_list|(
literal|"string"
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"values"
argument_list|,
name|values
argument_list|)
argument_list|)
decl_stmt|;
name|TemplateService
name|ts
init|=
name|TestTemplateService
operator|.
name|instance
argument_list|()
decl_stmt|;
name|ForEachProcessor
name|processor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
operator|new
name|CompoundProcessor
argument_list|(
literal|false
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|UppercaseProcessor
argument_list|(
literal|"_tag_upper"
argument_list|,
literal|"_ingest._value"
argument_list|)
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|AppendProcessor
argument_list|(
literal|"_tag"
argument_list|,
name|ts
operator|.
name|compile
argument_list|(
literal|"errors"
argument_list|)
argument_list|,
parameter_list|(
name|model
parameter_list|)
lambda|->
operator|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"added"
argument_list|)
operator|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|result
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values"
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"STRING"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|errors
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"errors"
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|errors
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
block|}
DECL|method|testScalarValueAllowsUnderscoreValueFieldToRemainAccessible
specifier|public
name|void
name|testScalarValueAllowsUnderscoreValueFieldToRemainAccessible
parameter_list|()
throws|throws
name|Exception
block|{
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
name|values
operator|.
name|add
argument_list|(
literal|"please"
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
literal|"change"
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
literal|"me"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"_value"
argument_list|,
literal|"new_value"
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"values"
argument_list|,
name|values
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|TestProcessor
name|processor
init|=
operator|new
name|TestProcessor
argument_list|(
name|doc
lambda|->
name|doc
operator|.
name|setFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|doc
operator|.
name|getFieldValue
argument_list|(
literal|"_source._value"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|ForEachProcessor
name|forEachProcessor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values"
argument_list|,
name|processor
argument_list|)
decl_stmt|;
name|forEachProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values"
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"new_value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"new_value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"new_value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNestedForEach
specifier|public
name|void
name|testNestedForEach
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|innerValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|innerValues
operator|.
name|add
argument_list|(
literal|"abc"
argument_list|)
expr_stmt|;
name|innerValues
operator|.
name|add
argument_list|(
literal|"def"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|value
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|value
operator|.
name|put
argument_list|(
literal|"values2"
argument_list|,
name|innerValues
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|innerValues
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|innerValues
operator|.
name|add
argument_list|(
literal|"ghi"
argument_list|)
expr_stmt|;
name|innerValues
operator|.
name|add
argument_list|(
literal|"jkl"
argument_list|)
expr_stmt|;
name|value
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|value
operator|.
name|put
argument_list|(
literal|"values2"
argument_list|,
name|innerValues
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"values1"
argument_list|,
name|values
argument_list|)
argument_list|)
decl_stmt|;
name|TestProcessor
name|testProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
name|doc
lambda|->
name|doc
operator|.
name|setFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|doc
operator|.
name|getFieldValue
argument_list|(
literal|"_ingest._value"
argument_list|,
name|String
operator|.
name|class
argument_list|)
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|ForEachProcessor
name|processor
init|=
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"values1"
argument_list|,
operator|new
name|ForEachProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"_ingest._value.values2"
argument_list|,
name|testProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values1.0.values2"
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ABC"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"DEF"
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"values1.1.values2"
argument_list|,
name|List
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"GHI"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"JKL"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

