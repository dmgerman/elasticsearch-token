begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|json
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|StringHelper
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
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Maps
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|JsonQueryParserRegistry
specifier|public
class|class
name|JsonQueryParserRegistry
block|{
DECL|field|queryParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|JsonQueryParser
argument_list|>
name|queryParsers
decl_stmt|;
DECL|field|filterParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|JsonFilterParser
argument_list|>
name|filterParsers
decl_stmt|;
DECL|method|JsonQueryParserRegistry
specifier|public
name|JsonQueryParserRegistry
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|AnalysisService
name|analysisService
parameter_list|,
annotation|@
name|Nullable
name|Iterable
argument_list|<
name|JsonQueryParser
argument_list|>
name|queryParsers
parameter_list|,
annotation|@
name|Nullable
name|Iterable
argument_list|<
name|JsonFilterParser
argument_list|>
name|filterParsers
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|JsonQueryParser
argument_list|>
name|queryParsersMap
init|=
name|newHashMap
argument_list|()
decl_stmt|;
comment|// add defaults
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|DisMaxJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|MatchAllJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|QueryStringJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|analysisService
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|BoolJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|TermJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|FieldJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|analysisService
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|RangeJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|PrefixJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|WildcardJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|FilteredJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|ConstantScoreQueryJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|SpanTermJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|SpanNotJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|SpanFirstJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|SpanNearJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|SpanOrJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|MoreLikeThisJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|MoreLikeThisFieldJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|FuzzyLikeThisJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|queryParsersMap
argument_list|,
operator|new
name|FuzzyLikeThisFieldJsonQueryParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
comment|// now, copy over the ones provided
if|if
condition|(
name|queryParsers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|JsonQueryParser
name|queryParser
range|:
name|queryParsers
control|)
block|{
name|add
argument_list|(
name|queryParsersMap
argument_list|,
name|queryParser
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|queryParsers
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|queryParsersMap
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|JsonFilterParser
argument_list|>
name|filterParsersMap
init|=
name|newHashMap
argument_list|()
decl_stmt|;
comment|// add defaults
name|add
argument_list|(
name|filterParsersMap
argument_list|,
operator|new
name|TermJsonFilterParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|filterParsersMap
argument_list|,
operator|new
name|TermsJsonFilterParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|filterParsersMap
argument_list|,
operator|new
name|RangeJsonFilterParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|filterParsersMap
argument_list|,
operator|new
name|PrefixJsonFilterParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|filterParsersMap
argument_list|,
operator|new
name|QueryJsonFilterParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|filterParsersMap
argument_list|,
operator|new
name|BoolJsonFilterParser
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterParsers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|JsonFilterParser
name|filterParser
range|:
name|filterParsers
control|)
block|{
name|add
argument_list|(
name|filterParsersMap
argument_list|,
name|filterParser
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|filterParsers
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|filterParsersMap
argument_list|)
expr_stmt|;
block|}
DECL|method|queryParser
specifier|public
name|JsonQueryParser
name|queryParser
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|queryParsers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|filterParser
specifier|public
name|JsonFilterParser
name|filterParser
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|filterParsers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|add
specifier|private
name|void
name|add
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|JsonFilterParser
argument_list|>
name|map
parameter_list|,
name|JsonFilterParser
name|filterParser
parameter_list|)
block|{
for|for
control|(
name|String
name|name
range|:
name|filterParser
operator|.
name|names
argument_list|()
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|StringHelper
operator|.
name|intern
argument_list|(
name|name
argument_list|)
argument_list|,
name|filterParser
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|add
specifier|private
name|void
name|add
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|JsonQueryParser
argument_list|>
name|map
parameter_list|,
name|JsonQueryParser
name|jsonQueryParser
parameter_list|)
block|{
for|for
control|(
name|String
name|name
range|:
name|jsonQueryParser
operator|.
name|names
argument_list|()
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|StringHelper
operator|.
name|intern
argument_list|(
name|name
argument_list|)
argument_list|,
name|jsonQueryParser
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

