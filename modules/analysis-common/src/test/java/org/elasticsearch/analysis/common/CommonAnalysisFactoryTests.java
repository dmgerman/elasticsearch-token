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
name|reverse
operator|.
name|ReverseStringFilterFactory
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
name|HtmlStripCharFilterFactory
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
name|AnalysisFactoryTestCase
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyList
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
operator|.
name|toList
import|;
end_import

begin_class
DECL|class|CommonAnalysisFactoryTests
specifier|public
class|class
name|CommonAnalysisFactoryTests
extends|extends
name|AnalysisFactoryTestCase
block|{
DECL|method|CommonAnalysisFactoryTests
specifier|public
name|CommonAnalysisFactoryTests
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getTokenizers
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|getTokenizers
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|tokenizers
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|super
operator|.
name|getTokenizers
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|tokenizers
return|;
block|}
annotation|@
name|Override
DECL|method|getTokenFilters
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|getTokenFilters
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|filters
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|super
operator|.
name|getTokenFilters
argument_list|()
argument_list|)
decl_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"asciifolding"
argument_list|,
name|ASCIIFoldingTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"worddelimiter"
argument_list|,
name|WordDelimiterTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"worddelimitergraph"
argument_list|,
name|WordDelimiterGraphTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|filters
return|;
block|}
annotation|@
name|Override
DECL|method|getCharFilters
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|getCharFilters
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|filters
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|super
operator|.
name|getCharFilters
argument_list|()
argument_list|)
decl_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"htmlstrip"
argument_list|,
name|HtmlStripCharFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|MappingCharFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"patternreplace"
argument_list|,
name|PatternReplaceCharFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// TODO: these charfilters are not yet exposed: useful?
comment|// handling of zwnj for persian
name|filters
operator|.
name|put
argument_list|(
literal|"persian"
argument_list|,
name|Void
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|filters
return|;
block|}
annotation|@
name|Override
DECL|method|getPreConfiguredTokenFilters
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|getPreConfiguredTokenFilters
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|filters
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|super
operator|.
name|getPreConfiguredTokenFilters
argument_list|()
argument_list|)
decl_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"asciifolding"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"classic"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"common_grams"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"edge_ngram"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"edgeNGram"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"kstem"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"length"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"ngram"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"nGram"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"porter_stem"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"reverse"
argument_list|,
name|ReverseStringFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"stop"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"trim"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"truncate"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"unique"
argument_list|,
name|Void
operator|.
name|class
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"uppercase"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"word_delimiter"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|filters
operator|.
name|put
argument_list|(
literal|"word_delimiter_graph"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|filters
return|;
block|}
comment|/**      * Fails if a tokenizer is marked in the superclass with {@link MovedToAnalysisCommon} but      * hasn't been marked in this class with its proper factory.      */
DECL|method|testAllTokenizersMarked
specifier|public
name|void
name|testAllTokenizersMarked
parameter_list|()
block|{
name|markedTestCase
argument_list|(
literal|"char filter"
argument_list|,
name|getTokenizers
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Fails if a char filter is marked in the superclass with {@link MovedToAnalysisCommon} but      * hasn't been marked in this class with its proper factory.      */
DECL|method|testAllCharFiltersMarked
specifier|public
name|void
name|testAllCharFiltersMarked
parameter_list|()
block|{
name|markedTestCase
argument_list|(
literal|"char filter"
argument_list|,
name|getCharFilters
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Fails if a char filter is marked in the superclass with {@link MovedToAnalysisCommon} but      * hasn't been marked in this class with its proper factory.      */
DECL|method|testAllTokenFiltersMarked
specifier|public
name|void
name|testAllTokenFiltersMarked
parameter_list|()
block|{
name|markedTestCase
argument_list|(
literal|"token filter"
argument_list|,
name|getTokenFilters
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|markedTestCase
specifier|private
name|void
name|markedTestCase
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|map
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|unmarked
init|=
name|map
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getValue
argument_list|()
operator|==
name|MovedToAnalysisCommon
operator|.
name|class
argument_list|)
operator|.
name|map
argument_list|(
name|Map
operator|.
name|Entry
operator|::
name|getKey
argument_list|)
operator|.
name|sorted
argument_list|()
operator|.
name|collect
argument_list|(
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|name
operator|+
literal|" marked in AnalysisFactoryTestCase as moved to analysis-common "
operator|+
literal|"but not mapped here"
argument_list|,
name|emptyList
argument_list|()
argument_list|,
name|unmarked
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

