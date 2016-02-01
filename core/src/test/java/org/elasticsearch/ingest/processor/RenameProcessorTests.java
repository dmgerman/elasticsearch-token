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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
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
DECL|class|RenameProcessorTests
specifier|public
class|class
name|RenameProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRename
specifier|public
name|void
name|testRename
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
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomExistingFieldName
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|)
decl_stmt|;
name|Object
name|fieldValue
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|fieldName
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|newFieldName
decl_stmt|;
do|do
block|{
name|newFieldName
operator|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|RandomDocumentPicks
operator|.
name|canAddField
argument_list|(
name|newFieldName
argument_list|,
name|ingestDocument
argument_list|)
operator|==
literal|false
operator|||
name|newFieldName
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
do|;
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|fieldName
argument_list|,
name|newFieldName
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
name|newFieldName
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|fieldValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRenameArrayElement
specifier|public
name|void
name|testRenameArrayElement
parameter_list|()
throws|throws
name|Exception
block|{
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
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"item1"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"item2"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"item3"
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"list"
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|one
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|one
operator|.
name|add
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"one"
argument_list|,
literal|"one"
argument_list|)
argument_list|)
expr_stmt|;
name|one
operator|.
name|add
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"two"
argument_list|,
literal|"two"
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"one"
argument_list|,
name|one
argument_list|)
expr_stmt|;
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
name|document
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"list.0"
argument_list|,
literal|"item"
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|Object
name|actualObject
init|=
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"list"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|actualObject
argument_list|,
name|instanceOf
argument_list|(
name|List
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|String
argument_list|>
name|actualList
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|actualObject
decl_stmt|;
name|assertThat
argument_list|(
name|actualList
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
name|actualList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"item2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualList
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"item3"
argument_list|)
argument_list|)
expr_stmt|;
name|actualObject
operator|=
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"item"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualObject
argument_list|,
name|instanceOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualObject
argument_list|,
name|equalTo
argument_list|(
literal|"item1"
argument_list|)
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"list.0"
argument_list|,
literal|"list.3"
argument_list|)
expr_stmt|;
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
literal|"[3] is out of bounds for array with length [2] as part of path [list.3]"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualList
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
name|actualList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"item2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualList
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"item3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRenameNonExistingField
specifier|public
name|void
name|testRenameNonExistingField
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
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|fieldName
argument_list|,
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
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
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] doesn't exist"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRenameNewFieldAlreadyExists
specifier|public
name|void
name|testRenameNewFieldAlreadyExists
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
name|String
name|fieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomExistingFieldName
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|RandomDocumentPicks
operator|.
name|randomExistingFieldName
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|)
argument_list|,
name|fieldName
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
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] already exists"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRenameExistingFieldNullValue
specifier|public
name|void
name|testRenameExistingFieldNullValue
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
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|fieldName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|String
name|newFieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|fieldName
argument_list|,
name|newFieldName
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
name|hasField
argument_list|(
name|fieldName
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
name|ingestDocument
operator|.
name|hasField
argument_list|(
name|newFieldName
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
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|newFieldName
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRenameAtomicOperationSetFails
specifier|public
name|void
name|testRenameAtomicOperationSetFails
parameter_list|()
throws|throws
name|Exception
block|{
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
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|put
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"new_field"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
return|return
name|super
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"list"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"item"
argument_list|)
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"list"
argument_list|,
literal|"new_field"
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
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|//the set failed, the old field has not been removed
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"list"
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
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"new_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRenameAtomicOperationRemoveFails
specifier|public
name|void
name|testRenameAtomicOperationRemoveFails
parameter_list|()
throws|throws
name|Exception
block|{
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
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"list"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
return|return
name|super
operator|.
name|remove
argument_list|(
name|key
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"list"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"item"
argument_list|)
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"list"
argument_list|,
literal|"new_field"
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
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|//the set failed, the old field has not been removed
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"list"
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
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"new_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

