begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.mutate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|mutate
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|Tuple
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
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|notNullValue
import|;
end_import

begin_class
DECL|class|MutateProcessorFactoryTests
specifier|public
class|class
name|MutateProcessorFactoryTests
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
name|MutateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|MutateProcessor
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
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|update
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|update
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|123
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"update"
argument_list|,
name|update
argument_list|)
expr_stmt|;
name|MutateProcessor
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
name|getUpdate
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|update
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateGsubPattern
specifier|public
name|void
name|testCreateGsubPattern
parameter_list|()
throws|throws
name|Exception
block|{
name|MutateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|MutateProcessor
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
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|gsub
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|gsub
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"\\s.*e\\s"
argument_list|,
literal|"<word_ending_with_e>"
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"gsub"
argument_list|,
name|gsub
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
argument_list|>
name|compiledGsub
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Pattern
name|searchPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\s.*e\\s"
argument_list|)
decl_stmt|;
name|compiledGsub
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|searchPattern
argument_list|,
literal|"<word_ending_with_e>"
argument_list|)
argument_list|)
expr_stmt|;
name|MutateProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|compiledGsub
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Tuple
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
name|actualSearchAndReplace
init|=
name|processor
operator|.
name|getGsub
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|actualSearchAndReplace
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualSearchAndReplace
operator|.
name|v1
argument_list|()
operator|.
name|pattern
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|v1
argument_list|()
operator|.
name|pattern
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualSearchAndReplace
operator|.
name|v2
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|v2
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCreateGsubPattern_InvalidFormat
specifier|public
name|void
name|testCreateGsubPattern_InvalidFormat
parameter_list|()
throws|throws
name|Exception
block|{
name|MutateProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|MutateProcessor
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
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|gsub
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|gsub
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"only_one"
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"gsub"
argument_list|,
name|gsub
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
argument_list|()
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
literal|"Invalid search and replace values ([only_one]) for field: foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

