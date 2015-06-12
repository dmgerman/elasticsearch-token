begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.significant.heuristics
package|package
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
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|inject
operator|.
name|AbstractModule
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
name|inject
operator|.
name|multibindings
operator|.
name|Multibinder
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

begin_class
DECL|class|SignificantTermsHeuristicModule
specifier|public
class|class
name|SignificantTermsHeuristicModule
extends|extends
name|AbstractModule
block|{
DECL|field|parsers
specifier|private
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|SignificanceHeuristicParser
argument_list|>
argument_list|>
name|parsers
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|method|SignificantTermsHeuristicModule
specifier|public
name|SignificantTermsHeuristicModule
parameter_list|()
block|{
name|registerParser
argument_list|(
name|JLHScore
operator|.
name|JLHScoreParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerParser
argument_list|(
name|PercentageScore
operator|.
name|PercentageScoreParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerParser
argument_list|(
name|MutualInformation
operator|.
name|MutualInformationParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerParser
argument_list|(
name|GND
operator|.
name|GNDParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerParser
argument_list|(
name|ChiSquare
operator|.
name|ChiSquareParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerParser
argument_list|(
name|ScriptHeuristic
operator|.
name|ScriptHeuristicParser
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|registerParser
specifier|public
name|void
name|registerParser
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|SignificanceHeuristicParser
argument_list|>
name|parser
parameter_list|)
block|{
name|parsers
operator|.
name|add
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|Multibinder
argument_list|<
name|SignificanceHeuristicParser
argument_list|>
name|parserMapBinder
init|=
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|SignificanceHeuristicParser
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|SignificanceHeuristicParser
argument_list|>
name|clazz
range|:
name|parsers
control|)
block|{
name|parserMapBinder
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
name|bind
argument_list|(
name|SignificanceHeuristicParserMapper
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
