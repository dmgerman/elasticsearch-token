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
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
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
name|query
operator|.
name|Operator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|ESIntegTestCase
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
name|Collection
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|queryStringQuery
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertHitCount
import|;
end_import

begin_class
DECL|class|QueryStringWithAnalyzersTests
specifier|public
class|class
name|QueryStringWithAnalyzersTests
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|CommonAnalysisPlugin
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**      * Validates that we properly split fields using the word delimiter filter in query_string.      */
DECL|method|testCustomWordDelimiterQueryString
specifier|public
name|void
name|testCustomWordDelimiterQueryString
parameter_list|()
block|{
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
literal|"analysis.analyzer.my_analyzer.type"
argument_list|,
literal|"custom"
argument_list|,
literal|"analysis.analyzer.my_analyzer.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|,
literal|"analysis.analyzer.my_analyzer.filter"
argument_list|,
literal|"custom_word_delimiter"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.generate_word_parts"
argument_list|,
literal|"true"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.generate_number_parts"
argument_list|,
literal|"false"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.catenate_numbers"
argument_list|,
literal|"true"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.catenate_words"
argument_list|,
literal|"false"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.split_on_case_change"
argument_list|,
literal|"false"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.split_on_numerics"
argument_list|,
literal|"false"
argument_list|,
literal|"analysis.filter.custom_word_delimiter.stem_english_possessive"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"field1"
argument_list|,
literal|"type=text,analyzer=my_analyzer"
argument_list|,
literal|"field2"
argument_list|,
literal|"type=text,analyzer=my_analyzer"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"foo bar baz"
argument_list|,
literal|"field2"
argument_list|,
literal|"not needed"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|queryStringQuery
argument_list|(
literal|"foo.baz"
argument_list|)
operator|.
name|useDisMax
argument_list|(
literal|false
argument_list|)
operator|.
name|defaultOperator
argument_list|(
name|Operator
operator|.
name|AND
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|response
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
