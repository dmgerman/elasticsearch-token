begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
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
name|search
operator|.
name|Query
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
name|ParseField
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteable
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|io
operator|.
name|stream
operator|.
name|Writeable
operator|.
name|Reader
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
name|lucene
operator|.
name|search
operator|.
name|function
operator|.
name|ScoreFunction
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
name|XContent
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
name|QueryBuilder
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
name|QueryParser
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
name|functionscore
operator|.
name|ScoreFunctionBuilder
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
name|functionscore
operator|.
name|ScoreFunctionParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|significant
operator|.
name|SignificantTerms
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|significant
operator|.
name|heuristics
operator|.
name|SignificanceHeuristic
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|significant
operator|.
name|heuristics
operator|.
name|SignificanceHeuristicParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|pipeline
operator|.
name|movavg
operator|.
name|MovAvgPipelineAggregator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|pipeline
operator|.
name|movavg
operator|.
name|models
operator|.
name|MovAvgModel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|FetchSubPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
operator|.
name|Highlighter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggester
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
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_comment
comment|/**  * Plugin for extending search time behavior.  */
end_comment

begin_interface
DECL|interface|SearchPlugin
specifier|public
interface|interface
name|SearchPlugin
block|{
comment|/**      * The new {@link ScoreFunction}s defined by this plugin.      */
DECL|method|getScoreFunctions
specifier|default
name|List
argument_list|<
name|ScoreFunctionSpec
argument_list|<
name|?
argument_list|>
argument_list|>
name|getScoreFunctions
parameter_list|()
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * The new {@link SignificanceHeuristic}s defined by this plugin. {@linkplain SignificanceHeuristic}s are used by the      * {@link SignificantTerms} aggregation to pick which terms are significant for a given query.      */
DECL|method|getSignificanceHeuristics
specifier|default
name|List
argument_list|<
name|SearchExtensionSpec
argument_list|<
name|SignificanceHeuristic
argument_list|,
name|SignificanceHeuristicParser
argument_list|>
argument_list|>
name|getSignificanceHeuristics
parameter_list|()
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * The new {@link MovAvgModel}s defined by this plugin. {@linkplain MovAvgModel}s are used by the {@link MovAvgPipelineAggregator} to      * model trends in data.      */
DECL|method|getMovingAverageModels
specifier|default
name|List
argument_list|<
name|SearchExtensionSpec
argument_list|<
name|MovAvgModel
argument_list|,
name|MovAvgModel
operator|.
name|AbstractModelParser
argument_list|>
argument_list|>
name|getMovingAverageModels
parameter_list|()
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * The new {@link FetchSubPhase}s defined by this plugin.      */
DECL|method|getFetchSubPhases
specifier|default
name|List
argument_list|<
name|FetchSubPhase
argument_list|>
name|getFetchSubPhases
parameter_list|(
name|FetchPhaseConstructionContext
name|context
parameter_list|)
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * Get the {@link Highlighter}s defined by this plugin.      */
DECL|method|getHighlighters
specifier|default
name|Map
argument_list|<
name|String
argument_list|,
name|Highlighter
argument_list|>
name|getHighlighters
parameter_list|()
block|{
return|return
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * The new {@link Suggester}s defined by this plugin.      */
DECL|method|getSuggesters
specifier|default
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSuggesters
parameter_list|()
block|{
return|return
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * The new {@link Query}s defined by this plugin.      */
DECL|method|getQueries
specifier|default
name|List
argument_list|<
name|QuerySpec
argument_list|<
name|?
argument_list|>
argument_list|>
name|getQueries
parameter_list|()
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * Specification of custom {@link ScoreFunction}.      */
DECL|class|ScoreFunctionSpec
class|class
name|ScoreFunctionSpec
parameter_list|<
name|T
extends|extends
name|ScoreFunctionBuilder
parameter_list|<
name|T
parameter_list|>
parameter_list|>
extends|extends
name|SearchExtensionSpec
argument_list|<
name|T
argument_list|,
name|ScoreFunctionParser
argument_list|<
name|T
argument_list|>
argument_list|>
block|{
DECL|method|ScoreFunctionSpec
specifier|public
name|ScoreFunctionSpec
parameter_list|(
name|ParseField
name|name
parameter_list|,
name|Reader
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|ScoreFunctionParser
argument_list|<
name|T
argument_list|>
name|parser
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|reader
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
DECL|method|ScoreFunctionSpec
specifier|public
name|ScoreFunctionSpec
parameter_list|(
name|String
name|name
parameter_list|,
name|Reader
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|ScoreFunctionParser
argument_list|<
name|T
argument_list|>
name|parser
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|reader
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Specification of custom {@link Query}.      */
DECL|class|QuerySpec
class|class
name|QuerySpec
parameter_list|<
name|T
extends|extends
name|QueryBuilder
parameter_list|>
extends|extends
name|SearchExtensionSpec
argument_list|<
name|T
argument_list|,
name|QueryParser
argument_list|<
name|T
argument_list|>
argument_list|>
block|{
comment|/**          * Specification of custom {@link Query}.          *          * @param name holds the names by which this query might be parsed. The {@link ParseField#getPreferredName()} is special as it          *        is the name by under which the reader is registered. So it is the name that the query should use as its          *        {@link NamedWriteable#getWriteableName()} too.          * @param reader the reader registered for this query's builder. Typically a reference to a constructor that takes a          *        {@link StreamInput}          * @param parser the parser the reads the query builder from xcontent          */
DECL|method|QuerySpec
specifier|public
name|QuerySpec
parameter_list|(
name|ParseField
name|name
parameter_list|,
name|Reader
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|QueryParser
argument_list|<
name|T
argument_list|>
name|parser
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|reader
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
comment|/**          * Specification of custom {@link Query}.          *          * @param name the name by which this query might be parsed or deserialized. Make sure that the query builder returns this name for          *        {@link NamedWriteable#getWriteableName()}.          * @param reader the reader registered for this query's builder. Typically a reference to a constructor that takes a          *        {@link StreamInput}          * @param parser the parser the reads the query builder from xcontent          */
DECL|method|QuerySpec
specifier|public
name|QuerySpec
parameter_list|(
name|String
name|name
parameter_list|,
name|Reader
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|QueryParser
argument_list|<
name|T
argument_list|>
name|parser
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|reader
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Specification of search time behavior extension like a custom {@link MovAvgModel} or {@link ScoreFunction}.      *      * @param W the type of the main {@link NamedWriteable} for this spec. All specs have this but it isn't always *for* the same thing      *        though, usually it is some sort of builder sent from the coordinating node to the data nodes executing the behavior      * @param P the type of the parser for this spec. The parser runs on the coordinating node, converting {@link XContent} into the      *        behavior to execute      */
DECL|class|SearchExtensionSpec
class|class
name|SearchExtensionSpec
parameter_list|<
name|W
extends|extends
name|NamedWriteable
parameter_list|,
name|P
parameter_list|>
block|{
DECL|field|name
specifier|private
specifier|final
name|ParseField
name|name
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|Writeable
operator|.
name|Reader
argument_list|<
name|W
argument_list|>
name|reader
decl_stmt|;
DECL|field|parser
specifier|private
specifier|final
name|P
name|parser
decl_stmt|;
comment|/**          * Build the spec with a {@linkplain ParseField}.          *          * @param name the name of the behavior as a {@linkplain ParseField}. The parser is registered under all names specified by the          *        {@linkplain ParseField} but the reader is only registered under the {@link ParseField#getPreferredName()} so be sure that          *        that is the name that W's {@link NamedWriteable#getWriteableName()} returns.          * @param reader reader that reads the behavior from the internode protocol          * @param parser parser that read the behavior from a REST request          */
DECL|method|SearchExtensionSpec
specifier|public
name|SearchExtensionSpec
parameter_list|(
name|ParseField
name|name
parameter_list|,
name|Writeable
operator|.
name|Reader
argument_list|<
name|W
argument_list|>
name|reader
parameter_list|,
name|P
name|parser
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|parser
operator|=
name|parser
expr_stmt|;
block|}
comment|/**          * Build the spec with a String.          *          * @param name the name of the behavior. The parser and the reader are are registered under this name so be sure that that is the          *        name that W's {@link NamedWriteable#getWriteableName()} returns.          * @param reader reader that reads the behavior from the internode protocol          * @param parser parser that read the behavior from a REST request          */
DECL|method|SearchExtensionSpec
specifier|public
name|SearchExtensionSpec
parameter_list|(
name|String
name|name
parameter_list|,
name|Writeable
operator|.
name|Reader
argument_list|<
name|W
argument_list|>
name|reader
parameter_list|,
name|P
name|parser
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|ParseField
argument_list|(
name|name
argument_list|)
argument_list|,
name|reader
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
comment|/**          * The name of the thing being specified as a {@link ParseField}. This allows it to have deprecated names.          */
DECL|method|getName
specifier|public
name|ParseField
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**          * The reader responsible for reading the behavior from the internode protocol.          */
DECL|method|getReader
specifier|public
name|Writeable
operator|.
name|Reader
argument_list|<
name|W
argument_list|>
name|getReader
parameter_list|()
block|{
return|return
name|reader
return|;
block|}
comment|/**          * The parser responsible for converting {@link XContent} into the behavior.          */
DECL|method|getParser
specifier|public
name|P
name|getParser
parameter_list|()
block|{
return|return
name|parser
return|;
block|}
block|}
comment|/**      * Context available during fetch phase construction.      */
DECL|class|FetchPhaseConstructionContext
class|class
name|FetchPhaseConstructionContext
block|{
DECL|field|highlighters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Highlighter
argument_list|>
name|highlighters
decl_stmt|;
DECL|method|FetchPhaseConstructionContext
specifier|public
name|FetchPhaseConstructionContext
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Highlighter
argument_list|>
name|highlighters
parameter_list|)
block|{
name|this
operator|.
name|highlighters
operator|=
name|highlighters
expr_stmt|;
block|}
DECL|method|getHighlighters
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Highlighter
argument_list|>
name|getHighlighters
parameter_list|()
block|{
return|return
name|highlighters
return|;
block|}
block|}
block|}
end_interface

end_unit

