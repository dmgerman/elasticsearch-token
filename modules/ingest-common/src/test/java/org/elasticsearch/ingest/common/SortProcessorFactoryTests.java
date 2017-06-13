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
name|ElasticsearchParseException
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
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|SortProcessorFactoryTests
specifier|public
class|class
name|SortProcessorFactoryTests
extends|extends
name|ESTestCase
block|{
DECL|method|testCreate
specifier|public
name|void
name|testCreate
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|processorTag
init|=
name|randomAlphaOfLength
argument_list|(
literal|10
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
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|SortProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|SortProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|SortProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|processorTag
argument_list|,
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
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getOrder
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SortProcessor
operator|.
name|SortOrder
operator|.
name|ASCENDING
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
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateWithOrder
specifier|public
name|void
name|testCreateWithOrder
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|processorTag
init|=
name|randomAlphaOfLength
argument_list|(
literal|10
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
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|SortProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|SortProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|SortProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|processorTag
argument_list|,
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
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getOrder
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SortProcessor
operator|.
name|SortOrder
operator|.
name|DESCENDING
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
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateWithTargetField
specifier|public
name|void
name|testCreateWithTargetField
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|processorTag
init|=
name|randomAlphaOfLength
argument_list|(
literal|10
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
name|targetFieldName
init|=
name|RandomDocumentPicks
operator|.
name|randomFieldName
argument_list|(
name|random
argument_list|()
argument_list|)
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
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
name|targetFieldName
argument_list|)
expr_stmt|;
name|SortProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|SortProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|SortProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|processorTag
argument_list|,
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
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getOrder
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SortProcessor
operator|.
name|SortOrder
operator|.
name|ASCENDING
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
name|targetFieldName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateWithInvalidOrder
specifier|public
name|void
name|testCreateWithInvalidOrder
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|processorTag
init|=
name|randomAlphaOfLength
argument_list|(
literal|10
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
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"order"
argument_list|,
literal|"invalid"
argument_list|)
expr_stmt|;
name|SortProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|SortProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"factory create should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
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
literal|"[order] Sort direction [invalid] not recognized. Valid values are: [asc, desc]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCreateMissingField
specifier|public
name|void
name|testCreateMissingField
parameter_list|()
throws|throws
name|Exception
block|{
name|SortProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|SortProcessor
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
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"factory create should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
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
literal|"[field] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

