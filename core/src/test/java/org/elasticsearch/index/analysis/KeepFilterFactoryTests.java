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
name|core
operator|.
name|WhitespaceTokenizer
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
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_class
DECL|class|KeepFilterFactoryTests
specifier|public
class|class
name|KeepFilterFactoryTests
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
literal|"/org/elasticsearch/index/analysis/keep_analysis.json"
decl_stmt|;
DECL|method|testLoadWithoutSettings
specifier|public
name|void
name|testLoadWithoutSettings
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
literal|"keep"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|tokenFilter
argument_list|)
expr_stmt|;
block|}
DECL|method|testLoadOverConfiguredSettings
specifier|public
name|void
name|testLoadOverConfiguredSettings
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.broken_keep_filter.type"
argument_list|,
literal|"keep"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.broken_keep_filter.keep_words_path"
argument_list|,
literal|"does/not/exists.txt"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.broken_keep_filter.keep_words"
argument_list|,
literal|"[\"Hello\", \"worlD\"]"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"path and array are configured"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{         }
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"expected IAE"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testKeepWordsPathSettings
specifier|public
name|void
name|testKeepWordsPathSettings
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.non_broken_keep_filter.type"
argument_list|,
literal|"keep"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.non_broken_keep_filter.keep_words_path"
argument_list|,
literal|"does/not/exists.txt"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
comment|// test our none existing setup is picked up
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected an exception due to non existent keep_words_path"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{         }
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"expected IAE"
argument_list|)
expr_stmt|;
block|}
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.non_broken_keep_filter.keep_words"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"test"
block|}
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
try|try
block|{
comment|// test our none existing setup is picked up
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected an exception indicating that you can't use [keep_words_path] with [keep_words] "
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{         }
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"expected IAE"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCaseInsensitiveMapping
specifier|public
name|void
name|testCaseInsensitiveMapping
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
literal|"my_keep_filter"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|tokenFilter
argument_list|,
name|instanceOf
argument_list|(
name|KeepWordFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|source
init|=
literal|"hello small world"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"hello"
block|,
literal|"world"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
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
argument_list|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testCaseSensitiveMapping
specifier|public
name|void
name|testCaseSensitiveMapping
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
literal|"my_case_sensitive_keep_filter"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|tokenFilter
argument_list|,
name|instanceOf
argument_list|(
name|KeepWordFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|source
init|=
literal|"Hello small world"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"Hello"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
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
argument_list|,
operator|new
name|int
index|[]
block|{
literal|1
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

