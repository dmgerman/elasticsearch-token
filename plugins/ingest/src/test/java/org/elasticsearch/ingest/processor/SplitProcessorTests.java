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
DECL|class|SplitProcessorTests
specifier|public
class|class
name|SplitProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSplit
specifier|public
name|void
name|testSplit
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
name|addRandomField
argument_list|(
name|random
argument_list|()
argument_list|,
name|ingestDocument
argument_list|,
literal|"127.0.0.1"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|SplitProcessor
argument_list|(
name|fieldName
argument_list|,
literal|"\\."
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
name|Arrays
operator|.
name|asList
argument_list|(
literal|"127"
argument_list|,
literal|"0"
argument_list|,
literal|"0"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSplitFieldNotFound
specifier|public
name|void
name|testSplitFieldNotFound
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
name|SplitProcessor
argument_list|(
name|fieldName
argument_list|,
literal|"\\."
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
literal|"split processor should have failed"
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
DECL|method|testSplitNullValue
specifier|public
name|void
name|testSplitNullValue
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
name|Processor
name|processor
init|=
operator|new
name|SplitProcessor
argument_list|(
literal|"field"
argument_list|,
literal|"\\."
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
literal|"split processor should have failed"
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
literal|"field [field] is null, cannot split."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSplitNonStringValue
specifier|public
name|void
name|testSplitNonStringValue
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
name|randomInt
argument_list|()
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|SplitProcessor
argument_list|(
name|fieldName
argument_list|,
literal|"\\."
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
literal|"split processor should have failed"
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
literal|"] of type [java.lang.Integer] cannot be cast to [java.lang.String]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

