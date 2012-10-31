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
name|Tokenizer
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
name|core
operator|.
name|WhitespaceTokenizer
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
name|util
operator|.
name|Version
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
name|analysis
operator|.
name|ShingleTokenFilterFactory
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
name|TokenFilterFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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

begin_class
DECL|class|ShingleTokenFilterFactoryTests
specifier|public
class|class
name|ShingleTokenFilterFactoryTests
block|{
DECL|field|RESOURCE
specifier|private
specifier|static
specifier|final
name|String
name|RESOURCE
init|=
literal|"org/elasticsearch/test/unit/index/analysis/shingle_analysis.json"
decl_stmt|;
annotation|@
name|Test
DECL|method|testDefault
specifier|public
name|void
name|testDefault
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromClassPath
argument_list|(
name|RESOURCE
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"shingle"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown fox"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"the quick"
block|,
literal|"quick"
block|,
literal|"quick brown"
block|,
literal|"brown"
block|,
literal|"brown fox"
block|,
literal|"fox"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|(
name|Version
operator|.
name|LUCENE_36
argument_list|,
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|AnalysisTestsHelper
operator|.
name|assertSimpleTSOutput
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testInverseMapping
specifier|public
name|void
name|testInverseMapping
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromClassPath
argument_list|(
name|RESOURCE
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"shingle_inverse"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|tokenFilter
argument_list|,
name|instanceOf
argument_list|(
name|ShingleTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|source
init|=
literal|"the quick brown fox"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the_quick_brown"
block|,
literal|"quick_brown_fox"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|(
name|Version
operator|.
name|LUCENE_36
argument_list|,
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|AnalysisTestsHelper
operator|.
name|assertSimpleTSOutput
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testInverseMappingNoShingles
specifier|public
name|void
name|testInverseMappingNoShingles
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromClassPath
argument_list|(
name|RESOURCE
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"shingle_inverse"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|tokenFilter
argument_list|,
name|instanceOf
argument_list|(
name|ShingleTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|source
init|=
literal|"the quick"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"quick"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|(
name|Version
operator|.
name|LUCENE_36
argument_list|,
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|AnalysisTestsHelper
operator|.
name|assertSimpleTSOutput
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

