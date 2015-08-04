begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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

begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|Analyzer
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
name|StandardAnalyzer
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

begin_class
DECL|class|SnowballAnalyzerTests
specifier|public
class|class
name|SnowballAnalyzerTests
extends|extends
name|ESTokenStreamTestCase
block|{
DECL|method|testEnglish
specifier|public
name|void
name|testEnglish
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|SnowballAnalyzer
argument_list|(
literal|"English"
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"he abhorred accents"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"he"
block|,
literal|"abhor"
block|,
literal|"accent"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testStopwords
specifier|public
name|void
name|testStopwords
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|SnowballAnalyzer
argument_list|(
literal|"English"
argument_list|,
name|StandardAnalyzer
operator|.
name|STOP_WORDS_SET
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"the quick brown fox jumped"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"quick"
block|,
literal|"brown"
block|,
literal|"fox"
block|,
literal|"jump"
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test turkish lowercasing    */
DECL|method|testTurkish
specifier|public
name|void
name|testTurkish
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|SnowballAnalyzer
argument_list|(
literal|"Turkish"
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"aÄacÄ±"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"aÄaÃ§"
block|}
argument_list|)
expr_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"AÄACI"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"aÄaÃ§"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testReusableTokenStream
specifier|public
name|void
name|testReusableTokenStream
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|SnowballAnalyzer
argument_list|(
literal|"English"
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"he abhorred accents"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"he"
block|,
literal|"abhor"
block|,
literal|"accent"
block|}
argument_list|)
expr_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"she abhorred him"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"she"
block|,
literal|"abhor"
block|,
literal|"him"
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

