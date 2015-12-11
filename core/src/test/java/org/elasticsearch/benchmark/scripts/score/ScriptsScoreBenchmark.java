begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.scripts.score
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|scripts
operator|.
name|score
package|;
end_package

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
name|benchmark
operator|.
name|scripts
operator|.
name|score
operator|.
name|plugin
operator|.
name|NativeScriptExamplesPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|scripts
operator|.
name|score
operator|.
name|script
operator|.
name|NativeNaiveTFIDFScoreScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|node
operator|.
name|MockNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
operator|.
name|Entry
import|;
end_import

begin_import
import|import static
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
name|settingsBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ScriptsScoreBenchmark
specifier|public
class|class
name|ScriptsScoreBenchmark
extends|extends
name|BasicScriptBenchmark
block|{
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|minTerms
init|=
literal|1
decl_stmt|;
name|int
name|maxTerms
init|=
literal|50
decl_stmt|;
name|int
name|maxIter
init|=
literal|100
decl_stmt|;
name|int
name|warmerIter
init|=
literal|10
decl_stmt|;
name|boolean
name|runMVEL
init|=
literal|false
decl_stmt|;
name|init
argument_list|(
name|maxTerms
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Results
argument_list|>
name|allResults
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|clusterName
init|=
name|ScriptsScoreBenchmark
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"node1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
name|clusterName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|plugins
init|=
name|Collections
operator|.
expr|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
operator|>
name|singletonList
argument_list|(
name|NativeScriptExamplesPlugin
operator|.
name|class
argument_list|)
decl_stmt|;
name|Node
name|node1
init|=
operator|new
name|MockNode
argument_list|(
name|settings
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|plugins
argument_list|)
decl_stmt|;
name|node1
operator|.
name|start
argument_list|()
expr_stmt|;
name|Client
name|client
init|=
name|node1
operator|.
name|client
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"10s"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|indexData
argument_list|(
literal|10000
argument_list|,
name|client
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"10s"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|Results
name|results
init|=
operator|new
name|Results
argument_list|()
decl_stmt|;
name|results
operator|.
name|init
argument_list|(
name|maxTerms
operator|-
name|minTerms
argument_list|,
literal|"native tfidf script score dense posting list"
argument_list|,
literal|"Results for native script score with dense posting list:"
argument_list|,
literal|"black"
argument_list|,
literal|"--"
argument_list|)
expr_stmt|;
comment|// init native script searches
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|searchRequests
init|=
name|initNativeSearchRequests
argument_list|(
name|minTerms
argument_list|,
name|maxTerms
argument_list|,
name|NativeNaiveTFIDFScoreScript
operator|.
name|NATIVE_NAIVE_TFIDF_SCRIPT_SCORE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// run actual benchmark
name|runBenchmark
argument_list|(
name|client
argument_list|,
name|maxIter
argument_list|,
name|results
argument_list|,
name|searchRequests
argument_list|,
name|minTerms
argument_list|,
name|warmerIter
argument_list|)
expr_stmt|;
name|allResults
operator|.
name|add
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|results
operator|=
operator|new
name|Results
argument_list|()
expr_stmt|;
name|results
operator|.
name|init
argument_list|(
name|maxTerms
operator|-
name|minTerms
argument_list|,
literal|"term query dense posting list"
argument_list|,
literal|"Results for term query with dense posting lists:"
argument_list|,
literal|"green"
argument_list|,
literal|"--"
argument_list|)
expr_stmt|;
comment|// init term queries
name|searchRequests
operator|=
name|initTermQueries
argument_list|(
name|minTerms
argument_list|,
name|maxTerms
argument_list|)
expr_stmt|;
comment|// run actual benchmark
name|runBenchmark
argument_list|(
name|client
argument_list|,
name|maxIter
argument_list|,
name|results
argument_list|,
name|searchRequests
argument_list|,
name|minTerms
argument_list|,
name|warmerIter
argument_list|)
expr_stmt|;
name|allResults
operator|.
name|add
argument_list|(
name|results
argument_list|)
expr_stmt|;
if|if
condition|(
name|runMVEL
condition|)
block|{
name|results
operator|=
operator|new
name|Results
argument_list|()
expr_stmt|;
name|results
operator|.
name|init
argument_list|(
name|maxTerms
operator|-
name|minTerms
argument_list|,
literal|"mvel tfidf dense posting list"
argument_list|,
literal|"Results for mvel score with dense posting list:"
argument_list|,
literal|"red"
argument_list|,
literal|"--"
argument_list|)
expr_stmt|;
comment|// init native script searches
name|searchRequests
operator|=
name|initNativeSearchRequests
argument_list|(
name|minTerms
argument_list|,
name|maxTerms
argument_list|,
literal|"score = 0.0; fi= _terminfo[\"text\"]; for(i=0; i<text.size(); i++){terminfo = fi[text.get(i)]; score = score + terminfo.tf()*fi.getDocCount()/terminfo.df();} return score;"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// run actual benchmark
name|runBenchmark
argument_list|(
name|client
argument_list|,
name|maxIter
argument_list|,
name|results
argument_list|,
name|searchRequests
argument_list|,
name|minTerms
argument_list|,
name|warmerIter
argument_list|)
expr_stmt|;
name|allResults
operator|.
name|add
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
name|indexData
argument_list|(
literal|10000
argument_list|,
name|client
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|results
operator|=
operator|new
name|Results
argument_list|()
expr_stmt|;
name|results
operator|.
name|init
argument_list|(
name|maxTerms
operator|-
name|minTerms
argument_list|,
literal|"native tfidf script score sparse posting list"
argument_list|,
literal|"Results for native script scorewith sparse posting list:"
argument_list|,
literal|"black"
argument_list|,
literal|"-."
argument_list|)
expr_stmt|;
comment|// init native script searches
name|searchRequests
operator|=
name|initNativeSearchRequests
argument_list|(
name|minTerms
argument_list|,
name|maxTerms
argument_list|,
name|NativeNaiveTFIDFScoreScript
operator|.
name|NATIVE_NAIVE_TFIDF_SCRIPT_SCORE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// run actual benchmark
name|runBenchmark
argument_list|(
name|client
argument_list|,
name|maxIter
argument_list|,
name|results
argument_list|,
name|searchRequests
argument_list|,
name|minTerms
argument_list|,
name|warmerIter
argument_list|)
expr_stmt|;
name|allResults
operator|.
name|add
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|results
operator|=
operator|new
name|Results
argument_list|()
expr_stmt|;
name|results
operator|.
name|init
argument_list|(
name|maxTerms
operator|-
name|minTerms
argument_list|,
literal|"term query sparse posting list"
argument_list|,
literal|"Results for term query with sparse posting lists:"
argument_list|,
literal|"green"
argument_list|,
literal|"-."
argument_list|)
expr_stmt|;
comment|// init term queries
name|searchRequests
operator|=
name|initTermQueries
argument_list|(
name|minTerms
argument_list|,
name|maxTerms
argument_list|)
expr_stmt|;
comment|// run actual benchmark
name|runBenchmark
argument_list|(
name|client
argument_list|,
name|maxIter
argument_list|,
name|results
argument_list|,
name|searchRequests
argument_list|,
name|minTerms
argument_list|,
name|warmerIter
argument_list|)
expr_stmt|;
name|allResults
operator|.
name|add
argument_list|(
name|results
argument_list|)
expr_stmt|;
if|if
condition|(
name|runMVEL
condition|)
block|{
name|results
operator|=
operator|new
name|Results
argument_list|()
expr_stmt|;
name|results
operator|.
name|init
argument_list|(
name|maxTerms
operator|-
name|minTerms
argument_list|,
literal|"mvel tfidf sparse posting list"
argument_list|,
literal|"Results for mvel score with sparse posting list:"
argument_list|,
literal|"red"
argument_list|,
literal|"-."
argument_list|)
expr_stmt|;
comment|// init native script searches
name|searchRequests
operator|=
name|initNativeSearchRequests
argument_list|(
name|minTerms
argument_list|,
name|maxTerms
argument_list|,
literal|"score = 0.0; fi= _terminfo[\"text\"]; for(i=0; i<text.size(); i++){terminfo = fi[text.get(i)]; score = score + terminfo.tf()*fi.getDocCount()/terminfo.df();} return score;"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// run actual benchmark
name|runBenchmark
argument_list|(
name|client
argument_list|,
name|maxIter
argument_list|,
name|results
argument_list|,
name|searchRequests
argument_list|,
name|minTerms
argument_list|,
name|warmerIter
argument_list|)
expr_stmt|;
name|allResults
operator|.
name|add
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
name|printOctaveScript
argument_list|(
name|allResults
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|node1
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

