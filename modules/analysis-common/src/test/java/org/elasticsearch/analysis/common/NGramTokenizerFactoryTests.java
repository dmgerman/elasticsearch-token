begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.analysis.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|analysis
operator|.
name|common
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
name|MockTokenizer
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
name|ngram
operator|.
name|EdgeNGramTokenFilter
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
name|reverse
operator|.
name|ReverseStringFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|Settings
operator|.
name|Builder
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
name|IndexSettings
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
name|EdgeNGramTokenizerFactory
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
name|NGramTokenizerFactory
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
name|elasticsearch
operator|.
name|test
operator|.
name|IndexSettingsModule
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
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedTest
operator|.
name|scaledRandomIntBetween
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
DECL|class|NGramTokenizerFactoryTests
specifier|public
class|class
name|NGramTokenizerFactoryTests
extends|extends
name|ESTokenStreamTestCase
block|{
DECL|method|testParseTokenChars
specifier|public
name|void
name|testParseTokenChars
parameter_list|()
block|{
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|name
init|=
literal|"ngr"
decl_stmt|;
specifier|final
name|Settings
name|indexSettings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|indexProperties
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tokenChars
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|"letters"
argument_list|,
literal|"number"
argument_list|,
literal|"DIRECTIONALITY_UNDEFINED"
argument_list|)
control|)
block|{
specifier|final
name|Settings
name|settings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|3
argument_list|)
operator|.
name|put
argument_list|(
literal|"token_chars"
argument_list|,
name|tokenChars
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
operator|new
name|NGramTokenizerFactory
argument_list|(
name|indexProperties
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|expected
parameter_list|)
block|{
comment|// OK
block|}
block|}
for|for
control|(
name|String
name|tokenChars
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|"letter"
argument_list|,
literal|" digit "
argument_list|,
literal|"punctuation"
argument_list|,
literal|"DIGIT"
argument_list|,
literal|"CoNtRoL"
argument_list|,
literal|"dash_punctuation"
argument_list|)
control|)
block|{
specifier|final
name|Settings
name|settings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|3
argument_list|)
operator|.
name|put
argument_list|(
literal|"token_chars"
argument_list|,
name|tokenChars
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexProperties
operator|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
operator|new
name|NGramTokenizerFactory
argument_list|(
name|indexProperties
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
comment|// no exception
block|}
block|}
DECL|method|testNoTokenChars
specifier|public
name|void
name|testNoTokenChars
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|name
init|=
literal|"ngr"
decl_stmt|;
specifier|final
name|Settings
name|indexSettings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Settings
name|settings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|4
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"token_chars"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|NGramTokenizerFactory
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|"1.34"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenizer
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"1."
block|,
literal|"1.3"
block|,
literal|"1.34"
block|,
literal|".3"
block|,
literal|".34"
block|,
literal|"34"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testPreTokenization
specifier|public
name|void
name|testPreTokenization
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Make sure that pretokenization works well and that it can be used even with token chars which are supplementary characters
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|name
init|=
literal|"ngr"
decl_stmt|;
specifier|final
name|Settings
name|indexSettings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|3
argument_list|)
operator|.
name|put
argument_list|(
literal|"token_chars"
argument_list|,
literal|"letter,digit"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|NGramTokenizerFactory
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|"Ãbc dÃ©f g\uD801\uDC00f "
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenizer
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Ãb"
block|,
literal|"Ãbc"
block|,
literal|"bc"
block|,
literal|"dÃ©"
block|,
literal|"dÃ©f"
block|,
literal|"Ã©f"
block|,
literal|"g\uD801\uDC00"
block|,
literal|"g\uD801\uDC00f"
block|,
literal|"\uD801\uDC00f"
block|}
argument_list|)
expr_stmt|;
name|settings
operator|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|3
argument_list|)
operator|.
name|put
argument_list|(
literal|"token_chars"
argument_list|,
literal|"letter,digit,punctuation,whitespace,symbol"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|tokenizer
operator|=
operator|new
name|NGramTokenizerFactory
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|" a!$ 9"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenizer
argument_list|,
operator|new
name|String
index|[]
block|{
literal|" a"
block|,
literal|" a!"
block|,
literal|"a!"
block|,
literal|"a!$"
block|,
literal|"!$"
block|,
literal|"!$ "
block|,
literal|"$ "
block|,
literal|"$ 9"
block|,
literal|" 9"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testPreTokenizationEdge
specifier|public
name|void
name|testPreTokenizationEdge
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Make sure that pretokenization works well and that it can be used even with token chars which are supplementary characters
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|name
init|=
literal|"ngr"
decl_stmt|;
specifier|final
name|Settings
name|indexSettings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|3
argument_list|)
operator|.
name|put
argument_list|(
literal|"token_chars"
argument_list|,
literal|"letter,digit"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|EdgeNGramTokenizerFactory
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|"Ãbc dÃ©f g\uD801\uDC00f "
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenizer
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Ãb"
block|,
literal|"Ãbc"
block|,
literal|"dÃ©"
block|,
literal|"dÃ©f"
block|,
literal|"g\uD801\uDC00"
block|,
literal|"g\uD801\uDC00f"
block|}
argument_list|)
expr_stmt|;
name|settings
operator|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|3
argument_list|)
operator|.
name|put
argument_list|(
literal|"token_chars"
argument_list|,
literal|"letter,digit,punctuation,whitespace,symbol"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|tokenizer
operator|=
operator|new
name|EdgeNGramTokenizerFactory
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|" a!$ 9"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenizer
argument_list|,
operator|new
name|String
index|[]
block|{
literal|" a"
block|,
literal|" a!"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testBackwardsCompatibilityEdgeNgramTokenFilter
specifier|public
name|void
name|testBackwardsCompatibilityEdgeNgramTokenFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|20
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|iters
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|name
init|=
literal|"ngr"
decl_stmt|;
name|Version
name|v
init|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Builder
name|builder
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"min_gram"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"max_gram"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|boolean
name|reverse
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|reverse
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"side"
argument_list|,
literal|"back"
argument_list|)
expr_stmt|;
block|}
name|Settings
name|settings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|newAnalysisSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|v
operator|.
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|MockTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|"foo bar"
argument_list|)
argument_list|)
expr_stmt|;
name|TokenStream
name|edgeNGramTokenFilter
init|=
operator|new
name|EdgeNGramTokenFilterFactory
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
decl_stmt|;
if|if
condition|(
name|reverse
condition|)
block|{
name|assertThat
argument_list|(
name|edgeNGramTokenFilter
argument_list|,
name|instanceOf
argument_list|(
name|ReverseStringFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|edgeNGramTokenFilter
argument_list|,
name|instanceOf
argument_list|(
name|EdgeNGramTokenFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|randomVersion
specifier|private
name|Version
name|randomVersion
parameter_list|(
name|Random
name|random
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IllegalAccessException
block|{
name|Field
index|[]
name|declaredFields
init|=
name|Version
operator|.
name|class
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Field
argument_list|>
name|versionFields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Field
name|field
range|:
name|declaredFields
control|)
block|{
if|if
condition|(
operator|(
name|field
operator|.
name|getModifiers
argument_list|()
operator|&
name|Modifier
operator|.
name|STATIC
operator|)
operator|!=
literal|0
operator|&&
name|field
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"V_"
argument_list|)
operator|&&
name|field
operator|.
name|getType
argument_list|()
operator|==
name|Version
operator|.
name|class
condition|)
block|{
name|versionFields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|Version
operator|)
name|versionFields
operator|.
name|get
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
name|versionFields
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|Version
operator|.
name|class
argument_list|)
return|;
block|}
block|}
end_class

end_unit

