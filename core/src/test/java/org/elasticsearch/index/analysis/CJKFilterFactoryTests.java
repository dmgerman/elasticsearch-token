begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
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
name|standard
operator|.
name|StandardTokenizer
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
name|ESTokenStreamTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
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

begin_class
DECL|class|CJKFilterFactoryTests
specifier|public
class|class
name|CJKFilterFactoryTests
extends|extends
name|ESTokenStreamTestCase
block|{
DECL|field|RESOURCE
specifier|private
specifier|static
specifier|final
name|String
name|RESOURCE
init|=
literal|"org/elasticsearch/index/analysis/cjk_analysis.json"
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
name|createTempDir
argument_list|()
argument_list|,
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
literal|"cjk_bigram"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"å¤ãã®å­¦çãè©¦é¨ã«è½ã¡ãã"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"å¤ã"
block|,
literal|"ãã®"
block|,
literal|"ã®å­¦"
block|,
literal|"å­¦ç"
block|,
literal|"çã"
block|,
literal|"ãè©¦"
block|,
literal|"è©¦é¨"
block|,
literal|"é¨ã«"
block|,
literal|"ã«è½"
block|,
literal|"è½ã¡"
block|,
literal|"ã¡ã"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|StandardTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
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
DECL|method|testNoFlags
specifier|public
name|void
name|testNoFlags
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
name|createTempDir
argument_list|()
argument_list|,
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
literal|"cjk_no_flags"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"å¤ãã®å­¦çãè©¦é¨ã«è½ã¡ãã"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"å¤ã"
block|,
literal|"ãã®"
block|,
literal|"ã®å­¦"
block|,
literal|"å­¦ç"
block|,
literal|"çã"
block|,
literal|"ãè©¦"
block|,
literal|"è©¦é¨"
block|,
literal|"é¨ã«"
block|,
literal|"ã«è½"
block|,
literal|"è½ã¡"
block|,
literal|"ã¡ã"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|StandardTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
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
DECL|method|testHanOnly
specifier|public
name|void
name|testHanOnly
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
name|createTempDir
argument_list|()
argument_list|,
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
literal|"cjk_han_only"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"å¤ãã®å­¦çãè©¦é¨ã«è½ã¡ãã"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"å¤"
block|,
literal|"ã"
block|,
literal|"ã®"
block|,
literal|"å­¦ç"
block|,
literal|"ã"
block|,
literal|"è©¦é¨"
block|,
literal|"ã«"
block|,
literal|"è½"
block|,
literal|"ã¡"
block|,
literal|"ã"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|StandardTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
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
DECL|method|testHanUnigramOnly
specifier|public
name|void
name|testHanUnigramOnly
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
name|createTempDir
argument_list|()
argument_list|,
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
literal|"cjk_han_unigram_only"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"å¤ãã®å­¦çãè©¦é¨ã«è½ã¡ãã"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"å¤"
block|,
literal|"ã"
block|,
literal|"ã®"
block|,
literal|"å­¦"
block|,
literal|"å­¦ç"
block|,
literal|"ç"
block|,
literal|"ã"
block|,
literal|"è©¦"
block|,
literal|"è©¦é¨"
block|,
literal|"é¨"
block|,
literal|"ã«"
block|,
literal|"è½"
block|,
literal|"ã¡"
block|,
literal|"ã"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|StandardTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
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

