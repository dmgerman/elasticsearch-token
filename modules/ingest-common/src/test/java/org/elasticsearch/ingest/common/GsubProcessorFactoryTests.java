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

begin_class
DECL|class|GsubProcessorFactoryTests
specifier|public
class|class
name|GsubProcessorFactoryTests
extends|extends
name|AbstractStringProcessorFactoryTestCase
block|{
annotation|@
name|Override
DECL|method|newFactory
specifier|protected
name|AbstractStringProcessor
operator|.
name|Factory
name|newFactory
parameter_list|()
block|{
return|return
operator|new
name|GsubProcessor
operator|.
name|Factory
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|modifyConfig
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|modifyConfig
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
block|{
name|config
operator|.
name|put
argument_list|(
literal|"pattern"
argument_list|,
literal|"\\."
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"replacement"
argument_list|,
literal|"-"
argument_list|)
expr_stmt|;
return|return
name|config
return|;
block|}
annotation|@
name|Override
DECL|method|assertProcessor
specifier|protected
name|void
name|assertProcessor
parameter_list|(
name|AbstractStringProcessor
name|processor
parameter_list|)
block|{
name|GsubProcessor
name|gsubProcessor
init|=
operator|(
name|GsubProcessor
operator|)
name|processor
decl_stmt|;
name|assertThat
argument_list|(
name|gsubProcessor
operator|.
name|getPattern
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"\\."
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|gsubProcessor
operator|.
name|getReplacement
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"-"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateNoPatternPresent
specifier|public
name|void
name|testCreateNoPatternPresent
parameter_list|()
throws|throws
name|Exception
block|{
name|GsubProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GsubProcessor
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
literal|"field1"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"replacement"
argument_list|,
literal|"-"
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
literal|"[pattern] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCreateNoReplacementPresent
specifier|public
name|void
name|testCreateNoReplacementPresent
parameter_list|()
throws|throws
name|Exception
block|{
name|GsubProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GsubProcessor
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
literal|"field1"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"pattern"
argument_list|,
literal|"\\."
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
literal|"[replacement] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCreateInvalidPattern
specifier|public
name|void
name|testCreateInvalidPattern
parameter_list|()
throws|throws
name|Exception
block|{
name|GsubProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GsubProcessor
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
literal|"field1"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"pattern"
argument_list|,
literal|"["
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"replacement"
argument_list|,
literal|"-"
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
name|containsString
argument_list|(
literal|"[pattern] Invalid regex pattern. Unclosed character class"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

