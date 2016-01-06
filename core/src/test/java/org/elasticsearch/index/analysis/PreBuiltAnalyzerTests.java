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
name|compress
operator|.
name|CompressedXContent
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
name|xcontent
operator|.
name|XContentFactory
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
name|mapper
operator|.
name|DocumentMapper
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
name|mapper
operator|.
name|FieldMapper
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
name|PreBuiltAnalyzers
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
name|ESSingleNodeTestCase
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|VersionUtils
operator|.
name|randomVersion
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
name|contains
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
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
name|not
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PreBuiltAnalyzerTests
specifier|public
class|class
name|PreBuiltAnalyzerTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testThatDefaultAndStandardAnalyzerAreTheSameInstance
specifier|public
name|void
name|testThatDefaultAndStandardAnalyzerAreTheSameInstance
parameter_list|()
block|{
name|Analyzer
name|currentStandardAnalyzer
init|=
name|PreBuiltAnalyzers
operator|.
name|STANDARD
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Analyzer
name|currentDefaultAnalyzer
init|=
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
comment|// special case, these two are the same instance
name|assertThat
argument_list|(
name|currentDefaultAnalyzer
argument_list|,
name|is
argument_list|(
name|currentStandardAnalyzer
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatDefaultAndStandardAnalyzerChangedIn10Beta1
specifier|public
name|void
name|testThatDefaultAndStandardAnalyzerChangedIn10Beta1
parameter_list|()
throws|throws
name|IOException
block|{
name|Analyzer
name|currentStandardAnalyzer
init|=
name|PreBuiltAnalyzers
operator|.
name|STANDARD
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_1_0_0_Beta1
argument_list|)
decl_stmt|;
name|Analyzer
name|currentDefaultAnalyzer
init|=
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_1_0_0_Beta1
argument_list|)
decl_stmt|;
comment|// special case, these two are the same instance
name|assertThat
argument_list|(
name|currentDefaultAnalyzer
argument_list|,
name|is
argument_list|(
name|currentStandardAnalyzer
argument_list|)
argument_list|)
expr_stmt|;
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_1_0_0_Beta1
argument_list|)
expr_stmt|;
specifier|final
name|int
name|n
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|Version
name|version
init|=
name|Version
operator|.
name|CURRENT
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
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|version
operator|.
name|equals
argument_list|(
name|Version
operator|.
name|V_1_0_0_Beta1
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|currentDefaultAnalyzer
argument_list|,
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|currentDefaultAnalyzer
argument_list|,
name|not
argument_list|(
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Analyzer
name|analyzer
init|=
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
decl_stmt|;
name|TokenStream
name|ts
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"foo"
argument_list|,
literal|"This is it Dude"
argument_list|)
decl_stmt|;
name|ts
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|charTermAttribute
init|=
name|ts
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|ts
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_0_0_Beta1
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
argument_list|,
name|contains
argument_list|(
literal|"this"
argument_list|,
literal|"is"
argument_list|,
literal|"it"
argument_list|,
literal|"dude"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
argument_list|,
name|contains
argument_list|(
literal|"dude"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ts
operator|.
name|close
argument_list|()
expr_stmt|;
name|version
operator|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testAnalyzerChangedIn10RC1
specifier|public
name|void
name|testAnalyzerChangedIn10RC1
parameter_list|()
throws|throws
name|IOException
block|{
name|Analyzer
name|pattern
init|=
name|PreBuiltAnalyzers
operator|.
name|PATTERN
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_1_0_0_RC1
argument_list|)
decl_stmt|;
name|Analyzer
name|standardHtml
init|=
name|PreBuiltAnalyzers
operator|.
name|STANDARD_HTML_STRIP
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_1_0_0_RC1
argument_list|)
decl_stmt|;
specifier|final
name|int
name|n
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|Version
name|version
init|=
name|Version
operator|.
name|CURRENT
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
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|version
operator|.
name|equals
argument_list|(
name|Version
operator|.
name|V_1_0_0_RC1
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|pattern
argument_list|,
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|PATTERN
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|standardHtml
argument_list|,
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|STANDARD_HTML_STRIP
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|pattern
argument_list|,
name|not
argument_list|(
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|standardHtml
argument_list|,
name|not
argument_list|(
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|DEFAULT
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Analyzer
name|analyzer
init|=
name|randomBoolean
argument_list|()
condition|?
name|PreBuiltAnalyzers
operator|.
name|PATTERN
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
else|:
name|PreBuiltAnalyzers
operator|.
name|STANDARD_HTML_STRIP
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
decl_stmt|;
name|TokenStream
name|ts
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"foo"
argument_list|,
literal|"This is it Dude"
argument_list|)
decl_stmt|;
name|ts
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|charTermAttribute
init|=
name|ts
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|ts
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_0_0_RC1
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|toString
argument_list|()
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
argument_list|,
name|contains
argument_list|(
literal|"this"
argument_list|,
literal|"is"
argument_list|,
literal|"it"
argument_list|,
literal|"dude"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
argument_list|,
name|contains
argument_list|(
literal|"dude"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ts
operator|.
name|close
argument_list|()
expr_stmt|;
name|version
operator|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testThatInstancesAreTheSameAlwaysForKeywordAnalyzer
specifier|public
name|void
name|testThatInstancesAreTheSameAlwaysForKeywordAnalyzer
parameter_list|()
block|{
name|assertThat
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|KEYWORD
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|KEYWORD
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_0_18_0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatInstancesAreCachedAndReused
specifier|public
name|void
name|testThatInstancesAreCachedAndReused
parameter_list|()
block|{
name|assertThat
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|ARABIC
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|ARABIC
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|ARABIC
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_0_18_0
argument_list|)
argument_list|,
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|ARABIC
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_0_18_0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatInstancesWithSameLuceneVersionAreReused
specifier|public
name|void
name|testThatInstancesWithSameLuceneVersionAreReused
parameter_list|()
block|{
comment|// both are lucene 4.4 and should return the same instance
name|assertThat
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|CATALAN
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_0_90_4
argument_list|)
argument_list|,
name|is
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|CATALAN
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|V_0_90_5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatAnalyzersAreUsedInMapping
specifier|public
name|void
name|testThatAnalyzersAreUsedInMapping
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|randomInt
init|=
name|randomInt
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|PreBuiltAnalyzers
name|randomPreBuiltAnalyzer
init|=
name|PreBuiltAnalyzers
operator|.
name|values
argument_list|()
index|[
name|randomInt
index|]
decl_stmt|;
name|String
name|analyzerName
init|=
name|randomPreBuiltAnalyzer
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|Version
name|randomVersion
init|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|randomVersion
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NamedAnalyzer
name|namedAnalyzer
init|=
operator|new
name|PreBuiltAnalyzerProvider
argument_list|(
name|analyzerName
argument_list|,
name|AnalyzerScope
operator|.
name|INDEX
argument_list|,
name|randomPreBuiltAnalyzer
operator|.
name|getAnalyzer
argument_list|(
name|randomVersion
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
name|analyzerName
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|indexSettings
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NamedAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|NamedAnalyzer
name|fieldMapperNamedAnalyzer
init|=
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fieldMapperNamedAnalyzer
operator|.
name|analyzer
argument_list|()
argument_list|,
name|is
argument_list|(
name|namedAnalyzer
operator|.
name|analyzer
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

