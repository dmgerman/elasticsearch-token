begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|index
operator|.
name|analysis
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|TokenStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|tokenattributes
operator|.
name|CharTermAttribute
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|tokenattributes
operator|.
name|PositionIncrementAttribute
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|Injector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|ModulesBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|SettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|EnvironmentModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexNameModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
operator|.
name|AnalysisModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
operator|.
name|AnalysisService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|settings
operator|.
name|IndexSettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|analysis
operator|.
name|IndicesAnalysisModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|analysis
operator|.
name|IndicesAnalysisService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|AnalysisTestsHelper
specifier|public
class|class
name|AnalysisTestsHelper
block|{
DECL|method|createAnalysisServiceFromClassPath
specifier|public
specifier|static
name|AnalysisService
name|createAnalysisServiceFromClassPath
parameter_list|(
name|String
name|resource
parameter_list|)
block|{
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
name|resource
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
return|;
block|}
DECL|method|createAnalysisServiceFromSettings
specifier|public
specifier|static
name|AnalysisService
name|createAnalysisServiceFromSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|Injector
name|parentInjector
init|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|SettingsModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|EnvironmentModule
argument_list|(
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|,
operator|new
name|IndicesAnalysisModule
argument_list|()
argument_list|)
operator|.
name|createInjector
argument_list|()
decl_stmt|;
name|AnalysisModule
name|analysisModule
init|=
operator|new
name|AnalysisModule
argument_list|(
name|settings
argument_list|,
name|parentInjector
operator|.
name|getInstance
argument_list|(
name|IndicesAnalysisService
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Injector
name|injector
init|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|IndexSettingsModule
argument_list|(
name|index
argument_list|,
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexNameModule
argument_list|(
name|index
argument_list|)
argument_list|,
name|analysisModule
argument_list|)
operator|.
name|createChildInjector
argument_list|(
name|parentInjector
argument_list|)
decl_stmt|;
return|return
name|injector
operator|.
name|getInstance
argument_list|(
name|AnalysisService
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|assertSimpleTSOutput
specifier|public
specifier|static
name|void
name|assertSimpleTSOutput
parameter_list|(
name|TokenStream
name|stream
parameter_list|,
name|String
index|[]
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|stream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|termAttr
init|=
name|stream
operator|.
name|getAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|termAttr
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|stream
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|i
operator|<
name|expected
operator|.
name|length
argument_list|,
literal|"got extra term: "
operator|+
name|termAttr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|termAttr
operator|.
name|toString
argument_list|()
argument_list|,
name|expected
index|[
name|i
index|]
argument_list|,
literal|"expected different term at index "
operator|+
name|i
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i
argument_list|,
name|expected
operator|.
name|length
argument_list|,
literal|"not all tokens produced"
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSimpleTSOutput
specifier|public
specifier|static
name|void
name|assertSimpleTSOutput
parameter_list|(
name|TokenStream
name|stream
parameter_list|,
name|String
index|[]
name|expected
parameter_list|,
name|int
index|[]
name|posInc
parameter_list|)
throws|throws
name|IOException
block|{
name|stream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|termAttr
init|=
name|stream
operator|.
name|getAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|PositionIncrementAttribute
name|posIncAttr
init|=
name|stream
operator|.
name|getAttribute
argument_list|(
name|PositionIncrementAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|termAttr
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|stream
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|i
operator|<
name|expected
operator|.
name|length
argument_list|,
literal|"got extra term: "
operator|+
name|termAttr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|termAttr
operator|.
name|toString
argument_list|()
argument_list|,
name|expected
index|[
name|i
index|]
argument_list|,
literal|"expected different term at index "
operator|+
name|i
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|posIncAttr
operator|.
name|getPositionIncrement
argument_list|()
argument_list|,
name|posInc
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i
argument_list|,
name|expected
operator|.
name|length
argument_list|,
literal|"not all tokens produced"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

