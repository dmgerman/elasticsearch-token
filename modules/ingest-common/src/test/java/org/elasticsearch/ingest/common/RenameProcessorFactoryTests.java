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
DECL|class|RenameProcessorFactoryTests
specifier|public
class|class
name|RenameProcessorFactoryTests
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
name|RenameProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|RenameProcessor
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
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"old_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
literal|"new_field"
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
name|RenameProcessor
name|renameProcessor
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
name|renameProcessor
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
name|renameProcessor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"old_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|renameProcessor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"new_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|renameProcessor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateWithIgnoreMissing
specifier|public
name|void
name|testCreateWithIgnoreMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|RenameProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|RenameProcessor
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
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"old_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
literal|"new_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"ignore_missing"
argument_list|,
literal|true
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
name|RenameProcessor
name|renameProcessor
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
name|renameProcessor
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
name|renameProcessor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"old_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|renameProcessor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"new_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|renameProcessor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateNoFieldPresent
specifier|public
name|void
name|testCreateNoFieldPresent
parameter_list|()
throws|throws
name|Exception
block|{
name|RenameProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|RenameProcessor
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
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
literal|"new_field"
argument_list|)
expr_stmt|;
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
DECL|method|testCreateNoToPresent
specifier|public
name|void
name|testCreateNoToPresent
parameter_list|()
throws|throws
name|Exception
block|{
name|RenameProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|RenameProcessor
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
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"old_field"
argument_list|)
expr_stmt|;
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
literal|"[target_field] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

