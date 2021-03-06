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
name|CharArraySet
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
DECL|class|FingerprintAnalyzerTests
specifier|public
class|class
name|FingerprintAnalyzerTests
extends|extends
name|ESTokenStreamTestCase
block|{
DECL|method|testFingerprint
specifier|public
name|void
name|testFingerprint
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|FingerprintAnalyzer
argument_list|(
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|,
literal|' '
argument_list|,
literal|255
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"foo bar@baz Baz $ foo foo FOO. FoO"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bar baz foo"
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
name|FingerprintAnalyzer
argument_list|(
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|,
literal|' '
argument_list|,
literal|255
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"foo bar baz Baz foo foo FOO. FoO"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bar baz foo"
block|}
argument_list|)
expr_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"xyz XYZ abc 123.2 abc"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"123.2 abc xyz"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testAsciifolding
specifier|public
name|void
name|testAsciifolding
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|FingerprintAnalyzer
argument_list|(
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|,
literal|' '
argument_list|,
literal|255
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"gÃ¶del escher bach"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bach escher godel"
block|}
argument_list|)
expr_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"gÃ¶del godel escher bach"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bach escher godel"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testLimit
specifier|public
name|void
name|testLimit
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|FingerprintAnalyzer
argument_list|(
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|,
literal|' '
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"e d c b a"
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"b a"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"a b"
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

